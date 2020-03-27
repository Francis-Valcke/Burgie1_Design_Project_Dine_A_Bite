package cobol.services.ordermanager;

import cobol.commons.Event;
import cobol.commons.Order;
import cobol.commons.ResponseModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
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

    private RestTemplate restTemplate;
    private HttpHeaders headers;
    private HttpEntity<String> entity;
    @Autowired // This means to get the bean called standRepository
    private MenuHandler mh=new MenuHandler();
    @Autowired
    private ObjectMapper objectMapper;

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
        List<String> s = mh.update();
        if (s.size()==0) return "No stands in database";
        String l ="Stands already in database: \n";
        for (int i=0;i<s.size();i++)l+= s.get(i)+"\n";
        return l;
    }

    /**
     *
     * @param order_object the order recieved from the attendee app
     * @return the order id, along with the json with recommended stands
     * @throws JsonProcessingException
     *
     * Add the order to the order processor, gets a recommendation from the scheduler and forwards it to the attendee app.
     */
    @PostMapping(value = "/placeOrder", consumes = "application/json", produces = "application/json")
    public JSONObject placeOrder(@RequestBody JSONObject order_object) throws JsonProcessingException {
        Order new_order = new Order(order_object);
        OrderProcessor processor = OrderProcessor.getOrderProcessor();
        processor.addOrder(new_order);
        String jsonString = objectMapper.writeValueAsString(new_order);
        String uri = "http://cobol.idlab.ugent.be:8092/getRecommendation";
        headers.add("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJPcmRlck1hbmFnZXIiLCJyb2xlcyI6WyJST0xFX0FQUExJQ0FUSU9OIl0sImlhdCI6MTU4NDkxMTY3MSwiZXhwIjoxNzQyNTkxNjcxfQ.VmujsURhZaXRp5FQJXzmQMB-e6QSNF-OyPLeMEMOVvI");
        entity = new HttpEntity<>(jsonString, headers);
        JSONObject ret = restTemplate.postForObject(uri, entity, JSONObject.class);
        Set<String> keys = ret.keySet(); //emptys keyset
        //Look if standname in JSON file

        for (String key : keys) {
            System.out.println("order recommender for: "+(key));
        }


        ret.put("order_id", new_order.getId());

        //The following is a hardcoded recommendation
        //JSONObject ret = new JSONObject();
        /*ret.put("order_id", 1);
        JSONObject stand = new JSONObject();
        stand.put("stand_id" , 1);
        stand.put("estimated_time", 5);
        ret.put("recommendation", stand);*/
        return ret;
    }


    /**
     *
     * @param order_id
     * @param stand_id id of the chosen stand
     *
     * Sets the order id parameter of order. Adds the order to the stand channel.
     */
    @RequestMapping(value = "/confirmStand", method = RequestMethod.GET)
    @ResponseBody
    public void confirmStand(@RequestParam(name = "order_id") int order_id, @RequestParam(name = "stand_id") int stand_id) throws JsonProcessingException{
        OrderProcessor processor = OrderProcessor.getOrderProcessor();
        Order order = processor.getOrder(order_id);
        order.setStand_id(stand_id);

        JSONObject order_json = new JSONObject();
        order_json.put("order", order);
        String[] types = {String.valueOf(order_id), String.valueOf(stand_id)};
        Event e = new Event(order_json, types);

        String jsonString = objectMapper.writeValueAsString(e);
        String uri = "http://cobol.idlab.ugent.be:8093/publishEvent";
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


