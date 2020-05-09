package cobol.services.ordermanager.controller;

import cobol.commons.communication.response.BetterResponseModel;
import cobol.commons.domain.*;
import cobol.commons.communication.response.ResponseModel;
import cobol.commons.exception.CommunicationException;
import cobol.commons.exception.DoesNotExistException;
import cobol.commons.exception.DuplicateStandException;
import cobol.commons.domain.Recommendation;
import cobol.commons.domain.SuperOrder;
import cobol.commons.domain.CommonUser;
import cobol.commons.stub.IOrderManager;
import cobol.commons.stub.OrderManagerStub;
import cobol.services.ordermanager.ASCommunicationHandler;
import cobol.services.ordermanager.CommunicationHandler;
import cobol.services.ordermanager.MenuHandler;
import cobol.services.ordermanager.OrderProcessor;
import cobol.services.ordermanager.domain.entity.*;
import cobol.services.ordermanager.domain.repository.BrandRepository;
import cobol.services.ordermanager.domain.repository.StandRepository;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static cobol.commons.communication.response.ResponseModel.status.ERROR;
import static cobol.commons.communication.response.ResponseModel.status.OK;
import static cobol.commons.stub.OrderManagerStub.*;

@RestController
public class OrderManagerController implements IOrderManager {

    @Autowired
    private OrderProcessor orderProcessor;
    @Autowired
    private ASCommunicationHandler aSCommunicationHandler;
    @Autowired
    private CommunicationHandler communicationHandler;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private MenuHandler menuHandler;
    @Autowired
    private StandRepository standRepository;

