package cobol.services.standmanager;


import cobol.commons.Event;
import cobol.commons.exception.CommunicationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;


@Service
public class CommunicationHandler {

    public CommunicationHandler() {
    }


    // ---- Communication with EventChannel ---- //

    /**
     * Sends get request to event channel in order to retrieve a subscriber id
     *
     * @return subscriber id used to poll events
     * @throws CommunicationException thrown when eventchannel can't be reached
     */
    public static int getSubscriberIdFromEC() throws CommunicationException {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", StandManager.authToken);
        String uri = StandManager.ECURL + "/registerSubscriber";
        //String uri = "http://cobol.idlab.ugent.be:8093/registerSubscriber";
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        if (response.getBody() != null) {
            return Integer.parseInt(response.getBody());
        } else {
            throw new CommunicationException("Could not retrieve a subscriber ID from the EventChannel");
        }
    }


    public static void registerToOrdersFromBrand(int subscriberId, String brandName) {

        RestTemplate restTemplate= new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", StandManager.authToken);
        HttpEntity httpEntity= new HttpEntity(headers);

        String uri = StandManager.ECURL + "/registerSubscriber/toChannel";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
                .queryParam("id", subscriberId)
                .queryParam("type", brandName);

        restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, String.class);

    }

    public static List<Event> pollEventsFromEC(int subscriberId) throws JsonProcessingException, ParseException {
        RestTemplate restTemplate= new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", StandManager.authToken);
        HttpEntity httpEntity= new HttpEntity(headers);
        String uri = StandManager.ECURL + "/events";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
                .queryParam("id", subscriberId);
        ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, String.class);


        // parse events
        ObjectMapper objectMapper= new ObjectMapper();
        JSONObject responseObject = objectMapper.readValue(response.getBody(), JSONObject.class);
        String details = (String) responseObject.get("details");
        return objectMapper.readValue(details, new TypeReference<List<Event>>() {});

    }
}
