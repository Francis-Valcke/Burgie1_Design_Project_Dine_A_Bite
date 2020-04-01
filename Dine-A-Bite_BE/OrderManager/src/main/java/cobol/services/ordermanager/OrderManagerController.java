package cobol.services.ordermanager;

import cobol.commons.CommonStand;
import cobol.commons.Event;
import cobol.commons.ResponseModel;
import cobol.commons.order.Recommendation;
import cobol.commons.security.CommonUser;
import cobol.services.ordermanager.domain.entity.Food;
import cobol.services.ordermanager.domain.entity.Order;
import cobol.services.ordermanager.domain.entity.Stand;
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
import java.util.Map;
import java.util.stream.Collectors;

import static cobol.commons.ResponseModel.status.OK;

/**
 * This class handles communication from standapplication: incoming changes to stand menus are registered in the menuhandler
 * This class also handles menurequests from the attendee applications, fetching the menus from the menuhandler
 * <p>
 * TODO: merge with code Wannes for Order functionality
 */

@RestController
public class OrderManagerController {
    @Autowired
    private MenuHandler menuHandler;

    private RestTemplate restTemplate;
    private HttpHeaders headers;
    private HttpEntity<String> entity;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    OrderProcessor orderProcessor = null;

    OrderManagerController() {
        this.restTemplate = new RestTemplate();
        this.headers = new HttpHeaders();
        this.headers.setContentType(MediaType.APPLICATION_JSON);
        this.headers.add("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJPcmRlck1hbmFnZXIiLCJyb2xlcyI6WyJST0xFX0FQUExJQ0FUSU9OIl0sImlhdCI6MTU4NDkxMTY3MSwiZXhwIjoxNzQyNTkxNjcxfQ.VmujsURhZaXRp5FQJXzmQMB-e6QSNF-OyPLeMEMOVvI");
    }

    /**
     * API endpoint to test if the server is still alive.
     *
     * @return "OrderManager is alive!"
     */
    @GetMapping("/pingOM")
    public ResponseEntity ping() {
        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(OK.toString())
                        .details("OrderManager is alive!")
                        .build().generateResponse()
        );
    }



    /**
     * will clear database and OM
     *
     * @return confirmation of deletion
     */
    @RequestMapping("/delete")
    public String delete() {
        menuHandler.deleteAll();
        return "deleting stands from database and Order Manager";
    }

    /**
     * Will check if there are already stands in database that are not in OM and add them to OM
     *
     * @return tells u if there are already stands in DB
     */
    @RequestMapping(value="/updateOM", method = RequestMethod.GET)
    public String update() throws JsonProcessingException {
        menuHandler.refreshCache();
        menuHandler.updateStandManager();

        if (menuHandler.getStands().isEmpty()) {
            return "No stands in database";
        }
        else{
            StringBuilder response = new StringBuilder();
            response.append("Stands already in database: \n");

            for (Stand s : menuHandler.getStands()) {
                response.append(s.getName()).append("\n");
            }
            return response.toString();
        }
    }

    @RequestMapping("/getOrderInfo")
    public JSONObject getOrderInfo(@RequestParam(name="orderId") int orderId) throws JsonProcessingException {

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
     * @throws JsonProcessingException
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
        String responseString= menuHandler.sendRestCallToStandManager("/getRecommendation", jsonString);

        JSONParser parser= new JSONParser();
        JSONObject response =  (JSONObject) parser.parse(responseString);
        List<Recommendation> recommendations= mapper.readValue(response.get("recommendations").toString(), new TypeReference<List<Recommendation>>() {});
        orderProcessor.addRecommendations(newOrder.getId(), recommendations);

        // send updated order and recommendation
        JSONObject completeResponse= new JSONObject();

        // ---- add updated order
        String updateOrder = mapper.writeValueAsString(newOrder);
        // String to JSON
        JSONObject updateOrderJson=new JSONObject();
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
        types.add("o"+String.valueOf(orderId));
        types.add("s_"+standName+"_"+brandName);
        Event e = new Event(orderJson, types, "Order");

        // Publish event to standmanager
        String jsonString = objectMapper.writeValueAsString(e);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String uri = OrderManager.ECURL+"/publishEvent";

        entity = new HttpEntity<>(jsonString, headers);
        String response = restTemplate.postForObject(uri, entity, String.class);

        System.out.println(response);
    }


    /**
     * Add stand to database
     * returns "saved" if correctly added
     */
    @PostMapping(path = "/addstand") // Map ONLY POST Requests
    @ResponseBody
    public String addStand(@RequestBody CommonStand stand) throws JsonProcessingException, ParseException {
        return menuHandler.addStand(stand);
    }


    /**
     * @return global menu in JSON format
     * sent request to localhost:8080/menu
     * This iterates all menus of the schedulers, checks for unique items,
     * and puts these in a JSON file with their price.
     * In the JSON file the keys are the menu item names and the values are the prices
     */
    @RequestMapping(value="/menu", method = RequestMethod.GET)
    @ResponseBody
    public List<Food> requestTotalMenu() { //start with id=1 (temporary)
        System.out.println("request total menu");
        return menuHandler.getGlobalMenu();
    }

    /**
     * @return specific stand menu in JSON format
     * sent POST request to localhost:8080/standmenu
     * @RequestParam() String standname: post the name of a stand
     * (In Postman: Select POST, go to params, enter "standname" as KEY and enter the name of a stand as value)
     * (ex:localhost:8080/standmenu?standname=food1)
     * (in current test above: "food1" and "food2" are names of stands)
     * This iterates menu of the named stand,
     * and puts the menu items in a JSON file with their price.
     * In the JSON file the keys are the menu item names and the values are the prices
     */
    @RequestMapping(value = "/standmenu", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Food>> requestStandMenu(@RequestParam String standName, @RequestParam String brandName) {
        return ResponseEntity.ok(new ArrayList<>(menuHandler.getStandMenu(standName, brandName)));
    }

    @RequestMapping(value = "/deleteStand", method = RequestMethod.GET)
    @ResponseBody
    public String deleteStand(@RequestParam() String standName, @RequestParam String brandName) {
        System.out.println("delete stand: " + standName);
        boolean b = menuHandler.deleteStand(standName, brandName);
        if (b) return "Stand " + standName + " deleted.";
        else return "No stand by that name exists";

    }

    /**
     * @return names of all stands:
     * key = number in list
     * value = standname
     */
    @RequestMapping(value="/stands", method = RequestMethod.GET)
    public ResponseEntity<Map<String, String>> requestStandnames() {
        return ResponseEntity.ok(menuHandler.getStands()
                .stream().collect(Collectors.toMap(Stand::getName, stand -> stand.getBrand().getName())));
    }
}


