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
     * API endpoint
     * Test if the server is still alive.
     *
     * @return "StandManager is alive!"
     */
    @GetMapping("/pingSM")
    public ResponseEntity<BetterResponseModel<String>> ping() {
        return ResponseEntity.ok(BetterResponseModel.ok("StandManager is alive", null));
    }

    /**
     * API endpoint
     * Load stands from database, and start corresponding schedulers for these stands.
     * When a stand is added, a scheduler for the stand is created and started automatically, but this function
     * can be used when we want to start schedulers automatically based on stands already present in database.
     * This can be used for quick startup, as well as a reboot (based on db date) when the standmanager would fail
     *
     * @param stands Stands to put in schedulers
     * @return ResponseModel.error if failed
     * ResponseModel.ok if succeeded
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

    /**
     * API endpoint
     * Deletes a scheduler
     *
     * @param standName the name of the stand of corresponding scheduler
     * @param brandName the name of the brand of corresponding scheduler
     * @return ResponseModel.error if failed
     * ResponseModel.ok if succeeded
     */
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
     * API endpoint
     * Create and start a scheduler when a stand is created in the order manager
     *
     * @param stand the stand for which a scheduler needs to be created and started
     * @return ResponseModel.error if failed
     * ResponseModel.ok if succeeded, in which the payload consists of a JSON object which states that the stand was added
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
     * API endpoint
     * Confirm an order and place it in the queue of the chosen scheduler
     *
     * @param order order to be added to queue
     * @return ResponseModel.error if failed
     * ResponseModel.ok if succeeded
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
     * API endpoint
     * Will split a superorder and give a recommendation for all the orders
     *
     * @param superOrder List with orderitems and corresponding brand
     * @return ResponseModel.error if failed
     * ResponseModel.ok if succeeded, in which the payload consists the lists of recommendations
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
                ObjectMapper mapper = new ObjectMapper();

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
     * API endpoint
     * Get recommendations for an order
     *
     * @param order order object for which the Order Manager wants a recommendation
     * @return ResponseModel.error if failed
     * ResponseModel.ok if succeeded, in which the payload consists the list of recommendations
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
