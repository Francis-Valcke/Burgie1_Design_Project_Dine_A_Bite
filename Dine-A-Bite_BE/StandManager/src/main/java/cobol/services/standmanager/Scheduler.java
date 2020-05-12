package cobol.services.standmanager;

import cobol.commons.communication.response.BetterResponseModel;
import cobol.commons.domain.CommonFood;
import cobol.commons.domain.Event;
import cobol.commons.exception.CommunicationException;
import cobol.commons.domain.CommonOrder;
import cobol.commons.stub.Action;
import cobol.commons.stub.EventChannelStub;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.log4j.Log4j2;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * schedulers all have:
 * TODO: change "inc" to proper schedule
 */
@Log4j2
public class Scheduler extends Thread {


    private EventChannelStub eventChannelStub;

    // incoming orders
    private List<CommonOrder> inc = new LinkedList<>();


    // Stand properties

    private String standName;
    private List<CommonFood> menu;
    private double lon;
    private double lat;


    // Scheduler properties
    private boolean subscribed = false;
    private boolean registered = false;
    private int subscriberId;
    private String brandName;

    private ObjectMapper objectMapper;
    public Scheduler(List<CommonFood> menu, String standName, String brandName, double lat, double lon, EventChannelStub eventChannelStub) throws CommunicationException {
        // set stand properties
        this.menu = menu;
        this.standName = standName;
        this.brandName = brandName;
        this.lat= lat;
        this.lon=lon;

        this.eventChannelStub = eventChannelStub;

        // Retrieve subscriber id for this scheduler
        eventChannelStub.doOnAvailable(() -> {

            boolean success;

            try {
                log.info("Trying to register at event channel");

                int id = eventChannelStub.getRegisterSubscriber("");
                subscriberId = id;
                registered = true;
                success = true;
                log.info("Successfully registered with subscriber ID from event channel: ID = " + id);
            } catch (Exception e) {
                success = false;
                registered = false;
                log.info("Could not request subscriber ID from event channel.");
                log.debug("Could not request subscriber ID from event channel.", e);
            }

            return success;

        }, Action.PRIORITY_HIGHEST, true, true);

        // Register to orders from a brand
        eventChannelStub.doOnAvailable(() -> {

            boolean success;

            try {
                log.info("Trying to subscribe to brand " + brandName);

                eventChannelStub.getRegisterSubscriberToChannel(subscriberId,brandName);

                success = true;
                subscribed = true;
                log.info("Successfully subscribed to brand " + brandName);
            } catch (Exception e) {
                success = false;
                subscribed = false;

                log.info("Could not subscribe to brand " + brandName);
                log.debug("Could not subscribe to brand " + brandName, e);
            }

            return success;

        }, Action.PRIORITY_HIGH, true, true);

        eventChannelStub.doOnUnavailable(()->{
            subscribed = false;
            registered = false;
            return true;
        }, Action.PRIORITY_NORMAL, false, false);

    }

    /**
     * update menuItem
     *  @param mi  new item
     * @param mi2 old item
     * @return
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
            BetterResponseModel<List<Event>> model = eventChannelStub.getEvents(subscriberId);
            List<Event> events = model.getOrThrow();

            for (Event event : events) {
                JSONObject eventData = event.getEventData();
                JSONObject menuChange = (JSONObject) eventData.get("menuItem");
                objectMapper.registerModule(new JavaTimeModule());
                CommonFood mi = objectMapper.readValue(menuChange.toString(), CommonFood.class);
                for (CommonFood mi2 : menu) {
                    updateItem(mi, mi2);
                }
            }

        } catch (Throwable e) {
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


    public boolean isSubscribed() {
        return subscribed;
    }

    public boolean isRegistered() {
        return registered;
    }
}
