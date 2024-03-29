package cobol.services.ordermanager;

import cobol.commons.Event;
import cobol.commons.exception.DoesNotExistException;
import cobol.commons.exception.OrderException;
import cobol.commons.order.CommonOrder;
import cobol.commons.order.Recommendation;
import cobol.services.ordermanager.domain.entity.Food;
import cobol.services.ordermanager.domain.entity.Order;
import cobol.services.ordermanager.domain.entity.OrderItem;
import cobol.services.ordermanager.domain.entity.Stand;
import cobol.services.ordermanager.domain.repository.FoodRepository;
import cobol.services.ordermanager.domain.repository.OrderItemRepository;
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
 * This class is responsible handling orders. This includes persisting them in the database,
 * updating fields when stands are chosen, and updating preparation times when orders are finished.
 * The class is also responsible for polling the event channel for orders.
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
    OrderItemRepository orderItemRepository;

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
        this.subscriberId = communicationHandler.getSubscriberIdFromEC();
    }

    // ---- Incoming Requests ---- //

    /**
     * Add a new incoming order
     *
     * @param newOrder: order just received from attendee app
     * @return Order: persisted Order object
     */
    public Order addNewOrder(Order newOrder) {
        newOrder.setOrderState(CommonOrder.State.PENDING);

        newOrder = orderRepository.save(newOrder);

        // subscribe to the channel of the order
        communicationHandler.registerOnOrder(subscriberId, newOrder.getId());

        return newOrder;
    }

    /**
     *
     * This function sets the definitive stand to which an order belongs
     *
     * @param orderId: id of the order
     * @param standName: stand the attendee has chosen
     * @param brandName: brand the stand belongs to
     * @return Order: order object with updated stand field
     * @throws Throwable
     */
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


            List<OrderItem> orderItems = updatedOrder.getOrderItems();

            //Check if enough stock for every item in the order
            for (OrderItem o : orderItems) {
                Optional<Food> food = foodRepository.findFoodById(o.getFoodName(), standName, brandName);
                if (food.isPresent()) {
                    if (food.get().getStock() < o.getAmount()) {
                        throw new OrderException("There is not enough stock for this order");
                    }
                } else {
                    throw new OrderException("An item is missing from the menu to complete this order");
                }
            }
            //send the updated order to stand to place it in the queue
            CommonOrder mappedOrder = updatedOrder.asCommonOrder();
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            String jsonString = mapper.writeValueAsString(mappedOrder);
            int currentWaitingTime = Integer.parseInt(communicationHandler.sendRestCallToStandManager("/placeOrder", jsonString, null));
            ZonedDateTime actualTime = ZonedDateTime.now(ZoneId.of("Europe/Brussels"));
            updatedOrder.setStartTime(actualTime);
            updatedOrder.setExpectedTime(actualTime.plusSeconds(currentWaitingTime));
            updatedOrder.setOrderState(CommonOrder.State.CONFIRMED);

            //Decrease stock
            for (OrderItem o : orderItems) {
                foodRepository.decreaseStock(o.getFoodName(), standName, brandName, o.getAmount());
            }

            orderRepository.save(updatedOrder);
        }
        return updatedOrder;
    }

    // ---- Update or Process existing orders ---- //

    /**
     *
     * This function updates the preparation time of items belonging to an order that has been finished
     *
     * @param order: the order which has just been finished
     */
    private void updatePreparationEstimate(Order order) {
        ZonedDateTime actualTime = ZonedDateTime.now(ZoneId.of("Europe/Brussels"));
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
        int updatedAverage = (int) (((1 - this.learningRate) * largestPreptime) + (learningRate * actualPrepTime));
        if (foodToUpdate != null) {
            foodToUpdate.setPreparationTime(updatedAverage);
            foodRepository.updatePreparationTime(foodToUpdate.getFoodId().getName(), order.getStand().getName(), brandName, updatedAverage);
        }

    }

    /**
     * This methods adds the recommendations to the orderRecommendation list
     * @param id the order id
     * @param recommendations the list of recommendations
     */
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

    /**
     * Scheduled task to poll new events from the Event Channel destined for the OrderProcessor.
     * @throws CommunicationException Error while sending request.
     * @throws JsonProcessingException Error while deserializing response.
     * @throws ParseException Error while parsing the response.
     */
    @Scheduled(fixedDelay = 500)
    public void pollEvents() throws CommunicationException, JsonProcessingException, ParseException {
        List<Event> newEvents = communicationHandler.pollEventsFromEC(subscriberId);
        eventQueue.addAll(newEvents);
    }

    /**
     * Scheduled task to process the events that were polled with the pollEvents() method.
     */
    @Scheduled(fixedDelay = 500)
    public void processEvents() throws Throwable {
        while (!eventQueue.isEmpty()) {
            Event e = eventQueue.poll();
            assert e != null;
            if (e.getDataType().equals("OrderStatusUpdate")) {
                JSONObject eventData = e.getEventData();
                String newStatusString = (String) eventData.get("newState");
                CommonOrder.State newStatus = CommonOrder.State.valueOf(newStatusString);
                int orderId = (int) eventData.get("orderId");
                Optional<Order> localOrderOptional = orderRepository.findFullOrderById(orderId);
                if (localOrderOptional.isPresent()) {

                    // update to new state
                    Order localOrder = localOrderOptional.get();
                    localOrder.setOrderState(newStatus);
                    if (newStatus.equals(CommonOrder.State.DECLINED) || newStatus.equals(CommonOrder.State.READY)) {
                        if (newStatus.equals(CommonOrder.State.READY)) {
                            updatePreparationEstimate(localOrder);
                        }


                        // deregister from order
                        communicationHandler.deregisterFromOrder(subscriberId, orderId);
                    }
                    if (newStatus.equals(CommonOrder.State.DECLINED) || newStatus.equals(CommonOrder.State.BEGUN)|| newStatus.equals(CommonOrder.State.READY)){
                        Map<String, String> params = new HashMap<>();
                        params.put("standName", localOrder.getStand().getName());
                        params.put("brandName", localOrder.getStand().getBrandName());
                        params.put("orderId", String.valueOf(localOrder.getId()));
                        params.put("newState", newStatus.name());
                        communicationHandler.sendRestCallToStandManager("/updateScheduler", null, params);


                    }

                    orderRepository.updateState(localOrder.getId(), localOrder.getOrderState());
                }
            }
            //receive statusupdates and add to database
            if (e.getDataType().equals("UpdateOrder")) {
                JSONObject eventData = e.getEventData();
                int orderId = (int) eventData.get("id");
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                ZonedDateTime expected = objectMapper.readValue(eventData.get("expectedTime").toString(), ZonedDateTime.class);//  (ZonedDateTime) eventData.get("expectedTime");
                Optional<Order> localOrderOptional = orderRepository.findFullOrderById(orderId);
                localOrderOptional.get().setExpectedTime(expected);


            }
        }

    }

}

