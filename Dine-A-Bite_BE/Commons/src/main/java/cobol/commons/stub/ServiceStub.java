package cobol.commons.stub;


import cobol.commons.communication.response.BetterResponseModel;
import cobol.commons.config.GlobalConfigurationBean;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.util.Pair;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;
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

    @Autowired
    protected AuthenticationHandler authenticationHandler;

    protected ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private Queue<ActionWrapper> onAvailableActionQueue = new PriorityQueue<>(Comparator.comparingInt(ActionWrapper::getPriority));
    private Queue<ActionWrapper> onUnavailableActionQueue = new PriorityQueue<>(Comparator.comparingInt(ActionWrapper::getPriority));

    // Wait PING_FREQUENCY seconds before pinging, and ping with a frequency of PING_FREQUENCY seconds
    @Scheduled(initialDelay = PING_FREQUENCY, fixedRate = PING_FREQUENCY)
    protected void heartBeat() {

        String url = getAddress() + GET_PING;

        try {
            request("GET", url, null);
            log.info("Ping: " + url + " success!");


            if (!available) {
                log.info(url + " => now AVAILABLE!");
                onAvailableActionQueue.forEach(ActionWrapper::execute);
            }

            available = true;

        } catch (IOException e) {
            log.error("Could not ping: " + url);


            if (available) {
                log.info(url + " => now UNAVAILABLE!");
                onUnavailableActionQueue.forEach(ActionWrapper::execute);
            }

            available = false;
        }

    }

    public void doOnAvailable(Action action, int priority, boolean tryImmediately) {

        ActionWrapper actionWrapper = new ActionWrapper(action, priority);

        // Execute immediately if currently available
        if (tryImmediately && available){
            actionWrapper.execute();
        }
        this.onAvailableActionQueue.add(actionWrapper);

    }

    public void doOnUnavailable(Action action, int priority, boolean tryImmediately) {

        ActionWrapper actionWrapper = new ActionWrapper(action, priority);

        // Execute immediately if currently unavailable
        if (tryImmediately && !available){
            actionWrapper.execute();
        }
        this.onUnavailableActionQueue.add(actionWrapper);

    }

    public abstract String getAddress();


    protected String request(String method, String url, String body) throws IOException {

        String response;

        //Create builder and add url
        Request.Builder requestBuilder = new Request.Builder().url(url);

        // If a token is available, add it as a header to the builder
        if (authenticationHandler.isAuthenticated()){
            requestBuilder = requestBuilder.addHeader("Authorization", authenticationHandler.getToken());
        }

        // Add method to the request builder
        switch (method){

            case "GET":{

                requestBuilder = requestBuilder.method("GET", null);

                break;
            }
            case "POST":{

                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);

                requestBuilder = requestBuilder.method("POST", requestBody);

                break;
            }

        }

        // Build the request
        Request request = requestBuilder.build();

        // Execute the request
        ResponseBody responseBody = okHttpClient.newCall(request).execute().body();
        response = Objects.requireNonNull(responseBody).string();
        responseBody.close();

        //Return the response
        return response;
    }

    public boolean isAvailable() {
        return available;
    }
}
