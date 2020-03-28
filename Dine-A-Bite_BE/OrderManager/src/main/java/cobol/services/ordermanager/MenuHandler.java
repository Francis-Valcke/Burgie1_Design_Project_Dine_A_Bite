package cobol.services.ordermanager;

import cobol.commons.MenuItem;
import cobol.commons.StandInfo;
import cobol.services.ordermanager.dbmenu.*;
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
    private Food_Repository food_Repository;
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
        food_Repository.deleteAll();
        standRepository.deleteAll();
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //String uri = "http://localhost:8082/delete";
        String uri = "http://cobol.idlab.ugent.be:8092/delete";
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
            if (standnames.contains(s[i].getFull_name())) {
                standRepository.delete(s[i]);
                continue;
            }
            standnames.add(s[i].getFull_name());
            fetchStandMenu(s[i].getFull_name());

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
            obj.put(s.get(i).getFull_name(), s.get(i).getBrandname());
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
        List<Food> menu = food_Repository.findByStand(standname);
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
            MenuItem mi = new MenuItem(menu.get(j).getName(), menu.get(j).getPrice(), menu.get(j).getPreptime(), -1, menu.get(j).getBrandname(), menu.get(j).getDescription(), menu.get(j).getCategory());
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
            List<Food> menu = food_Repository.findByStand(stands.get(j).getFull_name());
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
            if (stands.get(i).getFull_name().equals(si.getName())) {
                if (brandname.equals(stands.get(i).getBrandname())) {
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
            n.setBrandname(brandname);
            n.setFull_name(standname);
        }
        n.setLocation_lat(llat);
        n.setLocation_lon(llon);
        standRepository.save(n);
        si.setId(n.getId());
        List<Stock> items = stockRepository.findStockByStand(n.getId());
        List<Food> foodInStand = null;
        //current items in menu
        if (!newstand) {
            foodInStand = food_Repository.findByStand(standname);
        }
        //Add/edit menu
        List<Food> brandfood = food_Repository.findByBrand(brandname);
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
                food.setBrandname(mi.getBrandName());
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
                if (price.compareTo(BigDecimal.ZERO) <= 0) food.setPrice(price);
                if (preptime >= 0) food.setPreptime(preptime);
                if (!(desc.equals("")))food.setDescription(desc);
            } else {
                food.setPrice(price);
                food.setPreptime(preptime);
                if (cat.get(0).equals("")) food.setCategory(null);
                else food.setCategory(cat);
                if (desc.equals("")) food.setDescription(null);
                else food.setDescription(desc);
                if (brandname.equals("")) food.setBrandname(null);
                else food.setBrandname(brandname);
            }
            food_Repository.save(food);
            if (!newstand) foodInStand.remove(food);

            int count = mi.getStock();
            Stock s = null;
            boolean newitem = true;
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getFood_id() == food.getId()) {
                    newitem = false;
                }
            }
            if (!newitem) {
                s = stockRepository.findStock(food.getId(), n.getId());
                if (count < 0) ;
                else s.setCount(count);
            } else {
                s = new Stock();
                s.setCount(count);
                s.setFood_id(food.getId());
                s.setStand_id(n.getId());
            }
            stockRepository.save(s);
        }
        //delete items removed from menu
        if (!newstand) {
            for (Food food : foodInStand) {
                stockRepository.delete(stockRepository.findStock(food.getId(), n.getId()));
                food_Repository.deleteById(food.getId());
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
        int id = standRepository.findStandByName(name).getId();
        List<Stock> st = stockRepository.findStockByStand(id);
        for (int i = 0; i < st.size(); i++) {
            List<Integer> l = stockRepository.findStandIdByFoodId(st.get(i).getFood_id());
            if (l.size() < 2) food_Repository.deleteById(st.get(i).getFood_id());
        }
        stockRepository.deleteAll(st);
        standRepository.deleteById(id);
        return true;

    }

    public Food getFood(String name, String brandname) {
        return food_Repository.findByNameAndBrand(name, brandname);
    }

    public List<Food> getCategory(String name) {
        return food_Repository.findByName(name);
    }
}
