package cobol.commons.stub;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class StandManagerStub extends ServiceStub {

    public static final String POST_UPDATE = "/update";
    public static final String POST_DELETE_SCHEDULER = "/deleteScheduler";
    public static final String POST_NEW_STAND = "/newStand";
    public static final String POST_PLACE_ORDER = "/placeOrder";
    public static final String POST_GET_SUPER_RECOMMENDATION = "/getSuperRecommendation";
    public static final String POST_GET_RECOMMENDATION = "/getRecommendation";

    @Override
    String getAddress() {
        return globalConfigurationBean.getAddressStandManager();
    }
}
