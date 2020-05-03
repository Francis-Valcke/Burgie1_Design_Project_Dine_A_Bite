package cobol.services.ordermanager.controller;

import cobol.commons.exception.DoesNotExistException;
import cobol.commons.order.CommonOrder;
import cobol.commons.order.Recommendation;
import cobol.commons.order.SuperOrder;
import cobol.commons.security.CommonUser;
import cobol.services.ordermanager.CommunicationHandler;
import cobol.services.ordermanager.OrderProcessor;
import cobol.services.ordermanager.domain.entity.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Optional;

@RestController
public class OrderController {


    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderProcessor orderProcessor;// = null;
    @Autowired
    private CommunicationHandler communicationHandler;

    /**
     * This method will retrieve information about a given order identified by the orderId.
     *
     * @param orderId Id of the order
     * @return CommonOrder object
     * @throws JsonProcessingException Json processing error
     * @throws DoesNotExistException   Order does not exist
     */
    @GetMapping("/getOrderInfo")
    public ResponseEntity<CommonOrder> getOrderInfo(@RequestParam(name = "orderId") int orderId) throws JsonProcessingException, DoesNotExistException {
        // retrieve order
        Optional<Order> orderOptional = orderProcessor.getOrder(orderId);

        if (orderOptional.isPresent()) {
            return ResponseEntity.ok(orderOptional.get().asCommonOrder());
        } else {
            throw new DoesNotExistException("Order with id " + orderId + " does not exist, please create an order first");
        }
    }

    /**
     * This method will add the order to the order processor,
     * gets a recommendation from the scheduler and forwards it to the attendee app.
     *
     * @param orderObject the order recieved from the attendee app
     * @return JSONObject including CommonOrder "order" and JSONArray "Recommendation"
     * @throws JsonProcessingException Json processing error
     * @throws ParseException Json parsing error
     */
    @PostMapping(value = "/placeOrder", consumes = "application/json", produces = "application/json")
    public ResponseEntity<JSONObject> placeOrder(@AuthenticationPrincipal CommonUser userDetails, @RequestBody CommonOrder orderObject) throws JsonProcessingException, ParseException {

        // Add order to the processor
        Order newOrder = new Order(orderObject);
        newOrder = orderProcessor.addNewOrder(newOrder);

        // Put order in json to send to standmanager (as commonOrder object)
        CommonOrder mappedOrder = newOrder.asCommonOrder();
        mappedOrder.setBrandName(orderObject.getBrandName());
        mappedOrder.setStandName(orderObject.getStandName());
        mappedOrder.setRecType(orderObject.getRecType());
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String jsonString = mapper.writeValueAsString(mappedOrder);

        // Ask standmanager for recommendation
        String responseString = communicationHandler.sendRestCallToStandManager("/getRecommendation", jsonString, null);
        List<Recommendation> recommendations = mapper.readValue(responseString, new TypeReference<List<Recommendation>>() {});
        orderProcessor.addRecommendations(newOrder.getId(), recommendations);

        // send updated order and recommendation
        JSONObject completeResponse = new JSONObject();

        // Construct response
        completeResponse.put("order", newOrder.asCommonOrder());
        completeResponse.put("recommendations", recommendations);

        CommonOrder test_order = newOrder.asCommonOrder();

        return ResponseEntity.ok(completeResponse);

    }

    /**
     * This method will handle an order from different stands in a certain brand
     *
     * @param superOrder SuperOrder object containing a list of CommonOrderItems of a certain brand
     * @return JSONArray each element containing a field "recommendations" and a field "order" similar to return of placeOrder
     */
    @PostMapping(value="/placeSuperOrder", consumes = "application/json", produces = "application/json")
    public ResponseEntity<JSONArray> placeSuperOrder(@RequestBody SuperOrder superOrder) throws JsonProcessingException, ParseException {

        // Make complete response, values will be added
        JSONArray completeResponse= new JSONArray();


        // ask StandManger to split these orderItems in Orders and give A recommendation
        JSONArray ordersRecommendations= communicationHandler.getSuperRecommendationFromSM(superOrder);

        // parse orders and recommendations
        ObjectMapper mapper= new ObjectMapper();
        for (Object ordersRecommendation : ordersRecommendations) {
            JSONObject orderRec = (JSONObject) ordersRecommendation;

            JSONObject orderJSON = (JSONObject) orderRec.get("order");
            CommonOrder commonOrder = mapper.readValue(orderJSON.toJSONString(), CommonOrder.class);
            Order order= new Order(commonOrder);
            JSONArray recJSONs= (JSONArray) orderRec.get("recommendations");
            List<Recommendation> recommendations= mapper.readValue(recJSONs.toJSONString(), new TypeReference<List<Recommendation>>() {});

            // add all seperate orders to orderprocessor, this will give them an orderId and initial values
            orderProcessor.addNewOrder(order);

            // parse the response, add the recommendations to the hashmap of recommendations with the new orderIds
            orderProcessor.addRecommendations(order.getId(), recommendations);


            JSONObject orderResponse= new JSONObject();
            orderResponse.put("order", order.asCommonOrder());
            orderResponse.put("recommendations", recommendations);

            completeResponse.add(orderResponse);
        }


        // return all the updated orders in a JSONArray with the recommendations
        return ResponseEntity.ok(completeResponse);
    }


    /**
     * Sets stand- and brandname of according order when this recommendations is chosen
     *
     * @param orderId   integer id of order to be confirmed
     * @param standName name of stand
     * @param brandName name of brand
     * @throws JsonProcessingException jsonexception
     */
    @GetMapping("/confirmStand")
    public ResponseEntity<String> confirmStand(@RequestParam(name = "orderId") int orderId, @RequestParam(name = "standName") String standName, @RequestParam(name = "brandName") String brandName) throws JsonProcessingException, DoesNotExistException {
        Optional<Order> test = orderProcessor.getOrder(orderId);

        // Update order, confirm stand
        Order updatedOrder = orderProcessor.confirmStand(orderId, standName, brandName);

        // Publish event to standmanager
        String response= communicationHandler.publishConfirmedStand(updatedOrder.asCommonOrder(), standName, brandName);

        return ResponseEntity.ok(response);
    }
}
