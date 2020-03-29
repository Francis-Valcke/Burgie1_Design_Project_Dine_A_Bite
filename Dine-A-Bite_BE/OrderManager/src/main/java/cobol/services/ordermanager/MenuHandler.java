package cobol.services.ordermanager;

import cobol.commons.MenuItem;
import cobol.commons.StandInfo;
import cobol.services.ordermanager.domain.entity.Food;
import cobol.services.ordermanager.domain.entity.Stand;
import cobol.services.ordermanager.domain.entity.Stock;
import cobol.services.ordermanager.domain.repository.FoodRepository;
import cobol.services.ordermanager.domain.repository.StandRepository;
import cobol.services.ordermanager.domain.repository.StockRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class puts JSON menus into the database and creates JSON menus from the databse and refreshes them everytime a change is made
 * saving the menus as JSON files makes it so they dont have to be remade every call
 */
@Service
public class MenuHandler {
    private Map<String, JSONArray> standmenus;
    private JSONArray totalmenu;
    @Autowired // This means to get the bean called standRepository
    // Which is auto-generated by Spring, we will use it to handle the data
    private StandRepository standRepository;
    @Autowired
    private FoodRepository foodRepository;
    @Autowired
    private StockRepository stockRepository;

    private boolean SMon = true;

    public MenuHandler() {
        standmenus = new HashMap<String, JSONArray>();
    }

    public void SmSwitch(boolean b) {
        this.SMon = b;
    }

    /**
     * this will clear the database and the JSON cache files
     */
    public void deleteAll() {
        if (standmenus != null) standmenus.clear();

        if (totalmenu != null) totalmenu.clear();

        stockRepository.deleteAll();
        foodRepository.deleteAll();
        standRepository.deleteAll();
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //String uri = "http://localhost:8082/delete";
        String uri = OrderManager.SMURL+"/delete";
        headers.add("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJPcmRlck1hbmFnZXIiLCJyb2xlcyI6WyJST0xFX0FQUExJQ0FUSU9OIl0sImlhdCI6MTU4NDkxMTY3MSwiZXhwIjoxNzQyNTkxNjcxfQ.VmujsURhZaXRp5FQJXzmQMB-e6QSNF-OyPLeMEMOVvI");
        HttpEntity<String> request = new HttpEntity<>(headers);
        boolean delschedulers = (boolean) template.postForObject(uri, request, JSONObject.class).get("del");
        if (delschedulers) System.out.println("deleted schedulers");


    }

    /**
     * this is only necessary when there are already items in the database before starting up the menuhandler
     * --> when the Order manager needs to restart
     * <p>
     * Will also delete duplicates
     */
    public List<String> update() throws JsonProcessingException {
        Stand[] s = standRepository.findStands().toArray(new Stand[standRepository.findStands().size()]);
        List<String> standnames = new ArrayList<>();
        for (int i = 0; i < s.length; i++) {
            if (standnames.contains(s[i].getName())) {
                standRepository.delete(s[i]);
                continue;
            }
            standnames.add(s[i].getName());
            fetchStandMenu(s[i].getName());

        }
        fetchMenu();
        return standnames;
    }

    /**
     * @return JSONobject with standnames (TODO: and ids)
     */
    public JSONObject getStandnames() {
        JSONObject obj = new JSONObject();
        List<Stand> s = standRepository.findStands();
        for (int i = 0; i < s.size(); i++) {
            obj.put(s.get(i).getName(), s.get(i).getBrandName());
        }
        return obj;
    }

    public JSONArray getStandMenu(String standname) {
        if (!standmenus.containsKey(standname)) {
            System.out.println("Wrong name");
            return null;
        }
        return standmenus.get(standname);
    }

    public JSONArray getTotalmenu() {
        return totalmenu;
    }

    /**
     * this function makes the JSON menu for a specific stand
     *
     * @param standname
     */
    public void fetchStandMenu(String standname) throws JsonProcessingException {
        List<Food> menu = foodRepository.findByStand(standname);
        JSONArray obj = createMenuItems(menu, new JSONArray());
        standmenus.put(standname, obj);
    }

    /**
     * @param menu
     * @param obj
     * @return obj with added menuItems
     * @throws JsonProcessingException
     */
    public JSONArray createMenuItems(List<Food> menu, JSONArray obj) throws JsonProcessingException {
        for (int j = 0; j < menu.size(); j++) {
            MenuItem mi = new MenuItem(menu.get(j).getName(), BigDecimal.valueOf(menu.get(j).getPrice()), menu.get(j).getPreparationTime(), -1, menu.get(j).getBrandName(), menu.get(j).getDescription(), menu.get(j).getCategory());
            ObjectMapper om = new ObjectMapper();
            String jsonstring = om.writeValueAsString(mi);
            obj.add(jsonstring);
        }
        return obj;
    }

    /**
     * This function creates a global menu JSON file
     */
    public void fetchMenu() throws JsonProcessingException {
        JSONArray obj = new JSONArray();
        List<Stand> stands = standRepository.findStands();
        for (int j = 0; j < stands.size(); j++) {
            List<Food> menu = foodRepository.findByStand(stands.get(j).getName());
            obj = createMenuItems(menu, obj);
        }
        this.totalmenu = obj;
    }

