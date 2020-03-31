package cobol.services.standmanager;

import cobol.commons.MenuItem;
import cobol.commons.ResponseModel;
import cobol.commons.StandInfo;
import cobol.commons.order.CommonOrder;
import cobol.commons.order.CommonOrderItem;
import cobol.commons.order.Recommendation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cobol.commons.ResponseModel.status.OK;

@RestController
public class StandManagerController {
    @Autowired
    private SchedulerHandler schedulerHandler;

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
     *
     * @param standinfos
     * @throws JsonProcessingException when wrong input param
     */
    @PostMapping("/update")
    public void update(@RequestBody String[] standinfos) throws JsonProcessingException {
        schedulerHandler.clearSchedulers();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        for (String standinfo : standinfos) {
            StandInfo info = objectMapper.readValue(standinfo, StandInfo.class);
            Scheduler s = new Scheduler(info.getMenu(), info.getName(), info.getId(), info.getBrand());
            s.setLat(info.getLat());
            s.setLon(info.getLon());
            schedulerHandler.addScheduler(s);
            s.start();
        }
    }

    @PostMapping("/delete")
    public JSONObject deleteSchedulers() {
        schedulerHandler.clearSchedulers();
        JSONObject obj = new JSONObject();
        obj.put("del", true);
        return obj;
    }

    /**
     * @param info class object StandInfo which is used to start a scheduler for stand added in order manager
     *             available at localhost:8082/newStand
     * @return true (if no errors)
     */
    @PostMapping(value = "/newStand", consumes = "application/json")
    public JSONObject addNewStand(@RequestBody() StandInfo info) {
        boolean newScheduler = true;
        JSONObject obj = new JSONObject();
        for (Scheduler s : schedulerHandler.getSchedulers()) {
            if (s.getStandId() == info.getId()) {
                //remove scheduler
                if (info.getName() == null || info.getName().equals("")) {
                    schedulerHandler.removeScheduler(s);
                }

                //edit scheduler
                else {
                    ArrayList<String> l = new ArrayList<>();
                    for (MenuItem mi : info.getMenu()) {
                        l.add(mi.getFoodName());
                        boolean olditem=false;
                        for (MenuItem mi2 : s.getMenu()) {

                            olditem = Scheduler.updateItem(mi, mi2);

                        }
                        if (!olditem){
                            s.getMenu().add(mi);
                        }
                    }
                    for (MenuItem mi2 : s.getMenu()) {
                        if (!l.contains(mi2.getFoodName()))s.removeItem(mi2);
                    }
                }
                newScheduler = false;
                obj.put("added", true);
                break;
            }
        }
        //create scheduler
        if (newScheduler) {
            Scheduler s = new Scheduler(info.getMenu(), info.getName(), info.getId(), info.getBrand());
            s.setLat(info.getLat());
            s.setLon(info.getLon());
            schedulerHandler.addScheduler(s);
            s.start();
            obj.put("added", true);
        }

        return obj;
    }


    /**
     * @param order order which wants to be placed
     *              TODO: really implement this
     */
    @RequestMapping(value = "/placeOrder", consumes = "application/json")
    public void placeOrder(@RequestBody() CommonOrder order) {
        //add order to right scheduler
    }


    /**
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
     * @param order the order for which you want to find corresponding stands
     * @return list of schedulers (so the stands) which offer the correct food to complete the order
     */
    public ArrayList<Scheduler> findCorrespondStands(CommonOrder order) {
        // first get the Array with all the food of the order
        ArrayList<CommonOrderItem> orderItems = new ArrayList<>(order.getOrderItems());


        // group all stands (schedulers) with the correct type of food available
        ArrayList<Scheduler> goodSchedulers = new ArrayList<>();

        for (int i = 0; i < schedulerHandler.getSchedulers().size(); i++) {
            boolean validStand = true;

            Scheduler currentScheduler = schedulerHandler.getSchedulers().get(i);

            for (CommonOrderItem orderItem : orderItems) {
                String food = orderItem.getFoodname();
                if (currentScheduler.checkType(food)) {
                    validStand = true;
                } else {
                    validStand = false;
                    break;
                }
            }

            if (validStand) {
                goodSchedulers.add(currentScheduler);
            }
        }
        return goodSchedulers;
    }


    /**
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
        Collections.sort(goodSchedulers, new SchedulerComparatorDistance(order.getLatitude(), order.getLongitude()));

        /* TODO: this is how you sort based on combination, weight is how much time you add for each unit of distance */
        /* sort the stands (schedulers) based on combination of time and distance */
        //double weight = 5;
        //Collections.sort(goodSchedulers, new SchedulerComparator(order.getLat(), order.getLon(), weight);

        /* check if you have enough stands (for amount of recommendations you want) */
        if (goodSchedulers.size() < amountOfRecommends) {
            amountOfRecommends = goodSchedulers.size();
        }
        /* put everything into a JSON file to give as return value */
        List<Recommendation> recommendations = new ArrayList<>();

        for (int i = 0; i < amountOfRecommends; i++) {
            Scheduler curScheduler = goodSchedulers.get(i);
            System.out.println(curScheduler.getStandName());
            SchedulerComparatorDistance sc = new SchedulerComparatorDistance(curScheduler.getLat(), curScheduler.getLon());
            SchedulerComparatorTime st = new SchedulerComparatorTime(new ArrayList<>(order.getOrderItems()));
            recommendations.add(new Recommendation(curScheduler.getStandId(), curScheduler.getStandName(), sc.getDistance(order.getLatitude(), order.getLongitude()), st.getTimesum(curScheduler)));
            System.out.println(st.getTimesum(curScheduler));
        }

        // Arraylist recommendations to jsonobject
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(recommendations);
        JSONObject obj = new JSONObject();
        obj.put("recommendations", jsonString);

        return obj;
    }

}
