package cobol.services.standmanager;

import cobol.commons.order.PriorityOrder;
import cobol.commons.order.Recommendation;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * this class will serve ass extra estimation for queue times (by calculating times for NOT confirmed orders)
 * TODO: should this be as thread or not? (or instead a priorityQueue extra class for value in the hashmap and synchronizing on this?)
 */

@Component
@Scope(value = "singleton")
public class PriorityQueues {
    //this Map will use as key the ID of the scheduler (which is subscriberID in the scheduler object) and the waiting time for that scheduler (for only non confirmed orders)
    private Map<Integer, Integer> queueTimes ;
    //Map with al non confirmed orders, used to delete them easily from priority queues (and in future also to easily "change" them if the order gets changed or new recommendation for same order is asked)
    private Map<Integer, PriorityOrder> priorityOrders;

    public PriorityQueues(){
        this.queueTimes = new HashMap<Integer,Integer>();
        this.priorityOrders = new HashMap<Integer, PriorityOrder>();
    }

    /**
     * function which get called after initial recommendation calculation in the schedulerHandler
     * it will add some extra time to the recommendations, based on the non confirmed orders in the priorityqueues
     * @param recommends = the initial recommendation calculation list
     */
    public void computeExtraTime(List<Recommendation> recommends, int orderId){
        PriorityOrder priorityOrder = new PriorityOrder(orderId);
        for (Recommendation rec : recommends){

            int currentRecomTime = rec .getTimeEstimate();
            int currentSchedulerId = rec.getSchedulerId();
            int currentExtraTime = 0;
            int orderPrepTime = rec.getOrderPrepTime();

            //check if already a priority queue in the hashmap to get time from, otherwise create one and set time to 0
            if (!queueTimes.containsKey(currentSchedulerId)){
                queueTimes.put(currentSchedulerId, currentExtraTime);
            }
            else{
                currentExtraTime = queueTimes.get(currentSchedulerId);
            }

            //add extra time to this recommendations' original time estimate
            rec.setTimeEstimate(currentExtraTime + currentRecomTime);

            //add (factored) extra time to the specific priority queue and save priorityOrder in extra list (used for quick lookup and management)
            int addedTime = changePriorityTime(currentSchedulerId,orderPrepTime,rec.getRank());
            priorityOrder.addRecommend(currentSchedulerId, addedTime);

            System.out.println("ADDED: " + addedTime + "TO SCHEDULER: " + currentSchedulerId);
        }

        priorityOrders.put(orderId, priorityOrder);
    }

    /**
     *
     * @param schedulerId ID of the scheduler (and also priority queue ID for that scheduler)
     * @param prepTime preparation time of the order for the specific scheduler (this could be different for a different stand possibly at this point)
     * @param priority of the recommendation (so basically just the rank of that recommendation)
     */
    public int changePriorityTime(int schedulerId, int prepTime, int priority){
        //for now a set factor for each priority (given that only 3 recommends are given atm so this can be hard coded to check first implementation)
        double [] factors = new double[]{0.6, 0.3, 0.1};

        //-1 because ranks start at 1, not at 0 like indices
        double factor = factors[priority-1];
        //calculate extra time
        int extraTime = (int) (prepTime*factor);
        //set new priorityQueue time
        queueTimes.replace(schedulerId,queueTimes.get(schedulerId) + extraTime);

        return extraTime;
    }

    /**
     * remove the added times this order had on the priority queues, and then remove the priority order object from the list
     * this is done when order is confirmed or when time-out for non confirmed order occurs
     * TODO: time-out for non confirmed order
     * @param orderId the id of the order (which is the key to get the priority order object)
     */
    public void removeOrder(int orderId){
        PriorityOrder order = priorityOrders.get(orderId);
        for (int key : order.getrecommendMap().keySet()){
            int currentTime = queueTimes.get(key);
            int newTime = currentTime - order.getrecommendMap().get(key);
            if (newTime > 0){
                queueTimes.replace(key, newTime);
            }
            else {
                queueTimes.replace(key, 0);
            }
        }
        priorityOrders.remove(orderId);
    }

}
