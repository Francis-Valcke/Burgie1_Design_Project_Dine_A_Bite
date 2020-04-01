package cobol.services.ordermanager;

import cobol.commons.Event;
import cobol.commons.MenuItem;
import cobol.commons.StandInfo;
import cobol.services.ordermanager.dbmenu.*;
import cobol.services.ordermanager.exception.MissingEntityException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;


/**
 * This class puts JSON menus into the database and creates JSON menus from the databse and refreshes them everytime a change is made
 * saving the menus as JSON files makes it so they dont have to be remade every call
 */
@Service
public class MenuHandler {

    @Autowired
    private StandRepository standRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private StockRepository stockRepository;

    private ObjectMapper objectMapper;
    private ArrayList<String> standInfos;
    private Map<String, JSONArray> standmenus;
    private JSONArray totalmenu;
    private RestTemplate restTemplate;
    private HttpHeaders headers;
    private boolean sMon = true;

    private Logger logger = LoggerFactory.getLogger(MenuHandler.class);

    public MenuHandler() {
        standInfos = new ArrayList<>();
        standmenus = new HashMap<>();
        objectMapper = new ObjectMapper();
        restTemplate = new RestTemplate();
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJPcmRlck1hbmFnZXIiLCJyb2xlcyI6WyJST0xFX0FQUExJQ0FUSU9OIl0sImlhdCI6MTU4NDkxMTY3MSwiZXhwIjoxNzQyNTkxNjcxfQ.VmujsURhZaXRp5FQJXzmQMB-e6QSNF-OyPLeMEMOVvI");

        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
    /**
     * this method will change wether stands are added as schedulers in Stand Manager
     * @param b: if true, then stands will be added as schedulers
     */
    public void smSwitch(boolean b) {
        this.sMon = b;
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

        String uri = OrderManager.SMURL+"/delete";
        HttpEntity<String> request = new HttpEntity<>(headers);
        boolean delschedulers = (boolean) Objects.requireNonNull(restTemplate.postForObject(uri, request, JSONObject.class)).get("del");
        if (delschedulers) {
            logger.info("Deleted schedulers");
        }

    }

    /**
     * this is only necessary when there are already items in the database before starting up the menuhandler
     * --> when the Order manager needs to restart
     * <p>
     * Will also delete duplicates
     */
    @PostConstruct
    public List<String> update() throws JsonProcessingException {
        Stand[] s = standRepository.findStands().toArray(new Stand[standRepository.findStands().size()]);
        List<String> standnames = new ArrayList<>();
        standInfos.clear();
        for (Stand stand : s) {
            if (standnames.contains(stand.getFull_name())) {
                standRepository.delete(stand);
                continue;
            }
            standnames.add(stand.getFull_name());
            fetchStandMenu(stand.getFull_name());
            StandInfo si =new StandInfo(stand.getId(),stand.getFull_name(),stand.getBrandname(),(long)stand.getLocation_lat(),(long)stand.getLocation_lon());

            for (Food f : foodRepository.findByStand(stand.getFull_name())){
                si.addMenuItem(new MenuItem(f.getName(),f.getPrice(),f.getPreptime(),-1,f.getBrandname(),f.getDescription(),f.getCategory()));
            }
            standInfos.add(objectMapper.writeValueAsString(si));

        }
        fetchMenu();
        return standnames;
    }
    public void updateSM(){
        if (sMon && !standInfos.isEmpty()){
            JSONArray json = new JSONArray();
            json.addAll(standInfos);
            HttpEntity<String> request = new HttpEntity<>(json.toString(), headers);
            String uri = OrderManager.SMURL+"/update";
            restTemplate.postForObject(uri, request, JSONObject.class);
        }
    }

    /**
     * @return JSONobject with standnames (TODO: and ids)
     */
    public JSONObject getStandnames() {
        JSONObject obj = new JSONObject();
        List<Stand> s = standRepository.findStands();
        for (Stand stand : s) {
            obj.put(stand.getFull_name(), stand.getBrandname());
        }
        return obj;
    }

    public JSONArray getStandMenu(String standname) throws MissingEntityException {
        if (!standmenus.containsKey(standname)) {
            throw new MissingEntityException("No such stand: " + standname);
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
    public void fetchStandMenu(String standname) {
        List<Food> menu = foodRepository.findByStand(standname);
        JSONArray obj = createMenuItems(menu, new JSONArray());
        standmenus.put(standname, obj);
    }

    /**
     * @param menu
     * @param obj
     * @return obj with added menuItems
     */
    public JSONArray createMenuItems(List<Food> menu, JSONArray obj) {
        for (Food food : menu) {
            MenuItem mi = new MenuItem(food.getName(), food.getPrice(), food.getPreptime(), -1, food.getBrandname(), food.getDescription(), food.getCategory());
            obj.add(mi);
        }
        return obj;
    }

    /**
     * This function creates a global menu JSON file
     */
    public void fetchMenu() {
        JSONArray obj = new JSONArray();
        List<String> brands = standRepository.findBrands();
        //List<Stand> stands = standRepository.findStands();
        for (int j = 0; j < brands.size(); j++) {

            List<Food> menu = foodRepository.findByBrand(brands.get(j));
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

        //Initialise stand
        List<Stand> stands = standRepository.findStands();
        StandInfo si = objectMapper.readValue(menu.toJSONString(), StandInfo.class);
        //Look if stand already exists

        boolean newstand = true;
        Stand n = null;
        String standname = si.getName();
        String brandname = si.getBrand();
        for (Stand stand : stands) {
            if (stand.getFull_name().equals(si.getName())) {
                if (brandname.equals(stand.getBrandname())) {
                    newstand = false;
                    n = stand;
                } else return "stand name already taken";

            }
        }
        //Save stand

        double llon = si.getLon();
        double llat = si.getLat();
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
            foodInStand = foodRepository.findByStand(standname);
        }
        //Add/edit menu
        List<Food> brandfood = foodRepository.findByBrand(brandname);
        for (MenuItem mi : si.getMenu()) {
            mi.setBrandName(brandname);
            //check if food item already part of brand
            Food f = editMenu(mi, n, brandfood, newstand, items);
            if (!newstand) foodInStand.remove(f);
        }
        //delete items removed from menu
        if (!newstand) {
            for (Food food : foodInStand) {
                stockRepository.delete(stockRepository.findStock(food.getId(), n.getId()));
                foodRepository.deleteById(food.getId());
            }
        }

        fetchStandMenu(standname);
        fetchMenu();
        sendScheduler(si);
        return "Saved";
    }

    /**
     * change menu items if necessary
     *
     * @param mi        menuitem
     * @param n         stand
     * @param brandfood all food of a brand
     * @param newstand  wether stand is new
     * @param items     amount of items for each food in a stand
     * @return created food item
     * @throws JsonProcessingException when mapper fails
     */
    public Food editMenu(MenuItem mi, Stand n, List<Food> brandfood, boolean newstand, List<Stock> items) throws JsonProcessingException {
        String brandname = n.getBrandname();
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
        //check if there are other stands with this item
        if (existsInBrand) {
            //check if category empty
            if (!(cat == null || cat.isEmpty() || cat.get(0).equals(""))) {
                if (food.getCategory() == null) {
                    food.setCategory(cat);
                } else if (!food.getCategory().containsAll(cat)) {
                    for (String s : cat) food.addCategory(s);
                }
            }
            //check if price and preptime are intended changes (negative value is not intended change)
            if (price.compareTo(BigDecimal.ZERO) >= 0) food.setPrice(price);
            if (preptime >= 0) food.setPreptime(preptime);
            //checkk if description empty
            if (!(desc.equals(""))) food.setDescription(desc);
            if (sMon)publishMenuChange(mi, brandname);


        } else {
            food.setPrice(price);
            food.setPreptime(preptime);
            //check if category empty
            if (cat == null || cat.isEmpty() || cat.get(0).equals("")) food.setCategory(null);
            else food.setCategory(cat);
            //check if description empty
            if (desc == null || desc.equals("")) food.setDescription(null);
            else food.setDescription(desc);
            food.setBrandname(brandname);
        }
        foodRepository.save(food);


        int count = mi.getStock();
        Stock s;
        //look if new
        boolean newitem = true;
        for (Stock item : items) {
            if (item.getFood_id() == food.getId()) {
                newitem = false;
                break;
            }
        }
        if (!newitem) {
            s = stockRepository.findStock(food.getId(), n.getId());
            if (count >= 0) s.setCount(count);
        } else {
            s = new Stock();
            s.setCount(count);
            s.setFood_id(food.getId());
            s.setStand_id(n.getId());
        }
        stockRepository.save(s);
        return food;
    }

    /**
     * delete stand from OM, SM and db
     *
     * @param name stand name
     * @return true if no error
     */
    public boolean deleteStand(String name) throws JsonProcessingException {
        if (standRepository.findStandByName(name) == null) return false;
        int id = standRepository.findStandByName(name).getId();
        List<Stock> st = stockRepository.findStockByStand(id);
        for (int i = 0; i < st.size(); i++) {
            List<Integer> l = stockRepository.findStandIdByFoodId(st.get(i).getFood_id());
            if (l.size() < 2) foodRepository.deleteById(st.get(i).getFood_id());
        }
        stockRepository.deleteAll(st);
        standRepository.deleteById(id);
        StandInfo si = new StandInfo(id, null, null, 0, 0);
        sendScheduler(si);
        return true;

    }

    /**
     * send standinfo to SM
     *
     * @param si StandInfo
     * @throws JsonProcessingException when mapper fails
     */
    public void sendScheduler(StandInfo si) throws JsonProcessingException {
        String jsonString = objectMapper.writeValueAsString(si);
        standInfos.add(jsonString);
        if (sMon) {
            String uri = OrderManager.SMURL + "/newStand";
            HttpEntity<String> request = new HttpEntity<>(jsonString, headers);
            boolean addInfo = (boolean) Objects.requireNonNull(restTemplate.postForObject(uri, request, JSONObject.class)).get("added");
            if (addInfo) {
                logger.info("Scheduler added");
            }
        }
    }

    /**
     * publish changed menuItem Event for schedulers
     *
     * @param mi    MenuItem
     * @param brand brandname
     * @throws JsonProcessingException
     */
    public void publishMenuChange(MenuItem mi, String brand) throws JsonProcessingException {
        JSONObject itemJson = new JSONObject();
        itemJson.put("menuItem", mi);
        List<String> types = new ArrayList<>();
        types.add(brand);
        Event e = new Event(itemJson, types, "MenuItem");

        // Publish event to standmanager
        String jsonString = objectMapper.writeValueAsString(e);
        String uri = OrderManager.ECURL + "/publishEvent";
        HttpEntity<String> request = new HttpEntity<>(jsonString, headers);
        String response = restTemplate.postForObject(uri, request, String.class);
    }

    public Food getFood(String name, String brandname) {
        return foodRepository.findByNameAndBrand(name, brandname);
    }

    public List<Food> getCategory(String name) {
        return foodRepository.findByName(name);
    }
}
