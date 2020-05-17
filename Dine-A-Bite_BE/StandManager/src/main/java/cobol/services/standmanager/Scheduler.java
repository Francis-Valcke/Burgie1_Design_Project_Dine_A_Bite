package cobol.services.standmanager;

import cobol.commons.CommonFood;
import cobol.commons.Event;
import cobol.commons.exception.CommunicationException;
import cobol.commons.order.CommonOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Schedulers are an image of the queue of corresponding stand
 */
public class Scheduler extends Thread {


    private CommunicationHandler communicationHandler;

    // incoming orders
    private List<CommonOrder> inc = new LinkedList<>();


    // Stand properties

    private String standName;
    private List<CommonFood> menu;
    private double lon;
    private double lat;


    // Scheduler properties
    private int subscriberId;
    private String brandName;

    private ObjectMapper objectMapper;
    public Scheduler(List<CommonFood> menu, String standName, String brandName, double lat, double lon, CommunicationHandler communicationHandler) throws CommunicationException {
        // set stand properties
        this.menu = menu;
        this.standName = standName;
        this.brandName = brandName;
        this.lat= lat;
        this.lon=lon;

        this.communicationHandler=communicationHandler;
        // retrieve subscriber id
        subscriberId= communicationHandler.getSubscriberIdFromEC();

        // register to orders from a brand
        communicationHandler.registerToOrdersFromBrand(subscriberId, brandName);

    }

    /**
     * Update menuItem of the scheduler
     *
     *  @param mi  new item
     * @param mi2 old item
     * @return true if the item to update was present in the menu
     * false if the item to update was not present in the menu
     */
    static boolean updateItem(CommonFood mi, CommonFood mi2) {
        if (mi.getName().equals(mi2.getName())) {
            mi2.setPreparationTime(mi.getPreparationTime());
            mi2.setPrice(mi.getPrice());
            mi2.setStock(mi.getStock() + mi2.getStock());
            return true;
        }
        else return false;
    }

    public void pollEvents() {

        try {

            List<Event> eventList=communicationHandler.pollEventsFromEC(subscriberId);

            for (Event event : eventList) {
                JSONObject eventData = event.getEventData();
                JSONObject menuchange = (JSONObject) eventData.get("menuItem");
                objectMapper.registerModule(new JavaTimeModule());
                CommonFood mi = objectMapper.readValue(menuchange.toString(), CommonFood.class);
                for (CommonFood mi2 : menu) {
                    updateItem(mi, mi2);
                }
            }
        } catch (JsonProcessingException e) {
            System.err.println(e);
            e.printStackTrace();
        } catch (CommunicationException e) {
            e.printStackTrace();
        }
    }


    /**
     * Adds order to the scheduler queue
     *
     * @param o the order to be added
     */
    public void addOrder(CommonOrder o) {
        inc.add(o);
    }

    /**
     * Removes first order from schedule
     */
    public void orderDone() {
        inc.remove(0);
        System.out.println("Order done");
    }

    /**
     * Calculates scheduler queue time
     *
     * @return the scheduler queue time
     */
    public int timeSum() {
        if (inc.size() == 0){
            return 0;
        }
        else {
            return inc.get(inc.size() - 1).computeRemainingTime();
        }
    }

    /**
     * checks if a food item is present in the stand menu
     *
     * @param type: requested food item
     * @return true/false
     */
    public boolean checkType(String type) {
        for (CommonFood mi : menu) {
            if (mi.getName().equals(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes 1 (second) from the remaining time of the first scheduled order: the order that should be under preparation
     * TODO: remove 1 (second) from all orders that are flagged as "under preparation" (+ add flag for "preparation")
     */
    public void prepClock() {
        if (inc.size() == 0) {
            return;
        } else {
            if (inc.get(0).computeRemainingTime() < 0) {
                if (inc.size() == 0) return;
                orderDone();
            }
        }
    }

    public void run() {
        while (true) {
            try {
                TimeUnit.MINUTES.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            prepClock();
        }
    }
    /**
     * Calculate preparation time of an item that is available on the menu of the scheduler
     *
     * @param foodname name of item
     * @return preparation time of that item
     */
    public int getPreptime(String foodname) {
        for (CommonFood m : menu) {
            if (m.getName().equals(foodname)) return m.getPreparationTime();
        }
        return -1;
    }

    /**
     * Removes an item from the menu
     *
     * @param mi the item to remove from the menu
     */
    public void removeItem(CommonFood mi){
        this.menu.remove(mi);
    }
    public double getLon() {
        return this.lon;
    }

    public void setLon(double l) {
        this.lon = l;
    }

    public double getLat() {
        return this.lat;
    }

    public void setLat(double l) {
        this.lat = l;
    }

    public List<CommonFood> getMenu() {
        return this.menu;
    }


    public String getStandName() {
        return this.standName;
    }

    public String getBrand() {
        return this.brandName;
    }

    public int getSubscriberId() {
        return subscriberId;
    }
}
