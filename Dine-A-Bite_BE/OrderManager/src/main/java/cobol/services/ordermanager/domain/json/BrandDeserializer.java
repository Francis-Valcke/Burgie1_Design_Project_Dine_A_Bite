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

public class BrandDeserializer extends JsonDeserializer<Brand> {

    @Override
    public Brand deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        JsonNode node = p.getCodec().readTree(p);
        Brand brand = new Brand(node.get("name").asText());

        ObjectMapper objectMapper = new ObjectMapper();
        ctxt.setAttribute("brand", brand);


        node.get("stand").forEach(stand -> {
            try {

                brand.getStandList().add(objectMapper.readValue(stand.traverse(), Stand.class));

            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        int standCounter = 1;
        //Now do all the necessary internal linking to be able to persist into the database
        for (Stand stand : brand.getStandList()) {
            //Add this brand to the stand
            stand.setBrand(brand);

            //If the name of the stand is empty, use the name of the brand
            if ((stand.getStandId().getName() == null || stand.getName().equals(""))) {
                stand.setName(brand.getName() + " " + (standCounter++));
            }

            stand.getFoodList().forEach(food -> food.setStand(stand));

        }

        return brand;
    }
}
