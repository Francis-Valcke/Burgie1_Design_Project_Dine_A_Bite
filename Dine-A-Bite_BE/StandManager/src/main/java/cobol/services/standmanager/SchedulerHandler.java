package cobol.services.standmanager;

import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope(value = "singleton")
public class SchedulerHandler {

    /**
     * The schedulerhandler has a list of all schedulers.
     * More information on schedulers in class Scheduler
     */
    private List<Scheduler> schedulers;

    public SchedulerHandler() {
        schedulers = new ArrayList<Scheduler>();
    }


    public List<Scheduler> getSchedulers() {
        return schedulers;
    }

    public void clearSchedulers() {
        if (this.schedulers.size() == 0) return;
        this.schedulers.clear();
    }

    public void addScheduler(Scheduler scheduler) {
        this.schedulers.add(scheduler);
    }

    public void removeScheduler(Scheduler scheduler) {
        this.schedulers.remove(scheduler);
    }

    @Scheduled(fixedDelay = 5000)
    public void pollEvents() {
        if (schedulers.size() == 0) return;
        for (Scheduler s : schedulers) {
            s.pollEvents();
        }
    }
}
