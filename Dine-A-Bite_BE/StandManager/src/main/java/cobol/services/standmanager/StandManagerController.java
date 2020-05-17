package cobol.services.standmanager;

import cobol.commons.BetterResponseModel;
import cobol.commons.CommonStand;
import cobol.commons.exception.DoesNotExistException;
import cobol.commons.order.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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
    public ResponseEntity<BetterResponseModel<String>> ping() {
        return ResponseEntity.ok(BetterResponseModel.ok("StandManager is alive", null));
    }

    /**
     * adds schedulers to SM
     *
     * @param stands Stands to put in schedulers
     */
    @PostMapping("/update")
    public ResponseEntity<BetterResponseModel<String>> update(@RequestBody List<CommonStand> stands) {

        try {
            for (CommonStand stand : stands) {
                schedulerHandler.updateSchedulers(stand);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error("Error while updating schedulers", e));
        }

        return ResponseEntity.ok(BetterResponseModel.ok("Successfully updated schedulers", "success"));

    }
    @PostMapping("updateScheduler")
    public ResponseEntity<BetterResponseModel<String>> updateScheduler(@RequestParam(name="standName") String stand, @RequestParam(name="brandName") String brand,@RequestParam(name="orderId") int orderId) {
        try {
            for (Scheduler s : schedulerHandler.getSchedulers()){
                if (s.getStandName().equals(stand)&&s.getBrand().equals(brand)){

                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error("Error while updating schedulers", e));
        }

        return ResponseEntity.ok(BetterResponseModel.ok("Successfully updated schedulers", "success"));

    }


    @PostMapping("/deleteScheduler")
    public ResponseEntity<BetterResponseModel<String>> deleteScheduler(@RequestParam String standName, @RequestParam String brandName) {

        Optional<Scheduler> schedulerOptional = schedulerHandler.getSchedulers().stream()
                .filter(s -> s.getStandName().equals(standName) &&
                        s.getBrand().equals(brandName)).findAny();
        if (schedulerOptional.isPresent()) {
            schedulerOptional.ifPresent(scheduler -> schedulerHandler.removeScheduler(scheduler));
            return ResponseEntity.ok(BetterResponseModel.ok("Successfully deleted scheduler", null));
        } else {
            System.out.println("ERROR: stand does not exist exception while deleting");
            return ResponseEntity.ok(BetterResponseModel.error("Error while deleting scheduler", new DoesNotExistException("Stand does not exist")));
        }
    }


    /**
     * @param stand class object StandInfo which is used to start a scheduler for stand added in order manager
     *              available at localhost:8081/newStand
     * @return true (if no errors)
     */
    @RequestMapping(value = "/newStand", consumes = "application/json")
    public ResponseEntity<BetterResponseModel<String>> addNewStand(@RequestBody() CommonStand stand) {
        JSONObject object = null;
        try {
            object = schedulerHandler.updateSchedulers(stand);
            return ResponseEntity.ok(BetterResponseModel.ok("Successfully added stand", object.toJSONString()));
        } catch (Throwable e) {
            e.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error("Error while updating stand", e));
        }
    }


    /**
     * @param order order which wants to be placed
     * TODO: returns a String for now because sendRestTemplate in OM is also String, but when that is fixed, this should become just a regular JSON object as return
     */
    @RequestMapping(value = "/placeOrder", consumes = "application/json")
    public ResponseEntity<BetterResponseModel<String>> placeOrder(@RequestBody() CommonOrder order) {
        String waitingTime;
        try {
            waitingTime = schedulerHandler.addOrderToScheduler(order).get("waitingTime").toString();
        } catch (Throwable e) {
            e.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error("Error while placing order in standmanager", e));
        }

        return ResponseEntity.ok(BetterResponseModel.ok("Succesfully placed order in standmanager", waitingTime));
    }


    /**
     * This method will split a superorder and give a recommendation for all the orders
     *
     * @param superOrder List with orderitems and corresponding brand
     * @return JSONArray each element containing a field "recommendations" and a field "order" similar to return of placeOrder
     * recommendation field will be a JSONArray of Recommendation object
     */
    @PostMapping(value = "/getSuperRecommendation", consumes = "application/json", produces = "application/json")
    public ResponseEntity<BetterResponseModel<List<SuperOrderRec>>> getSuperRecommendation(@RequestBody SuperOrder superOrder) {

        // initialize response
        List<SuperOrderRec> completeResponse = new ArrayList<>();
        try {
            /* -- Split superorder in smaller orders -- */
            List<HashSet<CommonOrderItem>> itemSplit = schedulerHandler.splitSuperOrder(superOrder);


            /* -- Get recommendations for seperate orders -- */
            for (HashSet<CommonOrderItem> commonOrderItems : itemSplit) {
                ObjectMapper mapper= new ObjectMapper();

                // -- Construct a virtual order -- //
                CommonOrder order = new CommonOrder();
                // add order items for this order
                order.setOrderItems(new ArrayList<>(commonOrderItems));
                // set brandName
                order.setBrandName(superOrder.getBrandName());
                // set coordinates
                order.setLatitude(superOrder.getLatitude());
                order.setLongitude(superOrder.getLongitude());
                order.setRecType(superOrder.getRecType());


                // -- Ask recommendation for newly created order -- //
                List<Recommendation> recommendations = schedulerHandler.recommend(order);

                // -- Add to response of this super order recommendation -- //
                completeResponse.add(new SuperOrderRec(order, recommendations));

            }
        } catch (Throwable e) {
            e.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error("Error while retrieving superorder recommendations", e));
        }

        // return list of commonorderitems with corresponding recommendation
        return ResponseEntity.ok(BetterResponseModel.ok("Successfully retrieved recommendations for superorder", completeResponse));
    }


    /**
     * @param order order object for which the Order Manager wants a recommendation
     * @return recommendation in JSON format
     */
    @RequestMapping(value = "/getRecommendation", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<BetterResponseModel<String>> postCommonOrder(@RequestBody() CommonOrder order) {
        System.out.println("User requested recommended stand for " + order.getId());
        List<Recommendation> recommendations = schedulerHandler.recommend(order);
        ObjectMapper mapper = new ObjectMapper();
        String response = "";
        try {
            response = mapper.writeValueAsString(recommendations);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error("Error parsing recommendations on server", e));
        }

        if (recommendations.isEmpty()) {
            System.out.println("Error: No recommendations available on server");
            return ResponseEntity.ok(BetterResponseModel.error("Error fetching recommendations", new DoesNotExistException("No recommendations available on server")));
        } else {
            return ResponseEntity.ok(BetterResponseModel.ok("Successfully retrieved recommendations", response));
        }

    }


}
