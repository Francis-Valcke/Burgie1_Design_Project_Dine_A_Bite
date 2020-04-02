package cobol.services.standmanager;

import cobol.commons.CommonFood;
import cobol.commons.Event;
import cobol.commons.order.CommonOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * schedulers all have:
 * TODO: change "inc" to proper schedule
 */
public class Scheduler extends Thread {
    private List<CommonOrder> inc = new LinkedList<>();
    private List<CommonFood> menu;
    private String standName;
    private ObjectMapper objectMapper;
    private RestTemplate restTemplate;
    private HttpHeaders headers;
    private HttpEntity entity;
    // Coordinates of Stand
    private double lon;
    private double lat;
    private int subscriberId;
    private String brandName;

    public Scheduler(List<CommonFood> menu, String standName, String brandName, double lat, double lon) {
        this.menu = menu;
        this.standName = standName;
        this.brandName = brandName;
        this.lat= lat;
        this.lon=lon;
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        restTemplate = new RestTemplate();
        headers=new HttpHeaders();
        // subscribe to the channel of the brand
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJTdGFuZE1hbmFnZXIiLCJyb2xlcyI6WyJST0xFX0FQUExJQ0FUSU9OIl0sImlhdCI6MTU4NDkxMTc0MywiZXhwIjoxNzQyNTkxNzQzfQ.tuteSFjRJdQDMja2ioV0eiHvuCu0lkuS94zyhw9ZLIk");
        entity = new HttpEntity<>(headers);
        String uri = StandManager.ECURL + "/registerSubscriber";
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        this.subscriberId = Integer.valueOf(response.getBody());
        String channelId = brandName;
        uri = StandManager.ECURL + "/registerSubscriber/toChannel";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
                .queryParam("id", this.subscriberId)
                .queryParam("type", channelId);

        ResponseEntity<String> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

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
        String uri = StandManager.ECURL + "/events";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
                .queryParam("id", this.subscriberId);
        ResponseEntity<String> response = this.restTemplate.exchange(builder.toUriString(), HttpMethod.GET, this.entity, String.class);
        try {
            JSONObject responseObject = this.objectMapper.readValue(response.getBody(), JSONObject.class);
            String details = (String) responseObject.get("details");
            JSONParser parser = new JSONParser();
            JSONArray detailsJSON = (JSONArray) parser.parse(details);
            List<Event> eventList = objectMapper.readValue(details, new TypeReference<List<Event>>() {
            });
            for (int i = 0; i < detailsJSON.size(); i++) {
                JSONObject e = (JSONObject) detailsJSON.get(i);

                JSONObject eventData = (JSONObject) e.get("eventData");
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
