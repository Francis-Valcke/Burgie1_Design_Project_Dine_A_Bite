package cobol.services.standmanager;

import cobol.commons.domain.CommonStand;
import cobol.commons.communication.response.ResponseModel;
import cobol.commons.exception.CommunicationException;
import cobol.commons.exception.OrderException;
import cobol.commons.domain.CommonOrder;
import cobol.commons.domain.CommonOrderItem;
import cobol.commons.domain.Recommendation;
import cobol.commons.domain.SuperOrder;
import cobol.commons.stub.IStandManager;
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

import static cobol.commons.communication.response.ResponseModel.status.OK;
import static cobol.commons.stub.StandManagerStub.*;

@RestController
public class StandManagerController implements IStandManager {

    @Autowired
    private SchedulerHandler schedulerHandler;

    @Override
    @GetMapping(StandManagerStub.GET_PING)
    public ResponseEntity ping() {
        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(OK.toString())
                        .details("StandManager is alive!")
                        .build().generateResponse()
        );
    }

    @Override
    @PostMapping(POST_UPDATE)
    public void update(@RequestBody List<CommonStand> stands) throws CommunicationException {

        for (CommonStand stand : stands) {
            schedulerHandler.updateSchedulers(stand);
        }


    }

    @Override
    @PostMapping(POST_DELETE_SCHEDULER)
    public void deleteScheduler(@RequestParam String standName, @RequestParam String brandName) {


        Optional<Scheduler> schedulerOptional = schedulerHandler.getSchedulers().stream()
                .filter(s -> s.getStandName().equals(standName) &&
                        s.getBrand().equals(brandName)).findAny();
        schedulerOptional.ifPresent(scheduler -> schedulerHandler.removeScheduler(scheduler));
    }


    @Override
    @PostMapping(value = POST_NEW_STAND, consumes = "application/json")
    public JSONObject addNewStand(@RequestBody() CommonStand stand) throws CommunicationException {
        return schedulerHandler.updateSchedulers(stand);
    }


    @Override
    @PostMapping(value = POST_PLACE_ORDER, consumes = "application/json")
    public void placeOrder(@RequestBody() CommonOrder order) {
        schedulerHandler.addOrderToScheduler(order);
    }



    @Override
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


    @Override
    @PostMapping(value = POST_GET_RECOMMENDATION, consumes = "application/json")
    @ResponseBody
    public List<Recommendation> postCommonOrder(@RequestBody() CommonOrder order) throws JsonProcessingException {
        System.out.println("User requested recommended stand for " + order.getId());
        return schedulerHandler.recommend(order);
    }


}
