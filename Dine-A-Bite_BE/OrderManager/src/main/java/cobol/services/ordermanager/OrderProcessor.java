package cobol.services.ordermanager;

import cobol.commons.Event;
import cobol.commons.order.Recommendation;
import cobol.services.ordermanager.dbmenu.Order;
import cobol.services.ordermanager.dbmenu.OrderRepository;
import cobol.services.ordermanager.dbmenu.Stand;
import cobol.services.ordermanager.dbmenu.StandRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    // orderid - recommendations
    ListMultimap<Integer, Recommendation> orderRecommendations= ArrayListMultimap.create();

    private Map<Integer, Order> runningOrders = new HashMap<>();

    private OrderProcessor() {}

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
        Order o = runningOrders.get(order_id);
        o.setState(state);
        String[] order_channel = {String.valueOf(order_id), String.valueOf(o.getStandId())};
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
        headers.add("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJPcmRlck1hbmFnZXIiLCJyb2xlcyI6WyJST0xFX0FQUExJQ0FUSU9OIl0sImlhdCI6MTU4NDkxMTY3MSwiZXhwIjoxNzQyNTkxNjcxfQ.VmujsURhZaXRp5FQJXzmQMB-e6QSNF-OyPLeMEMOVvI");
        String uri = OrderManager.ECURL+"/publishEvent";
        HttpEntity<String> request = new HttpEntity<String>(jsonString, headers);

        restTemplate.postForObject(uri, request, String.class);

        if (state == Order.status.DECLINED) {
            runningOrders.remove(order_id);
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
        return newOrder;
    }

    /**
     * Add an already existing order (for example from database)
      * @param o: order that is already running but OrderProcessor lost track of
     */
    public void addOrder(Order o){
       runningOrders.put(o.getId(), o);
    }

    public Order getOrder(int orderId) {
        // look in running orders hashmap
        Order requestedOrder= runningOrders.getOrDefault(orderId,null);
        if(requestedOrder==null){
           requestedOrder=orders.findById(orderId).get();
           this.addOrder(requestedOrder);
        }
        return requestedOrder;
    }

    public Order confirmStand(int orderId, int standId) {
        Order updatedOrder=this.getOrder(orderId);

        if(updatedOrder.getStandId()==-1){
            Stand stand= stands.findById(standId).get();
            updatedOrder.setStandId(standId);
            updatedOrder.setState(Order.status.PENDING);
            updatedOrder.setBrandName(stand.getBrandname());
            updatedOrder.setStandName(stand.getFull_name());

            Recommendation recommendation= orderRecommendations.get(orderId).stream()
                    .filter(r -> r.getStandId() == standId)
                    .findFirst().get();

            updatedOrder.setRemtime(recommendation.getTimeEstimate()*1000);

            orders.saveAndFlush(updatedOrder);
        }

        return updatedOrder;
    }

    public void addRecommendations(int id, List<Recommendation> recommendations) {
        for (Recommendation recommendation : recommendations) {
            orderRecommendations.put(id, recommendation);
        }
    }
}

