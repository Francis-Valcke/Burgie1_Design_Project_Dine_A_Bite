package cobol.services.ordermanager;

import cobol.commons.Event;
import cobol.commons.order.CommonOrder;
import cobol.commons.order.Recommendation;
import cobol.services.ordermanager.domain.entity.Order;
import cobol.services.ordermanager.domain.entity.Stand;
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

import java.util.*;

/**
 * This is a Singleton
 */
@Component
@Scope(value = "singleton")
public class OrderProcessor {

    @Autowired
    private MenuHandler menuHandler;

    @Autowired
    StandRepository standRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    StandRepository stands;

    private Map<Integer, Order> runningOrders = new HashMap<>();

    private int subscriberId;
    private double learningRate;
    private volatile LinkedList<Event> eventQueue = new LinkedList<Event>();
    private RestTemplate restTemplate;
    private HttpHeaders headers;
    private HttpEntity entity;
    private ObjectMapper objectMapper;
    // orderid - recommendations
    ListMultimap<Integer, Recommendation> orderRecommendations = ArrayListMultimap.create();

    private OrderProcessor() {
        objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
        this.headers = new HttpHeaders();
        this.headers.add("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJPcmRlck1hbmFnZXIiLCJyb2xlcyI6WyJST0xFX0FQUExJQ0FUSU9OIl0sImlhdCI6MTU4NDkxMTY3MSwiZXhwIjoxNzQyNTkxNjcxfQ.VmujsURhZaXRp5FQJXzmQMB-e6QSNF-OyPLeMEMOVvI");
        String uri = OrderManager.ECURL + "/registerSubscriber";
        //String uri = "http://cobol.idlab.ugent.be:8093/registerSubscriber";
        this.entity = new HttpEntity(headers);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        this.subscriberId = Integer.valueOf(response.getBody());

        //set learning rate for the running averages
        this.learningRate = 0.2;
    };


    /**
     * Function that is called regularly to get events from the EC.
     */
    @Scheduled(fixedDelay = 500)
    public void pollEvents() {
        String uri = OrderManager.ECURL + "/events";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
                .queryParam("id", this.subscriberId);
        ResponseEntity<String> response = this.restTemplate.exchange(builder.toUriString(), HttpMethod.GET, this.entity, String.class);
        try {
            JSONObject responseObject = this.objectMapper.readValue(response.getBody(), JSONObject.class);
            String details = (String) responseObject.get("details");
            List<Event> eventList = objectMapper.readValue(details, new TypeReference<List<Event>>() {
            });
            eventQueue.addAll(eventList);
        } catch (JsonProcessingException e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }

    /**
     * Process events that were received.
     */
    @Scheduled(fixedDelay = 500)
    public void processEvents() {
        while (!eventQueue.isEmpty()) {
            Event e = eventQueue.poll();
            if (e.getDataType().equals("OrderStatusUpdate")) {
                JSONObject eventData = e.getEventData();
                String newStatusString = (String) eventData.get("newStatus");
                CommonOrder.State newStatus = CommonOrder.State.valueOf(newStatusString);
                int orderId = (int) eventData.get("orderId");
                Order localOrder = runningOrders.get(orderId);
                localOrder.setState(newStatus);
                if (newStatus.equals(Order.status.DECLINED) || newStatus.equals(Order.status.READY)) {
                    if (newStatus.equals(Order.status.READY)) {
                        updatePreparationEstimate(localOrder);
                    }
                    runningOrders.remove(orderId);
                    String uri = OrderManager.ECURL + "/deregisterSubscriber";
                    String channelId = "o" + Integer.toString(orderId);
                    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
                            .queryParam("id", this.subscriberId)
                            .queryParam("type", channelId);
                    ResponseEntity<String> response = this.restTemplate.exchange(builder.toUriString(), HttpMethod.GET, this.entity, String.class);
                }
            }
        }
    }

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
        this.runningOrders.put(newOrder.getId(), newOrder);

        // subscribe to the channel of the order
        String uri = OrderManager.ECURL + "/registerSubscriber/toChannel";
        String channelId = "o" + Integer.toString(newOrder.getId());
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
                .queryParam("id", this.subscriberId)
                .queryParam("type", channelId);
        ResponseEntity<String> responseEntity = this.restTemplate.exchange(builder.toUriString(), HttpMethod.GET, this.entity, String.class);
        return newOrder;
    }

    /**
     * Add an already existing order (for example from database)
     *
     * @param o: order that is already running but OrderProcessor lost track of
     */
    public void addOrder(Order o) {
        runningOrders.put(o.getId(), o);
    }

    public Optional<Order> getOrder(int orderId) {
        Optional<Order> optionalOrder = Optional.ofNullable(runningOrders.getOrDefault(orderId, null));

        if (!optionalOrder.isPresent()) {
            optionalOrder = orderRepository.findById(orderId);
            optionalOrder.ifPresent(this::addOrder);
        }

        return optionalOrder;
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

    private void updatePreparationEstimate(Order order) {
        Calendar actualTime =  Calendar.getInstance();
        int actualPrepTime = (int) ((actualTime.getTime().getTime() - order.getStartTime().getTime().getTime())) / 1000;
        String brandName = order.getBrandName();
        int largestPreptime = 0;
        Food foodToUpdate = null;
        for (OrderItem item : order.getOrderItems()) {
            String foodName = item.getFoodname();
            Food food = foodRepository.findByNameAndBrand(foodName, brandName);
            if (food.getPreptime() > largestPreptime) {
                foodToUpdate = food;
                largestPreptime = food.getPreptime();
            }
        }
        int updatedAverage = (int) (((1-this.learningRate) * largestPreptime) + (learningRate * actualPrepTime));
        foodToUpdate.setPreptime(updatedAverage);
    }

    public void addRecommendations(int id, List<Recommendation> recommendations) {
        for (Recommendation recommendation : recommendations) {
            orderRecommendations.put(id, recommendation);
        }
    }
}

