package cobol.commons.order;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Not confirmed order used in priority queues system in stand manager
 * A better name for this would be "NonConfirmedOrder" or "VirtualOrder"
 */
public class PriorityOrder {
    //order ID of this non confirmed order
    private int orderId;

    //map with keys scheduler ID's this order got as recommendation, and values the added time (including priority factor) that this had on that scheduler priorityQueue
    private Map<Integer, Integer> recommendMap;

    //time when this order asked for recommendations. Used to check if it is not expired (and hence should be deleted from priority queues)
    private ZonedDateTime requestTime;

    /**
     * Class Constructor
     *
     * @param orderId id of the order
     */
    public PriorityOrder(int orderId) {
        this.orderId = orderId;
        recommendMap = new HashMap<Integer, Integer>();
        requestTime = ZonedDateTime.now(ZoneId.of("Europe/Brussels"));
    }

    /**
     * Save recommend id and the time added (to later easily substract it again)
     *
     * @param Id        id of scheduler
     * @param addedTime time added to that specific priority queue
     */
    public void addRecommend(int Id, int addedTime) {
        recommendMap.put(Id, addedTime);
    }

    /**
     * Checks if the expiration window for confirming is already done, and if so we should remove this order from priorityQueues
     *
     * @param timeWindowSeconds defines the timeWindow in seconds
     * @return true if the timeWindow is done so we should remove the order from priority queues
     */
    public boolean checkExpirationWindow(int timeWindowSeconds) {
        if (this.requestTime.plusSeconds(timeWindowSeconds).isBefore(ZonedDateTime.now(ZoneId.of("Europe/Brussels")))) {
            return true;
        } else {
            return false;
        }
    }

    public Map<Integer, Integer> getRecommendMap() {
        return recommendMap;
    }

    public int getOrderId() {
        return orderId;
    }
}
