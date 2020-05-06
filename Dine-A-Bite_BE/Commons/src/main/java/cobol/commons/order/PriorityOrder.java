package cobol.commons.order;

import java.util.HashMap;
import java.util.Map;

/**
 * not confirmed order used in priorityqueues
 */
public class PriorityOrder {
    //order ID of this non confirmed order
    private int orderId;

    //map with keys scheduler ID's this order got as recommendation, and values the added time (including priority factor) that this had on that scheduler priorityQueue
    private Map<Integer, Integer> schedulerId;

    public PriorityOrder(int orderId){
        this.orderId = orderId;
        schedulerId = new HashMap<Integer, Integer>();
    }

    /**
     *
     * @param Id
     * @param addedTime
     */
    public void addRecommend(int Id, int addedTime){
        schedulerId.put(Id, addedTime);
    }

    public Map<Integer, Integer> getSchedulerId() {
        return schedulerId;
    }
}
