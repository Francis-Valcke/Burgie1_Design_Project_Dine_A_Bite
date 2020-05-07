package cobol.services.standmanager;

import cobol.commons.order.PriorityOrder;
import cobol.commons.order.Recommendation;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * this class will serve ass extra estimation for queue times (by calculating times for NOT confirmed orders)
 */

@Component
@Scope(value = "singleton")
public class PriorityQueues {
    //this Map will use as key the ID of the scheduler (which is subscriberID in the scheduler object) and the priority orders in that scheduler
    private Map<Integer, ArrayList<PriorityOrder>> queues; ;
    //Map with al non confirmed orders, used to delete them easily from priority queues (and in future also to easily "change" them if the order gets changed or new recommendation for same order is asked)
    private Map<Integer, PriorityOrder> priorityOrders;

    public PriorityQueues(){
        this.queues = new HashMap<Integer,ArrayList<PriorityOrder>>();
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
            int orderPrepTime = rec.getOrderPrepTime();


            //check if already a priority queue in the hashmap to get time from, otherwise create one and create the list for priorityOrders
            if (!queues.containsKey(currentSchedulerId)){
                queues.put(currentSchedulerId, new ArrayList<PriorityOrder>());
            }

            //calculate the queue time of this specific priorityQueue
            int extraTime = checkQueueTime(queues.get(currentSchedulerId), currentSchedulerId);


            //add extra time to this recommendations' original time estimate
            rec.setTimeEstimate(extraTime + currentRecomTime);

            //add (factored) extra time to the specific priority queue and save priorityOrder in extra list (used for quick lookup and management)
            int addedTime = calculatePriorityTime(orderPrepTime,rec.getRank());
            priorityOrder.addRecommend(currentSchedulerId, addedTime);

            System.out.println("ADDED: " + addedTime + "TO SCHEDULER: " + currentSchedulerId);
        }

        priorityOrders.put(orderId, priorityOrder);
    }

    /**
     *
     * @param prepTime preparation time of the order for the specific scheduler (this could be different for a different stand possibly at this point)
     * @param priority of the recommendation (so basically just the rank of that recommendation)
     */
    public int calculatePriorityTime(int prepTime, int priority){
        //for now a set factor for each priority (given that only 3 recommends are given atm so this can be hard coded to check first implementation)
        double [] factors = new double[]{0.6, 0.3, 0.1};

        //-1 because ranks start at 1, not at 0 like indices
        double factor = factors[priority-1];
        //calculate extra time
        int extraTime = (int) (prepTime*factor);

        return extraTime;
    }

    /**
     * remove the added times this order had on the priority queues, and then remove the priority order object from the list
     * this is done when order is confirmed or when time-out for non confirmed order occurs
     * @param orderId the id of the order (which is the key to get the priority order object)
     */
    public void removeOrder(int orderId){
        PriorityOrder order = priorityOrders.get(orderId);
        for (int key : order.getrecommendMap().keySet()){
            queues.get(key).remove(order);
        }
        priorityOrders.remove(orderId);
    }

    /**
     * calculate current queue time of priority queue and delete orders which are past their time window
     * @param prioQueue the current priority queue
     * @param iD the scheduler id of this priorityqueue
     * @return returns the current extra time off this priority queue
     */
    public int checkQueueTime(ArrayList<PriorityOrder> prioQueue, int iD){
        //TimeWindow, currently hard coded to be 120 seconds but can be easily changed/used dynamically
        int timeWindow = 120;

        int extraTime = 0;
        //check if order in prio queue is expired, and if so delete it
        //if not, we can add the weighted time of this order to the total time
        for (PriorityOrder o : prioQueue){
            //if true, expired, if false, calculate time
            if (o.checkExpirationWindow(120)){
                removeOrder(o.getOrderId());
            }
            else{
                extraTime += o.getrecommendMap().get(iD);
            }
        }
        return extraTime;
    }

}
