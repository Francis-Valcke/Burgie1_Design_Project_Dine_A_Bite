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
import org.hibernate.annotations.Cache;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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

    public MenuHandler() {
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
    @Caching(evict={
            @CacheEvict(value="stand", allEntries = true),
            @CacheEvict(value="stands", allEntries = true),
            @CacheEvict(value="food", allEntries = true),
            @CacheEvict(value="foodItems", allEntries = true),
            @CacheEvict(value="brand", allEntries = true),
            @CacheEvict(value="brands", allEntries = true)
    })
    public void deleteAll() throws ParseException, JsonProcessingException {

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
     * This method will refresh the cache of the StandManager based on the local cache
     *
     * @throws JsonProcessingException Json processing error
     */
    public void updateStandManager() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(findAllStands());
        sendRestCallToStandManager("/update", json, null);
    }

    public List<Food> getStandMenu(String standName, String brandName) throws DoesNotExistException {
        Stand stand = findStandById(standName, brandName);
        if(stand==null){
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
        List<CommonFood> globalMenu= new ArrayList<>();
        List<Food> allFoodItems = findAllFood();
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
     * @param commonStand Stand to be updated
     * @return
     */
    @Caching(evict={
            @CacheEvict(value="stand", allEntries = true),
            @CacheEvict(value="stands", allEntries = true),
            @CacheEvict(value="food", allEntries = true),
            @CacheEvict(value="foodItems", allEntries = true)
    })
    public void updateStand(CommonStand commonStand) throws DoesNotExistException {
        Stand stand = findStandById(commonStand.getName(), commonStand.getBrandName());
        if (stand == null) {
            throw new DoesNotExistException("The stand to be updated does not yet exist, please create the stand first.");
        }

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
    @Caching(evict={
            @CacheEvict(value="stand", allEntries = true),
            @CacheEvict(value="stands", allEntries = true),
            @CacheEvict(value="brand", allEntries = true),
            @CacheEvict(value="brands", allEntries = true),
            @CacheEvict(value="food", allEntries = true),
            @CacheEvict(value="foodItems", allEntries = true)
    })
    public void addStand(CommonStand newCommonStand) throws JsonProcessingException, ParseException, DuplicateStandException {

        // look if stands already exists
         Stand stand = findStandById(newCommonStand.getName(), newCommonStand.getBrandName());

        if (stand!=null) {
            throw new DuplicateStandException("The stand with id: " + stand.getStandId() + " already exists.");
        }

        // get brand from commonstand
        Brand brand= findBrandById(newCommonStand.getBrandName());
        if(brand == null){
            brand= new Brand(newCommonStand.getBrandName());
            brandRepository.save(brand);
        }

        // create stand object
        Stand newStand = new Stand(newCommonStand, brand);
        brandRepository.save(brand);


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
    @Caching(evict={
            @CacheEvict(value= "stand", allEntries = true),
            @CacheEvict(value= "stands", allEntries = true)
    })
    public void deleteStandById(String standName, String brandName) throws JsonProcessingException, DoesNotExistException {

        Stand stand = findStandById(standName, brandName);
        if (stand!=null) {
            standRepository.delete(stand);

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

    @Cacheable("foodItems")
    public List<Food> findAllFood(){
        return foodRepository.findAll();
    }

    @Cacheable("food")
    public Food findFoodById(String foodName, String standName, String brandName){
        Stand stand= findStandById(standName, brandName);
        return foodRepository.findById(new Food.FoodId(foodName, stand)).orElse(null);
    }

    @Cacheable("brand")
    public Brand findBrandById(String brandName){
        return brandRepository.findById(brandName).orElse(null);
    }

    @Cacheable("brands")
    public List<Brand> findAllBrands(){
        return brandRepository.findAll();
    }

    /**
     * Get stand from cache or database by composite key (standName and brandName)
     * On miss in cache, it will search in database and add to cache if present in db
     *
     * @param standName name of stand
     * @param brandName name of brand
     * @return Stand object or returns null if no such stand exists
     */
    @Cacheable("stand")
    public Stand findStandById(String standName, String brandName) {
        Brand brand= findBrandById(brandName);
        if(brand!=null){
            return standRepository.findById(new Stand.StandId(standName, brand)).orElse(null);
        }
        return null;
    }

    @Cacheable("stands")
    public List<Stand> findAllStands(){
        return standRepository.findAll();
    }

    @Caching(evict={
            @CacheEvict(value="stand", allEntries = true),
            @CacheEvict(value="stands", allEntries = true),
            @CacheEvict(value="brand", allEntries = true),
            @CacheEvict(value="brands", allEntries = true),
            @CacheEvict(value="food", allEntries = true),
            @CacheEvict(value="foodItems", allEntries = true)
    })
    public void addBrands(List<Brand> data) {
        data.forEach(brandRepository::saveAndFlush);
    }
}
