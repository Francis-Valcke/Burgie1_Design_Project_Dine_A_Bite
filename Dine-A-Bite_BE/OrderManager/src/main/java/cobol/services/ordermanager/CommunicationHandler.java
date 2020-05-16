package cobol.services.ordermanager;

import cobol.commons.BetterResponseModel;
import cobol.commons.CommonFood;
import cobol.commons.Event;
import cobol.commons.order.CommonOrder;
import cobol.commons.order.SuperOrder;
import cobol.commons.order.SuperOrderRec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.naming.CommunicationException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CommunicationHandler {

    @Autowired
    ConfigurationBean configurationBean;

    @Autowired
    CommunicationHandler communicationHandler;

    private RestTemplate template;
    private HttpHeaders headers;

    public CommunicationHandler() {

        //Configure all rest requests that will be sent by the CommunicationHandler
        template = new RestTemplate();
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJPcmRlck1hbmFnZXIiLCJyb2xlcyI6WyJST0xFX0FQUExJQ0FUSU9OIl0sImlhdCI6MTU4NDkxMTY3MSwiZXhwIjoxNzQyNTkxNjcxfQ.VmujsURhZaXRp5FQJXzmQMB-e6QSNF-OyPLeMEMOVvI");

    }


    // ---- Communication with stand manager ---- //

    /**
     * This method will issue HTTP request to StandManager.
     *      * Returns a String assuming the caller knows what to expect from the response.
     *      * Ex. JSONArray or JSONObject
     *
     * @param path       Example: "/..."
     * @param jsonObject JSONObject or JSONArray format
     * @param params Parameters to put in the http request
     * @return response as String
     * @throws Throwable
     */
    public String sendRestCallToStandManager(String path, String jsonObject, Map<String, String> params) throws Throwable {
        if (configurationBean.isUnitTest()) {
            if (path.equals("/newStand")) return "{\"added\": true}";
            if(path.equals("/deleteScheduler")) return "{\"del\": true}";
            else return "";
        }


        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", OrderManager.authToken);

        HttpEntity<String> request = new HttpEntity<>(jsonObject, headers);
        String uri = OrderManager.SMURL + path;

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(uri);
        if (params != null) {
            for (String s : params.keySet()) {
                try {
                    builder.queryParam(s, URLEncoder.encode(params.get(s), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        String response= template.postForObject(builder.toUriString(), request, String.class);
        BetterResponseModel<String> responseModel=null;
        if(response!=null){
            ObjectMapper om = new ObjectMapper();
            responseModel= om.readValue(response, new TypeReference<BetterResponseModel<String>>() {});
            if(responseModel.getStatus().equals(BetterResponseModel.Status.OK)){
                return responseModel.getPayload();
            }
            else{
                throw responseModel.getException();
            }
        }
        else{
            return "";
        }

    }

    /**
     * This function will pass the superorder to the standmanager and return seperate orders
     * @param superOrder Super order to get recommendations for
     * @return JSONArray of  JSONObject with field "recommendations" and a field "order" similar to return of placeOrder
     *         recommendation field will be a JSONArray of Recommendation object
     * @throws Throwable throw exception to frontend.
     */
    public List<SuperOrderRec> getSuperRecommendationFromSM(SuperOrder superOrder) throws Throwable {

        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", OrderManager.authToken);

        HttpEntity<SuperOrder> request = new HttpEntity<>(superOrder, headers);
        String uri = OrderManager.SMURL + "/getSuperRecommendation";

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(uri);


        String responseString= template.postForObject(builder.toUriString(), request, String.class);
        ObjectMapper mapper= new ObjectMapper();
        BetterResponseModel<List<SuperOrderRec>> responseModel= mapper.readValue(responseString, new TypeReference<BetterResponseModel<List<SuperOrderRec>>>() {});
        if(responseModel!=null){
            if(responseModel.isOk()){
                return responseModel.getPayload();
            }
            else{
                throw responseModel.getException();
            }
        }
        else{
            throw new NullPointerException("empty response from standmanager for superorder recommendations");
        }
    }


    // ---- Communication with event channel ---- //


    /**
     * Sends get request to event channel in order to retrieve a subscriber id
     *
     * @return subscriber id used to poll events
     * @throws CommunicationException thrown when eventchannel can't be reached
     */
    public int getSubscriberIdFromEC() throws CommunicationException {
        if (configurationBean.isUnitTest()) return 0;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", OrderManager.authToken);
        String uri = OrderManager.ECURL + "/registerSubscriber";
        //String uri = "http://cobol.idlab.ugent.be:8093/registerSubscriber";
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        if (response.getBody() != null) {
            return Integer.parseInt(response.getBody());
        } else {
            throw new CommunicationException("Could not retrieve a subscriber ID from the EventChannel");
        }
    }

    public List<Event> pollEventsFromEC(int subscriberId) throws JsonProcessingException, CommunicationException {
        if (configurationBean.isUnitTest()) return new ArrayList<>();

        String uri = OrderManager.ECURL + "/events";

        // Headers and URL
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
                .queryParam("id", subscriberId);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", OrderManager.authToken);
        HttpEntity httpEntity = new HttpEntity(headers);

        // Send Request
        ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, String.class);


        // parse BetterResponseModel from stringresponse
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        BetterResponseModel<List<Event>> responseModel= objectMapper.readValue(response.getBody(), new TypeReference<BetterResponseModel<List<Event>>>() {});

        if(responseModel!=null){
            if(responseModel.isOk()){
                return responseModel.getPayload();
            }
        }
        throw new CommunicationException("EventChannel cannot be reached while polling events in ordermanager");
    }

    /**
     * Unsubscribe from channel of a certain order
     *
     * @param subscriberId subscriber id from ordermanager
     * @param orderId      order id from order to unsubscribe from
     */
    public void deregisterFromOrder(int subscriberId, int orderId) {
        if (configurationBean.isUnitTest()) return;

        String uri = OrderManager.ECURL + "/deregisterSubscriber";
        String channelId = "o" + orderId;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", OrderManager.authToken);
        HttpEntity httpEntity = new HttpEntity(httpHeaders);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
                .queryParam("id", subscriberId)
                .queryParam("type", channelId);
        ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, String.class);
    }


    /**
     * Subscribe to a channel of a certain order
     *
     * @param subscriberId Subscriber id from the ordermanager
     * @param orderId      order id from order to subscribe to
     */
    public void registerOnOrder(int subscriberId, int orderId) {
        if (configurationBean.isUnitTest()) return;

        String uri = OrderManager.ECURL + "/registerSubscriber/toChannel";
        String channelId = "o_" + orderId;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", OrderManager.authToken);
        HttpEntity httpEntity = new HttpEntity(httpHeaders);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
                .queryParam("id", subscriberId)
                .queryParam("type", channelId);
        ResponseEntity<String> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, String.class);
    }

    public String publishConfirmedStand(CommonOrder updatedOrder, String standName, String brandName) throws JsonProcessingException {
        if (configurationBean.isUnitTest()) return "";

        // Create event for eventchannel
        JSONObject orderJson = new JSONObject();
        orderJson.put("order", updatedOrder);
        List<String> types = new ArrayList<>();
        types.add("o_" + updatedOrder.getId());
        types.add("s_" + standName + "_" + brandName);
        Event e = new Event(orderJson, types, "Order");

        // Send Request
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String jsonString = objectMapper.writeValueAsString(e);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", OrderManager.authToken);
        RestTemplate restTemplate = new RestTemplate();
        String uri = OrderManager.ECURL + "/publishEvent";

        HttpEntity<String> entity = new HttpEntity<>(jsonString, headers);
        return restTemplate.postForObject(uri, entity, String.class);

    }



    // ---- TODO te verwijderen? ----//

    /**
     * Waarom zouden de ordermanager en de standmanager communiceren via de eventchannel?
     * <p>
     * publish changed menuItem Event for schedulers
     *
     * @param mi    MenuItem
     * @param brand brandname
     * @throws JsonProcessingException
     */
    public void publishMenuChange(CommonFood mi, String brand) throws JsonProcessingException {
        JSONObject itemJson = new JSONObject();
        itemJson.put("menuItem", mi);
        List<String> types = new ArrayList<>();
        types.add(brand);
        Event e = new Event(itemJson, types, "MenuItem");


        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String jsonString = objectMapper.writeValueAsString(e);
        String uri = OrderManager.ECURL + "/publishEvent";
        HttpEntity<String> entity = new HttpEntity<>(jsonString, headers);
        String response = template.postForObject(uri, entity, String.class);
        System.out.println(response);
    }



}
