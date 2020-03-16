package cobol.services.standmanager;

import cobol.services.standmanager.dbmenu.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
public class StandManagerController {
    @Autowired // This means to get the bean called standRepository
    // Which is auto-generated by Spring, we will use it to handle the data
    private StandRepository standRepository;
    @Autowired
    private Food_priceRepository food_priceRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private Food_categoryRepository foodCategoryRepository;


    /**
     * The controller has a list of all schedulers.
     * More information on schedulers in class Scheduler
     */
    private List<Scheduler> schedulers = new ArrayList<Scheduler>();

    /**
     * Add stand to database
     * returns "saved" if correctly added
     */
    @PostMapping(path = "/addstand") // Map ONLY POST Requests
    public @ResponseBody
    String addStand(@RequestBody JSONObject menu) {
        // @ResponseBody means the returned String is the response, not a view name
        // @RequestBody means it is requires a body from the POST request
        String standname = null;
        double llon = 0;
        double llat = 0;

        Set<String> keys = menu.keySet();
        System.out.println(keys);
        for (String key : keys) {
            if (((ArrayList)menu.get(key)).size()==2){
                standname = key;
            }
        }
        ArrayList st = (ArrayList) menu.get(standname);

        llon = (double) st.get(0);
        llat = (double) st.get(1);
        Stand n = new Stand();
        n.setFull_name(standname);
        n.setLocation_lat(llat);
        n.setLocation_lon(llon);
        standRepository.save(n);
        for (String key : keys) {
            //JSONArray a = (JSONArray)menu.get(key);
            ArrayList a = (ArrayList) menu.get(key);
            if (((ArrayList)menu.get(key)).size()==2){
                continue;
            }

            Food_price fp = new Food_price();
            fp.setName(key);
            Double d = (Double) a.get(0);
            fp.setPrice(d.floatValue());
            fp.setPreptime((int) a.get(1));
            fp.setDescription((String) a.get(4));
            food_priceRepository.save(fp);
            Food_category fc = new Food_category();
            fc.setCategory((String) a.get(3));
            fc.setFood_id(fp.getId());
            foodCategoryRepository.save(fc);
            Stock s = new Stock();
            s.setCount((int) a.get(2));
            s.setFood_id(fp.getId());
            s.setStand_id(n.getId());
            stockRepository.save(s);

        }
        return "Saved";
    }

    /**
     * @return the name of the recommended stand in JSON format.
     * @RequestParam() String foodtype: post a type of food
     * (In Postman: Select POST, go to params, enter "foodtype" as KEY and enter a menu item as value)
     * (ex:localhost:8080/post?foodtype=pizza)
     * (in current test above: "pizza", "apple", "burger" and "pizza with salami" are menu items)
     */
    @RequestMapping(value ="/post", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject postOrder(@RequestParam() String foodtype) {
        System.out.println(foodtype);
        return recommend(foodtype);
    }


    /**
     * @param type: type of food for which attendee seeks stand recommendation
     * @return JSON object with standname
     * Iterates over schedulers and looks for scheduler (that has the requested food item in menu) with shortest queue
     * TODO: instead of returning 1 stand: return list of stands in recommended order
     */
    public JSONObject recommend(String type) {
        JSONObject obj = new JSONObject();
        int s = 1000;
        String standname="";
        for (int i = 0; i < schedulers.size(); i++) {
            if (schedulers.get(i).checkType(type)) {
                int n = schedulers.get(i).timeSum();
                if (n < s) {
                    s = n;
                    standname = schedulers.get(i).getStandname();
                }
            }
        }
        obj.put("stand", standname);
        //obj.put("stand2", "foo");
        //obj.put("stand3", "foo");
        //obj.put("stand4", "foo");
        //obj.put("stand5", "foo");

        System.out.print(obj);
        return obj;
    }

}
