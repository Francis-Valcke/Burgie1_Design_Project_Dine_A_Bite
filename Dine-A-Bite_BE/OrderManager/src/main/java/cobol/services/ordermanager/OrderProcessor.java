package cobol.services.ordermanager;

import cobol.services.eventchannel.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.valueOf;

/**
 * This is a Singleton
 */
public class OrderProcessor {

    private final static OrderProcessor ourInstance = new OrderProcessor();

    private Map<Integer, Order> running_orders = new HashMap<>();

    public static OrderProcessor getOrderProcessor() {
        return ourInstance;
    }

    private OrderProcessor() {};


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
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(e);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String uri = "http://localhost:8080/publishEvent";
        HttpEntity<String> request = new HttpEntity<String>(jsonString, headers);

        restTemplate.postForObject(uri, request, String.class);

        if (state == Order.status.DECLINED) {
            running_orders.remove(order_id);
        }
    }

    public void addOrder(Order o) {
        this.running_orders.put(o.getId(), o);
    }

    public Order getOrder(int order_id) {
        return running_orders.get(order_id);
    }
}

