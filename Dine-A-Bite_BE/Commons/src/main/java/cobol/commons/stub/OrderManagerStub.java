package cobol.commons.stub;

import cobol.commons.domain.*;
import cobol.commons.exception.CommunicationException;
import cobol.commons.exception.DoesNotExistException;
import cobol.commons.exception.DuplicateStandException;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.log4j.Log4j2;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
@Scope(value = "singleton")
public class OrderManagerStub extends ServiceStub implements IOrderManager{

    // DBController
    public static final String POST_DB_IMPORT = "/db/import";
    public static final String DELETE_DB_CLEAR = "/db/clear";
    public static final String GET_DB_EXPORT = "/db/export";
    public static final String GET_UPDATE_SM = "/updateSM";
    public static final String DELETE_DELETE = "/delete";

    // MenuController
    public static final String GET_MENU = "/menu";
    public static final String GET_STAND_MENU = "/standMenu";

    // OrderController
    public static final String GET_GET_ORDER_INFO = "/getOrderInfo";
    public static final String POST_PLACE_ORDER = "/placeOrder";
    public static final String POST_PLACE_SUPER_ORDER = "/placeSuperOrder";
    public static final String GET_CONFIRM_STAND = "/confirmStand";
    public static final String GET_USER_ORDERS = "/getUserOrders";

    // StandController
    public static final String GET_VERIFY = "/verify";
    public static final String POST_ADD_STAND = "/addStand";
    public static final String POST_UPDATE_STAND = "/updateStand";
    public static final String DELETE_DELETE_STAND = "/deleteStand";
    public static final String GET_STANDS = "/stands";
    public static final String GET_STAND_LOCATIONS = "/standLocations";
    public static final String GET_REVENUE = "/revenue";


    @Override
    public String getAddress() {
        return globalConfigurationBean.getAddressOrderManager();
    }

    @Override
    public ResponseEntity<HashMap<Object, Object>> verify(String standName, String brandName, CommonUser authenticatedUser) {
        return null;
    }

    @Override
    public ResponseEntity<HashMap<Object, Object>> addStand(CommonStand stand, CommonUser user) throws JsonProcessingException, ParseException, DuplicateStandException, CommunicationException {
        return null;
    }

    @Override
    public ResponseEntity<HashMap<Object, Object>> updateStand(CommonStand stand) throws DoesNotExistException, JsonProcessingException {
        return null;
    }

    @Override
    public ResponseEntity<HashMap<Object, Object>> deleteStand(String standName, String brandName) throws JsonProcessingException, DoesNotExistException {
        return null;
    }

    @Override
    public ResponseEntity<Map<String, String>> requestStandNames() {
        return null;
    }

    @Override
    public ResponseEntity<Map<String, Map<String, Double>>> requestStandLocations() {
        return null;
    }

    @Override
    public ResponseEntity<HashMap<Object, Object>> requestRevenue(String standName, String brandName) throws DoesNotExistException {
        return null;
    }

    @Override
    public ResponseEntity<CommonOrder> getOrderInfo(int orderId) throws JsonProcessingException, DoesNotExistException {
        return null;
    }

    @Override
    public ResponseEntity<JSONObject> placeOrder(CommonUser userDetails, CommonOrder orderObject) throws Throwable {
        return null;
    }

    @Override
    public ResponseEntity<JSONArray> placeSuperOrder(CommonUser userDetails, SuperOrder superOrder) throws Throwable {
        return null;
    }

    @Override
    public ResponseEntity<String> confirmStand(int orderId, String standName, String brandName, CommonUser userDetails) throws Throwable {
        return null;
    }

    @Override
    public ResponseEntity<List<CommonOrder>> getUserOrders(CommonUser userDetails) {
        return null;
    }

    @Override
    public ResponseEntity<List<CommonFood>> requestGlobalMenu() throws JsonProcessingException {
        return null;
    }

    @Override
    public ResponseEntity<List<CommonFood>> requestStandMenu(String standName, String brandName) throws DoesNotExistException {
        return null;
    }

    @Override
    public ResponseEntity<String> load(List<CommonBrand> data) {
        return null;
    }

    @Override
    public ResponseEntity<String> clear() throws ParseException, JsonProcessingException {
        return null;
    }

    @Override
    public ResponseEntity<List<CommonBrand>> export() {
        return null;
    }

    @Override
    public ResponseEntity<List<String>> update() throws JsonProcessingException {
        return null;
    }

    @Override
    public ResponseEntity<String> delete() throws ParseException, JsonProcessingException {
        return null;
    }
}
