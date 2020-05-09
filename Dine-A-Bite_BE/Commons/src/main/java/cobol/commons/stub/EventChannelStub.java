package cobol.commons.stub;

import cobol.commons.domain.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;

@Service
public class EventChannelStub extends ServiceStub implements IEventChannel{

    public static final String GET_TEST = "/test";
    public static final String GET_REGISTER_SUBSCRIBER = "/registerSubscriber";
    public static final String GET_REGISTER_SUBSCRIBER_TO_CHANNEL = "/registerSubscriber/toChannel";
    public static final String GET_DEREGISTER_SUBSCRIBER = "/deregisterSubscriber";
    public static final String POST_PUBLISH_EVENT = "/publishEvent";
    public static final String GET_EVENTS = "/events";

    @Override
    public String getAddress() {
        return globalConfigurationBean.getAddressEventChannel();
    }

    @Override
    public ResponseEntity getPing() {
        return null;
    }

    @Override
    public String getTest(String name) {
        return null;
    }

    @Override
    public int getRegisterSubscriber(String types) {
        return 0;
    }

    @Override
    public void getRegisterSubscriberToChannel(int subscriberId, String type) throws IOException {

        String uri = globalConfigurationBean.getAddressEventChannel() + GET_REGISTER_SUBSCRIBER_TO_CHANNEL;

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
                .queryParam("id", subscriberId)
                .queryParam("type", type);

        Request request = new Request.Builder()
                .url(builder.toUriString())
                .method("GET", null)
                //.addHeader("Authorization", authorizationToken)
                .build();

        okHttpClient.newCall(request).execute();

    }

    @Override
    public void getDeregisterSubscriber(int subscriberId, String type) {

    }

    @Override
    public void postPublishEvent(Event e) {

    }

    @Override
    public ResponseEntity getEvents(int id) throws JsonProcessingException {

        return null;
    }
}
