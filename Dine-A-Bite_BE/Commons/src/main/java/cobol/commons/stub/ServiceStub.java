package cobol.commons.stub;


import cobol.commons.config.GlobalConfigurationBean;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.util.Pair;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.*;
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
    private Queue<Pair<Integer, Action>> onAvailableActionQueue = new PriorityQueue<>(Comparator.comparingInt(Pair::getKey));
    private Queue<Pair<Integer, Action>> onUnavailableActionQueue = new PriorityQueue<>(Comparator.comparingInt(Pair::getKey));

    // Wait PING_FREQUENCY seconds before pinging, and ping with a frequency of PING_FREQUENCY seconds
    @Scheduled(initialDelay = PING_FREQUENCY, fixedRate = PING_FREQUENCY)
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
                onAvailableActionQueue.forEach(pair -> pair.getValue().execute());
            }

            available = true;

        } catch (IOException e) {

            log.error("Could not ping: " + url);
            if (available) {
                log.info(url + " => now UNAVAILABLE!");
                onUnavailableActionQueue.forEach(pair -> pair.getValue().execute());
            }

            available = false;
        }

    }

    public void doOnAvailable(Action action, int priority, boolean tryImmediately) {
        // Execute immediately if currently available
        if (tryImmediately && available){
            action.execute();
        }
        this.onAvailableActionQueue.add(new Pair<>(priority,action));
    }

    public void doOnUnavailable(Action action, int priority, boolean tryImmediately) {
        // Execute immediately if currently unavailable
        if (tryImmediately && !available){
            action.execute();
        }
        this.onUnavailableActionQueue.add(new Pair<>(priority,action));
    }

    public abstract String getAddress();

    public void setAuthorizationToken(String authorizationToken) {
        ServiceStub.authorizationToken = "Bearer " + authorizationToken;
    }

}
