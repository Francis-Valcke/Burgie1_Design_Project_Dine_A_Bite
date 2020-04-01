package cobol.services.ordermanager;

import cobol.commons.CommonStand;
import cobol.services.ordermanager.domain.entity.Brand;
import cobol.services.ordermanager.domain.entity.Food;
import cobol.services.ordermanager.domain.entity.Stand;
import cobol.services.ordermanager.domain.repository.BrandRepository;
import cobol.services.ordermanager.domain.repository.CategoryRepository;
import cobol.services.ordermanager.domain.repository.FoodRepository;
import cobol.services.ordermanager.domain.repository.StandRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
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
public class MenuHandler {

    private List<Stand> stands;
    private List<Food> globalMenu=new ArrayList<>();

    @Autowired
    private StandRepository standRepository;
    @Autowired
    private FoodRepository foodRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    public MenuHandler() {
        stands = new ArrayList<>();
    }

    /**
     * this will clear the database and the cache files
     */
    public void deleteAll() {

        // Clear cache
        stands.clear();

        // Clear database
        brandRepository.deleteAll();

        // Ask standmanager to delete cache
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String uri = OrderManager.SMURL + "/delete";
        headers.add("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJPcmRlck1hbmFnZXIiLCJyb2xlcyI6WyJST0xFX0FQUExJQ0FUSU9OIl0sImlhdCI6MTU4NDkxMTY3MSwiZXhwIjoxNzQyNTkxNjcxfQ.VmujsURhZaXRp5FQJXzmQMB-e6QSNF-OyPLeMEMOVvI");
        HttpEntity<String> request = new HttpEntity<>(headers);

        // Retrieve response
        boolean delschedulers = (boolean) Objects.requireNonNull(template.postForObject(uri, request, JSONObject.class)).get("del");
        if (delschedulers) System.out.println("deleted schedulers");

    }

    /**
     * This method will sync cache with database and update global menu
     */
    @PostConstruct
    public void refreshCache() throws JsonProcessingException {
        // retrieve database
        stands = standRepository.findAll();
        // update cache
        updateGlobalMenu();
    }

    public void updateStandManager() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(stands);
        sendRestCallToStandManager("/update", json);
    }

    public List<Stand> getStands() {
        return stands;
    }

    public List<Food> getStandMenu(String standName, String brandName) {
        Optional<Stand> standOptional = getStand(standName, brandName);
        return standOptional.map(Stand::getFoodList).orElse(null);
    }

    /**
     * Get stand from cache or database by composite key (standName and brandName)
     * On miss in cache, it will search in database and add to cache if present in db
     *
     * @param standName name of stand
     * @param brandName name of brand
     * @return optional stand
     */
    public Optional<Stand> getStand(String standName, String brandName) {
        Optional<Stand> standOptional = stands.stream()
                .filter(s -> s.getName().equals(standName) && s.getBrand().getName().equals(brandName))
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
     * This method will update the cache of Food objects
     */
    public void updateGlobalMenu() throws JsonProcessingException {
        List<Food> allFoodItems = foodRepository.findAll();
        Map<Object, Boolean> isDuplicate = allFoodItems.stream()
                .collect(Collectors.toMap(f -> Arrays.asList(f.getName(), f.getStand().getBrand().getName()),
                        f -> false,
                        (a, b) -> true));


        for (Food f : allFoodItems) {
            Object key = Arrays.asList(f.getName(), f.getStand().getBrand().getName());
            if (isDuplicate.containsKey(key)) {
                globalMenu.add(f);
                isDuplicate.remove(key);
            }
        }
    }


    public String updateStand(CommonStand commonStand) {
        Optional<Stand> standOptional = getStand(commonStand.getName(), commonStand.getBrand());
        if (!standOptional.isPresent()) {
            return "stand does not exist";
        }

        Stand stand = standOptional.get();

        stand.setFoodList(commonStand.getMenu().stream()
                .map(cf -> {
                    Food food;
                    Optional<Food> optionalFood = foodRepository.findById(new Food.FoodId(cf.getFoodName(), stand));
                    if (optionalFood.isPresent()) {
                        optionalFood.get().update(cf);
                        food = optionalFood.get();
                    } else {
                        food = new Food(cf, stand);
                    }
                    return food;
                }).collect(Collectors.toList()));

        standRepository.save(stand);

        return "Stand menu updated";
    }


    /**
     * Add stand to database
     * If a stand already has the chosen name isof same brand, the stand will be updated TODO: only correct standmanager can update stand
     * If the stand belongs to a certain brand, food items with the same name as other food items of this brand will overwrite the previous food items!
     *
     * @return "stand name already taken" if a stand tries to take a name of an existing stand of a different brand
     */
    public String addStand(CommonStand newCommonStand) throws JsonProcessingException, ParseException {

        // update list of stands from database
        refreshCache();

        // look if stands already exists
        Optional<Stand> standOptional = getStand(newCommonStand.getName(), newCommonStand.getBrand());

        if (standOptional.isPresent()) {
            return "stand name already taken";
        }


        // get brand from commonstand
        Optional<Brand> brandOptional = brandRepository.findById(newCommonStand.getBrand());
        Brand brand = null;
        brand = brandOptional.orElseGet(() -> new Brand(newCommonStand.getBrand()));

        // create stand object
        Stand newStand = new Stand(newCommonStand, brand);
        brandRepository.save(brand);

        refreshCache();
        updateGlobalMenu();


        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(newStand);
        String response = sendRestCallToStandManager("/newStand", jsonString);

        JSONParser parser = new JSONParser();
        JSONObject responseObject = (JSONObject) parser.parse(response);
        boolean addinfo = (boolean) Objects.requireNonNull(responseObject).get("added");
        if (addinfo) System.out.println("Scheduler added");
        return "Saved";
    }

    public boolean deleteStand(String standName, String brandName) {

        Optional<Stand> standOptional = getStand(standName, brandName);
        if (standOptional.isPresent()) {
            standRepository.delete(standOptional.get());
            stands.remove(standOptional.get());
            return true;
        } else {
            return false;
        }
    }


    /**
     * This method will send rest call to standmanager
     *
     * @param path       example: "/..."
     * @param jsonObject JSONObject or JSONArray format
     * @return response as String
     */
    public String sendRestCallToStandManager(String path, String jsonObject) {
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", OrderManager.authToken);
        HttpEntity<String> request = new HttpEntity<>(jsonObject, headers);
        String uri = OrderManager.SMURL + path;
        return template.postForObject(uri, request, String.class);
    }


    public static <T> Predicate<T> distinctByKey(
            Function<? super T, ?> keyExtractor) {

        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public List<Food> getGlobalMenu() {
        return globalMenu;
    }
}
