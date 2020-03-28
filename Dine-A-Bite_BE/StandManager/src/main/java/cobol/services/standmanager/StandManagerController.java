package cobol.services.standmanager;

import cobol.commons.order.CommonOrder;
import cobol.commons.order.CommonOrderItem;
import cobol.commons.order.Recommendation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.*;


import cobol.commons.ResponseModel;
import org.springframework.http.ResponseEntity;
import org.json.simple.JSONObject;

import java.util.*;

import static cobol.commons.ResponseModel.status.OK;

@RestController
public class StandManagerController {

    /**
     * API endpoint to test if the server is still alive.
     *
     * @return "StandManager is alive!"
     */
    @GetMapping("/pingSM")
    public ResponseEntity ping() {
        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(OK.toString())
                        .details("StandManager is alive!")
                        .build().generateResponse()
        );
    }

    /**
     * The controller has a list of all schedulers.
     * More information on schedulers in class Scheduler
     */
    private List<Scheduler> schedulers = new ArrayList<Scheduler>();


    /**
     * just a function for testing and starting some schedulers for practice
     */
    @RequestMapping("/start")
    public void start(){
        // Initialize stand menus and schedulers
        System.out.println("TESTTEST");
        Map<String, int[]> menu = new HashMap<>();
        int[] prijsenpreptime = {2,3};
        menu.put("burger", prijsenpreptime);
        Scheduler a = new Scheduler(menu, "food1", 1, "mcdo");
        Scheduler b = new Scheduler(menu, "food2",2, "burgerking");

        schedulers.add(a);
        schedulers.add(b);
        // start running schedulers
        for (int i=0;i<schedulers.size();i++){
            schedulers.get(i).start();
        }

    }

    /**
     *
     * @param info class object StandInfo which is used to start a scheduler for stand added in order manager
     * available at localhost:8081/newStand
     * @return true (if no errors)
     */
    @RequestMapping(value = "/newStand", consumes = "application/json")
    public JSONObject addNewStand(@RequestBody() StandInfo info){
        Scheduler s = new Scheduler(info.getMenu(), info.getName(), info.getId(), info.getBrand());
        s.setLat(info.getLat());
        s.setLon(info.getLon());
        schedulers.add(s);
        s.start();
        System.out.println("lol");
        JSONObject obj = new JSONObject();
        obj.put("added",true);
        return obj;
    }


    /**
     *
     * @param order order which wants to be placed
     * TODO: really implement this
     */
    @RequestMapping(value = "/placeOrder", consumes = "application/json")
    public void placeOrder(@RequestBody() CommonOrder order){
        //add order to right scheduler
    }



    /**
     *
     * @param order order object for which the Order Manager wants a recommendation
     * @return recommendation in JSON format
     */
    @RequestMapping(value = "/getRecommendation", consumes = "application/json")
    @ResponseBody
    public JSONObject postCommonOrder(@RequestBody() CommonOrder order) throws JsonProcessingException {
        System.out.println("User requested recommended stand for " + order.getId());
        return recommend(order);
    }


    /**
     *
     * @param order the order for which you want to find corresponding stands
     * @return list of schedulers (so the stands) which offer the correct food to complete the order
     */
    public ArrayList<Scheduler> findCorrespondStands(CommonOrder order){
        // first get the Array with all the food of the order
        ArrayList<CommonOrderItem> orderItems = new ArrayList<>(order.getOrderItems());


        // group all stands (schedulers) with the correct type of food available
        ArrayList<Scheduler> goodSchedulers = new ArrayList<>();

        for (int i = 0; i < schedulers.size(); i++) {
            boolean validStand = true;

            Scheduler currentScheduler = schedulers.get(i);

            for (CommonOrderItem orderItem : orderItems) {
                String food= orderItem.getFoodname();
                if (currentScheduler.checkType(food)) {
                    validStand = true;
                }
                else{
                    validStand = false;
                    break;
                }
            }

            if (validStand){
                goodSchedulers.add(currentScheduler);
            }
        }
        return goodSchedulers;
    }


    /**
     *
     * @param order is the order for which the recommended stands are required
     * @return JSON with a certain amount of recommended stands (currently based on lowest queue time only)
     */
    public JSONObject recommend(CommonOrder order) throws JsonProcessingException {
        /* choose how many recommends you want */
        int amountOfRecommends = 3;

        /* find stands (schedulers) which offer correct food for the order */
        ArrayList<Scheduler> goodSchedulers = findCorrespondStands(order);

        /* sort the stands (schedulers) based on remaining time */
        //Collections.sort(goodSchedulers, new SchedulerComparatorTime(order.getFull_order()));

        /* sort the stands (schedulers) based on distance */
        Collections.sort(goodSchedulers, new SchedulerComparatorDistance(order.getLatitude(),order.getLongitude()));

        /* TODO: this is how you sort based on combination, weight is how much time you add for each unit of distance */
        /* sort the stands (schedulers) based on combination of time and distance */
        //double weight = 5;
        //Collections.sort(goodSchedulers, new SchedulerComparator(order.getLat(), order.getLon(), weight);

        /* check if you have enough stands (for amount of recommendations you want) */
        if (goodSchedulers.size() < amountOfRecommends){
            amountOfRecommends = goodSchedulers.size();
        }

        /* put everything into a JSON file to give as return value */
        List<Recommendation> recommendations=new ArrayList<>();

        for (int i = 0 ; i < amountOfRecommends ; i++){
            Scheduler curScheduler = goodSchedulers.get(i);
            System.out.println(curScheduler.getStandName());
            SchedulerComparatorDistance sc = new SchedulerComparatorDistance(curScheduler.getLat(),curScheduler.getLon());
            SchedulerComparatorTime st = new SchedulerComparatorTime(new ArrayList<>(order.getOrderItems()));

            recommendations.add(new Recommendation(curScheduler.getStandId(), curScheduler.getStandName(), sc.getDistance(order.getLatitude(), order.getLongitude()), st.getTimesum(curScheduler)));
            System.out.println(st.getTimesum(curScheduler));
        }

        // Arraylist recommendations to jsonobject
        ObjectMapper mapper=new ObjectMapper();
        String jsonString=mapper.writeValueAsString(recommendations);
        JSONObject obj= new JSONObject();
        obj.put("recommendations", jsonString);

        return obj;
    }

}
