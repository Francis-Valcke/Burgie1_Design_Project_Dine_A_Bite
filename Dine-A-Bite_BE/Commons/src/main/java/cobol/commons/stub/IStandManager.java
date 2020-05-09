package cobol.commons.stub;

import cobol.commons.domain.CommonOrder;
import cobol.commons.domain.CommonStand;
import cobol.commons.exception.CommunicationException;
import cobol.commons.exception.OrderException;
import cobol.commons.domain.Recommendation;
import cobol.commons.domain.SuperOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface IStandManager {
    /**
     * API endpoint to test if the server is still alive.
     *
     * @return "StandManager is alive!"
     */
    ResponseEntity ping();

    /**
     * adds schedulers to SM
     *
     * @param stands
     * @throws JsonProcessingException when wrong input param
     */
    void update(List<CommonStand> stands) throws CommunicationException;

    void deleteScheduler(String standName, @RequestParam String brandName);

    /**
     * @param stand class object StandInfo which is used to start a scheduler for stand added in order manager
     *              available at localhost:8081/newStand
     * @return true (if no errors)
     */
    JSONObject addNewStand(CommonStand stand) throws CommunicationException;

    /**
     * @param order order which wants to be placed
     * TODO: really implement this
     */
    void placeOrder(CommonOrder order);

    /**
     * This method will split a superorder and give a recommendation for all the orders
     *
     * @param superOrder List with orderitems and corresponding brand
     * @return JSONArray each element containing a field "recommendations" and a field "order" similar to return of placeOrder
     * recommendation field will be a JSONArray of Recommendation object
     */
    ResponseEntity<JSONArray> getSuperRecommendation(SuperOrder superOrder) throws JsonProcessingException, OrderException;

    /**
     * @param order order object for which the Order Manager wants a recommendation
     * @return recommendation in JSON format
     */
    List<Recommendation> postCommonOrder(CommonOrder order) throws JsonProcessingException;
}
