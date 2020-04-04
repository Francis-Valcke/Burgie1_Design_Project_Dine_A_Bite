package cobol.services.ordermanager.domain.json;

import cobol.services.ordermanager.domain.entity.Brand;
import cobol.services.ordermanager.domain.entity.Stand;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class StandDeserializer extends JsonDeserializer<Stand> {

    @Override
    public Stand deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {


        Brand brand = (Brand) ctxt.getAttribute("brand");
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = p.getCodec().readTree(p);

        String name = node.get("name").asText();
        double longitude = node.get("lon").asDouble();
        double latitude = node.get("lat").asDouble();

        //Stand stand = new Stand(node.get("name").asText());




        return null;
    }
}
