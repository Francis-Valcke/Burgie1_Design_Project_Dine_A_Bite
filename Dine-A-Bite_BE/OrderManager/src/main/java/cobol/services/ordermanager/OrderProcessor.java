package cobol.services.ordermanager;

import cobol.commons.Event;
import cobol.commons.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * This is a Singleton
 */
public class OrderProcessor {

    private final static OrderProcessor ourInstance = new OrderProcessor();
    private Map<Integer, Order> running_orders = new HashMap<>();
    private int subscriberId;
    private volatile LinkedList<Event> eventQueue = new LinkedList<Event>();
    private RestTemplate restTemplate;
    private HttpHeaders headers;
    private HttpEntity entity;
    private ObjectMapper objectMapper;

    private OrderProcessor() {
        objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
        this.headers = new HttpHeaders();
        this.headers.add("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJPcmRlck1hbmFnZXIiLCJyb2xlcyI6WyJST0xFX0FQUExJQ0FUSU9OIl0sImlhdCI6MTU4NDkxMTY3MSwiZXhwIjoxNzQyNTkxNjcxfQ.VmujsURhZaXRp5FQJXzmQMB-e6QSNF-OyPLeMEMOVvI");
        String uri = "http://localhost:8083/registerSubscriber";
        this.entity = new HttpEntity(headers);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        this.subscriberId = Integer.valueOf(response.getBody());
    };


    public void run() {
        System.out.println("I got here");
        String uri = "http://localhost:8083/events";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
                .queryParam("id", this.subscriberId);
        ResponseEntity<String> response = this.restTemplate.exchange(builder.toUriString(), HttpMethod.GET, this.entity, String.class);
        try {
            JSONObject responseObject = this.objectMapper.readValue(response.getBody(), JSONObject.class);
            String details = (String) responseObject.get("details");
            List<Event> eventList = objectMapper.readValue(details, new TypeReference<List<Event>>() {});
            eventQueue.addAll(eventList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void processEvents() throws JsonProcessingException {
        while (!eventQueue.isEmpty()) {
            Event e = eventQueue.poll();
            if (e.getDataType().equals("Order")) {
                Order o = objectMapper.readValue(e.getOrderData().toString(), new TypeReference<Order>() {});
                Order.status newStatus = o.getOrderStatus();
                Order localOrder = running_orders.get(o.getId());
                localOrder.setState(newStatus);
                if (newStatus.equals(Order.status.DECLINED)) {
                    running_orders.remove(o.getId());
                }
            }
        }
    }

    public void addOrder(Order o) {
        this.running_orders.put(o.getId(), o);

        String uri = "http://cobol.idlab.ugent.be:8092/registerSubscriber/toChannel";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
                .queryParam("id", this.subscriberId)
                .queryParam("type", o.getId());
        ResponseEntity<String> response = this.restTemplate.exchange(builder.toUriString(), HttpMethod.GET, this.entity, String.class);
    }

    public Order getOrder(int order_id) {
        return running_orders.get(order_id);
    }

    public static OrderProcessor getOrderProcessor() {
        return ourInstance;
    }
}

