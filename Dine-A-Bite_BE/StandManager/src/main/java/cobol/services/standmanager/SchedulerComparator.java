package cobol.services.standmanager;

import java.util.Comparator;

/**
 * this class is used to compare and sort Stands (so schedulers) based on their remaining queuetime
 */
public class SchedulerComparator implements Comparator<Scheduler> {
    @Override
    public int compare(Scheduler o1, Scheduler o2) {
        return Long.compare(o1.timeSum(), o2.timeSum());
    }
}
