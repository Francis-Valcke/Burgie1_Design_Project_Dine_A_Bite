package cobol.services.eventchannel;

import cobol.commons.communication.response.BetterResponseModel;
import cobol.commons.domain.Event;
import cobol.commons.communication.response.ResponseModel;
import cobol.commons.stub.EventChannelStub;
import cobol.commons.stub.IEventChannel;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cobol.commons.communication.response.ResponseModel.status.OK;
import static cobol.commons.stub.EventChannelStub.*;

@RestController
public class EventChannelController implements IEventChannel {

    @Override
    @GetMapping(EventChannelStub.GET_PING)
    public ResponseEntity getPing() {
        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(OK.toString())
                        .details("EventChannel is alive!")
                        .build().generateResponse()
        );
    }

    @Override
    @GetMapping(GET_TEST)
    public String getTest(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }

    @Override
    @GetMapping(GET_REGISTER_SUBSCRIBER)
    public int getRegisterSubscriber(@RequestParam(value = "types", defaultValue = "") String types) {
        EventSubscriber newSubscriber = new EventSubscriber(types);
        newSubscriber.subscribe();
        return newSubscriber.getId();
    }

    @Override
    @GetMapping(GET_REGISTER_SUBSCRIBER_TO_CHANNEL)
    public void getRegisterSubscriberToChannel(@RequestParam(value = "id") int stubId, @RequestParam(value = "type", defaultValue = "") String type) {
        EventBroker broker = EventBroker.getInstance();
        EventSubscriber subscriber = broker.getSubscriberStub(stubId);
        subscriber.addType(type);
        List<String> newTypes = new ArrayList<>();
        newTypes.add(type);
        broker.subscribe(subscriber, newTypes);
    }

    @Override
    @GetMapping(GET_DEREGISTER_SUBSCRIBER)
    public void getDeregisterSubscriber(@RequestParam(value = "id") int stubId, @RequestParam(value = "type", defaultValue = "") String type) {
        EventBroker broker = EventBroker.getInstance();
        EventSubscriber subscriber = broker.getSubscriberStub(stubId);
        if (type.equals("")) {
            broker.unSubscribe(subscriber, subscriber.getTypes());
        } else {
            List<String> typeList = new ArrayList<>();
            String[] tempList = type.split(",");
            Collections.addAll(typeList, tempList);
            broker.unSubscribe(subscriber, typeList);
        }
    }

    @Override
    @PostMapping(value = POST_PUBLISH_EVENT, consumes = "application/json")
    public void postPublishEvent(@RequestBody Event e) {
        EventPublisher.publish(e);
    }

    @Override
    @GetMapping(GET_EVENTS)
    public BetterResponseModel<List<Event>> getEvents(@RequestParam(value = "id") int id) {

        List<Event> response = null;
        try {
            EventBroker broker = EventBroker.getInstance();
            EventSubscriber subscriber = broker.getSubscriberStub(id);
            response = subscriber.getUnhandledEvents();
            return BetterResponseModel.ok("Payload consists of list of events", response);

        } catch (Exception e) {
            e.printStackTrace();
            return BetterResponseModel.error("Error while trying to get events", e);
        }
    }
}