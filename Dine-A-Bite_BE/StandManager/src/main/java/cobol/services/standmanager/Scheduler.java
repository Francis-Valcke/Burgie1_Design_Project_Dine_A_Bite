package cobol.services.standmanager;

import cobol.commons.CommonFood;
import cobol.commons.Event;
import cobol.commons.exception.CommunicationException;
import cobol.commons.order.CommonOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * schedulers all have:
 * TODO: change "inc" to proper schedule
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
     * update menuItem
     *  @param mi  new item
     * @param mi2 old item
     * @return
     */
    static boolean updateItem(CommonFood mi, CommonFood mi2) {
        if (mi.getName().equals(mi2.getName())) {
            if (mi.getPreparationTime() >= 0) mi2.setPreparationTime(mi.getPreparationTime());
            if (mi.getPrice().compareTo(BigDecimal.ZERO) >= 0) mi2.setPrice(mi.getPrice());
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
                CommonFood mi = objectMapper.readValue(menuchange.toString(), CommonFood.class);
                for (CommonFood mi2 : menu) {
                    updateItem(mi, mi2);
                }
            }
        } catch (JsonProcessingException | ParseException e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }



    /**
     * schedules order: add new order to the end of schedule
     */
    public void addOrder(CommonOrder o) {
        inc.add(o);
    }

    /**
     * removes first order from schedule
     */
    public void orderDone() {
        inc.remove(0);
        System.out.println("Order done");
    }

    /**
     * calculates total time to end of schedule
     *
     * @return this time
     */
    public int timeSum() {
        int s = 0;
        for (int i = 0; i < inc.size(); i++) {
            s += inc.get(i).computeRemainingTime();
        }
        System.out.println(s);
        return s;
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
     * gives preptime of item in scheduler
     *
     * @param foodname name of item
     * @return preptime
     */
    public int getPreptime(String foodname) {
        for (CommonFood m : menu) {
            if (m.getName().equals(foodname)) return m.getPreparationTime();
        }
        return -1;
    }
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



}
