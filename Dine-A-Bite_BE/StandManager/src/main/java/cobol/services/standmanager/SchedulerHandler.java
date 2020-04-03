package cobol.services.standmanager;

import cobol.commons.CommonFood;
import cobol.commons.CommonStand;
import cobol.commons.exception.CommunicationException;
import cobol.commons.order.CommonOrder;
import cobol.commons.order.CommonOrderItem;
import cobol.commons.order.Recommendation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
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
     * @param order the order for which you want to find corresponding stands
     * @return list of schedulers (so the stands) which offer the correct food to complete the order
     */
    public ArrayList<Scheduler> findCorrespondStands(CommonOrder order) {
        // first get the Array with all the food of the order
        ArrayList<CommonOrderItem> orderItems = new ArrayList<>(order.getOrderItems());


        // group all stands (schedulers) with the correct type of food available
        ArrayList<Scheduler> goodSchedulers = new ArrayList<>();

        for (int i = 0; i < this.schedulers.size(); i++) {
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
        return goodSchedulers;
    }


    /**
     * @param order is the order for which the recommended stands are required
     * @return JSON with a certain amount of recommended stands (currently based on lowest queue time only)
     */
    public List<Recommendation> recommend(CommonOrder order) throws JsonProcessingException {
        /* choose how many recommends you want */
        int amountOfRecommends = 3;

        /* find stands (schedulers) which offer correct food for the order */
        ArrayList<Scheduler> goodSchedulers = findCorrespondStands(order);

        /* sort the stands (schedulers) based on remaining time */
        //Collections.sort(goodSchedulers, new SchedulerComparatorTime(order.getFull_order()));

        /* sort the stands (schedulers) based on distance */
        Collections.sort(goodSchedulers, new SchedulerComparatorDistance(order.getLatitude(), order.getLongitude()));

        /* TODO: this is how you sort based on combination, weight is how much time you add for each unit of distance */
        /* sort the stands (schedulers) based on combination of time and distance */
        //double weight = 5;
        //Collections.sort(goodSchedulers, new SchedulerComparator(order.getLat(), order.getLon(), weight);

        /* check if you have enough stands (for amount of recommendations you want) */
        if (goodSchedulers.size() < amountOfRecommends) {
            amountOfRecommends = goodSchedulers.size();
        }
        /* put everything into a JSON file to give as return value */
        List<Recommendation> recommendations = new ArrayList<>();

        for (int i = 0; i < amountOfRecommends; i++) {
            Scheduler curScheduler = goodSchedulers.get(i);
            System.out.println(curScheduler.getStandName());
            SchedulerComparatorDistance sc = new SchedulerComparatorDistance(curScheduler.getLat(), curScheduler.getLon());
            SchedulerComparatorTime st = new SchedulerComparatorTime(new ArrayList<>(order.getOrderItems()));
            recommendations.add(new Recommendation(curScheduler.getStandName(), curScheduler.getBrand(), sc.getDistance(order.getLatitude(), order.getLongitude()), st.getTimesum(curScheduler)));
            System.out.println(st.getTimesum(curScheduler));
        }

        return recommendations;
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
                        boolean olditem=false;
                        for (CommonFood mi2 : s.getMenu()) {

                            olditem = Scheduler.updateItem(mi, mi2);

                        }
                        if (!olditem){
                            s.getMenu().add(mi);
                        }
                    }

                    // TODO is dit nooit getest geweest ?? je kan geen items
                    //  verwijderen uit een lijst dat je aan het itereren bent
                    List<CommonFood> toRemove= new ArrayList<>();
                    for (CommonFood mi2 : s.getMenu()) {
                        if (!l.contains(mi2.getName())){
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
            Scheduler s = new Scheduler(info.getMenu(), info.getName(), info.getBrandName(), info.getLatitude(), info.getLongitude());
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
