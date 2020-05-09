package cobol.services.systemtester.stage;

import cobol.commons.CommonFood;
import cobol.commons.order.CommonOrder;
import cobol.services.systemtester.EventSimulation;
import cobol.services.systemtester.ServerConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class Stand extends Thread{
    private static int idCounter = 0;
    private int id;
    private String token;
    private double latitude;
    private double longitude;
    private String standName;
    private String brandName;
    private List<CommonFood> menu = new ArrayList<>();
    private List<CommonOrder> orders = new ArrayList<>();
    private int subscriberId;
    private Logger log;


    public Stand(double latitude,double longitude ) {
        this.id = idCounter;
        idCounter++;
        this.longitude=longitude;
        this.latitude=latitude;
    }


    public Stand(){
        this.id = idCounter;
        idCounter++;
    }

    public Stand(String standName, String brandName, double latitude, double longitude, List<CommonFood> menu){
        this.id = idCounter;
        idCounter++;
        this.standName = standName ;
        this.brandName = brandName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.menu = menu;
    }

    public Stand(String standName, String brandName, double latitude, double longitude) {
        this.id = idCounter;
        idCounter++;
        this.standName = standName ;
        this.brandName = brandName;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public Single<JSONObject> create() {
        //Prepare body
        JSONObject requestBody = new JSONObject();
        requestBody.put("username", id);
        requestBody.put("password", id);

        //Create single from request
        return Single.create((SingleOnSubscribe<JSONObject>) emitter -> {

            try {
                JSONObject responseBody = Unirest.post(ServerConfig.ACURL + "/createStandManager")
                        .header("Content-Type", "application/json")
                        .body(requestBody)
                        .asJson()
                        .getBody()
                        .getObject();

                emitter.onSuccess(responseBody);

            } catch (UnirestException | JSONException e) {
                emitter.onError(e);
            }

        }).observeOn(Schedulers.io());
    }
    public Single<JSONObject> authenticate() {

        //Prepare body
        JSONObject requestBody = new JSONObject();
        requestBody.put("username", id);
        requestBody.put("password", id);

        //Create single from request
        return Single.create((SingleOnSubscribe<JSONObject>) emitter -> {

            try {
                JSONObject responseBody = Unirest.post(ServerConfig.ACURL + "/authenticate")
                        .header("Content-Type", "application/json")
                        .body(requestBody)
                        .asJson()
                        .getBody()
                        .getObject();

                token = responseBody.getJSONObject("details").getString("token");

                emitter.onSuccess(responseBody);

            } catch (UnirestException | JSONException e) {
                emitter.onError(e);
            }


        }).observeOn(Schedulers.io());

    }
    public Single<JSONObject> verify(){
        String url = ServerConfig.OMURL + "/verify?brandName=" + brandName
                + "&standName=" + standName;
        url = url.replace(' ', '+');
        //Create single from request
        String finalUrl = url;
        return Single.create((SingleOnSubscribe<JSONObject>) emitter -> {

            try {
                JSONObject responseBody = Unirest.get(finalUrl)
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .asJson()
                        .getBody()
                        .getObject();

                //responseBody.getJSONObject("details").getString("");

                emitter.onSuccess(responseBody);

            } catch (UnirestException | JSONException e) {
                emitter.onError(e);
            }


        }).observeOn(Schedulers.io());
    }
    public Single<Integer> subscribe(){
        return Single.create((SingleOnSubscribe<Integer>) emitter -> {

            /*
            //this does not seem to work when response is integer
            GetRequest responseBody =  Unirest.get(ServerConfig.ECURL + "/registerSubscriber")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token);

             */
            RestTemplate template = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);

            String uri = ServerConfig.ECURL + "/registerSubscriber";
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Integer> response = template.exchange(
                    uri, HttpMethod.GET, entity, Integer.class);
            subscriberId=response.getBody();
            emitter.onSuccess(response.getBody());
        }).observeOn(Schedulers.io());
    }
    public Single<String> subscribeToChannel(){
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            String url2 = ServerConfig.ECURL + "/registerSubscriber/toChannel?type=s_" + standName
                    + "_" + brandName + "&id=" + subscriberId;
            url2 = url2.replace(' ', '+');
            try {
                Unirest.get(url2)
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .asJson()
                        .getBody();
                emitter.onSuccess("subscribed");
            } catch (JSONException e) {
                emitter.onError(e);
            }
        }).observeOn(Schedulers.io());
    }
    public Single<JSONObject> addstand(){
        //Prepare body
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", standName);
        requestBody.put("brandName", brandName);
        requestBody.put("latitude", latitude);
        requestBody.put("longitude", longitude);
        ArrayList<JSONObject> m = new ArrayList<>();
        for (CommonFood f:menu){
            JSONObject o = new JSONObject();
            o.put("name", f.getName());
            o.put("price", f.getPrice());
            o.put("preparationTime", f.getPreparationTime());
            o.put("description", f.getDescription());
            o.put("category", f.getCategory());
            o.put("stock",f.getStock());
            m.add(o);
        }
        //JSONArray json = new JSONArray(m);
        requestBody.put("menu",m);

        //Create single from request
        return Single.create((SingleOnSubscribe<JSONObject>) emitter -> {

            try {
                JSONObject responseBody = Unirest.post(ServerConfig.OMURL + "/addStand")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .body(requestBody)
                        .asJson()
                        .getBody()
                        .getObject();
                emitter.onSuccess(responseBody);


            } catch (UnirestException | JSONException e) {
                emitter.onError(e);
            }

        }).observeOn(Schedulers.io());
    }
    public void setup(Logger log){
        this.log=log;
        create().subscribe(
                o -> authenticate().subscribe(
                        auth -> verify().subscribe(
                                v -> subscribe().subscribe(
                                        sub -> subscribeToChannel().subscribe(
                                                subto -> addstand().subscribe(
                                                        add -> log.info("Stand " + this.getStandName() + " added and authenticated with token: " + auth.getJSONObject("details").getString("token")),
                                                        throwable -> log.error(throwable.getMessage())
                                                ),
                                                throwable -> log.error(throwable.getMessage())
                                        ),
                                        throwable -> log.error(throwable.getMessage())
                                ),
                                throwable -> log.error(throwable.getMessage())
                        ),
                        throwable -> log.error(throwable.getMessage())
                ),
                throwable -> log.error(throwable.getMessage())
        );
    }
    public Single<JSONArray> pollEvents(){
        return Single.create((SingleOnSubscribe<JSONArray>) emitter -> {

            try {
                JsonNode responseBody = Unirest.get(ServerConfig.ECURL + "/events?id=" + subscriberId)
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .asJson()
                        .getBody();
                if (responseBody.getArray().length()==0)emitter.onSuccess(new JSONArray().put("no orders"));
                else {
                    for (int i=0;i<responseBody.getArray().length();i++){
                        ObjectMapper om = new ObjectMapper();
                        om.registerModule(new JavaTimeModule());
                        JSONObject eventJSON = (JSONObject) responseBody.getArray().get(i);
                        JSONObject eventData = (JSONObject) eventJSON.get("eventData");
                        JSONObject orderJSON = (JSONObject) eventData.get("order");
                        CommonOrder order = om.readValue(orderJSON.toString(), CommonOrder.class);
                        orders.add(order);
                    }

                    emitter.onSuccess(responseBody.getArray());
                }


            } catch (UnirestException | JSONException e) {
                emitter.onError(e);
            }

        }).observeOn(Schedulers.io());
    }
    public void run(){
        int time=0;
        while(time<30){
            time++;
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pollEvents().subscribe(
                    o-> log.info("Orders polled"),
                    throwable -> log.error(throwable.getMessage())
            );
        }
        //receive orders
        //prepare orders
    }
    public Single<JSONObject> delete(){
        String url = ServerConfig.OMURL + "/deleteStand?brandName=" + brandName
                + "&standName=" + standName;
        url = url.replace(' ', '+');
        //Create single from request
        String finalUrl = url;
        return Single.create((SingleOnSubscribe<JSONObject>) emitter -> {

            try {
                JSONObject responseBody = Unirest.delete(finalUrl)
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .asJson()
                        .getBody()
                        .getObject();
                emitter.onSuccess(responseBody);

            } catch (UnirestException | JSONException e) {
                emitter.onError(e);
            }


        }).observeOn(Schedulers.io());
    }
    public void addMenuItem(CommonFood mi){
        menu.add(mi);
    }

    public List<CommonFood> getMenu(){
        return this.menu;
    }

    public String getStandName(){
        return this.standName;
    }

    public String getBrandName(){
        return this.brandName;
    }

    public double getLongitude(){
        return this.longitude;
    }

    public double getLatitude(){
        return this.latitude;
    }

    public List<CommonOrder> getOrders(){ return orders; }
    public void setLatitude(double latitude){
        this.latitude=latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
