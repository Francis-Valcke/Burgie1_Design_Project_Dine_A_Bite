package cobol.services.ordermanager;

import cobol.services.eventchannel.EventPublisher;
import cobol.services.eventchannel.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class OrderProcesser {

    private EventPublisher publisher;

    /**
     *
     * @param o the order object whose state has changed
     * @param state new state of the order
     * @throws JsonProcessingException
     *
     * This method changes the state of an order an sends an event to the channel of this order
     */
    public static void publishStateChange(Order o, Order.status state) throws JsonProcessingException{
        o.setState(state);
        String[] order_channel = {String.valueOf(o.id)};
        String data = state.name();
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
    }

}

