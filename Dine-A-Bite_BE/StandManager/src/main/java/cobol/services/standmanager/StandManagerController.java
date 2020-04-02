package cobol.services.standmanager;

import cobol.commons.ResponseModel;
import cobol.commons.CommonStand;
import cobol.commons.order.CommonOrder;
import cobol.commons.order.CommonOrderItem;
import cobol.commons.order.Recommendation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static cobol.commons.ResponseModel.status.OK;

@RestController
public class StandManagerController {

    /**
     * The controller has a list of all schedulers.
     * More information on schedulers in class Scheduler
     */
    private List<Scheduler> schedulers = new ArrayList<Scheduler>();

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
     * adds schedulers to SM
     * @param stands
     * @throws JsonProcessingException when wrong input param
     */
    @PostMapping("/update")
    public void update(@RequestBody ArrayList<CommonStand> stands) throws JsonProcessingException {
        schedulers.clear();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        for (CommonStand stand : stands) {
            Scheduler s = new Scheduler(stand.getMenu(), stand.getName(), stand.getBrandName(), stand.getLatitude(), stand.getLongitude());
            schedulers.add(s);
            s.start();
        }
    }

    @PostMapping("/deleteScheduler")
    public void deleteScheduler(@RequestParam String standName, @RequestParam String brandName){
        Optional<Scheduler> schedulerOptional= schedulers.stream()
                .filter(s -> s.getStandName().equals(standName) &&
                        s.getBrandName().equals(brandName)).findAny();
        if(schedulerOptional.isPresent()){
            Scheduler scheduler= schedulerOptional.get();
            schedulers.remove(scheduler);
        }
    }


    @PostMapping("/delete")
    public JSONObject deleteSchedulers() {
        schedulers.clear();
        JSONObject obj = new JSONObject();
        obj.put("del", true);
        return obj;
    }

    /**
     * @param stand class object StandInfo which is used to start a scheduler for stand added in order manager
     *             available at localhost:8081/newStand
     * @return true (if no errors)
     */
    @PostMapping(value = "/newStand", consumes = "application/json")
    public JSONObject addNewStand(@RequestBody() CommonStand stand) {
        Scheduler s = new Scheduler(stand.getMenu(), stand.getName(), stand.getBrandName(), stand.getLatitude(), stand.getLongitude());
        schedulers.add(s);
        s.start();
        JSONObject obj = new JSONObject();
        obj.put("added", true);
        return obj;
    }


    /**
     * @param order order which wants to be placed
     *              TODO: really implement this
     */
    @RequestMapping(value = "/placeOrder", consumes = "application/json")
    public void placeOrder(@RequestBody() CommonOrder order){
        //add order to right scheduler
    }


    /**
     * @param order order object for which the Order Manager wants a recommendation
     * @return recommendation in JSON format
     */
    @RequestMapping(value = "/getRecommendation", consumes = "application/json")
    @ResponseBody
    public List<Recommendation> postCommonOrder(@RequestBody() CommonOrder order) throws JsonProcessingException {
        System.out.println("User requested recommended stand for " + order.getId());
        return recommend(order);
    }


    /**
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
                String food= orderItem.getFoodName();
                if (currentScheduler.checkType(food)) {
                    validStand = true;
                } else {
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
     * @param order is the order for which the recommended stands are required
     * @return JSON with a certain amount of recommended stands (currently based on lowest queue time only)
     */
    public List<Recommendation> recommend(CommonOrder order) throws JsonProcessingException {
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
        if (goodSchedulers.size() < amountOfRecommends) {
            amountOfRecommends = goodSchedulers.size();
        }

        /* put everything into a JSON file to give as return value */
        List<Recommendation> recommendations=new ArrayList<>();

        for (int i = 0 ; i < amountOfRecommends ; i++){
            Scheduler curScheduler = goodSchedulers.get(i);
            System.out.println(curScheduler.getStandName());
            SchedulerComparatorDistance sc = new SchedulerComparatorDistance(curScheduler.getLat(),curScheduler.getLon());
            SchedulerComparatorTime st = new SchedulerComparatorTime(new ArrayList<>(order.getOrderItems()));

            recommendations.add(new Recommendation(curScheduler.getStandName(), curScheduler.getBrandName(), sc.getDistance(order.getLatitude(), order.getLongitude()), st.getTimesum(curScheduler)));
            System.out.println(st.getTimesum(curScheduler));
        }



        return recommendations;
    }

}
