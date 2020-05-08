package cobol.services.systemtester.stage;

import cobol.services.systemtester.ServerConfig;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
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
@Log4j2
@Data
public class  Attendee{

    private static int idCounter = 0;
    private int id;
    private String token;
    private double latitude;
    private double longitude;
    private double orderTime;
    private JSONArray recommendations;
    private int orderid;
    private Logger log;


    public Attendee(double latitude,double longitude ) {
        this.id = idCounter;
        idCounter++;
        this.longitude=longitude;
        this.latitude=latitude;
    }



    public Single<JSONObject> create() {

        //Prepare body
        JSONObject requestBody = new JSONObject();
        requestBody.put("username", "a"+id);
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


    public Single<JSONObject> authenticate() {

        //Prepare body
        JSONObject requestBody = new JSONObject();
        requestBody.put("username", "a"+id);
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


    public Single<JSONArray> getGlobalMenu() {

        return Single.create((SingleOnSubscribe<JSONArray>) emitter -> {

            try {
                JSONArray responseBody = Unirest.get(ServerConfig.OMURL + "/menu")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .asJson()
                        .getBody()
                        .getArray();

                emitter.onSuccess(responseBody);

            } catch (UnirestException | JSONException e) {
                emitter.onError(e);
            }

        }).observeOn(Schedulers.io());

    }


    public Single<JSONObject> placeRandomOrder(JSONArray items, int itemCount) {
        //Prepare order
        Random random = new Random();
        JSONObject order = new JSONObject();
        JSONArray orderItems = new JSONArray();

        //Random brandname to choose items from
        String brandName = items.getJSONObject(random.nextInt(items.length())).getString("brandName");
        while(!brandName.contains("_test"))brandName = items.getJSONObject(random.nextInt(items.length())).getString("brandName");
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


        return Single.create((SingleOnSubscribe<JSONObject>) emitter -> {

            try {
                //Send order
                JSONObject responseBody = Unirest.post(ServerConfig.OMURL + "/placeOrder")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .body(order)
                        .asJson()
                        .getBody()
                        .getObject();
                recommendations=responseBody.getJSONArray("recommendations");
                orderid=responseBody.getJSONObject("order").getInt("id");
                emitter.onSuccess(responseBody);

            } catch (UnirestException | JSONException e) {
                emitter.onError(e);
            }

        }).observeOn(Schedulers.io());

    }
    public String getNearestStand(){

        String brand = (String)((JSONObject) recommendations.get(0)).get("brandName");
        String stand = (String)((JSONObject) recommendations.get(0)).get("standName");
        double distance = (double) ((JSONObject) recommendations.get(0)).get("distance");
        for (int i=1;i<recommendations.length();i++){
            if ((double)((JSONObject) recommendations.get(i)).get("distance")<distance){
                brand=(String)((JSONObject) recommendations.get(i)).get("brandName");
                stand=(String)((JSONObject) recommendations.get(i)).get("standName");
            };
        }
        return stand.concat("&brandName=").concat(brand);
    }
    public Single<JSONObject> confirmNearestStand(){
        String url = ServerConfig.OMURL + "/confirmStand?standName=" + getNearestStand() + "&orderId=" + orderid;
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
                log.info("confirm");
                JSONObject o = new JSONObject();
                emitter.onSuccess(o.put("succes",1));

            } catch (UnirestException | JSONException e) {
                emitter.onError(e);
            }

        }).observeOn(Schedulers.io());
    }

    public void setup(Logger log){
        this.log = log;
        this.orderid=0;
        create().subscribe(
                o -> log.info("User " + getId() + " created!"),
                throwable -> log.error(throwable.getMessage())
        );
        authenticate().subscribe(
                o -> log.info("User " + getId() + " authenticated with token: " + o.getJSONObject("details").getString("token")),
                throwable -> log.error(throwable.getMessage())
        );

    }
    public void setOrdertime(double time){
        if (time<1)time=1;
        this.orderTime=time;
    }
    public int getOrderid(){
        return this.orderid;
    }
    public double getOrderTime(){
        return this.orderTime;
    }
    public int getId() {
        return this.id;
    }
}
