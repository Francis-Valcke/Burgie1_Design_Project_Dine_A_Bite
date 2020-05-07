package cobol.services.standmanager;

import cobol.commons.CommonFood;
import cobol.commons.CommonStand;
import cobol.commons.exception.CommunicationException;
import cobol.commons.exception.OrderException;
import cobol.commons.order.CommonOrder;
import cobol.commons.order.CommonOrderItem;
import cobol.commons.order.Recommendation;
import cobol.commons.order.SuperOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope(value = "singleton")
public class SchedulerHandler {


    @Autowired
    CommunicationHandler communicationHandler;

    /**
     * The schedulerhandler has a list of all schedulers.
     * More information on schedulers in class Scheduler
     */
    private List<Scheduler> schedulers;

    public SchedulerHandler() {
        schedulers = new ArrayList<Scheduler>();
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

    /**
     * @param order the order for which you want to find corresponding stands (brand
     * @return list of schedulers (so the stands) which offer the correct food to complete the order
     */
    public ArrayList<Scheduler> findCorrespondStands(CommonOrder order) {
        // first get the Array with all the food of the order
        ArrayList<CommonOrderItem> orderItems = new ArrayList<>(order.getOrderItems());


        // group all stands (schedulers) with the correct type of food available
        ArrayList<Scheduler> goodSchedulers = new ArrayList<>();

        for (int i = 0; i < this.schedulers.size(); i++) {
            if (order.getBrandName().equals(this.schedulers.get(i).getBrand())) {
                boolean validStand = true;
                Scheduler currentScheduler = this.schedulers.get(i);

                for (CommonOrderItem orderItem : orderItems) {
                    String food = orderItem.getFoodName();
                    if (currentScheduler.checkType(food)) {
                        validStand = true;
                    } else {
                        validStand = false;
                        break;
                    }
                }
                if (validStand) {
                    goodSchedulers.add(currentScheduler);
                }
            }
        }
        return goodSchedulers;
    }


    public List<HashSet<CommonOrderItem>> splitSuperOrder(SuperOrder superOrder) throws OrderException {

        // Extract and copy complete list of CommonOrderItems from superOrder
        List<CommonOrderItem> items = new ArrayList<>(superOrder.getOrderItems());

        // search items that can be executed together
        List<HashSet<CommonOrderItem>> itemSplit = new ArrayList<>();

        // Get schedulers from this brand
        List<Scheduler> brandSchedulers = getSchedulers()
                .stream().filter(s -> s.getBrand().equals(superOrder.getBrandName()))
                .collect(Collectors.toList());

        // Check if there is a scheduler that can make all items together
        List<Scheduler> everythingSchedulers = brandSchedulers
                .stream().filter(bs -> bs.getMenu().stream().map(CommonFood::getName).collect(Collectors.toList())
                        .containsAll(items.stream().map(CommonOrderItem::getFoodName).collect(Collectors.toList())))
                .collect(Collectors.toList());


        if (!everythingSchedulers.isEmpty()) {
            itemSplit.add(new HashSet<>(items));
        } else {
            // Split order items in sets which can be executed together
            for (Scheduler scheduler : brandSchedulers) {
                // As long items list is not empty, search stand which can do it
                if (!items.isEmpty()) {
                    List<String> stringMenu = scheduler.getMenu().stream().map(CommonFood::getName).collect(Collectors.toList());
                    List<CommonOrderItem> canExecuteTogether = items.stream().filter(item -> stringMenu.contains(item.getFoodName())).collect(Collectors.toList());

                    if(!canExecuteTogether.isEmpty()){
                        itemSplit.add(new HashSet<>(canExecuteTogether));
                        items.removeAll(canExecuteTogether);
                    }
                } else {
                    break;
                }
            }

            if(!items.isEmpty()){
                throw new OrderException("Super order contains items from other brands");
            }
        }

        return itemSplit;
    }

    /**
     * @param order is the order for which the recommended stands are required
     * @return JSON with a certain amount of recommended stands (currently based on lowest queue time only)
     */
    public List<Recommendation> recommend(CommonOrder order) throws JsonProcessingException {
        //choose how many recommends you want
        int amountOfRecommends = 3;

        // find stands (schedulers) which offer correct food for the order
        ArrayList<Scheduler> goodSchedulers = findCorrespondStands(order);

        // weight for when using mixed recommender, for now this is set (like amount of recs), but could also be chosen by attendee in future
        double weight = 5;

        //now look which type of recommendation we want and order the scheduler based on that
        if (order.getRecType().equals(CommonOrder.RecommendType.TIME)){
            //sort the stands (schedulers) based on remaining time
            Collections.sort(goodSchedulers, new SchedulerComparatorTime(new ArrayList<>(order.getOrderItems())));
        }
        else if (order.getRecType().equals(CommonOrder.RecommendType.DISTANCE)){
            //sort the stands (schedulers) based on distance
            Collections.sort(goodSchedulers, new SchedulerComparatorDistance(order.getLatitude(), order.getLongitude()));
        }
        else if (order.getRecType().equals(CommonOrder.RecommendType.DISTANCE_AND_TIME)) {
            //sort the stands (schedulers) based on mix between distance and time
            Collections.sort(goodSchedulers, new SchedulerComparator(order.getLatitude(), order.getLongitude(), weight, new ArrayList<>(order.getOrderItems())));
        }
        else {
            System.out.println("THE CHOSEN RECOMMENDATION TYPE IS NOT VALID");
        }

        // check if you have enough stands (for amount of recommendations you want)
        if (goodSchedulers.size() < amountOfRecommends) {
            amountOfRecommends = goodSchedulers.size();
        }
        // put everything into a JSON file to give as return value
        List<Recommendation> recommendations = new ArrayList<>();

        for (int i = 0; i < amountOfRecommends; i++) {
            Scheduler curScheduler = goodSchedulers.get(i);
            SchedulerComparatorDistance sc = new SchedulerComparatorDistance(curScheduler.getLat(), curScheduler.getLon());
            SchedulerComparatorTime st = new SchedulerComparatorTime(new ArrayList<>(order.getOrderItems()));
            recommendations.add(new Recommendation(curScheduler.getStandName(), curScheduler.getBrand(), sc.getDistance(order.getLatitude(), order.getLongitude()), st.getTimesum(curScheduler), i+1));
        }

        return recommendations;
    }

    public JSONObject addOrderToScheduler(CommonOrder order) {
        JSONObject obj = new JSONObject();
        for (Scheduler s : schedulers) {
            if (s.getStandName().equals(order.getStandName()) && s.getBrand().equals(order.getBrandName())) {
                s.addOrder(order);
                obj.put("added", true);
                break;
            }
        }
        return obj;
    }


    @Scheduled(fixedDelay = 5000)
    public void pollEvents() {
        if (schedulers.size() == 0) return;
        for (Scheduler s : schedulers) {
            s.pollEvents();
        }
    }

    public JSONObject updateSchedulers(CommonStand info) throws CommunicationException {
        boolean newScheduler = true;
        JSONObject obj = new JSONObject();
        for (Scheduler s : getSchedulers()) {
            if (s.getStandName().equals(info.getName()) && s.getBrand().equals(info.getBrandName())) {
                //remove scheduler
                if (info.getName() == null || info.getName().equals("")) {
                    removeScheduler(s);
                }

                //edit scheduler
                else {
                    ArrayList<String> l = new ArrayList<>();
                    for (CommonFood mi : info.getMenu()) {
                        l.add(mi.getName());
                        boolean olditem = false;
                        for (CommonFood mi2 : s.getMenu()) {

                            olditem = Scheduler.updateItem(mi, mi2);

                        }
                        if (!olditem) {
                            s.getMenu().add(mi);
                        }
                    }

                    List<CommonFood> toRemove = new ArrayList<>();
                    for (CommonFood mi2 : s.getMenu()) {
                        if (!l.contains(mi2.getName())) {
                            toRemove.add(mi2);
                        }
                    }
                    s.getMenu().removeAll(toRemove);

                }
                newScheduler = false;
                obj.put("added", true);
                break;
            }
        }
        //create scheduler
        if (newScheduler) {
            Scheduler s = new Scheduler(info.getMenu(), info.getName(), info.getBrandName(), info.getLatitude(), info.getLongitude(), communicationHandler);
            addScheduler(s);
            s.start();
            obj.put("added", true);
        }

        return obj;
    }

    public List<Scheduler> getSchedulers() {
        return schedulers;
    }

}
