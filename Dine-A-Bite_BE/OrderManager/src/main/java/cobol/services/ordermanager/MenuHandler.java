package cobol.services.ordermanager;

import cobol.commons.CommonFood;
import cobol.commons.CommonStand;
import cobol.services.ordermanager.domain.entity.Brand;
import cobol.services.ordermanager.domain.entity.Food;
import cobol.services.ordermanager.domain.entity.Stand;
import cobol.services.ordermanager.domain.repository.BrandRepository;
import cobol.services.ordermanager.domain.repository.FoodRepository;
import cobol.services.ordermanager.domain.repository.StandRepository;
import cobol.services.ordermanager.exception.DoesNotExistException;
import cobol.services.ordermanager.exception.DuplicateStandException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * This class puts JSON menus into the database and creates JSON menus from the databse and refreshes them everytime a change is made
 * saving the menus as JSON files makes it so they dont have to be remade every call
 */
@Service
@Getter
public class MenuHandler {

    private List<Stand> stands;
    private List<CommonFood> globalMenu;

    @Autowired
    private StandRepository standRepository;
    @Autowired
    private FoodRepository foodRepository;
    @Autowired
    private BrandRepository brandRepository;

    public MenuHandler() {
        stands = new ArrayList<>();
        globalMenu = new ArrayList<>();
    }

    /**
     * This method will:
     * - Clear database
     * - Clear OrderManager cache
     * - Request StandManager to clear cache
     *
     * @throws ParseException Json parsing error
     * @throws JsonProcessingException Json processing error
     */
    public void deleteAll() throws ParseException, JsonProcessingException {

        // Clear cache
        stands.clear();
        globalMenu.clear();

        // Clear database
        brandRepository.deleteAll();

        String response = sendRestCallToStandManager("/delete", null, null);
        JSONParser parser = new JSONParser();
        JSONObject responseObject = (JSONObject) parser.parse(response);

        // Retrieve response
        boolean delschedulers = (boolean) Objects.requireNonNull(responseObject).get("del");
        if (delschedulers) System.out.println("deleted schedulers");

    }

    /**
     * This method will refresh the cache based on the database contents
     *
     * @throws JsonProcessingException Json processing error
     */
    @PostConstruct
    public void refreshCache() throws JsonProcessingException {
        // retrieve database
        stands = standRepository.findAll();
        // update cache
        updateGlobalMenu();
    }

    /**
     * This method will refresh the cache of the StandManager based on the local cache
     *
     * @throws JsonProcessingException Json processing error
     */
    public void updateStandManager() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(stands);
        sendRestCallToStandManager("/update", json, null);
    }

    public List<Food> getStandMenu(String standName, String brandName) {
        Optional<Stand> standOptional = findStandById(standName, brandName);
        return standOptional.map(Stand::getFoodList).orElse(new ArrayList<>());
    }

    /**
     * Get stand from cache or database by composite key (standName and brandName)
     * On miss in cache, it will search in database and add to cache if present in db
     *
     * @param standName name of stand
     * @param brandName name of brand
     * @return optional stand
     */
    public Optional<Stand> findStandById(String standName, String brandName) {
        Optional<Stand> standOptional = stands.stream()
                .filter(s -> s.getName().equals(standName) && s.getBrandName().equals(brandName))
                .findFirst();

        // not present in cache
        if (!standOptional.isPresent()) {
            Optional<Brand> brand = brandRepository.findById(brandName);
            // present in database
            if (brand.isPresent()) {
                standOptional = brand.get().getStandList().stream().filter(s -> s.getName().equals(standName)).findAny();
                // add to cache
                standOptional.ifPresent(stand -> stands.add(stand));
            }
        }
        return standOptional;
    }

    /**
     * Refresh the global menu cache based on the database contents.
     *
     * @throws JsonProcessingException Json processing error
     */
    public void updateGlobalMenu() throws JsonProcessingException {
        globalMenu.clear();
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
    }


    /**
     * This method will update a existing stand in the database with updated information.
     *
     * @param commonStand Stand to be updated
     * @return
     */
    public void updateStand(CommonStand commonStand) throws DoesNotExistException {
        Optional<Stand> standOptional = findStandById(commonStand.getName(), commonStand.getBrandName());
        if (!standOptional.isPresent()) {
            throw new DoesNotExistException("The stand to be updated does not yet exist, please create the stand first.");
        }

        Stand stand = standOptional.get();

        stand.setFoodList(commonStand.getMenu().stream()
                /*
                This will map a entry of common food to a food entity.
                Already existing items will be updated, new ones will be created,
                missing items will implicitly be discarded.
                */
                .map(cf -> {
                    Food food;
                    Optional<Food> optionalFood = foodRepository.findById(new Food.FoodId(cf.getName(), stand));
                    if (optionalFood.isPresent()) {
                        optionalFood.get().update(cf);
                        food = optionalFood.get();
                    } else {
                        food = new Food(cf, stand);
                    }
                    return food;
                }).collect(Collectors.toList()));

        standRepository.save(stand);
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
    public void addStand(CommonStand newCommonStand) throws JsonProcessingException, ParseException, DuplicateStandException {

        // update list of stands from database
        refreshCache();

        // look if stands already exists
        Optional<Stand> standOptional = findStandById(newCommonStand.getName(), newCommonStand.getBrandName());

        if (standOptional.isPresent()) {
            throw new DuplicateStandException("The stand with id: " + standOptional.get().getStandId() + " already exists.");
        }

        // get brand from commonstand
        Optional<Brand> brandOptional = brandRepository.findById(newCommonStand.getBrandName());
        Brand brand = brandOptional.orElseGet(() -> new Brand(newCommonStand.getBrandName()));


        // create stand object
        Stand newStand = new Stand(newCommonStand, brand);
        brandRepository.save(brand);

        refreshCache();
        updateGlobalMenu();

        // Also send the new stand to the StandManager
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(newStand.asCommonStand());
        String response = sendRestCallToStandManager("/newStand", jsonString, null);

        JSONParser parser = new JSONParser();
        JSONObject responseObject = (JSONObject) parser.parse(response);
        boolean addinfo = (boolean) Objects.requireNonNull(responseObject).get("added");
        if (addinfo) System.out.println("Scheduler added");

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

        Optional<Stand> standOptional = findStandById(standName, brandName);
        if (standOptional.isPresent()) {
            standRepository.delete(standOptional.get());
            stands.remove(standOptional.get());
            updateGlobalMenu();

            Map<String, String> params = new HashMap<>();
            params.put("standName", standName);
            params.put("brandName", brandName);
            sendRestCallToStandManager("/deleteScheduler", null, params);
        } else {
            throw new DoesNotExistException("The stand can't be deleted when it does not exist.");
        }
    }


    /**
     * This method will issue HTTP request to StandManager.
     * Returns a String assuming the caller knows what to expect from the response.
     * Ex. JSONArray or JSONObject
     *
     * @param path Example: "/..."
     * @param jsonObject JSONObject or JSONArray format
     * @return response as String
     */
    public String sendRestCallToStandManager(String path, String jsonObject, Map<String, String> params) throws JsonProcessingException {
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

        return template.postForObject(builder.toUriString(), request, String.class);
    }
}
