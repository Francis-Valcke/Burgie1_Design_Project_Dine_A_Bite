package cobol.services.standmanager;

import cobol.commons.CommonStand;
import cobol.commons.ResponseModel;
import cobol.commons.exception.CommunicationException;
import cobol.commons.exception.OrderException;
import cobol.commons.order.CommonOrder;
import cobol.commons.order.CommonOrderItem;
import cobol.commons.order.Recommendation;
import cobol.commons.order.SuperOrder;
import cobol.commons.stub.StandManagerStub;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static cobol.commons.ResponseModel.status.OK;
import static cobol.commons.stub.StandManagerStub.*;

@RestController
public class StandManagerController {
    @Autowired
    private SchedulerHandler schedulerHandler;

    /**
     * API endpoint to test if the server is still alive.
     *
     * @return "StandManager is alive!"
     */
    @GetMapping(StandManagerStub.GET_PING)
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
     * @param stands
     * @throws JsonProcessingException when wrong input param
     */
    @PostMapping(POST_UPDATE)
    public void update(@RequestBody List<CommonStand> stands) throws CommunicationException {

        for (CommonStand stand : stands) {
            schedulerHandler.updateSchedulers(stand);
        }


    }

    @PostMapping(POST_DELETE_SCHEDULER)
    public void deleteScheduler(@RequestParam String standName, @RequestParam String brandName) {


        Optional<Scheduler> schedulerOptional = schedulerHandler.getSchedulers().stream()
                .filter(s -> s.getStandName().equals(standName) &&
                        s.getBrand().equals(brandName)).findAny();
        schedulerOptional.ifPresent(scheduler -> schedulerHandler.removeScheduler(scheduler));
    }


    /**
     * @param stand class object StandInfo which is used to start a scheduler for stand added in order manager
     *              available at localhost:8081/newStand
     * @return true (if no errors)
     */
    @PostMapping(value = POST_NEW_STAND, consumes = "application/json")
    public JSONObject addNewStand(@RequestBody() CommonStand stand) throws CommunicationException {
        return schedulerHandler.updateSchedulers(stand);
    }


    /**
     * @param order order which wants to be placed
     *              TODO: really implement this
     */
    @PostMapping(value = POST_PLACE_ORDER, consumes = "application/json")
    public void placeOrder(@RequestBody() CommonOrder order) {
        schedulerHandler.addOrderToScheduler(order);
    }



    /**
     * This method will split a superorder and give a recommendation for all the orders
     *
     * @param superOrder List with orderitems and corresponding brand
     * @return JSONArray each element containing a field "recommendations" and a field "order" similar to return of placeOrder
     * recommendation field will be a JSONArray of Recommendation object
     */
    @PostMapping(value = POST_GET_SUPER_RECOMMENDATION, consumes = "application/json", produces = "application/json")
    public ResponseEntity<JSONArray> getSuperRecommendation(@RequestBody SuperOrder superOrder) throws JsonProcessingException, OrderException {

        // initialize response
        JSONArray completeResponse= new JSONArray();

        /* -- Split superorder in smaller orders -- */
        List<HashSet<CommonOrderItem>> itemSplit= schedulerHandler.splitSuperOrder(superOrder);


        /* -- Get recommendations for seperate orders -- */
        for (HashSet<CommonOrderItem> commonOrderItems : itemSplit) {
            JSONObject orderResponse= new JSONObject();

            // -- Construct a virtual order -- //
            CommonOrder order = new CommonOrder();
            // add order items for this order
            order.setOrderItems(new ArrayList<>(commonOrderItems));
            // set brandName
            order.setBrandName(superOrder.getBrandName());
            // set coordinates
            order.setLatitude(superOrder.getLatitude());
            order.setLongitude(superOrder.getLongitude());

            // -- Ask recommendation for newly created order -- //
            List<Recommendation> recommendations= schedulerHandler.recommend(order);

            // -- Add to response of this super order recommendation -- //
            orderResponse.put("order", order);
            orderResponse.put("recommendations", recommendations);

            completeResponse.add(orderResponse);
        }


        // return list of commonorderitems with corresponding recommendation
        return ResponseEntity.ok(completeResponse);
    }


    /**
     * @param order order object for which the Order Manager wants a recommendation
     * @return recommendation in JSON format
     */
    @PostMapping(value = POST_GET_RECOMMENDATION, consumes = "application/json")
    @ResponseBody
    public List<Recommendation> postCommonOrder(@RequestBody() CommonOrder order) throws JsonProcessingException {
        System.out.println("User requested recommended stand for " + order.getId());
        return schedulerHandler.recommend(order);
    }


}
