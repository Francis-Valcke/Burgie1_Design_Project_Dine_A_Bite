package cobol.services.standmanager;

import cobol.commons.CommonFood;
import cobol.commons.Event;
import cobol.commons.exception.CommunicationException;
import cobol.commons.order.CommonOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * schedulers all have:
 * TODO: change "inc" to proper schedule
 */
public class Scheduler {


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
     * Updates order in queue with new state
     * @param orderId
     * @param state
     * @throws JsonProcessingException
     */
    public void updateOrder(int orderId, CommonOrder.State state) throws JsonProcessingException, ParseException {
        CommonOrder order=null;

        //look for order
        for (CommonOrder or : inc){
            if (or.getId()==orderId) order=or;
        }
        if (order==null) {
            //should not be possible
            System.out.println("Order not in scheduler");
            return;
        }
        boolean alreadyBegun=false; //if somehow standapp starts order twice, it wont restart timer
        if (order.getOrderState().equals(CommonOrder.State.BEGUN)){alreadyBegun=true;}
        order.setOrderState(state);
        //if order is ready or canceled, the order has to be removed. Queue isnt updated until another order starts preparing
        if (order.getOrderState().equals(CommonOrder.State.READY)||order.getOrderState().equals(CommonOrder.State.DECLINED) ){
            inc.remove(order);
            return;
        }
        //if order is under preparation, all other orders are affected
        if (order.getOrderState().equals(CommonOrder.State.BEGUN)&&(!alreadyBegun)) {
            SchedulerComparatorTime st = new SchedulerComparatorTime(new ArrayList<>(order.getOrderItems()));
            ZonedDateTime actualTime = ZonedDateTime.now(ZoneId.of("Europe/Brussels"));
            int queueTime = st.getLongestFoodPrepTime(this);
            order.setExpectedTime(actualTime.plusSeconds(queueTime));
            sendOrderStatusUpdate(order);
            updateQueue(queueTime);
        }
    }
    /**
     * updates expected times for orders depending on their positions in queue
     * @param queueTime time after orders under preparation are estimated to be ready
     */
    public void updateQueue(int queueTime) throws JsonProcessingException, ParseException {
        ZonedDateTime actualTime = ZonedDateTime.now(ZoneId.of("Europe/Brussels"));
        for (CommonOrder o : inc){
            if (o.getOrderState().equals(CommonOrder.State.BEGUN)) continue;
            SchedulerComparatorTime st2 = new SchedulerComparatorTime(new ArrayList<>(o.getOrderItems()));
            // queueTime of an order is sum of all previous queuetimes
            queueTime+=st2.getLongestFoodPrepTime(this);
            o.setExpectedTime(actualTime.plusSeconds(queueTime));
            sendOrderStatusUpdate(o);

        }

    }

    /**
     * send new expected times to EventChannel for attendee app and OrderManager to fetch
     * @param order CommonOrder
     * @return response
     * @throws JsonProcessingException
     */
    public String sendOrderStatusUpdate(CommonOrder order) throws JsonProcessingException, ParseException {
        ObjectMapper mapper = new ObjectMapper();
        // Create event for eventchannel
        JSONObject orderJson = new JSONObject();
        JSONParser parser = new JSONParser();
        orderJson = (JSONObject) parser.parse(order.toString());
        ArrayList<String> types = new ArrayList<>();
        types.add("s_" + standName + "_" + brandName);
        types.add("o_" + order.getId());
        Event e = new Event(orderJson, types, "Order");

        // Send Request
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String jsonString = objectMapper.writeValueAsString(e);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", StandManager.authToken);
        RestTemplate restTemplate = new RestTemplate();
        String uri = StandManager.ECURL + "/publishEvent";

        System.out.println("Send order status update: " + jsonString);
        HttpEntity<String> entity = new HttpEntity<>(jsonString, headers);
        return restTemplate.postForObject(uri, entity, String.class);


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
    /*
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
     */



    /**
     * schedules order: add new order to the end of schedule
     */
    public void addOrder(CommonOrder o) {
        inc.add(o);
    }

    /*
     * removes first order from schedule
     */
    /*
    public void orderDone() {
        inc.remove(0);
        System.out.println("Order done");
    }
    */

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

    /*
     * Removes 1 (second) from the remaining time of the first scheduled order: the order that should be under preparation
     *
     */
    /*
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
     */
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

    public int getSubscriberId() {
        return subscriberId;
    }
}
