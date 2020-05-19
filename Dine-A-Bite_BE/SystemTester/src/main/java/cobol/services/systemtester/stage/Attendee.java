package cobol.services.systemtester.stage;

import cobol.commons.BetterResponseModel;
import cobol.commons.Event;
import cobol.commons.order.CommonOrder;
import cobol.commons.order.Recommendation;
import cobol.services.systemtester.ServerConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.RequestBodyEntity;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * An attendee should be able to do the following:
 * -Request menu
 * -Place order
 */
@Data
public class Attendee {

    private static int idCounter = 0;
    private static final double walkingSpeed = 40; //meters in a minute
    private final int id;
    private String token;
    private final double latitude;
    private final double longitude;
    private double orderTime;
    private double initialOrderTime;
    private double orderReadyTime;
    private double walkingStartTime;
    private JSONArray recommendations;
    private int orderid;
    private Logger log;
    private JSONObject order;
    private boolean walking;


    public Attendee(double latitude, double longitude) {
        walking=false;
        this.id = idCounter;
        idCounter++;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * create user
     * @return single
     */
    public Single<JSONObject> create() {

        //Prepare body
        JSONObject requestBody = new JSONObject();
        requestBody.put("username", "a" + id);
        requestBody.put("password", id);

        //Create single from request
        return Single.create((SingleOnSubscribe<JSONObject>) emitter -> {

            try {
                JSONObject responseBody = Unirest.post(ServerConfig.ACURL + "/createUser")
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

    /**
     * authenticate user
     * @return single
     */
    public Single<JSONObject> authenticate() {

        //Prepare body
        JSONObject requestBody = new JSONObject();
        requestBody.put("username", "a" + id);
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

    /**
     * getUser
     * @return single
     */
    public Single<JSONObject> getUser() {

        return Single.create((SingleOnSubscribe<JSONObject>) emitter -> {

            try {
                JSONObject responseBody = Unirest.get(ServerConfig.ACURL + "/user")
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

    /**
     * fetch menu
     * @return single with menu
     */
    public Single<JSONObject> getGlobalMenu() {

        return Single.create((SingleOnSubscribe<JSONObject>) emitter -> {

            try {
                JSONObject responseBody = Unirest.get(ServerConfig.OMURL + "/menu")
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

    /**
     * choose random item from menu
     * @param response menu as json
     * @param itemCount amount to order
     */
    public void chooseOrder(JSONObject response, int itemCount){
        //Prepare order
        Random random = new Random();
        JSONObject order = new JSONObject();
        JSONArray orderItems = new JSONArray();
        JSONArray items = (JSONArray) response.get("payload");
        //Random brandname to choose items from
        String brandName = items.getJSONObject(random.nextInt(items.length())).getString("brandName");
        while (!brandName.contains("_test"))
            brandName = items.getJSONObject(random.nextInt(items.length())).getString("brandName");
        List<JSONObject> itemsList = new ArrayList<>();
        items.forEach(o -> itemsList.add((JSONObject) o));
        String finalBrandName = brandName;
        List<JSONObject> filteredItemsList = itemsList.stream().filter(o -> o.getString("brandName").equals(finalBrandName)).collect(Collectors.toList());
        //Choose items at random from the chose brand
        for (int i = 0; i < itemCount; i++) {
            JSONObject selectedItem = filteredItemsList.get(random.nextInt(filteredItemsList.size()));
            JSONObject orderItem = new JSONObject();
            orderItem.put("foodName", selectedItem.getString("name"));
            orderItem.put("amount", itemCount);
            orderItems.put(orderItem);
        }
        //Complete the order
        order.put("latitude", this.latitude);
        order.put("longitude", this.longitude);
        order.put("orderStatus", "SEND");
        order.put("brandName", brandName);
        order.put("orderItems", orderItems);

        this.order=order;
    }

    /**
     * ask for recommendation
     * @param recType recommendation type
     * @return single
     */
    public Single<JSONObject> placeOrder(CommonOrder.RecommendType recType) {
        order.put("recType", recType);
        return Single.create((SingleOnSubscribe<JSONObject>) emitter -> {
            try {
                //Send order
                JSONObject responseBody =  Unirest.post(ServerConfig.OMURL + "/placeOrder")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .body(order)
                        .asJson()
                        .getBody()
                        .getObject();
                if (responseBody.get("payload")==null) emitter.onSuccess(responseBody);
                JSONObject object = (JSONObject) responseBody.get("payload");
                recommendations = object.getJSONArray("recommendations");
                orderid = object.getJSONObject("order").getInt("id");
                emitter.onSuccess(responseBody);

            } catch (JSONException e) {
                emitter.onError(e);
            }
        }).observeOn(Schedulers.io());
    }

    /**
     * this method is just to finf the nearest stand, no followup is made on the actual order
     * @return
     */
    public Single<JSONObject> orderForNearestStand() {
        order.put("recType", CommonOrder.RecommendType.DISTANCE);
        return Single.create((SingleOnSubscribe<JSONObject>) emitter -> {
            try {
                //Send order
                JSONObject responseBody =  Unirest.post(ServerConfig.OMURL + "/placeOrder")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .body(order)
                        .asJson()
                        .getBody()
                        .getObject();
                if (responseBody.get("payload")==null) emitter.onSuccess(responseBody);
                JSONObject object = (JSONObject) responseBody.get("payload");
                walking=true;
                recommendations = object.getJSONArray("recommendations");
                emitter.onSuccess(responseBody);

            } catch (JSONException e) {
                emitter.onError(e);
            }
        }).observeOn(Schedulers.io());
    }

    /**
     * look at recommendations and choose rank 1 recommendation
     * @return uri string with stand and brand for confirmstand
     */
    public String getRecommendedStand() {
        //look for closest recommendation
        int index = 0;
        for (int i=0;i<recommendations.length();i++){
            if ((Integer)((JSONObject)recommendations.get(0)).get("rank")==1) index=i;
        }
        double distance = (double) ((JSONObject) recommendations.get(0)).get("distance");
        orderReadyTime = orderTime+((double) ((Integer) ((JSONObject) recommendations.get(0)).get("timeEstimate"))) / 60;
        //calculate time to start walking to order (time in minutes after ordering)
        double orderDistance = (double) ((JSONObject) recommendations.get(index)).get("distance");
        walkingStartTime =  orderReadyTime - orderDistance / walkingSpeed;
        if (walkingStartTime < 0) walkingStartTime = 0;
        walkingStartTime+=orderTime;
        //return parameterstring
        String brand = (String) ((JSONObject) recommendations.get(index)).get("brandName");
        String stand = (String) ((JSONObject) recommendations.get(index)).get("standName");
        return stand.concat("&brandName=").concat(brand);
    }

    /**
     * confirm stand
     * @return single
     */
    public Single<JSONObject> confirmStand() {
        String url = ServerConfig.OMURL + "/confirmStand?standName=" + getRecommendedStand() + "&orderId=" + orderid;
        String finalUrl = url.replace(' ', '+');
        return Single.create((SingleOnSubscribe<JSONObject>) emitter -> {
            try {
                //Send order
                Unirest.get(finalUrl)
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .asJson()
                        .getBody()
                        .getObject();
                log.info("Confirm order: " + orderid);
                JSONObject o = new JSONObject();
                emitter.onSuccess(o.put("succes", 1));

            } catch (UnirestException | JSONException e) {
                emitter.onError(e);
            }

        }).observeOn(Schedulers.io());
    }

    /**
     * setup attendee by creating and authenticating user
     * @param log logger
     */
    public void setup(Logger log) {
        this.log = log;
        this.orderid = 0;
        create().subscribe(
                o -> authenticate().subscribe(
                        auth -> log.info("User " + getId() + " created and authenticated with token: " + auth.getJSONObject("details").getString("token")),
                        throwable -> log.error(throwable.getMessage())
                ),
                throwable -> log.error(throwable.getMessage())
        );


    }
    public void reset(){
        setOrdertime(initialOrderTime);
        walking=false;
    }

    /**
     * in case of systemOff, attendee orders twice (to same stand), once to find stand and once to order
     * @param time
     */
    public void setOrdertime(double time) {
        if (time < 1) time = 1;
        else if (time > 119) time = 119;
        this.orderTime = time;
        this.initialOrderTime=time;
    }
    public void setNewOrdertime(double time) {
        this.orderTime = time;
    }

    /**
     * Returns queue time if attendee ordered at stand
     * @return
     */
    public double getQueueTime(){
        return orderReadyTime - orderTime;
    }
    public double getOrderReadyTime(){
        return orderReadyTime;
    }
    public double getBetweenOrderTime(){
        return orderTime-initialOrderTime;
    }
    public double getTotalTime(){
        return orderReadyTime-initialOrderTime;
    }

    public double getWalkingTime() {
        return orderReadyTime - walkingStartTime;
    }

    /**
     * time before attendee walks to stand, only applicable if he orders before going to stand
     * @return
     */
    public double getWaitingTime() {
        return walkingStartTime-orderTime;
    }
    public double getWalkingStartTime(){
        return walkingStartTime;
    }

    public int getOrderid() {
        return this.orderid;
    }

    public double getOrderTime() {
        return this.orderTime;
    }

    public int getId() {
        return this.id;
    }
    public boolean getWalking(){
        return walking;
    }
}
