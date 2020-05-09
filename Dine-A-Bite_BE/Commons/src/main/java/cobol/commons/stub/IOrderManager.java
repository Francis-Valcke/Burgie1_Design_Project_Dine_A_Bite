package cobol.commons.stub;

import cobol.commons.domain.*;
import cobol.commons.exception.CommunicationException;
import cobol.commons.exception.DoesNotExistException;
import cobol.commons.exception.DuplicateStandException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IOrderManager {

    ResponseEntity<HashMap<Object,Object>> verify(String standName, String brandName, CommonUser authenticatedUser);

    /**
     * This API will add a stand to:
     * - The database
     * - The cache
     *
     * @param stand The stand that needs to be created
     * @return Success message or exception
     * @throws JsonProcessingException Json processing error
     * @throws ParseException          Json parsing error
     * @throws DuplicateStandException Such a stand already exists
     */
    ResponseEntity<HashMap<Object,Object>> addStand(CommonStand stand, CommonUser user) throws JsonProcessingException, ParseException, DuplicateStandException, CommunicationException;

    /**
     * This API will update a stands
     * - The database
     * - The cache
     *
     * @param stand The stand that needs to be created
     * @return Success message or exception
     */
    ResponseEntity<HashMap<Object,Object>> updateStand(CommonStand stand) throws DoesNotExistException, JsonProcessingException;

    /**
     * This API will delete a stand based on its Id.
     * The stand will be removed in:
     * - The database
     * - The local cache
     * - The cache of the StandManager
     *
     * @param standName standName
     * @param brandName brandName
     * @return Success message or exception
     * @throws JsonProcessingException Json processing error
     */
    ResponseEntity<HashMap<Object,Object>> deleteStand(String standName, String brandName) throws JsonProcessingException, DoesNotExistException;

    /**
     * This method will retrieve all stand names with corresponding brand names
     *
     * @return HashMap of "standName":"brandName"
     */
    ResponseEntity<Map<String, String>> requestStandNames();

    ResponseEntity<Map<String, Map<String, Double>>> requestStandLocations();

    ResponseEntity<HashMap<Object, Object>> requestRevenue(String standName, String brandName) throws DoesNotExistException;

    /**
     * This method will retrieve information about a given order identified by the orderId.
     *
     * @param orderId Id of the order
     * @return CommonOrder object
     * @throws JsonProcessingException Json processing error
     * @throws DoesNotExistException   Order does not exist
     */
    ResponseEntity<CommonOrder> getOrderInfo(int orderId) throws JsonProcessingException, DoesNotExistException;

    /**
     * This method will add the order to the order processor,
     * gets a recommendation from the scheduler and forwards it to the attendee app.
     *
     * @param orderObject the order recieved from the attendee app
     * @return JSONObject including CommonOrder "order" and JSONArray "Recommendation"
     * @throws JsonProcessingException Json processing error
     * @throws ParseException Json parsing error
     */
    ResponseEntity<JSONObject> placeOrder(CommonUser userDetails, CommonOrder orderObject) throws Throwable;

    /**
     * This method will handle an order from different stands in a certain brand
     *
     * @param superOrder SuperOrder object containing a list of CommonOrderItems of a certain brand
     * @return JSONArray each element containing a field "recommendations" and a field "order" similar to return of placeOrder
     */
    ResponseEntity<JSONArray> placeSuperOrder(CommonUser userDetails, SuperOrder superOrder) throws Throwable;

    /**
     * Sets stand- and brandname of according order when this recommendations is chosen
     *
     * @param orderId   integer id of order to be confirmed
     * @param standName name of stand
     * @param brandName name of brand
     * @throws JsonProcessingException jsonexception
     */
    ResponseEntity<String> confirmStand(int orderId, String standName, String brandName, CommonUser userDetails) throws Throwable;

    ResponseEntity<List<CommonOrder>> getUserOrders(CommonUser userDetails);

    /**
     * This API will retrieve the all of the food items in the system.
     * It will filter items with identical name and brandName's
     *
     * @return Global menu
     */
    ResponseEntity<List<CommonFood>> requestGlobalMenu() throws JsonProcessingException;

    /**
     * Rest call for retrieving food items of a given stand by it's id.
     *
     * @param standName name of stand
     * @param brandName name of brand
     * @return List of food items
     */
    ResponseEntity<List<CommonFood>> requestStandMenu(String standName, String brandName) throws DoesNotExistException;

    /**
     * This API will accept a JSON that describes the database contents.
     * This is an easy way to fill the database with contents.
     *
     * @param data List of Brand objects deserialized from json
     * @return Success message or exception
     */
    ResponseEntity<String> load(List<CommonBrand> data);

    /**
     * This API will clear all of the database contents.
     * This will not clear the local cache.
     *
     * @return Success message or exception
     */
    ResponseEntity<String> clear() throws ParseException, JsonProcessingException;

    /**
     * This API will export the database contents in json format.
     *
     * @return Success message or exception
     */
    ResponseEntity<List<CommonBrand>> export();

    /**
     * This API will refresh:
     * - The cache in StandManager
     * with respect to the database
     *
     * @return List of stand names
     * @throws JsonProcessingException Json processing error
     */
    ResponseEntity<List<String>> update() throws JsonProcessingException;

    /**
     * This API will clear database contents and the local and StandManager cache.
     *
     * @return Success message or exception
     * @throws ParseException Parsing error
     * @throws JsonProcessingException Json processing error
     */
    ResponseEntity<String> delete() throws ParseException, JsonProcessingException;
}
