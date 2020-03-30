package cobol.services.dataset.domain.json;

import cobol.services.dataset.domain.entity.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.HashMap;

public class BrandDeserializer extends JsonDeserializer<Brand> {

    @Override
    public Brand deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = p.getCodec().readTree(p);

        Brand brand = new Brand(node.get("name").asText());

        node.get("stand").forEach(stand -> {
            try {
                brand.getStand().add(objectMapper.readValue(stand.traverse(), Stand.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        int standCounter = 1;
        //Now do all the necessary internal linking to be able to persist into the database
        for (Stand stand : brand.getStand()) {

            //If the name of the stand is empty, use the name of the brand
            if ((stand.getName() == null || stand.getName().equals(""))) {
                stand.setName(brand.getName() + " " + (standCounter++));
            }

            //Add this brand to the stand
            stand.setBrand(brand);

            //Add all of the food items that are not yet in the set
            //The food objects in this list will be persisted
            //It is important to further use these same objects in all other references
            stand.getFood().stream()
                    .filter(food -> !brand.getFood().contains(food))
                    .forEach(food -> {
                        //add food item to the brand menu
                        brand.getFood().add(food);
                        food.setBrand(brand);
                    });

            stand.getFood().forEach(food -> {

                //Retrieve the food object of the brand by comparing the food object from the stand
                Food correctFood = brand.getFood().stream()
                        .filter(f -> f.equals(food))
                        .findFirst()
                        .orElse(null);

                //Create new stock item and add to the stand
                stand.getStock().add(new Stock(correctFood, stand, food.getStock()));
            });
        }

        return brand;
    }
}
