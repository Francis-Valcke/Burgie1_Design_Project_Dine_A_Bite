package cobol.commons.stub;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class StandManagerStub extends ServiceStub {

    @Override
    @Scheduled(fixedRate = PING_FREQUENCY)
    void ping() {
        log.info("Ping");
    }
}
