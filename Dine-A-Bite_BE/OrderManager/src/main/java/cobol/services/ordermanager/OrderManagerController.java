package cobol.services.ordermanager;

import cobol.commons.Event;
import cobol.commons.ResponseModel;
import cobol.commons.order.Recommendation;
import cobol.commons.security.CommonUser;
import cobol.services.ordermanager.dbmenu.Order;
import cobol.services.ordermanager.exception.AlreadySetException;
import cobol.services.ordermanager.exception.MissingEntityException;
import cobol.services.ordermanager.exception.MissingRunException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.LoggerFactory;
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
import org.slf4j.Logger;

import static cobol.commons.ResponseModel.status.OK;

/**
 * This class handles communication from standapplication: incoming changes to stand menus are registered in the menuhandler
 * This class also handles menurequests from the attendee applications, fetching the menus from the menuhandler
 */

@RestController
public class OrderManagerController {

    private RestTemplate restTemplate;
    private HttpHeaders headers;
    private HttpEntity<String> entity;
    private Logger logger = LoggerFactory.getLogger(OrderManagerController.class);

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    OrderProcessor orderProcessor = null;
    @Autowired
    private MenuHandler mh;


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
     * this method will change wether stands are added as schedulers in Stand Manager
     * @param sm: if true, then stands will be added as schedulers
     */
    @GetMapping("/SMswitch")
    public void sMswitch(@RequestParam(name = "on") boolean sm) {
        this.mh.smSwitch(sm);
    }


    /**
     * will clear database and OM
     *
     * @return confirmation of deletion
     */
    @GetMapping("/delete")
    public String delete() {
        mh.deleteAll();
        return "deleting stands from Order Manager";
    }

    /**
     * Will check if there are already stands in database that are not in OM and add them to OM
     *
     * @return tells u if there are already stands in DB
     */
    @GetMapping("/updateOM")
    public String update() throws JsonProcessingException {
        List<String> stands = mh.update();
        mh.updateSM();
        if (stands.size()==0) {
            return "No stands in database";
        }
        else{
            StringBuilder response = new StringBuilder();
            response.append("Stands already in database: \n");

            for (String s : stands) {
                response.append(s).append("\n");
            }
            return response.toString();
        }

    }

    @GetMapping("/getOrderInfo")
    public JSONObject getOrderInfo(@RequestParam(name="orderId") int orderId) throws JsonProcessingException, MissingEntityException {

        // retrieve order
        Order order= orderProcessor.getOrder(orderId);

        // write order to json
        String jsonString = objectMapper.writeValueAsString(order);
        JSONParser parser = new JSONParser();
        JSONObject orderResponse = new JSONObject();
        try {
            orderResponse = (JSONObject) parser.parse(jsonString);
        } catch (ParseException e) {
            logger.error("Exception occurred!", e);
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
    public JSONObject placeOrder(@AuthenticationPrincipal CommonUser userDetails, @RequestBody JSONObject orderObject) throws JsonProcessingException {

        // Add order to the processor
        Order newOrder= new Order(orderObject);
        newOrder=orderProcessor.addNewOrder(newOrder);


        // Put order in json to send to standmanager (as commonOrder object)
        String jsonString = objectMapper.writeValueAsString(newOrder);


        // Ask standmanager for recommendation
        headers.setContentType(MediaType.APPLICATION_JSON);
        String uri = OrderManager.SMURL+"/getRecommendation";
        entity = new HttpEntity<>(jsonString, headers);
        JSONObject response = restTemplate.postForObject(uri, entity, JSONObject.class);

        List<Recommendation> recommendations= objectMapper.readValue(response.get("recommendations").toString(), new TypeReference<List<Recommendation>>() {});
        orderProcessor.addRecommendations(newOrder.getId(), recommendations);

        // send updated order and recommendation
        JSONObject completeResponse= new JSONObject();

        // ---- add updated order
        String updateOrder = objectMapper.writeValueAsString(newOrder);
        // String to JSON
        JSONParser parser= new JSONParser();
        JSONObject updateOrderJson=new JSONObject();
        try {
            updateOrderJson = (JSONObject) parser.parse(updateOrder);
        } catch (ParseException e) {
            logger.error("Exception occurred!", e);
        }


        // ---- add recommendations
        String recommendationsResponse=objectMapper.writeValueAsString(recommendations);
        JSONArray recomResponseJson= new JSONArray();
        try {
            recomResponseJson=(JSONArray) parser.parse(recommendationsResponse);
        } catch (ParseException e) {
            logger.error("Exception occurred!", e);
        }

        // Construct response
        completeResponse.put("order", updateOrderJson);
        completeResponse.put("recommendations", recomResponseJson);
        return completeResponse;
    }


    /**
     * Sets the order id parameter of order. Adds the order to the stand channel.
     *
     * @param orderId the id of the order for which the stand is to be confirmed
     * @param standId id of the chosen stand
     */
    @GetMapping("/confirmStand")
    @ResponseBody
    public void confirmStand(@RequestParam(name = "order_id") int orderId, @RequestParam(name = "stand_id") int standId) throws JsonProcessingException, MissingEntityException, AlreadySetException, MissingRunException {
        // Update order, confirm stand
        Order updatedOrder = orderProcessor.confirmStand(orderId, standId);

        // Make event for eventchannel (orderId, standId)
        String orderString = objectMapper.writeValueAsString(updatedOrder);
        JSONObject orderJson = new JSONObject();
        orderJson.put("order", orderString);
        List<String> types = new ArrayList<>();
        types.add("o"+orderId);
        types.add("s"+standId);
        Event e = new Event(orderJson, types, "Order");

        // Publish event to standmanager
        String jsonString = objectMapper.writeValueAsString(e);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String uri = OrderManager.ECURL+"/publishEvent";

        entity = new HttpEntity<>(jsonString, headers);
        String response = restTemplate.postForObject(uri, entity, String.class);

    }


    /**
     * Add stand to database
     * returns "saved" if correctly added
     */
    @PostMapping(path = "/addstand") // Map ONLY POST Requests
    public @ResponseBody
    String addStand(@RequestBody JSONObject menu) throws JsonProcessingException {
        return mh.addStand(menu);
    }


    /**
     * @return global menu in JSON format
     * sent request to localhost:8080/menu
     * This iterates all menus of the schedulers, checks for unique items,
     * and puts these in a JSON file with their price.
     * In the JSON file the keys are the menu item names and the values are the prices
     */
    @GetMapping("/menu")
    @ResponseBody
    public JSONArray requestTotalMenu() { //start with id=1 (temporary)
        return mh.getTotalmenu();
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
    @GetMapping("/standmenu")
    @ResponseBody
    public JSONArray requestStandMenu(@RequestParam(name = "standname") String standname) {
        return mh.getStandMenu(standname);
    }

    /**
     * delete a stand from server (OM, SM and DB)
     *
     * @param standname name of stand
     * @return message
     * @throws JsonProcessingException when mapper fails
     */
    @GetMapping("/deleteStand")
    @ResponseBody
    public String deleteStand(@RequestParam(name = "standname") String standname) throws JsonProcessingException {
        boolean b = mh.deleteStand(standname);
        if (b) return "Stand " + standname + " deleted.";
        else return "No stand by that name exists";

    }

    /**
     * @return names of all stands:
     * key = number in list
     * value = standname
     */
    @GetMapping("/stands")
    public JSONObject requestStandnames() { //start with id=1 (temporary)
        return mh.getStandnames();
    }
}


