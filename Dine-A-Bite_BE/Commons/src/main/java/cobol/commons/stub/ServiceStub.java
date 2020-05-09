package cobol.commons.stub;


import cobol.commons.annotation.Authenticated;
import cobol.commons.config.GlobalConfigurationBean;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Log4j2
public abstract class ServiceStub {

    public static final String GET_PING = "/ping";
    protected static final int PING_FREQUENCY = 10000; //10 seconds
    protected final OkHttpClient okHttpClient = new OkHttpClient().newBuilder().connectTimeout(1, TimeUnit.SECONDS).build();
    protected boolean available = false;
    @Autowired
    protected GlobalConfigurationBean globalConfigurationBean;

    protected static String authorizationToken;

    protected ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private List<Action> onAvailableActionList = new ArrayList<>();
    private List<Action> onUnavailableActionList = new ArrayList<>();

    @Scheduled(fixedRate = PING_FREQUENCY)
    protected void heartBeat() {

        String url = getAddress() + GET_PING;

        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .build();

        try {

            okHttpClient.newCall(request).execute().close();
            log.info("Ping: " + url + " success!");
            if (!available) {
                log.info(url + " => now AVAILABLE!");
                onAvailableActionList.forEach(Action::execute);
            }

            available = true;

        } catch (IOException e) {

            log.error("Could not ping: " + url);
            if (available) {
                log.info(url + " => now UNAVAILABLE!");
                onUnavailableActionList.forEach(Action::execute);
            }

            available = false;
        }
    }

    public void doOnAvailable(Action action) {
        this.onAvailableActionList.add(action);
    }

    public void doOnUnavailable(Action action) {
        this.onUnavailableActionList.add(action);
    }

    protected String buildUrl(String baseUrl, HashMap<String, String> params) {

        if (params != null && !params.isEmpty()) {
            baseUrl += "?";
            StringBuilder baseUrlBuilder = new StringBuilder(baseUrl);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                baseUrlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            baseUrl = baseUrlBuilder.toString();
        }
        //remove the last &
        return baseUrl.substring(0, baseUrl.length() - 1);
    }

    public abstract String getAddress();

    public void setAuthorizationToken(String authorizationToken) {
        ServiceStub.authorizationToken = "Bearer " + authorizationToken;
    }

}