    /**
     * Add stand to database
     * If a stand already has the chosen name isof same brand, the stand will be updated TODO: only correct standmanager can update stand
     * If the stand belongs to a certain brand, food items with the same name as other food items of this brand will overwrite the previous food items!
     *
     * @return "stand name already taken" if a stand tries to take a name of an existing stand of a different brand
     */

    public String addStand(JSONObject menu) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        //Initialise stand
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        List<Stand> stands = standRepository.findStands();
        StandInfo si = objectMapper.readValue(menu.toJSONString(), StandInfo.class);
        //Look if stand already exists

        boolean newstand = true;
        Stand n = null;
        String standname = si.getName();
        String brandname = si.getBrand();
        for (int i = 0; i < stands.size(); i++) {
            if (stands.get(i).getName().equals(si.getName())) {
                if (brandname.equals(stands.get(i).getBrandName())) {
                    newstand = false;
                    n = stands.get(i);
                } else return "stand name already taken";

            }
        }
        //Save stand

        long llon = si.getLon();
        long llat = si.getLat();
        if (newstand) {
            n = new Stand();
            n.setBrandName(brandname);
            n.setName(standname);
        }
        n.setLatitude(llat);
        n.setLongitude(llon);
        standRepository.save(n);
        si.setId(n.getId());
        List<Food> items = n.getFoodList();
        List<Food> foodInStand = null;
        //current items in menu
        if (!newstand) {
            foodInStand = foodRepository.findByStand(standname);
        }
        //Add/edit menu
        List<Food> brandfood = foodRepository.findByBrand(brandname);
        for (MenuItem mi : si.getMenu()) {
            mi.setBrandName(brandname);
            //check if food item already part of brand

            boolean existsInBrand = false;
            Food food = null;
            for (Food f : brandfood) {
                if (f.getName().equals(mi.getFoodName())) {
                    existsInBrand = true;
                    food = f;
                }
            }
            //make new food item if there does not exist a food item of brand with same name already

            if (!existsInBrand) {
                food = new Food();
                food.setName(mi.getFoodName());
                food.setBrandName(mi.getBrandName());
            }
            //Edit food item attributes
            BigDecimal price = mi.getPrice();
            int preptime = mi.getPreptime();
            String desc = mi.getDescription();
            List<String> cat = mi.getCategory();
            if (existsInBrand) {
                if (cat == null || food.getCategory().containsAll(cat) || cat.get(0).equals("")) ;
                else {
                    if (food.getCategory() == null) {
                        food.setCategory(cat);
                    } else {
                        for (int p = 0; p < cat.size(); p++) food.addCategory(cat.get(p));
                    }
                }
                if (price.compareTo(BigDecimal.ZERO) >= 0) food.setPrice(price.floatValue());
                if (preptime >= 0) food.setPreparationTime(preptime);
                if (!(desc.equals("")))food.setDescription(desc);
            } else {
                food.setPrice(price.floatValue());
                food.setPreparationTime(preptime);
                if (cat.get(0).equals("")) food.setCategory(null);
                else food.setCategory(cat);
                if (desc.equals("")) food.setDescription(null);
                else food.setDescription(desc);
                if (brandname.equals("")) food.setBrandName(null);
                else food.setBrandName(brandname);
            }
            foodRepository.save(food);
            if (!newstand) foodInStand.remove(food);

            int count = mi.getStock();
            Food s = null;
            boolean newitem = true;
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getId() == food.getId()) {
                    newitem = false;
                }
            }
            if (!newitem) {
                s = food;
                if (count < 0) ;
                else s.setStock(count);
            } else {
                food.setStock(count);
            }
            foodRepository.save(s);
        }
        //delete items removed from menu
        if (!newstand) {
            for (Food food : foodInStand) {
                foodRepository.deleteById(food.getId());
            }
        }

        fetchStandMenu(standname);
        fetchMenu();
        if (newstand && SMon) {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(si);
            RestTemplate template = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String uri = OrderManager.SMURL+"/newStand";
            headers.add("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJPcmRlck1hbmFnZXIiLCJyb2xlcyI6WyJST0xFX0FQUExJQ0FUSU9OIl0sImlhdCI6MTU4NDkxMTY3MSwiZXhwIjoxNzQyNTkxNjcxfQ.VmujsURhZaXRp5FQJXzmQMB-e6QSNF-OyPLeMEMOVvI");
            HttpEntity<String> request = new HttpEntity<>(jsonString, headers);
            boolean addinfo = (boolean) template.postForObject(uri, request, JSONObject.class).get("added");
            if (addinfo) System.out.println("Scheduler added");

        }
        return "Saved";
    }

    public boolean deleteStand(String name) {
        if (standRepository.findStandByName(name) == null) return false;
        Stand stand = standRepository.findStandByName(name);
        //List<Food> st = stand.getFoodList();
        //for (int i = 0; i < st.size(); i++) {
        //    List<Integer> l = stockRepository.findStandIdByFoodId(st.get(i).getId());
        //    if (l.size() < 2) foodRepository.deleteById(st.get(i).getFood_id());
        //}
        //stockRepository.deleteAll(st);
        standRepository.delete(stand);
        return true;

    }

    public Food getFood(String name, String brandname) {
        return foodRepository.findByNameAndBrand(name, brandname);
    }

    public List<Food> getCategory(String name) {
        return foodRepository.findByName(name);
    }
}
