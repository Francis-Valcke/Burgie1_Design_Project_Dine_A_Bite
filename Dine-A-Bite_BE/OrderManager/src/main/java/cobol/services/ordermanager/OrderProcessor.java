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

    public static OrderProcessor getOrderProcessor() {
        return ourInstance;
    }

    private int subscriberId;

    private List<Event> eventQueue = new LinkedList<Event>();

    @Autowired
    private ObjectMapper objectMapper;

    private OrderProcessor() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJPcmRlck1hbmFnZXIiLCJyb2xlcyI6WyJST0xFX0FQUExJQ0FUSU9OIl0sImlhdCI6MTU4NDkxMTY3MSwiZXhwIjoxNzQyNTkxNjcxfQ.VmujsURhZaXRp5FQJXzmQMB-e6QSNF-OyPLeMEMOVvI");
        String uri = "http://cobol.idlab.ugent.be:8092/registerSubscriber";
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        this.subscriberId = Integer.valueOf(response.toString());
    };


    //TODO: Orderprocessor needs to listen the the right channels to receive notifications of the stands (DECLINED, etc)
    /**
     *
     * @param order_id the id of the order whose state has changed
     * @param state new state of the order
     * @throws JsonProcessingException
     *
     * This method changes the state of an order an sends an event to the channel of this order
     */
    public void publishStateChange(int order_id, Order.status state) throws JsonProcessingException{
        Order o = running_orders.get(order_id);
        o.setState(state);
        String[] order_channel = {String.valueOf(order_id), String.valueOf(o.getStand_id())};
        JSONObject data = new JSONObject();
        data.put("order_id", o.getId());
        data.put("state_change", state);
        data.put("order", o);
        Event e = new Event(data, order_channel);
        System.out.println(e.getOrderData());
        String jsonString = objectMapper.writeValueAsString(e);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJPcmRlck1hbmFnZXIiLCJyb2xlcyI6WyJST0xFX0FQUExJQ0FUSU9OIl0sImlhdCI6MTU4NDkxMTY3MSwiZXhwIjoxNzQyNTkxNjcxfQ.VmujsURhZaXRp5FQJXzmQMB-e6QSNF-OyPLeMEMOVvI");
        String uri = "http://cobol.idlab.ugent.be:8092/publishEvent";
        HttpEntity<String> request = new HttpEntity<String>(jsonString, headers);

        restTemplate.postForObject(uri, request, String.class);

        if (state == Order.status.DECLINED) {
            running_orders.remove(order_id);
        }
    }

    public void pollEvents() throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJPcmRlck1hbmFnZXIiLCJyb2xlcyI6WyJST0xFX0FQUExJQ0FUSU9OIl0sImlhdCI6MTU4NDkxMTY3MSwiZXhwIjoxNzQyNTkxNjcxfQ.VmujsURhZaXRp5FQJXzmQMB-e6QSNF-OyPLeMEMOVvI");
        String uri = "http://cobol.idlab.ugent.be:8092/events";
        HttpEntity<?> entity = new HttpEntity<>(headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
                .queryParam("id", this.subscriberId);
        ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);
        JSONObject responseObject = objectMapper.readValue(response.toString(), JSONObject.class);
        String details = (String) responseObject.get("details");
        List<Event> eventList = objectMapper.readValue(details, new TypeReference<List<Event>>() {});
        eventQueue.addAll(eventList);
    }

    public void addOrder(Order o) {
        this.running_orders.put(o.getId(), o);
    }

    public Order getOrder(int order_id) {
        return running_orders.get(order_id);
    }
}

