package cobol.services.ordermanager;


import cobol.services.ordermanager.dbmenu.*;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 *
 * This class puts JSON menus into the database and creates JSON menus from the databse and refreshes them everytime a change is made
 * saving the menus as JSON files makes it so they dont have to be remade every call
 */
@Service
public class MenuHandler {
    private Map<String, JSONObject> standmenus;
    private JSONObject totalmenu;
    @Autowired // This means to get the bean called standRepository
    // Which is auto-generated by Spring, we will use it to handle the data
    private StandRepository standRepository;
    @Autowired
    private Food_Repository food_Repository;
    @Autowired
    private StockRepository stockRepository;

    public MenuHandler() {
        standmenus = new HashMap<>();
    }

    /**
     * this will clear the database and the JSON cache files
     */
    public void deleteAll(){
        if (standmenus!=null)standmenus.clear();

        if (totalmenu!=null)totalmenu.clear();

        stockRepository.deleteAll();
        food_Repository.deleteAll();
        standRepository.deleteAll();

    }
    /**
     * this is only necessary when there are already items in the database before starting up the menuhandler
     * --> when the Order manager needs to restart
     *
     * Will also delete duplicates
     */
    public List<String> update(){
        Stand[] s = standRepository.findStands().toArray(new Stand[standRepository.findStands().size()]);
        List<String> standnames = new ArrayList<>();
        System.out.println(s.length);
        for ( int i=0;i<s.length;i++){
            if (standnames.contains(s[i].getFull_name())){
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
     *
     * @return JSONobject with standnames (TODO: and ids)
     */
    public JSONObject getStandnames(){
        JSONObject obj = new JSONObject();
        List<Stand> s = standRepository.findStands();
        for (int i =0;i<s.size();i++){
            obj.put(s.get(i).getFull_name(), s.get(i).getBrandname());
        }
        return obj;
    }

    public JSONObject getStandMenu(String standname){
        if (!standmenus.containsKey(standname)){
            System.out.println("Wrong name");
            return null;
        }
        return standmenus.get(standname);
    }
    public JSONObject getTotalmenu() {
        return totalmenu;
    }

    /**
     * this function makes the JSON menu for a specific stand
     * @param standname
     */
    public void fetchStandMenu(String standname){
        List<Food> menu = food_Repository.findByStand(standname);
        JSONObject obj = new JSONObject();
        for (int j = 0; j<menu.size();j++) {
            List l = new ArrayList();
            l.add(menu.get(j).getBrandname());
            l.add(menu.get(j).getPrice());
            l.add(menu.get(j).getCategory());
            l.add(menu.get(j).getDescription());
            obj.put(menu.get(j).getName(), l);

        }
        standmenus.put(standname, obj);
    }

    /**
     * This function creates a global menu JSON file
     */
    public void fetchMenu(){
        JSONObject obj = new JSONObject();
        List<Stand> stands = standRepository.findStands();
        for (int j = 0; j<stands.size();j++){
            List<Food> f = food_Repository.findByStand(stands.get(j).getFull_name());
            for (int i = 0; i<f.size();i++){
                List l = new ArrayList();
                String k = f.get(i).getName()+"_"+stands.get(j).getBrandname();
                l.add(f.get(i).getPrice());
                l.add(f.get(i).getCategory());
                l.add(f.get(i).getDescription());
                obj.put(k,l);

            }
        }

        this.totalmenu=obj;
    }
    /**
     * Add stand to database
     * If a stand already has the chosen name isof same brand, the stand will be updated TODO: only correct standmanager can update stand
     * If the stand belongs to a certain brand, food items with the same name as other food items of this brand will overwrite the previous food items!
     * @return "saved" if correctly added
     * @return "stand name already taken" if a stand tries to take a name of an existing stand of a different brand
     */

    String addStand(JSONObject menu) {

        List<Stand> stands = standRepository.findStands();
        String standname = null;
        double llon = 0;
        double llat = 0;
        String brandname = null;
        Set<String> keys = menu.keySet();
        System.out.println(keys);
        for (String key : keys) {
            if (((ArrayList)menu.get(key)).size()==3){
                standname = key;
            }
        }
        boolean newstand = true;
        Stand n = null;
        ArrayList st = (ArrayList) menu.get(standname);
        brandname = (String) st.get(0);
        for (int i = 0; i<stands.size();i++){
            if (stands.get(i).getFull_name().equals(standname)) {
                if (brandname.equals(stands.get(i).getBrandname())){
                    newstand=false;
                    n = stands.get(i);
                }
                else return "stand name already taken";

            }
        }

        llon = (double) st.get(1);
        llat = (double) st.get(2);
        if (newstand){
            n = new Stand();
            n.setBrandname(brandname);
            n.setFull_name(standname);
        }

        n.setLocation_lat(llat);
        n.setLocation_lon(llon);
        standRepository.save(n);
        List<Food> f = (List<Food>) food_Repository.findByBrand(brandname);
        for (String key : keys) {
            //JSONArray a = (JSONArray)menu.get(key);
            ArrayList a = (ArrayList) menu.get(key);
            if (((ArrayList)menu.get(key)).size()==3){
                continue;
            }
            boolean b=false;//check if food item already part of brand
            Food fp=null;
            for (int k =0;k<f.size();k++){
                if (f.get(k).getName().equals(key)){
                    b=true;
                    fp = f.get(k);

                }
            }
            if (!b) {
                fp = new Food();
                fp.setName(key);
            }

            Double d = (Double) a.get(0);

            int preptime =(int) a.get(1);
            String desc = (String) a.get(4);
            List<String> cat = Arrays.asList(new String[]{(String) a.get(3)});
            if (b) {
                System.out.println(cat);
                System.out.println(fp.getCategory());
                if (cat == null || fp.getCategory().containsAll(cat)||cat.get(0).equals("")) ;
                else {
                    if (fp.getCategory() == null) {
                        if (cat.get(0) == "") fp.setCategory(null);
                        else fp.setCategory(cat);
                    } else {
                        for (int p = 0; p < cat.size(); p++) fp.addCategory(cat.get(p));
                    }
                }
                if (d.floatValue()<0);
                else fp.setPrice(d.floatValue());
                if (preptime <0) ;
                else {
                    fp.setPreptime(preptime);
                }
                if (desc == "") ;
                else fp.setDescription(desc);
            }
            else {
                fp.setPrice(d.floatValue());
                fp.setPreptime(preptime);
                if (cat.get(0).equals("")) fp.setCategory(null);
                else fp.setCategory(cat);
                if (desc.equals("")) fp.setDescription(null);
                else fp.setDescription(desc);
                if (brandname.equals("")) fp.setBrandname(null);
                else fp.setBrandname(brandname);
            }
            food_Repository.save(fp);
            int count = (int) a.get(2);
            Stock s = null;
            if (!newstand){
                System.out.println(fp.getId());
                s =stockRepository.findStock(fp.getId(),n.getId());
                if (count<0);
                else s.setCount(count);
            }
            else{
                s = new Stock();
                s.setCount(count);
                s.setFood_id(fp.getId());
                s.setStand_id(n.getId());
                System.out.println(fp.getId());
            }
            stockRepository.save(s);
        }

        fetchStandMenu(standname);
        fetchMenu();

        return "Saved";
    }

    public Food getFood(String name, String brandname){
        return food_Repository.findByNameAndBrand(name, brandname);
    }
}
