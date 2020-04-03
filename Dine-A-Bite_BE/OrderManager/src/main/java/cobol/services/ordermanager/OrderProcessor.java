package cobol.services.ordermanager;

import cobol.commons.CommonFood;
import cobol.commons.Event;
import cobol.commons.order.CommonOrder;
import cobol.commons.order.Recommendation;
import cobol.services.ordermanager.domain.entity.Food;
import cobol.services.ordermanager.domain.entity.Order;
import cobol.services.ordermanager.domain.entity.OrderItem;
import cobol.services.ordermanager.domain.entity.Stand;
import cobol.services.ordermanager.domain.repository.FoodRepository;
import cobol.services.ordermanager.domain.repository.OrderRepository;
import cobol.services.ordermanager.domain.repository.StandRepository;
import cobol.services.ordermanager.exception.DoesNotExistException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.naming.CommunicationException;
import java.util.*;

/**
 * This is a Singleton
 */
@Component
@Scope(value = "singleton")
public class OrderProcessor {

    @Autowired
    StandRepository standRepository;

    @Autowired
    CommunicationHandler communicationHandler;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    FoodRepository foodRepository;

    @Autowired
    StandRepository stands;


    private int subscriberId;
    private double learningRate;
    private volatile LinkedList<Event> eventQueue = new LinkedList<Event>();


    // key order id
    ListMultimap<Integer, Recommendation> orderRecommendations = ArrayListMultimap.create();

    private OrderProcessor() throws CommunicationException {
        this.subscriberId= communicationHandler.getSubscriberIdFromEC();

        //set learning rate for the running averages
        this.learningRate = 0.2;
    };

    // ---- Incoming Requests ---- //

    /**
     * Add a new incoming order
     *
     * @param newOrder: order just received from attendee app
     * @return Order: persisted Order object
     */
    public Order addNewOrder(Order newOrder) {
        // update order and save to database
        newOrder.setRemtime(0);

        newOrder.setState(CommonOrder.State.PENDING);
        //orderRepository.saveAndFlush(newOrder);

        newOrder=orderRepository.save(newOrder);

        // subscribe to the channel of the order
        communicationHandler.registerOnOrder(subscriberId, newOrder.getId());

        return newOrder;
    }

    public Order confirmStand(int orderId, String standName, String brandName) throws DoesNotExistException {
        Optional<Order> orderOptional = this.getOrder(orderId);
        Stand stand = standRepository.findStandById(standName, brandName).orElseThrow(() -> new DoesNotExistException("Stand does not exist"));

        if (!orderOptional.isPresent()) {
            throw new DoesNotExistException("Order is does not exist, please make an order first before confirming a stand");
        }

        if (stand == null) {
            throw new DoesNotExistException("Stand does not exist, please check if you confirmed the right stand");
        }


        Order updatedOrder = orderOptional.get();
        if (!updatedOrder.hasChosenStand()) {

            updatedOrder.setStand(stand);
            Optional<Recommendation> recomOptional = orderRecommendations.get(orderId).stream()
                    .filter(r -> r.getStandName().equals(standName))
                    .findFirst();

            recomOptional.ifPresent(recommendation -> updatedOrder.setRemtime(recommendation.getTimeEstimate()));
            orderRepository.save(updatedOrder);
        }
        return updatedOrder;
    }

    // ---- Update or Process existing orders ---- //

    private void updatePreparationEstimate(Order order) {
        Calendar actualTime =  Calendar.getInstance();
        // Compute how long the stand has been working on this order
        int actualPrepTime = (int) ((actualTime.getTime().getTime() - order.getStartTime().getTime().getTime())) / 1000;

        String brandName = order.getStand().getBrandName();
        int largestPreptime = 0;
        Food foodToUpdate = null;
        for (OrderItem item : order.getOrderItems()) {
            String foodName = item.getFoodName();
            Food food = foodRepository.findFoodById(foodName, order.getStand().getName(), brandName).orElse(null);
            if (food != null && food.getPreparationTime() > largestPreptime) {
                foodToUpdate = food;
                largestPreptime = food.getPreparationTime();
            }
        }
        int updatedAverage = (int) (((1-this.learningRate) * largestPreptime) + (learningRate * actualPrepTime));
        if(foodToUpdate!=null){
            foodToUpdate.setPreparationTime(updatedAverage);
        }
    }

    public void addRecommendations(int id, List<Recommendation> recommendations) {
        for (Recommendation recommendation : recommendations) {
            orderRecommendations.put(id, recommendation);
        }
    }
    // ---- Getters ---- //

    public Optional<Order> getOrder(int orderId) {
        return orderRepository.findById(orderId);
    }

    // ---- Scheduled Requests ---- //

    @Scheduled(fixedDelay = 500)
    public void pollEvents() throws CommunicationException, JsonProcessingException {
        List<Event> newEvents= communicationHandler.pollEventsFromEC(subscriberId);
        eventQueue.addAll(newEvents);
    }

    /**
     * Process events that were received.
     */
    @Scheduled(fixedDelay = 500)
    public void processEvents() {
        while (!eventQueue.isEmpty()) {
            Event e = eventQueue.poll();
            assert e != null;
            if (e.getDataType().equals("OrderStatusUpdate")) {
                JSONObject eventData = e.getEventData();
                String newStatusString = (String) eventData.get("newStatus");
                CommonOrder.State newStatus = CommonOrder.State.valueOf(newStatusString);
                int orderId = (int) eventData.get("orderId");
                Order localOrder = orderRepository.findById(orderId).orElse(null);
                if(localOrder!=null){

                    // update to new state
                    localOrder.setState(newStatus);
                    if (newStatus.equals(CommonOrder.State.DECLINED) || newStatus.equals(CommonOrder.State.READY)) {
                        if (newStatus.equals(CommonOrder.State.READY)) {
                            updatePreparationEstimate(localOrder);
                        }

                        orderRepository.delete(localOrder);

                        // deregister from order
                        communicationHandler.deregisterFromOrder(subscriberId, orderId);

                    }
                }
            }
        }
    }
}

