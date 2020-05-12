package cobol.commons.stub;

import cobol.commons.annotation.Authenticated;
import cobol.commons.communication.response.BetterResponseModel;
import cobol.commons.domain.Event;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.naming.CommunicationException;
import java.io.IOException;
import java.util.List;

@Service
@Scope(value = "singleton")
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

    @Authenticated
    @Override
    public int getRegisterSubscriber(String types) throws IOException {

        String url = getAddress() + GET_REGISTER_SUBSCRIBER;

        String response = request("GET", url, null);

        return Integer.parseInt(response);
    }

    @Authenticated
    @Override
    public void getRegisterSubscriberToChannel(int subscriberId, String type) throws IOException {

        String url = UriComponentsBuilder.fromUriString(getAddress() + GET_REGISTER_SUBSCRIBER_TO_CHANNEL)
                .queryParam("id", subscriberId)
                .queryParam("type", type).toUriString();

        request("GET", url, null);
    }

    @Override
    public void getDeregisterSubscriber(int subscriberId, String type) {

    }

    @Override
    public void postPublishEvent(Event e) {

    }

    @Authenticated
    @Override
    public BetterResponseModel<List<Event>> getEvents(int id) throws IOException {

        String url = UriComponentsBuilder.fromUriString(getAddress() + GET_EVENTS)
                .queryParam("id", id).toUriString();

        String response = request("GET", url, null);

        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper.readValue(response, new TypeReference<BetterResponseModel<List<Event>>>() {});
    }
}
