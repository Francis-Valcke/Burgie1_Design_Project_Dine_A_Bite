package cobol.services.ordermanager;

import cobol.commons.Event;
import cobol.commons.exception.DoesNotExistException;
import cobol.commons.order.CommonOrder;
import cobol.commons.order.Recommendation;
import cobol.services.ordermanager.domain.entity.Food;
import cobol.services.ordermanager.domain.entity.Order;
import cobol.services.ordermanager.domain.entity.OrderItem;
import cobol.services.ordermanager.domain.entity.Stand;
import cobol.services.ordermanager.domain.repository.FoodRepository;
import cobol.services.ordermanager.domain.repository.OrderRepository;
import cobol.services.ordermanager.domain.repository.StandRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.naming.CommunicationException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    OrderRepository orderRepository;

    @Autowired
    FoodRepository foodRepository;

    @Autowired
    CommunicationHandler communicationHandler;

    @Autowired
    StandRepository stands;

    private int subscriberId;
    private double learningRate;
    private volatile LinkedList<Event> eventQueue = new LinkedList<>();



    // key order id
    ListMultimap<Integer, Recommendation> orderRecommendations = ArrayListMultimap.create();




    private OrderProcessor() throws CommunicationException {

        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Brussels"));
        //set learning rate for the running averages
        this.learningRate = 0.2;
    }

    @PostConstruct
    private void run() throws CommunicationException {
        this.subscriberId= communicationHandler.getSubscriberIdFromEC();
    }

    // ---- Incoming Requests ---- //

    /**
     * Add a new incoming order
     *
     * @param newOrder: order just received from attendee app
     * @return Order: persisted Order object
     */
    public Order addNewOrder(Order newOrder) {
        newOrder.setState(CommonOrder.State.PENDING);

        newOrder=orderRepository.save(newOrder);

        // subscribe to the channel of the order
        communicationHandler.registerOnOrder(subscriberId, newOrder.getId());

        return newOrder;
    }

    public Order confirmStand(int orderId, String standName, String brandName) throws Throwable {
        Optional<Order> orderOptional = this.getOrder(orderId);
        Stand stand = standRepository.findStandById(standName, brandName).orElseThrow(() -> new DoesNotExistException("Stand does not exist"));

        if (!orderOptional.isPresent()) {
            throw new DoesNotExistException("Order does not exist, please make an order first before confirming a stand");
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

            //send the updated order to stand to place it in the queue
            CommonOrder mappedOrder = updatedOrder.asCommonOrder();
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            String jsonString = mapper.writeValueAsString(mappedOrder);
            int currentWaitingTime = Integer.parseInt(communicationHandler.sendRestCallToStandManager("/placeOrder", jsonString, null));
            ZonedDateTime actualTime =  ZonedDateTime.now(ZoneId.of("Europe/Brussels"));
            updatedOrder.setStartTime(actualTime);
            updatedOrder.setExpectedTime(actualTime.plusSeconds(currentWaitingTime));
            updatedOrder.setState(CommonOrder.State.CONFIRMED);
            orderRepository.save(updatedOrder);
        }


        return updatedOrder;
    }

    // ---- Update or Process existing orders ---- //

    private void updatePreparationEstimate(Order order) {
        ZonedDateTime actualTime =  ZonedDateTime.now(ZoneId.of("Europe/Brussels"));
        // Compute how long the stand has been working on this order
        int actualPrepTime = (int) Duration.between(actualTime, order.getStartTime()).getSeconds();

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
            foodRepository.updatePreparationTime(foodToUpdate.getFoodId().getName(), order.getStand().getName(), brandName, updatedAverage);
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
    public void pollEvents() throws CommunicationException, JsonProcessingException, ParseException {
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
                String newStatusString = (String) eventData.get("newState");
                CommonOrder.State newStatus = CommonOrder.State.valueOf(newStatusString);
                int orderId = (int) eventData.get("orderId");
                Optional<Order> localOrderOptional = orderRepository.findFullOrderById(orderId);
                if(localOrderOptional.isPresent()){

                    // update to new state
                    Order localOrder= localOrderOptional.get();
                    localOrder.setState(newStatus);
                    if (newStatus.equals(CommonOrder.State.DECLINED) || newStatus.equals(CommonOrder.State.READY)) {
                        if (newStatus.equals(CommonOrder.State.READY)) {
                            updatePreparationEstimate(localOrder);
                        }


                        // deregister from order
                        communicationHandler.deregisterFromOrder(subscriberId, orderId);
                    }

                    orderRepository.updateState(localOrder.getId(), localOrder.getOrderState());
                }
            }
        }
    }

}

