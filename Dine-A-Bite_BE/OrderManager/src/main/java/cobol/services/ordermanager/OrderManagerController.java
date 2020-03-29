package cobol.services.ordermanager;

import cobol.commons.Event;
import cobol.commons.order.Recommendation;
import cobol.services.ordermanager.dbmenu.Order;
import cobol.commons.ResponseModel;
import cobol.commons.security.CommonUser;
import cobol.services.ordermanager.dbmenu.OrderItem;
import cobol.services.ordermanager.dbmenu.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

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
    private OrderProcessor orderProcessor=null;


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
    
    @Autowired // This means to get the bean called standRepository
    private MenuHandler mh=new MenuHandler();

    /**
     * will clear database and OM
     * @return confirmation of deletion
     */
    @RequestMapping("/delete")
    public String delete() {
        mh.deleteAll();
        return "deleting stands from Order Manager";
    }

    /**
     * Will check if there are already stands in database that are not in OM and add them to OM
     * @return tells u if there are already stands in DB
     */
    @PostConstruct
    @RequestMapping("/updateOM")
    public String index() {
        List<String> stands = mh.update();
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
    public JSONObject placeOrder(@AuthenticationPrincipal CommonUser userDetails, @RequestBody JSONObject orderObject) throws JsonProcessingException {

        // Add order to the processor
        Order newOrder= new Order(orderObject);
        newOrder=orderProcessor.addNewOrder(newOrder);


        // Put order in json to send to standmanager (as commonOrder object)
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(newOrder);


        // Ask standmanager for recommendation
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String uri = OrderManager.SMURL+"/getRecommendation";
        headers.add("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJPcmRlck1hbmFnZXIiLCJyb2xlcyI6WyJST0xFX0FQUExJQ0FUSU9OIl0sImlhdCI6MTU4NDkxMTY3MSwiZXhwIjoxNzQyNTkxNjcxfQ.VmujsURhZaXRp5FQJXzmQMB-e6QSNF-OyPLeMEMOVvI");
        HttpEntity<String> request = new HttpEntity<>(jsonString, headers);
        JSONObject response = template.postForObject(uri, request, JSONObject.class);

        List<Recommendation> recommendations= mapper.readValue(response.get("recommendations").toString(), new TypeReference<List<Recommendation>>() {});
        orderProcessor.addRecommendations(newOrder.getId(), recommendations);

        // send updated order and recommendation
        JSONObject completeResponse= new JSONObject();

        // ---- add updated order
        String updateOrder = mapper.writeValueAsString(newOrder);
        // String to JSON
        JSONParser parser= new JSONParser();
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
     * Sets the order id parameter of order. Adds the order to the stand channel.
     *
     * @param orderId
     * @param standId id of the chosen stand
     */
    @RequestMapping(value = "/confirmStand", method = RequestMethod.GET)
    @ResponseBody
    public void confirmStand(@RequestParam(name = "order_id") int orderId, @RequestParam(name = "stand_id") int standId) throws JsonProcessingException{
        // Update order, confirm stand
        Order updatedOrder=orderProcessor.confirmStand(orderId, standId);

        // Make event for eventchannel (orderId, standId)
        JSONObject orderJson = new JSONObject();
        orderJson.put("order",updatedOrder);
        String[] types = {String.valueOf(orderId), String.valueOf(standId)};
        Event e = new Event(orderJson, types);

        // Publish event to standmanager
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(e);
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String uri = OrderManager.ECURL+"/publishEvent";
        headers.add("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJPcmRlck1hbmFnZXIiLCJyb2xlcyI6WyJST0xFX0FQUExJQ0FUSU9OIl0sImlhdCI6MTU4NDkxMTY3MSwiZXhwIjoxNzQyNTkxNjcxfQ.VmujsURhZaXRp5FQJXzmQMB-e6QSNF-OyPLeMEMOVvI");

        HttpEntity<String> request = new HttpEntity<>(jsonString, headers);
        String response = template.postForObject(uri, request, String.class);

        System.out.println(response);


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
    @RequestMapping("/menu")
    @ResponseBody
    public JSONObject requestTotalMenu() { //start with id=1 (temporary)
        System.out.println("request total menu");
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
    @RequestMapping(value ="/standmenu", method = RequestMethod.GET)
    @ResponseBody
    public JSONObject requestStandMenu(@RequestParam() String standname) {
        System.out.println("request menu of stand " + standname);
        return mh.getStandMenu(standname);
    }

    /**
     *
     * @return names of all stands:
     * key = number in list
     * value = standname
     */
    @RequestMapping("/stands")
    public JSONObject requestStandnames() { //start with id=1 (temporary)
        System.out.println("request stand names");

        return mh.getStandnames();
    }
}


