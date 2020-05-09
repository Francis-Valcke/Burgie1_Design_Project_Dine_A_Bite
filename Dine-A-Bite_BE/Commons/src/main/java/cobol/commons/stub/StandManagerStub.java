package cobol.commons.stub;

import cobol.commons.domain.CommonOrder;
import cobol.commons.domain.CommonStand;
import cobol.commons.exception.CommunicationException;
import cobol.commons.exception.OrderException;
import cobol.commons.domain.Recommendation;
import cobol.commons.domain.SuperOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.log4j.Log4j2;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
public class StandManagerStub extends ServiceStub implements IStandManager{

    public static final String POST_UPDATE = "/update";
    public static final String POST_DELETE_SCHEDULER = "/deleteScheduler";
    public static final String POST_NEW_STAND = "/newStand";
    public static final String POST_PLACE_ORDER = "/placeOrder";
    public static final String POST_GET_SUPER_RECOMMENDATION = "/getSuperRecommendation";
    public static final String POST_GET_RECOMMENDATION = "/getRecommendation";

    @Override
    public String getAddress() {
        return globalConfigurationBean.getAddressStandManager();
    }

    @Override
    public ResponseEntity ping() {
        return null;
    }

    @Override
    public void update(List<CommonStand> stands) throws CommunicationException {

    }

    @Override
    public void deleteScheduler(String standName, String brandName) {

    }

    @Override
    public JSONObject addNewStand(CommonStand stand) throws CommunicationException {
        return null;
    }

    @Override
    public void placeOrder(CommonOrder order) {

    }

    @Override
    public ResponseEntity<JSONArray> getSuperRecommendation(SuperOrder superOrder) throws JsonProcessingException, OrderException {
        return null;
    }

    @Override
    public List<Recommendation> postCommonOrder(CommonOrder order) throws JsonProcessingException {
        return null;
    }
}
