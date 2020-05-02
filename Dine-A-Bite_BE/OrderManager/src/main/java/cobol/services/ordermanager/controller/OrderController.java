package cobol.services.ordermanager.controller;

import cobol.commons.BetterResponseModel;
import cobol.commons.BetterResponseModel.*;
import cobol.commons.exception.DoesNotExistException;
import cobol.commons.order.CommonOrder;
import cobol.commons.order.CommonOrderItem;
import cobol.commons.order.Recommendation;
import cobol.commons.security.CommonUser;
import cobol.services.ordermanager.ASCommunicationHandler;
import cobol.services.ordermanager.CommunicationHandler;
import cobol.services.ordermanager.OrderProcessor;
import cobol.services.ordermanager.domain.entity.Food;
import cobol.services.ordermanager.domain.entity.Order;
import cobol.services.ordermanager.domain.repository.FoodRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
public class OrderController {


    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderProcessor orderProcessor = null;
    @Autowired
    private ASCommunicationHandler aSCommunicationHandler;
    @Autowired
    private CommunicationHandler communicationHandler;
    @Autowired
    private FoodRepository foodRepository;

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
    public ResponseEntity<JSONObject> placeOrder(@AuthenticationPrincipal CommonUser userDetails, @RequestBody CommonOrder orderObject) throws Throwable {

        // First calculate the total price of the order
        // TODO would make a lot more sense if the total price of the order was just sent with the order, but for now it will work this way too, just not that efficient.
        BigDecimal total = BigDecimal.ZERO;
        String brandName = orderObject.getBrandName();
        String standName = orderObject.getStandName();

        for (CommonOrderItem orderItem : orderObject.getOrderItems()) {
            // Find the food item in the database to get its price
            Food food = foodRepository.findFoodById(orderItem.getFoodName(), standName, brandName)
                    .orElseThrow(() -> new DoesNotExistException("A food item in the order does not exist in the database, this should not be possible."));

            total = total.add(BigDecimal.valueOf(-food.getPrice() * orderItem.getAmount()));
        }

        // With this price we try to create a transaction
        BetterResponseModel<GetBalanceResponse> response = aSCommunicationHandler.callCreateTransaction(userDetails.getUsername(), total);
        if (response.getStatus().equals(Status.ERROR)){
            // There was an error creating the transaction. Throw this.
            throw response.getException();
        }


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
    public ResponseEntity<String> confirmStand(@RequestParam(name = "orderId") int orderId, @RequestParam(name = "standName") String standName, @RequestParam(name = "brandName") String brandName, @AuthenticationPrincipal CommonUser userDetails) throws Throwable {
        // Update order, confirm stand
        Order updatedOrder = orderProcessor.confirmStand(orderId, standName, brandName);

        // Publish event to standmanager
        String response= communicationHandler.publishConfirmedStand(updatedOrder.asCommonOrder(), standName, brandName);

        // Also complete the payment
        BetterResponseModel<GetBalanceResponse> asResponse = aSCommunicationHandler.callConfirmTransaction(userDetails.getUsername());
        if (asResponse.getStatus().equals(Status.ERROR)){
            // There was an error creating the transaction. Throw this.
            throw asResponse.getException();
        }

        return ResponseEntity.ok(response);
    }
}