    /**
     * This API will test if the server is still alive.
     *
     * @return "OrderManager is alive!"
     */
    @GetMapping(OrderManagerStub.GET_PING)
    public ResponseEntity<HashMap<Object, Object>> ping() {
        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(OK.toString())
                        .details("OrderManager is alive!")
                        .build().generateResponse()
        );
    }

    @Override
    @GetMapping(path = "/verify")
    public ResponseEntity<HashMap<Object,Object>> verify(@RequestParam String standName, @RequestParam String brandName, @AuthenticationPrincipal CommonUser authenticatedUser){

        Stand stand = standRepository.findStandById(standName, brandName).orElse(null);
        User user = new User(authenticatedUser); //Convert to real User object to be able to compare

        if (stand == null) {
            return ResponseEntity.ok(
                    ResponseModel.builder()
                            .status(OK.toString())
                            .details("The stand does not exist and is free to be created.")
                            .build().generateResponse()
            );
        } else if (stand.getOwners().contains(user)) {
            return ResponseEntity.ok(
                    ResponseModel.builder()
                            .status(OK.toString())
                            .details("The currently authenticated user is a verified owner of this stand.")
                            .build().generateResponse()
            );
        } else {
            return ResponseEntity.ok(
                    ResponseModel.builder()
                            .status(ERROR.toString())
                            .details("This currently authenticated user is not a owner of this stand.")
                            .build().generateResponse()
            );
        }
    }

    @Override
    @PostMapping(path = GET_VERIFY)
    @ResponseBody
    public ResponseEntity<HashMap<Object,Object>> addStand(@RequestBody CommonStand stand, @AuthenticationPrincipal CommonUser user) throws JsonProcessingException, ParseException, DuplicateStandException, CommunicationException {

        menuHandler.addStand(stand, user);

        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(OK.toString())
                        .details("The stand was added.")
                        .build().generateResponse()
        );
    }


    @Override
    @PostMapping(path = POST_UPDATE_STAND)
    @ResponseBody
    public ResponseEntity<HashMap<Object,Object>> updateStand(@RequestBody CommonStand stand) throws DoesNotExistException, JsonProcessingException {
        menuHandler.updateStand(stand);
        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(OK.toString())
                        .details("The stand was updated.")
                        .build().generateResponse()
        );
    }


    @Override
    @DeleteMapping(value = DELETE_DELETE_STAND)
    @ResponseBody
    public ResponseEntity<HashMap<Object,Object>> deleteStand(@RequestParam String standName, @RequestParam String brandName) throws JsonProcessingException, DoesNotExistException {
        menuHandler.deleteStandById(standName, brandName);
        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(OK.toString())
                        .details("The stand was removed.")
                        .build().generateResponse()
        );
    }


    @Override
    @GetMapping(value = GET_STANDS)
    public ResponseEntity<Map<String, String>> requestStandNames() {
        return ResponseEntity.ok(standRepository.findAll()
                .stream().collect(Collectors.toMap(Stand::getName, stand -> stand.getBrand().getName())));
    }

    @Override
    @GetMapping(value = GET_STAND_LOCATIONS)
    public ResponseEntity<Map<String, Map<String, Double>>> requestStandLocations() {
        return ResponseEntity.ok(standRepository.findAll()
                .stream().collect(Collectors.toMap(Stand::getName, Stand::getLocation)));
    }

    @Override
    @GetMapping(GET_GET_ORDER_INFO)
    public ResponseEntity<CommonOrder> getOrderInfo(@RequestParam(name = "orderId") int orderId) throws JsonProcessingException, DoesNotExistException {
        // retrieve order
        Optional<Order> orderOptional = orderProcessor.getOrder(orderId);

        if (orderOptional.isPresent()) {
            return ResponseEntity.ok(orderOptional.get().asCommonOrder());
        } else {
            throw new DoesNotExistException("Order with id " + orderId + " does not exist, please create an order first");
        }
    }

    @Override
    @PostMapping(value = POST_PLACE_ORDER, consumes = "application/json", produces = "application/json")
    public ResponseEntity<JSONObject> placeOrder(@AuthenticationPrincipal CommonUser userDetails, @RequestBody CommonOrder orderObject) throws Throwable {

        // First calculate the total price of the order
        Brand brand = brandRepository.findById(orderObject.getBrandName())
                .orElseThrow(() -> new DoesNotExistException("The brand of the given order does not exist in the database, this should not be possible."));

        // Get all distinct food items that are present for the brand
        List<Food> brandFood = brand.getStandList().stream().flatMap(s -> s.getFoodList().stream()).distinct().collect(Collectors.toList());
        // Each ordered item should be in this list, search this list to find the price.
        BigDecimal total = BigDecimal.ZERO;
        for (CommonOrderItem orderItem : orderObject.getOrderItems()) {
            total = total.subtract(
                    brandFood.stream()
                            .filter(f -> f.getName().equals(orderItem.getFoodName()) && f.getBrandName().equals(orderObject.getBrandName()))
                            .findAny()
                            .orElseThrow(() -> new DoesNotExistException("OrderItem " +orderItem.getFoodName() + " does not exist in the backend, this should not be possible"))
                            .getPrice()
                            .multiply(new BigDecimal(orderItem.getAmount()))
            );
        }

        // With this price we try to create a transaction
        BetterResponseModel<BetterResponseModel.GetBalanceResponse> response = aSCommunicationHandler.callCreateTransaction(userDetails.getUsername(), total);
        if (response.getStatus().equals(BetterResponseModel.Status.ERROR)){
            // There was an error creating the transaction. Throw this.
            throw response.getException();
        }

        // Add order to the processor
        Order newOrder = new Order(orderObject);
        newOrder = orderProcessor.addNewOrder(newOrder);

        // Put order in json to send to standmanager (as commonOrder object)
        CommonOrder mappedOrder = newOrder.asCommonOrder();
        mappedOrder.setBrandName(orderObject.getBrandName());
        mappedOrder.setStandName(orderObject.getStandName());
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

    @Override
    @PostMapping(value=POST_PLACE_SUPER_ORDER, consumes = "application/json", produces = "application/json")
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


    @Override
    @GetMapping(GET_CONFIRM_STAND)
    public ResponseEntity<String> confirmStand(@RequestParam(name = "orderId") int orderId, @RequestParam(name = "standName") String standName, @RequestParam(name = "brandName") String brandName, @AuthenticationPrincipal CommonUser userDetails) throws Throwable {
        Optional<Order> test = orderProcessor.getOrder(orderId);
        // Update order, confirm stand
        Order updatedOrder = orderProcessor.confirmStand(orderId, standName, brandName);

        // Publish event to standmanager
        String response= communicationHandler.publishConfirmedStand(updatedOrder.asCommonOrder(), standName, brandName);

        // Also complete the payment
        BetterResponseModel<BetterResponseModel.GetBalanceResponse> asResponse = aSCommunicationHandler.callConfirmTransaction(userDetails.getUsername());
        if (asResponse.getStatus().equals(BetterResponseModel.Status.ERROR)){
            // There was an error creating the transaction. Throw this.
            throw asResponse.getException();
        }

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping(GET_MENU)
    @ResponseBody
    public ResponseEntity<List<CommonFood>> requestGlobalMenu() throws JsonProcessingException {
        return ResponseEntity.ok(menuHandler.getGlobalMenu());
    }

    @Override
    @GetMapping(GET_STAND_MENU)
    @ResponseBody
    public ResponseEntity<List<CommonFood>> requestStandMenu(@RequestParam String standName, @RequestParam String brandName) throws DoesNotExistException {
        return ResponseEntity.ok(
                menuHandler.getStandMenu(standName, brandName)
                        .stream()
                        .map(Food::asCommonFood)
                        .collect(Collectors.toList())
        );
    }

    @Override
    @PostMapping(POST_DB_IMPORT)
    public ResponseEntity<String> load(@RequestBody List<CommonBrand> data) {

        //Add stand as default owner
        //User user = userRepository.save(User.builder().username("stand").build());
        //data.stream().flatMap(brand -> brand.getStandList().stream()).forEach(stand -> stand.getOwners().add(user));
        //TODO FIX THIS
        //data.forEach(brand -> brandRepository.save(brand));
        return ResponseEntity.ok("Success");
    }

    @Override
    @DeleteMapping(DELETE_DB_CLEAR)
    public ResponseEntity<String> clear() throws ParseException, JsonProcessingException {

        brandRepository.deleteAll();
        return ResponseEntity.ok("Success");

    }

    @Override
    @GetMapping(GET_DB_EXPORT)
    public ResponseEntity<List<CommonBrand>> export() {

        List<Brand> data = brandRepository.findAll();
        //TODO FIX THIS
        //return ResponseEntity.ok(data);
        return null;
    }


    @Override
    @GetMapping(GET_UPDATE_SM)
    public ResponseEntity<List<String>> update() throws JsonProcessingException {
        menuHandler.updateStandManager();

        List<Stand> stands = standRepository.findAll();
        List<String> standNames = stands.stream().map(Stand::getName).collect(Collectors.toList());
        return ResponseEntity.ok(standNames);
    }

    @Override
    @DeleteMapping(DELETE_DELETE)
    public ResponseEntity<String> delete() throws ParseException, JsonProcessingException {
        menuHandler.deleteAll();
        return ResponseEntity.ok("Database from OrderManager and StandManager cleared.");
    }

}
