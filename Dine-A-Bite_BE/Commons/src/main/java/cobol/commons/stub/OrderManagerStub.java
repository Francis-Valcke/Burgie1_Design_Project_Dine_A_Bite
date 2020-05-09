package cobol.commons.stub;

import lombok.extern.log4j.Log4j2;
import okhttp3.Request;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Log4j2
@Service
public class OrderManagerStub extends ServiceStub{

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

    // StandController
    public static final String GET_VERIFY = "/verify";
    public static final String POST_UPDATE_STAND = "/updateStand";
    public static final String DELETE_DELETE_STAND = "/deleteStand";
    public static final String GET_STANDS = "/stands";
    public static final String GET_STAND_LOCATIONS = "/standLocations";


    @Override
    public String getAddress() {
        return globalConfigurationBean.getAddressOrderManager();
    }
}
