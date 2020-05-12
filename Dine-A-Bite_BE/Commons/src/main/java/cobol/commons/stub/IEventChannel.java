package cobol.commons.stub;

import cobol.commons.communication.response.BetterResponseModel;
import cobol.commons.domain.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

public interface IEventChannel {


    /**
     * API endpoint to test if the server is still alive.
     *
     * @return "EventChannel is alive!"
     */
    ResponseEntity getPing();

    /**
     * This is a test function
     *
     * @param name test value
     * @return hello world
     */
    String getTest(String name);

    /**
     * The callee sends this request along with the channels it wants to subscribe to. A stub is created and gets a
     * unique id. This is returned to the callee.
     *
     * @param types channels the caller wants to subscribe to, types are separated by ','.
     * @return The unique id of the event subscriber stub
     */
    int getRegisterSubscriber(String types) throws IOException;

    /**
     * This method allows subscribers to subscribe to channels they were previously not subscribed to.
     *
     * @param stubId The id to identify the subscriberstub
     * @param type   the channels the stub has to subscribe to
     */
    void getRegisterSubscriberToChannel(int stubId, String type) throws IOException;

    /**
     * @param stubId id of the stub to desubscribe
     * @param type   channels to desubscribe from, separated by commas. If none are given, stub desubscribes from all channels
     */
    void getDeregisterSubscriber(int stubId, String type);

    /**
     * Receives an event in JSON format, forwards it to the proper channels
     *
     * @param e The event to publish
     */
    void postPublishEvent(Event e);

    /**
     * @param id unique id of stub
     * @return the events that were received by the stub since the last poll
     */
    BetterResponseModel<List<Event>> getEvents(int id) throws IOException;
}
