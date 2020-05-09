package cobol.commons.stub;

import org.springframework.stereotype.Service;

@Service
public class EventChannelStub extends ServiceStub {

    public static final String GET_TEST = "/test";
    public static final String GET_REGISTER_SUBSCRIBER = "/registerSubscriber";
    public static final String GET_REGISTER_SUBSCRIBER_TO_CHANNEL = "/registerSubscriber/toChannel";
    public static final String GET_DEREGISTER_SUBSCRIBER = "/deregisterSubscriber";
    public static final String POST_PUBLISH_EVENT = "/publishEvent";
    public static final String GET_EVENTS = "/events";

    @Override
    String getAddress() {
        return globalConfigurationBean.getAddressEventChannel();
    }

}
