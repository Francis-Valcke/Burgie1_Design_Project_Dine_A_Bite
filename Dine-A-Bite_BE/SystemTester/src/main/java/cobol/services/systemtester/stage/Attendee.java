package cobol.services.systemtester.stage;

import cobol.commons.security.exception.DuplicateUserException;
import cobol.services.systemtester.ServerConfig;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.reactivex.Emitter;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.SingleSubject;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
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
            JSONObject selectedItem = items.getJSONObject(random.nextInt(filteredItemsList.size()));
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
                System.out.println(responseBody.toString());
                emitter.onSuccess(responseBody);

            } catch (UnirestException | JSONException e) {
                emitter.onError(e);
            }

        }).observeOn(Schedulers.io());

    }

    public void setup(Logger log){
        this.log = log;
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

    public double getOrderTime(){
        return this.orderTime;
    }
    public int getId() {
        return this.id;
    }
}
