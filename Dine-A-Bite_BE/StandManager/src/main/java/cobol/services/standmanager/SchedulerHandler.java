package cobol.services.standmanager;

import cobol.commons.MenuItem;
import cobol.commons.StandInfo;
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
                String food = orderItem.getFoodname();
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
    public JSONObject recommend(CommonOrder order) throws JsonProcessingException {
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
            recommendations.add(new Recommendation(curScheduler.getStandId(), curScheduler.getStandName(), sc.getDistance(order.getLatitude(), order.getLongitude()), st.getTimesum(curScheduler)));
            System.out.println(st.getTimesum(curScheduler));
        }

        // Arraylist recommendations to jsonobject
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(recommendations);
        JSONObject obj = new JSONObject();
        obj.put("recommendations", jsonString);

        return obj;
    }
    @Scheduled(fixedDelay = 5000)
    public void pollEvents() {
        if (schedulers.size() == 0) return;
        for (Scheduler s : schedulers) {
            s.pollEvents();
        }
    }
    public JSONObject updateSchedulers(StandInfo info){
        boolean newScheduler = true;
        JSONObject obj = new JSONObject();
        for (Scheduler s : getSchedulers()) {
            if (s.getStandId() == info.getId()) {
                //remove scheduler
                if (info.getName() == null || info.getName().equals("")) {
                    removeScheduler(s);
                }

                //edit scheduler
                else {
                    ArrayList<String> l = new ArrayList<>();
                    for (MenuItem mi : info.getMenu()) {
                        l.add(mi.getFoodName());
                        boolean olditem=false;
                        for (MenuItem mi2 : s.getMenu()) {

                            olditem = Scheduler.updateItem(mi, mi2);

                        }
                        if (!olditem){
                            s.getMenu().add(mi);
                        }
                    }
                    for (MenuItem mi2 : s.getMenu()) {
                        if (!l.contains(mi2.getFoodName()))s.removeItem(mi2);
                    }
                }
                newScheduler = false;
                obj.put("added", true);
                break;
            }
        }
        //create scheduler
        if (newScheduler) {
            Scheduler s = new Scheduler(info.getMenu(), info.getName(), info.getId(), info.getBrand());
            s.setLat(info.getLat());
            s.setLon(info.getLon());
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
