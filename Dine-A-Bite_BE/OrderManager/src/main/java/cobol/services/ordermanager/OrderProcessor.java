package cobol.services.ordermanager;

import cobol.commons.Event;
import cobol.commons.order.Recommendation;
import cobol.services.ordermanager.dbmenu.Order;
import cobol.services.ordermanager.dbmenu.OrderRepository;
import cobol.services.ordermanager.dbmenu.Stand;
import cobol.services.ordermanager.dbmenu.StandRepository;
import cobol.services.ordermanager.exception.AlreadySetException;
import cobol.services.ordermanager.exception.MissingEntityException;
import cobol.services.ordermanager.exception.MissingRunException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Scope(value="singleton")
public class OrderProcessor {

    @Autowired
    OrderRepository orders;

    @Autowired
    StandRepository stands;

    private Map<Integer, Order> runningOrders = new HashMap<>();
    private int subscriberId;
    private LinkedList<Event> eventQueue = new LinkedList<Event>();
    private RestTemplate restTemplate;
    private HttpHeaders headers;
    private HttpEntity entity;
    private ObjectMapper objectMapper;
    ListMultimap<Integer, Recommendation> orderRecommendations= ArrayListMultimap.create();
    private Logger logger = LoggerFactory.getLogger(OrderProcessor.class);

    private OrderProcessor() {
        objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
        this.headers = new HttpHeaders();
        this.headers.add("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJPcmRlck1hbmFnZXIiLCJyb2xlcyI6WyJST0xFX0FQUExJQ0FUSU9OIl0sImlhdCI6MTU4NDkxMTY3MSwiZXhwIjoxNzQyNTkxNjcxfQ.VmujsURhZaXRp5FQJXzmQMB-e6QSNF-OyPLeMEMOVvI");
        String uri = "http://cobol.idlab.ugent.be:8093/registerSubscriber";
        //String uri = OrderManager.ECURL + "/registerSubscriber";
        this.entity = new HttpEntity(headers);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        this.subscriberId = Integer.valueOf(response.getBody());
    };


    /**
     * Function that is called regularly to get events from the EC.
     */
    @Scheduled(fixedDelay=500)
    public void pollEvents() {
        String uri = OrderManager.ECURL + "/events";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
                .queryParam("id", this.subscriberId);
        ResponseEntity<String> response = this.restTemplate.exchange(builder.toUriString(), HttpMethod.GET, this.entity, String.class);
        try {
            JSONObject responseObject = this.objectMapper.readValue(response.getBody(), JSONObject.class);
            String details = (String) responseObject.get("details");
            List<Event> eventList = objectMapper.readValue(details, new TypeReference<List<Event>>() {});
            eventQueue.addAll(eventList);
        } catch (JsonProcessingException e) {
            logger.error("Exception occurred!", e);
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
                Order.status newStatus = Order.status.valueOf(newStatusString);
                int orderId = (int) eventData.get("orderId");
                Order localOrder = runningOrders.get(orderId);
                localOrder.setState(newStatus);
                if (newStatus.equals(Order.status.DECLINED)) {
                    runningOrders.remove(orderId);
                    String uri = OrderManager.ECURL + "/deregisterSubscriber";
                    String channelId = "o" + orderId;
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
     * @param newOrder: order just received from attendee app
     * @return Order: persisted Order object
     */
    public Order addNewOrder(Order newOrder) {
        // update order and save to database
        newOrder.setRemtime(0);
        newOrder.setStandId(-1);
        newOrder.setStandName("notset");
        newOrder.setState(Order.status.PENDING);
        orders.saveAndFlush(newOrder);
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
      * @param o: order that is already running but OrderProcessor lost track of
     */
    public void addOrder(Order o){
       runningOrders.put(o.getId(), o);
    }

    public Order getOrder(int orderId) throws MissingEntityException {
        // look in running orders hashmap
        Order requestedOrder= runningOrders.getOrDefault(orderId,null);
        if(requestedOrder==null){
            Optional<Order> optionalOrder = orders.findById(orderId);
            if (optionalOrder.isPresent()) {
                requestedOrder = optionalOrder.get();
            } else {
                throw new MissingEntityException("Order can't be found in database or running instances.");
            }
        }
        return requestedOrder;
    }

    public Order confirmStand(int orderId, int standId) throws MissingEntityException, AlreadySetException, MissingRunException {
        Order updatedOrder=this.getOrder(orderId);

        // If stand is not yet set, update stand
        if(updatedOrder.getStandId()!=-1){
            throw new AlreadySetException("Stand has already been confirmed for this order");
        }
        else if(stands.findById(standId).isPresent()){
            Stand stand= stands.findById(standId).get();
            updatedOrder.setStandId(standId);
            updatedOrder.setState(Order.status.PENDING);
            updatedOrder.setBrandName(stand.getBrandname());
            updatedOrder.setStandName(stand.getFull_name());

            Optional<Recommendation> orderRecomOptional= orderRecommendations.get(orderId).stream()
                    .filter(r -> r.getStandId() == standId)
                    .findFirst();
            if(orderRecomOptional.isPresent()){
                Recommendation recommendation= orderRecomOptional.get();
                updatedOrder.setRemtime(recommendation.getTimeEstimate());
            }
            else{
                throw new MissingRunException("No recommendations can be found, make sure you first place an order with this id");
            }

            orders.saveAndFlush(updatedOrder);
        }
        else{
            throw new MissingEntityException("Stand can not be found, so can't be assign to an order");
        }

        return updatedOrder;
    }

    public void addRecommendations(int id, List<Recommendation> recommendations) {
        for (Recommendation recommendation : recommendations) {
            orderRecommendations.put(id, recommendation);
        }
    }
}

