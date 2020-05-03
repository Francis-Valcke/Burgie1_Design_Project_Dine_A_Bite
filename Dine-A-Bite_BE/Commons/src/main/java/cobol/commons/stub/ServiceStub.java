package cobol.commons.stub;


import org.springframework.scheduling.annotation.Scheduled;

public abstract class ServiceStub {

    protected static final int PING_FREQUENCY = 10000; //10 seconds
    protected boolean enabled;

    abstract void ping();
}
