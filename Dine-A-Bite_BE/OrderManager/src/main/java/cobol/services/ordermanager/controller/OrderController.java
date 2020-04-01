package cobol.services.ordermanager.controller;

import cobol.commons.Event;
import cobol.commons.order.CommonOrder;
import cobol.commons.order.Recommendation;
import cobol.commons.security.CommonUser;
import cobol.services.ordermanager.MenuHandler;
import cobol.services.ordermanager.OrderManager;
import cobol.services.ordermanager.OrderProcessor;
import cobol.services.ordermanager.domain.entity.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
public class OrderController {

    @Autowired
    private MenuHandler menuHandler;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderProcessor orderProcessor = null;

    /**
     * This method will retrieve information about a given order identified by the orderId.
     *
     * @param orderId Id of the order
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping("/getOrderInfo")
    public ResponseEntity<CommonOrder> getOrderInfo(@RequestParam(name="orderId") int orderId) throws JsonProcessingException {

        // retrieve order
        Order order= orderProcessor.getOrder(orderId);

        // write order to json
        ObjectMapper mapper= new ObjectMapper();
        String jsonString= mapper.writeValueAsString(order);
        JSONParser parser= new JSONParser();
        JSONObject orderResponse=new JSONObject();
        try {
            orderResponse = (JSONObject) parser.parse(jsonString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return orderResponse;
    }

    /**
     * Add the order to the order processor, gets a recommendation from the scheduler and forwards it to the attendee app.
     *
     * @param orderObject the order recieved from the attendee app
     * @return the order id, along with the json with recommended stands
     * @throws JsonProcessingException json error
     *
     */
    @PostMapping(value = "/placeOrder", consumes = "application/json", produces = "application/json")
    public JSONObject placeOrder(@AuthenticationPrincipal CommonUser userDetails, @RequestBody JSONObject orderObject) throws JsonProcessingException, ParseException {

        // Add order to the processor
        Order newOrder= new Order(orderObject);
        newOrder=orderProcessor.addNewOrder(newOrder);


        // Put order in json to send to standmanager (as commonOrder object)
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(newOrder);



        // Ask standmanager for recommendation
        String responseString= menuHandler.sendRestCallToStandManager("/getRecommendation", jsonString, null);

        List<Recommendation> recommendations= mapper.readValue(responseString, new TypeReference<List<Recommendation>>() {});
        orderProcessor.addRecommendations(newOrder.getId(), recommendations);

        // send updated order and recommendation
        JSONObject completeResponse= new JSONObject();

        // ---- add updated order
        String updateOrder = mapper.writeValueAsString(newOrder);
        // String to JSON
        JSONObject updateOrderJson=new JSONObject();
        JSONParser parser= new JSONParser();
        try {
            updateOrderJson = (JSONObject) parser.parse(updateOrder);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // ---- add recommendations
        String recommendationsResponse=mapper.writeValueAsString(recommendations);
        JSONArray recomResponseJson= new JSONArray();
        try {
            recomResponseJson=(JSONArray) parser.parse(recommendationsResponse);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Construct response
        completeResponse.put("order", updateOrderJson);
        completeResponse.put("recommendations", recomResponseJson);
        return completeResponse;
    }


    /**
     * Sets stand- and brandname of according order when this recommendations is chosen
     * @param orderId integer id of order to be confirmed
     * @param standName name of stand
     * @param brandName name of brand
     * @throws JsonProcessingException jsonexception
     */
    @RequestMapping(value = "/confirmStand", method = RequestMethod.GET)
    @ResponseBody
    public void confirmStand(@RequestParam(name = "orderId") int orderId, @RequestParam(name = "standName") String standName, @RequestParam(name = "brandName") String brandName) throws JsonProcessingException{
        // Update order, confirm stand
        Order updatedOrder=orderProcessor.confirmStand(orderId, standName, brandName);

        // Make event for eventchannel (orderId, standId)
        JSONObject orderJson = new JSONObject();
        orderJson.put("order", updatedOrder);
        List<String> types = new ArrayList<>();
        types.add("o"+orderId);
        types.add("s_"+standName+"_"+brandName);
        Event e = new Event(orderJson, types, "Order");

        // Publish event to standmanager
        String jsonString = objectMapper.writeValueAsString(e);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String uri = OrderManager.ECURL+"/publishEvent";

        HttpEntity<String> entity = new HttpEntity<>(jsonString, headers);
        String response = restTemplate.postForObject(uri, entity, String.class);

        System.out.println(response);
    }
}
