package cobol.services.ordermanager.controller;

import cobol.commons.BetterResponseModel;
import cobol.commons.BetterResponseModel.GetBalanceResponse;
import cobol.commons.BetterResponseModel.Status;
import cobol.commons.exception.DoesNotExistException;
import cobol.commons.order.*;
import cobol.commons.security.CommonUser;
import cobol.services.ordermanager.ASCommunicationHandler;
import cobol.services.ordermanager.CommunicationHandler;
import cobol.services.ordermanager.OrderProcessor;
import cobol.services.ordermanager.domain.entity.*;
import cobol.services.ordermanager.domain.repository.BrandRepository;
import cobol.services.ordermanager.domain.repository.FoodRepository;
import cobol.services.ordermanager.domain.repository.StandRepository;
import cobol.services.ordermanager.domain.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class OrderController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FoodRepository foodRepository;
    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderProcessor orderProcessor;// = null;
    @Autowired
    private ASCommunicationHandler aSCommunicationHandler;
    @Autowired
    private CommunicationHandler communicationHandler;
    @Autowired
    private StandRepository standRepository;

    /**
     * This method will retrieve information about a given order identified by the orderId.
     *
     * @param orderId Id of the order
     * @return CommonOrder object
     */
    @GetMapping("/getOrderInfo")
    public ResponseEntity<BetterResponseModel<CommonOrder>> getOrderInfo(@RequestParam(name = "orderId") int orderId) {
        // retrieve order
        Optional<Order> orderOptional = orderProcessor.getOrder(orderId);

        if (orderOptional.isPresent()) {
            return ResponseEntity.ok(BetterResponseModel.ok("Successfully retrieved order info", orderOptional.get().asCommonOrder()));
        } else {
            DoesNotExistException e = new DoesNotExistException("Order with id " + orderId + " does not exist, please create an order first");
            System.out.println("ERROR: " + e.getMessage());
            return ResponseEntity.ok(BetterResponseModel.error("Error thrown while retrieving order info", e));
        }
    }
    /*
    For now this happens with events
    @PostMapping(value = "/updateOrder")
    public ResponseEntity<BetterResponseModel<JSONObject>> updateOrder(@RequestParam(name = "orderId") int orderId, @RequestParam(name = "newStatus") CommonOrder.State newStatus){
        JSONObject completeResponse = new JSONObject();
        try {
            Optional<Order> orderOptional = orderProcessor.getOrder(orderId);
            if (orderOptional.isPresent()) {
                orderOptional.get().setState(newStatus);
                orderOptional.get().getStand();

            }
            else {
                throw new Exception();
            }
        }catch (Throwable e) {
            e.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error("Error while changing order status", e));
        }
        return ResponseEntity.ok(BetterResponseModel.ok("Successfully updated order status", completeResponse));
    }
    */

    /**
     * This method is called when an attendee places an order
     * First the transaction is handled
     * A CommonOrder is transformed to an Order object, so it can be updated and saved to the database if the order can be placed
     * Recommendations are fetched from the standmanager and the updated order (with id) is sent back with recommendations to the attendee app
     *
     * @param orderObject the order recieved from the attendee app
     * @return JSONObject including CommonOrder "order" and JSONArray "Recommendation"
     */
    @PostMapping(value = "/placeOrder", consumes = "application/json", produces = "application/json")
    public ResponseEntity<BetterResponseModel<JSONObject>> placeOrder(@AuthenticationPrincipal CommonUser userDetails, @RequestBody CommonOrder orderObject) {
        JSONObject completeResponse = new JSONObject();
        try {
            /* -- Money Transaction for this order -- */
            orderTransaction(orderObject, userDetails);


            /* -- Convert CommonOrder to normal Order object -- */
            Order newOrder = new Order(orderObject);
            // Set user for this order
            User user = userRepository.findById(userDetails.getUsername()).orElse(userRepository.save(new User(userDetails)));
            newOrder.setUser(user);
            // Add order to the processor
            newOrder = orderProcessor.addNewOrder(newOrder);


            /* -- Prepare and send updated order to standmanager --*/

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
            // Parse recommendations
            List<Recommendation> recommendations = mapper.readValue(responseString, new TypeReference<List<Recommendation>>() {
            });
            orderProcessor.addRecommendations(newOrder.getId(), recommendations);


            /* -- Prepare and send response back to application -- */

            // Construct response
            completeResponse.put("order", newOrder.asCommonOrder());
            completeResponse.put("recommendations", recommendations);


        } catch (Throwable e) {
            e.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error("Error while placing order", e));
        }
        return ResponseEntity.ok(BetterResponseModel.ok("Successfully placed order", completeResponse));
    }


    /**
     * Check if the transaction of this order can be succeeded and prepare
     * It will simultaneously add prices to the incoming orderitems in the commonorder object
     *
     * @param orderObject CommonOrder object
     * @param userDetails CommonUser which places the order
     * @throws Throwable all exceptions
     */
    public void orderTransaction(CommonOrder orderObject, CommonUser userDetails) throws Throwable {
        // First calculate the total price of the order
        Brand brand = brandRepository.findById(orderObject.getBrandName())
                .orElseThrow(() -> new DoesNotExistException("The brand of the given order does not exist in the database, this should not be possible."));

        // Get all distinct food items that are present for the brand
        List<Food> brandFood = brand.getStandList().stream().flatMap(s -> s.getFoodList().stream()).distinct().collect(Collectors.toList());
        // Each ordered item should be in this list, search this list to find the price.
        BigDecimal total = BigDecimal.ZERO;
        for (CommonOrderItem orderItem : orderObject.getOrderItems()) {
            // search for current price of this orderitem
            BigDecimal itemPrice = brandFood.stream()
                    .filter(f -> f.getName().equals(orderItem.getFoodName()) && f.getBrandName().equals(orderObject.getBrandName()))
                    .findAny()
                    .orElseThrow(() -> new DoesNotExistException("OrderItem " + orderItem.getFoodName() + " does not exist in the backend, this should not be possible"))
                    .getPrice();


            // add this price of this orderitem to total
            total = total.subtract(
                    itemPrice
                            .multiply(new BigDecimal(orderItem.getAmount()))
            );

            // update price of this CommonOrderItem
            orderItem.setPrice(itemPrice);
        }

        // With this price we try to create a transaction
        BetterResponseModel<GetBalanceResponse> response = aSCommunicationHandler.callCreateTransaction(userDetails.getUsername(), total);
        if (response.getStatus().equals(Status.ERROR)) {
            // There was an error creating the transaction. Throw this.
            throw response.getException();
        }
    }

    /**
     * This method will handle an order from different stands in a certain brand, the superorder will be split in multiple normal orders
     *
     * @param superOrder SuperOrder object containing a list of CommonOrderItems of a certain brand
     * @return JSONArray each element containing a field "recommendations" and a field "order" similar to return of placeOrder
     */
    @PostMapping(value = "/placeSuperOrder", consumes = "application/json", produces = "application/json")
    public ResponseEntity<BetterResponseModel<JSONArray>> placeSuperOrder(@RequestBody SuperOrder superOrder, @AuthenticationPrincipal CommonUser userDetails) {

        // Make complete response, values will be added later on
        JSONArray completeResponse = new JSONArray();

        try {
            //Temporary "fix" for superorder issue with priorityqueues
            //Will be a useless (empty) order in db if superorder really does need to be split
            //Only necessary to "reserve" an id in db for if superorder doesn't need to be split at all
            Order orderTemp = new Order();
            orderProcessor.addNewOrder(orderTemp);
            // ask StandManger to split these orderItems in Orders and give A recommendation
            superOrder.setTempId(orderTemp.getId());
            List<SuperOrderRec> ordersRecommendations = communicationHandler.getSuperRecommendationFromSM(superOrder);

            boolean firstSplit = true;
            for (SuperOrderRec ordersRecommendation : ordersRecommendations) {
                CommonOrder commonOrder= ordersRecommendation.getOrder();
                List<Recommendation> recommendations= ordersRecommendation.getRecommendations();

                orderTransaction(commonOrder, userDetails);

                Order order;
                // add all seperate orders to orderprocessor, this will give them an orderId and initial values
                if (firstSplit){
                    order = orderTemp;
                    order.setStartTime(ZonedDateTime.now(ZoneId.of("Europe/Brussels")));
                    order.setExpectedTime(ZonedDateTime.now(ZoneId.of("Europe/Brussels")));
                    order.setOrderState(commonOrder.getOrderState());
                    order.setOrderItems(new ArrayList<>());
                    for (CommonOrderItem commonOrderItem : commonOrder.getOrderItems()) {
                        order.addOrderItem(new OrderItem(commonOrderItem, order));
                    }
                    order.setLatitude(commonOrder.getLatitude());
                    order.setLongitude(commonOrder.getLongitude());
                    order.setRecType(commonOrder.getRecType());
                    firstSplit = false;
                }
                else {
                    order = new Order(commonOrder);
                }
                User user = userRepository.findById(userDetails.getUsername()).orElse(userRepository.save(new User(userDetails)));
                order.setUser(user);
                orderProcessor.addNewOrder(order);
                // parse the response, add the recommendations to the hashmap of recommendations with the new orderIds
                orderProcessor.addRecommendations(order.getId(), recommendations);


                JSONObject orderResponse = new JSONObject();
                orderResponse.put("order", order.asCommonOrder());
                orderResponse.put("recommendations", recommendations);

                completeResponse.add(orderResponse);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error("Server: Error while placing a superorder", e));
        }


        // return all the updated orders in a JSONArray with the recommendations
        return ResponseEntity.ok(BetterResponseModel.ok("Server: Successfully placed a superorder", completeResponse));
    }


    /**
     * Sets stand- and brandname of according order when one of the recommendations is chosen
     *
     * @param orderId   integer id of order to be confirmed
     * @param standName name of stand
     * @param brandName name of brand
     */
    @GetMapping("/confirmStand")
    public ResponseEntity<BetterResponseModel<String>> confirmStand(@RequestParam(name = "orderId") int orderId, @RequestParam(name = "standName") String standName, @RequestParam(name = "brandName") String brandName, @AuthenticationPrincipal CommonUser userDetails) {
        String response = "";
        try {
            // Update order, confirm stand
            Order updatedOrder = orderProcessor.confirmStand(orderId, standName, brandName);

            // Publish event to standmanager
            // TODO: WHY DOES THIS HAVE TO BE DONE, YOU ALREADY SEND REST-CALL TO SM???  SHOULDN'T OM JUST SUBSCRIBE THE ORDER ON THAT STAND, SO SM CAN THEN PUBLISH EVENTS ABOUT THAT ORDER?
            response = communicationHandler.publishConfirmedStand(updatedOrder.asCommonOrder(), standName, brandName);

            //Update stand revenue
            Optional<Stand> optStand = standRepository.findStandById(standName, brandName);
            BigDecimal price = BigDecimal.ZERO;
            if (optStand.isPresent()) {
                for (OrderItem item : updatedOrder.getOrderItems()) {
                    price = price.add(foodRepository.findFoodById(item.getFoodName(), standName, brandName).get().getPrice().multiply(BigDecimal.valueOf(item.getAmount())));
                }
                Stand stand = optStand.get();
                stand.addToRevenue(price);
                standRepository.save(stand);
            }

            // Also complete the payment
            BetterResponseModel<GetBalanceResponse> asResponse = aSCommunicationHandler.callConfirmTransaction(userDetails.getUsername());
            if (asResponse.getStatus().equals(Status.ERROR)) {
                // There was an error creating the transaction. Throw this.
                throw asResponse.getException();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error("Error while confirming stand for this order", e));
        }


        return ResponseEntity.ok(BetterResponseModel.ok("Successfully confirmed stand for this order", response));
    }


    /**
     * This method returns all orders that were place by a certain user
     *
     * @param userDetails based on this user, orders are returned (based on token)
     * @return List of CommonFood items, place by user that calls this function
     */
    @GetMapping(value = "/getUserOrders", produces = "application/json")
    public ResponseEntity<BetterResponseModel<List<CommonOrder>>> getUserOrders(@AuthenticationPrincipal CommonUser userDetails) {
        try{
            User user = userRepository.findById(userDetails.getUsername()).orElseThrow(() -> new UsernameNotFoundException("Can't find user to fetch orders from"));
            return ResponseEntity.ok(BetterResponseModel.ok("Successfully retrieved orders of user",user.getOrders().stream().map(Order::asCommonOrder).collect(Collectors.toList())));
        }
        catch(UsernameNotFoundException e){
            e.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error(e.getMessage(), e));
        }
        catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error("Server: Error while retrieving orders from db", e));
        }
    }

}
