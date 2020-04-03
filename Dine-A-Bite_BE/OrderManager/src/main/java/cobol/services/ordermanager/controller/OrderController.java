package cobol.services.ordermanager.controller;

import cobol.commons.Event;
import cobol.commons.order.CommonOrder;
import cobol.commons.order.Recommendation;
import cobol.commons.security.CommonUser;
import cobol.services.ordermanager.CommunicationHandler;
import cobol.services.ordermanager.OrderManager;
import cobol.services.ordermanager.OrderProcessor;
import cobol.services.ordermanager.domain.entity.Order;
import cobol.commons.exception.DoesNotExistException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
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
import java.util.Optional;

@RestController
public class OrderController {


    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderProcessor orderProcessor = null;
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
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(newOrder.asCommonOrder());

        // Ask standmanager for recommendation
        String responseString = communicationHandler.sendRestCallToStandManager("/getRecommendation", jsonString, null);
        List<Recommendation> recommendations = mapper.readValue(responseString, new TypeReference<List<Recommendation>>() {});
        orderProcessor.addRecommendations(newOrder.getId(), recommendations);

        // send updated order and recommendation
        JSONObject completeResponse = new JSONObject();

        // Construct response
        completeResponse.put("order", newOrder.asCommonOrder());
        completeResponse.put("recommendations", recommendations);
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
    @RequestMapping(value = "/confirmStand", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> confirmStand(@RequestParam(name = "orderId") int orderId, @RequestParam(name = "standName") String standName, @RequestParam(name = "brandName") String brandName) throws JsonProcessingException, DoesNotExistException {
        // Update order, confirm stand
        Order updatedOrder = orderProcessor.confirmStand(orderId, standName, brandName);

        // Publish event to standmanager
        String response= communicationHandler.publishConfirmedStand(updatedOrder, standName, brandName);

        return ResponseEntity.ok(response);
    }
}
