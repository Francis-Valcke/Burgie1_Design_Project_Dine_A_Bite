package cobol.services.ordermanager;

import cobol.commons.CommonFood;
import cobol.commons.CommonStand;
import cobol.commons.exception.CommunicationException;
import cobol.services.ordermanager.domain.entity.Brand;
import cobol.services.ordermanager.domain.entity.Food;
import cobol.services.ordermanager.domain.entity.Stand;
import cobol.services.ordermanager.domain.repository.BrandRepository;
import cobol.services.ordermanager.domain.repository.FoodRepository;
import cobol.services.ordermanager.domain.repository.StandRepository;
import cobol.commons.exception.DoesNotExistException;
import cobol.commons.exception.DuplicateStandException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * This class puts JSON menus into the database and creates JSON menus from the databse and refreshes them everytime a change is made
 * saving the menus as JSON files makes it so they dont have to be remade every call
 */
@Service
@Getter
public class MenuHandler {


    @Autowired
    private StandRepository standRepository;
    @Autowired
    private FoodRepository foodRepository;
    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CommunicationHandler communicationHandler;

    public MenuHandler() {
    }

    /**
     * This method will:
     * - Clear database
     * - Clear OrderManager cache
     * - Request StandManager to clear cache
     *
     * @throws ParseException          Json parsing error
     * @throws JsonProcessingException Json processing error
     */
    public void deleteAll() throws ParseException, JsonProcessingException {

        // Clear database
        brandRepository.deleteAll();

        String response = communicationHandler.sendRestCallToStandManager("/delete", null, null);
        JSONParser parser = new JSONParser();
        JSONObject responseObject = (JSONObject) parser.parse(response);

        // Retrieve response
        boolean delschedulers = (boolean) Objects.requireNonNull(responseObject).get("del");
        if (delschedulers) System.out.println("deleted schedulers");

    }


    /**
     * This method will refresh the cache of the StandManager based on the local cache
     *
     * @throws JsonProcessingException Json processing error
     */
    public void updateStandManager() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(standRepository.findAll()
                .stream().map(Stand::asCommonStand).collect(Collectors.toList()));
        communicationHandler.sendRestCallToStandManager("/update", json, null);
    }

    public List<Food> getStandMenu(String standName, String brandName) throws DoesNotExistException {
        Stand stand = standRepository.findStandById(standName, brandName).orElse(null);
        if (stand == null) {
            throw new DoesNotExistException("Such stand does not exist");
        }
        return stand.getFoodList();
    }


    /**
     * Refresh the global menu cache based on the database contents.
     *
     * @throws JsonProcessingException Json processing error
     */
    public List<CommonFood> getGlobalMenu() throws JsonProcessingException {
        List<CommonFood> globalMenu = new ArrayList<>();
        List<Food> allFoodItems = foodRepository.findAll();
        Map<Object, Boolean> isDuplicate = allFoodItems.stream()
                .collect(Collectors.toMap(f -> Arrays.asList(f.getName(), f.getStand().getBrand().getName()),
                        f -> false,
                        (a, b) -> true));


        for (Food f : allFoodItems) {
            Object key = Arrays.asList(f.getName(), f.getStand().getBrand().getName());
            if (isDuplicate.containsKey(key)) {
                globalMenu.add(f.asCommonFood());
                isDuplicate.remove(key);
            }
        }

        return globalMenu;
    }


    /**
     * This method will update a existing stand in the database with updated information.
     *
     * @param commonStand CommonStand with CommonFood Objects that need to be updated
     * @throws DoesNotExistException Stand does not exist
     */
    public void updateStand(CommonStand commonStand) throws DoesNotExistException, JsonProcessingException {

        // Update the stand information and persist
        Stand originalStand = standRepository.findStandById(commonStand.getName(), commonStand.getBrandName())
                .orElseThrow(() -> new DoesNotExistException("Does not exist"));

        // The same stand modified by the new information
        Stand modifiedStand = new Stand(commonStand);

        //This map will contain mapping between the hash of the original food item and the adjusted food item
        Map<Food, Food> originalModifiedFoodMapping = new HashMap<>();

        modifiedStand.getFoodList().forEach(modifiedFood -> {
            //Every modified food item that is not new will be added to the mapping
            foodRepository.findFoodById(modifiedFood.getName(), modifiedFood.getStandName(), modifiedFood.getBrandName()).ifPresent(originalFood -> {
                originalModifiedFoodMapping.put(originalFood, modifiedFood);
            });
        });


        originalModifiedFoodMapping.entrySet().forEach(mapping -> {

            // look in all other stands of the brand and find the one that needs to be adjusted
            originalStand.getBrand().getStandList().stream()
                    .filter(s -> !s.equals(originalStand)) //get other stands of the brand
                    .map(Stand::getFoodList) // map every stand to its list of food items
                    .flatMap(Collection::stream) //bring lists in one stream
                    .filter(food -> food.equals(mapping.getKey())) //Get all of the food items that match the one that needs to be adjusted
                    .forEach(food -> {

                        food.updateGlobalProperties(mapping.getValue());
                        food.updateStock(mapping.getValue());

                        foodRepository.save(food);

                    });

        });


        standRepository.save(modifiedStand);
    }


    /**
     * This method will create a stand entity based on a CommonStand object when such a stand is not yet in the system.
     * This stand will be saved to the database, saved in the cache and sent to the StandManager.
     *
     * @param newCommonStand The stand that needs to be created
     * @throws JsonProcessingException A json processing error
     * @throws ParseException          A json parsing error
     * @throws DuplicateStandException Duplicate stand detected
     */
    public void addStand(CommonStand newCommonStand) throws JsonProcessingException, ParseException, DuplicateStandException, CommunicationException {

        // look if stands already exists
        Stand stand = standRepository.findStandById(newCommonStand.getName(), newCommonStand.getBrandName()).orElse(null);

        if (stand != null) {
            throw new DuplicateStandException("The stand with id: " + stand.getStandId() + " already exists.");
        }

        // get brand from commonstand
        Brand brand = brandRepository.findById(newCommonStand.getBrandName())
                .orElse(new Brand(newCommonStand.getBrandName()));

        // create stand object
        Stand newStand = new Stand(newCommonStand, brand);
        brand.getStandList().add(newStand);
        brandRepository.save(brand);

        // Also send the new stand to the StandManager
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(newStand.asCommonStand());
        String response = communicationHandler.sendRestCallToStandManager("/newStand", jsonString, null);

        JSONParser parser = new JSONParser();
        JSONObject responseObject = (JSONObject) parser.parse(response);
        boolean addinfo = (boolean) responseObject.getOrDefault("added", false);
        if (addinfo) {
            System.out.println("Scheduler added");
        }
        else{
            throw new CommunicationException("Scheduler could not be added");
        }

    }

    /**
     * This method will delete a given stand by its id.
     * This will also remove all linked food items.
     * The method will also instruct StandManager to remove the corresponding Scheduler.
     *
     * @param standName Name of the stand
     * @param brandName Name of the stand's brand
     * @throws JsonProcessingException Json processing error
     */
    public void deleteStandById(String standName, String brandName) throws JsonProcessingException, DoesNotExistException {

        Stand stand = standRepository.findStandById(standName, brandName).orElse(null);
        if (stand != null) {
            standRepository.delete(stand);

            Map<String, String> params = new HashMap<>();
            params.put("standName", standName);
            params.put("brandName", brandName);
            communicationHandler.sendRestCallToStandManager("/deleteScheduler", null, params);
        } else {
            throw new DoesNotExistException("The stand can't be deleted when it does not exist.");
        }
    }








}
