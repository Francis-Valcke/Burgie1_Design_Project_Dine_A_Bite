package cobol.services.ordermanager;

import cobol.commons.communication.response.BetterResponseModel;
import cobol.commons.domain.*;
import cobol.commons.exception.DoesNotExistException;
import cobol.commons.stub.Action;
import cobol.commons.stub.EventChannelStub;
import cobol.services.ordermanager.config.ConfigurationBean;
import cobol.services.ordermanager.domain.entity.*;
import cobol.services.ordermanager.domain.repository.BrandRepository;
import cobol.services.ordermanager.domain.repository.FoodRepository;
import cobol.services.ordermanager.domain.repository.OrderRepository;
import cobol.services.ordermanager.domain.repository.StandRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import lombok.extern.log4j.Log4j2;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.naming.CommunicationException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This is a Singleton
 */
@Log4j2
@Service
@Scope(value = "singleton")
public class OrderProcessor {

    @Autowired
    EventChannelStub eventChannelStub;

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

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    ASCommunicationHandler asCommunicationHandler;

    @Autowired
    ConfigurationBean configurationBean;

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

        newOrder=orderRepository.save(newOrder);

        // subscribe to the channel of the order
        communicationHandler.registerOnOrder(subscriberId, newOrder.getId());

        return newOrder;
    }

    public Order confirmStand(int orderId, String standName, String brandName) throws DoesNotExistException, JsonProcessingException {
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

            //send the updated order to stand to place it in the queue
            CommonOrder mappedOrder = updatedOrder.asCommonOrder();
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            String jsonString = mapper.writeValueAsString(mappedOrder);
            communicationHandler.sendRestCallToStandManager("/placeOrder", jsonString, null);
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


    /**
     * Prepare and do the transaction of this order
     * It will simultaneously add prices to the incoming orderitems in the commonorder object
     *
     * @param orderObject CommonOrder object
     * @param userDetails CommonUser which places the order
     * @throws Throwable all exceptions
     */
    public void orderTransaction(CommonOrder orderObject, CommonUser userDetails) throws Throwable {
        // First calculate the total price of the order
        Brand brand = brandRepository.findById(orderObject.getBrandName())
                .orElseThrow(() -> new DoesNotExistException("The brand of the given order does not exist in the database, this should not be possible."));

        // Get all distinct food items that are present for the brand
        List<Food> brandFood = brand.getStandList().stream().flatMap(s -> s.getFoodList().stream()).distinct().collect(Collectors.toList());
        // Each ordered item should be in this list, search this list to find the price.
        BigDecimal total = BigDecimal.ZERO;
        for (CommonOrderItem orderItem : orderObject.getOrderItems()) {
            // search for current price of this orderitem
            BigDecimal itemPrice= brandFood.stream()
                    .filter(f -> f.getName().equals(orderItem.getFoodName()) && f.getBrandName().equals(orderObject.getBrandName()))
                    .findAny()
                    .orElseThrow(() -> new DoesNotExistException("OrderItem " +orderItem.getFoodName() + " does not exist in the backend, this should not be possible"))
                    .getPrice();


            // add this price of this orderitem to total
            total = total.subtract(
                    itemPrice
                            .multiply(new BigDecimal(orderItem.getAmount()))
            );

            // update price of this CommonOrderItem
            orderItem.setPrice(itemPrice);
        }

        // With this price we try to create a transaction
        BetterResponseModel<BetterResponseModel.GetBalanceResponse> response = asCommunicationHandler.callCreateTransaction(userDetails.getUsername(), total);
        if (response.getStatus().equals(BetterResponseModel.Status.ERROR)){
            // There was an error creating the transaction. Throw this.
            throw response.getException();
        }
    }


    // ---- Getters ---- //

    public Optional<Order> getOrder(int orderId) {
        return orderRepository.findById(orderId);
    }

    // ---- Scheduled Requests ---- //

    @Scheduled(fixedDelay = 500)
    public void pollEvents() throws CommunicationException, JsonProcessingException, ParseException {

        if (configurationBean.isAuthenticated() && configurationBean.isSubscribed()) {
            List<Event> newEvents = communicationHandler.pollEventsFromEC(subscriberId);
            eventQueue.addAll(newEvents);
        }

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

    public void setSubscriberId(int subscriberId) {
        this.subscriberId = subscriberId;
    }

}

