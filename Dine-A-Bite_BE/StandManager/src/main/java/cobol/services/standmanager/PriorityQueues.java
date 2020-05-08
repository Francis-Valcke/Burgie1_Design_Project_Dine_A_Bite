package cobol.services.standmanager;

import cobol.commons.order.CommonOrder;
import cobol.commons.order.PriorityOrder;
import cobol.commons.order.Recommendation;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


import java.util.*;
import java.util.stream.Collectors;

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
    public void computeExtraTime(List<Recommendation> recommends, int orderId, CommonOrder.RecommendType recType){
        PriorityOrder priorityOrder = new PriorityOrder(orderId);
        for (Recommendation rec : recommends){
            int currentRecomTime = rec .getTimeEstimate();
            int currentSchedulerId = rec.getSchedulerId();

            //check if already a priority queue in the hashmap to get time from, otherwise create one and create the list for priorityOrders
            if (!queues.containsKey(currentSchedulerId)){
                queues.put(currentSchedulerId, new ArrayList<PriorityOrder>());
            }

            //calculate the queue time of this specific priorityQueue
            int extraTime = checkQueueTime(queues.get(currentSchedulerId), currentSchedulerId);

            //add extra time to this recommendations' original time estimate
            rec.setTimeEstimate(extraTime + currentRecomTime);

        }
        //sort and rerank the recommendations now before adding weighted times to prio queues
        List<Recommendation> newOrderedRecommends = recommends;
        if (recType != CommonOrder.RecommendType.DISTANCE) {
            newOrderedRecommends = sortAndRerank(recommends);
        }

        //add (factored) extra time to the specific priority queue and save priorityOrder in extra list (used for quick lookup and management)
        for (Recommendation rec: newOrderedRecommends){
            int orderPrepTime = rec.getOrderPrepTime();
            int currentSchedulerId = rec.getSchedulerId();

            int addedTime = calculatePriorityTime(orderPrepTime,rec.getRank(), newOrderedRecommends.size());
            priorityOrder.addRecommend(currentSchedulerId, addedTime);

            System.out.println("ADDED: " + addedTime + " TO SCHEDULER: " + currentSchedulerId);
        }

        //add priorityOrder to the corresponding queues and add it to the priorityOrder list itself
        for (int key: priorityOrder.getRecommendMap().keySet()){
            queues.get(key).add(priorityOrder);
        }
        priorityOrders.put(orderId, priorityOrder);
    }

    /**
     *
     * @param prepTime preparation time of the order for the specific scheduler (this could be different for a different stand possibly at this point)
     * @param priority of the recommendation (so basically just the rank of that recommendation)
     */
    public int calculatePriorityTime(int prepTime, int priority, int amount){
        //for now a set factor for each priority (given that only 3 recommends are given atm so this can be hard coded to check first implementation)
        double [] factors = getDescendingUnit(amount);

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
        //remove from queues (only if not previously removed, this could be the case if timewindow expires, but still confirm later on (until no time window is available in frontend)
        if (priorityOrders.containsKey(orderId)) {
            PriorityOrder order = priorityOrders.get(orderId);
            for (int key : order.getRecommendMap().keySet()) {
                queues.get(key).remove(order);
            }
            priorityOrders.remove(orderId);
        }
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
        //Extra time to be added to a recommendation (based on the priority queue waiting time with this scheduler id)
        int extraTime = 0;

        ArrayList<Integer> ordersToRemove = new ArrayList<>();
        //check if order in prio queue is expired, and if so delete it
        //if not, we can add the weighted time of this order to the total time
        for (PriorityOrder o : prioQueue){
            //if true, expired, if false, calculate time
            if (o.checkExpirationWindow(timeWindow)){
                System.out.println("Window passed, removing order nr " + o.getOrderId() + " from priority queues");
                ordersToRemove.add(o.getOrderId());
            }

            else{
                extraTime += o.getRecommendMap().get(iD);
            }

        }

        //remove orders from priority queues as well as from the list of priority orders
        for (int id : ordersToRemove){
            removeOrder(id);
        }

        return extraTime;
    }

    /**
     * functions for dynamic factoring, where each element of higher priority is double of the next one with lower priority (except for relation between 2nd to last and 3rd to last)
     * this type of function for computing factors could be change if you use a different (statistical) model
     * @param amount the amount of factors we need (which is the length of recommend list, or the amount of priorities)
     * @return the factor array for weighted extra times in priority queues
     */
    public double[] getDescendingUnit(int amount){
        double [] factors = new double[amount];

        if (amount == 1){
            factors[0] = 1;
        }
        else{
            double constant = 1;
            for (int i = 0; i < amount - 2; i++){
                constant = constant/2;
                factors[i] = constant;
            }
            factors[amount-2] = ((2*constant)/3);
            factors[amount-1] = (constant/3);
        }
        return factors;
    }

    /**
     * recommendation times are changed, so we need to reorder the list, but also change the ranks of these recommends based on the new ordering
     * @param recommendations the recommendation list that needs to be resorted
     * @return resorted and reranked recommendation list
     */
    public List<Recommendation> sortAndRerank(List<Recommendation> recommendations){
        List<Recommendation> sortedRecommends = recommendations.stream()
                .sorted(Comparator.comparing(Recommendation::getTimeEstimate))
                .collect(Collectors.toList());

        //also reset the ranks according to this new ordering
        for (int i = 0; i < sortedRecommends.size();i++){
            Recommendation curRec = sortedRecommends.get(i);
            curRec.setRank(i+1);
        }

        return sortedRecommends;
    }
}
