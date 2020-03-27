package cobol.services.standmanager;

import cobol.commons.Order;
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
        /**
         * Initialize stand menus and schedulers
         */
        System.out.println("TESTTEST");
        Map<String, int[]> menu = new HashMap<>();
        int[] prijsenpreptime = {2,3};
        menu.put("burger", prijsenpreptime);
        Scheduler a = new Scheduler(menu, "food1", 1, "mcdo");
        Scheduler b = new Scheduler(menu, "food2",2, "burgerking");

        schedulers.add(a);
        schedulers.add(b);
        /**
         * start running schedulers
         */
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
    public void placeOrder(@RequestBody() Order order){
        //add order to right scheduler
    }



    /**
     *
     * @param order order object for which the Order Manager wants a recommendation
     * @return recommendation in JSON format
     */
    @RequestMapping(value = "/getRecommendation", consumes = "application/json")
    @ResponseBody
    public JSONObject postOrder(@RequestBody() Order order) {
        System.out.println("User requested recommended stand for " + order.getId());
        return recommend(order);
    }


    /**
     *
     * @param order the order for which you want to find corresponding stands
     * @return list of schedulers (so the stands) which offer the correct food to complete the order
     */
    public ArrayList<Scheduler> findCorrespondStands(Order order){
        /* first get the Map with all the food of the order */
        Map<String, Integer> foodMap = order.getFull_order();

        /* group all stands (schedulers) with the correct type of food available */
        ArrayList<Scheduler> goodSchedulers = new ArrayList<>();
        for (int i = 0; i < schedulers.size(); i++) {
            Boolean validStand = true;
            Scheduler currentScheduler = schedulers.get(i);
            for (String food : foodMap.keySet()) {
                if (currentScheduler.checkType(food)) {
                    validStand = true;
                }
                else{
                    validStand = false;
                    break;
                }
            }
            if (validStand == true){
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
    public JSONObject recommend(Order order) {
        /* choose how many recommends you want */
        int amountOfRecommends = 3;

        /* find stands (schedulers) which offer correct food for the order */
        ArrayList<Scheduler> goodSchedulers = findCorrespondStands(order);

        /* sort the stands (schedulers) based on remaining time */
        //Collections.sort(goodSchedulers, new SchedulerComparatorTime(order.getFull_order()));

        /* sort the stands (schedulers) based on distance */
        Collections.sort(goodSchedulers, new SchedulerComparatorDistance(order.getLat(),order.getLon()));

        /* TODO: this is how you sort based on combination, weight is how much time you add for each unit of distance */
        /* sort the stands (schedulers) based on combination of time and distance */
        //double weight = 5;
        //Collections.sort(goodSchedulers, new SchedulerComparator(order.getLat(), order.getLon(), weight);

        /* check if you have enough stands (for amount of recommendations you want) */
        if (goodSchedulers.size() < amountOfRecommends){
            amountOfRecommends = goodSchedulers.size();
        }

        /* put everything into a JSON file to give as return value */
        JSONObject obj = new JSONObject();
        for (int i = 0 ; i < amountOfRecommends ; i++){
            JSONObject add = new JSONObject();
            Scheduler curScheduler = goodSchedulers.get(i);
            System.out.println(curScheduler.getStandName());
            SchedulerComparatorDistance sc = new SchedulerComparatorDistance(curScheduler.getLat(),curScheduler.getLon());
            SchedulerComparatorTime st = new SchedulerComparatorTime(order.getFull_order());
            add.put("stand_id", curScheduler.getStandId());
            add.put("distance", sc.getDistance(order.getLat(),order.getLon()));
            add.put("time_estimate", st.getTimesum(curScheduler));
            System.out.println(st.getTimesum(curScheduler));
            obj.put(curScheduler.getStandName(), add);
        }
        return obj;
    }

}
