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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
     * @param stands
     * @throws JsonProcessingException when wrong input param
     */
    @PostMapping("/update")
    public void update(@RequestBody ArrayList<CommonStand> stands) throws JsonProcessingException {
        for (CommonStand stand : stands) {
           schedulerHandler.updateSchedulers(stand);
        }

    }

    @PostMapping("/deleteScheduler")
    public void deleteScheduler(@RequestParam String standName, @RequestParam String brandName){



        Optional<Scheduler> schedulerOptional= schedulerHandler.getSchedulers().stream()
                .filter(s -> s.getStandName().equals(standName) &&
                        s.getBrand().equals(brandName)).findAny();
        schedulerOptional.ifPresent(scheduler -> schedulerHandler.removeScheduler(scheduler));
    }



   
    /**
     * @param stand class object StandInfo which is used to start a scheduler for stand added in order manager
     *             available at localhost:8081/newStand
     * @return true (if no errors)
     */
    @PostMapping(value = "/newStand", consumes = "application/json")
    public JSONObject addNewStand(@RequestBody() CommonStand stand) {
            return schedulerHandler.updateSchedulers(stand);
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
    public List<Recommendation> postCommonOrder(@RequestBody() CommonOrder order) throws JsonProcessingException {
        System.out.println("User requested recommended stand for " + order.getId());
        return schedulerHandler.recommend(order);
    }




}
