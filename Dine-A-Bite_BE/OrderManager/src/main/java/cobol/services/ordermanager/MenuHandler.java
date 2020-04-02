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
        String json = mapper.writeValueAsString(standRepository.findAll());
        sendRestCallToStandManager("/update", json, null);
    }

    public List<Food> getStandMenu(String standName, String brandName) throws DoesNotExistException {
        Stand stand = standRepository.findStandById(standName, brandName).orElse(null);
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
     * @param commonStand Stand to be updated
     * @return
     */
    public void updateStand(CommonStand commonStand) throws DoesNotExistException {

        // First get the stand entity based on the given CommonStand
        Optional<Stand> standOptional = standRepository.findStandById(commonStand.getName(), commonStand.getBrandName());
        Stand stand = standOptional
                .orElseThrow(() -> new DoesNotExistException("The stand to be updated does not yet exist, please create the stand first."));





        List<Food> allFood = new ArrayList<>();
        stands.stream().filter(s -> s.getBrandName().equals(commonStand.getBrandName())).forEach(st -> allFood.addAll(st.getFoodList()));

        stand.setFoodList(commonStand.getMenu().stream()
                .map(cf -> new Pair<>(foodRepository.findById(new Food.FoodId(cf.getName(), stand)).orElse(new Food(cf, stand)), cf))
                .map(pair -> {

                    allFood.stream().filter(food -> food.equals(pair.getKey())).forEach(food -> {
                        food.updateGlobalProperties(pair.getValue());
                    });

                    return pair;
                })
                .map(pair -> {

                    Food newFood = pair.getKey();
                    newFood.updateStock(pair.getValue());
                    newFood.updateGlobalProperties(pair.getValue());
                    return pair.getKey();

                })

                .collect(Collectors.toList()));

                /*
                This will map a entry of common food to a food entity.
                Already existing items will be updated, new ones will be created,
                missing items will implicitly be discarded.
                */

                allFood.forEach(f -> foodRepository.save(f));







        Food referenceFood = new Food(cf, standOptional.get());
        allFood.stream().filter(f -> f.hashCode())

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
                        optionalFood.get().updateGlobalProperties(cf);
                        optionalFood.get().updateStock(cf);
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

        // look if stands already exists
         Stand stand = standRepository.findStandById(newCommonStand.getName(), newCommonStand.getBrandName()).orElse(null);

        if (stand!=null) {
            throw new DuplicateStandException("The stand with id: " + stand.getStandId() + " already exists.");
        }

        // get brand from commonstand
        Brand brand= brandRepository.findById(newCommonStand.getBrandName()).orElse(null);
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
    public void deleteStandById(String standName, String brandName) throws JsonProcessingException, DoesNotExistException {

        Stand stand = standRepository.findStandById(standName, brandName).orElse(null);
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









}
