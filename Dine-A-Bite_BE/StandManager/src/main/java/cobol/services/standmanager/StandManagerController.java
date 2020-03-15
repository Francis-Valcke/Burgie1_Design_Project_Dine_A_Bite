package cobol.services.standmanager;

import org.springframework.web.bind.annotation.*;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

@RestController
public class StandManagerController {
    /**
     * The controller has a list of all schedulers.
     * More information on schedulers in class Scheduler
     */
    private List<Scheduler> schedulers = new ArrayList<Scheduler>();

    /**
     * In order to temporarily test the functionalities of the stand manager,
     * without a proper ordering process and without proper initialization of stands,
     * this function initializes a possible state of the server with 2 running schedulers of 2 distincts foodstands: "food1" and "food2".
     * To start this up, simply send a GET message to localhost:8080/start
     * TODO: replace this with proper testing function? (JUnit/Rest assured)
     */
    @RequestMapping("/start")
    public void start(){
        /**
         * Initialize stand menus and schedulers
         */
        Food f1 = new Food("apple", 10, 4);
        Food f2 = new Food("burger", 15,  10);
        Food f3 = new Food("pizza", 20,  15);
        Food f4 = new Food("pizza with salami", 20,  16);
        List<Food> fs = new ArrayList<Food>();
        fs.add(f1);
        fs.add(f2);
        fs.add(f3);
        List<Food> fs2 = new ArrayList<Food>();
        fs2.add(f1);
        fs2.add(f2);
        fs2.add(f4);
        Scheduler a = new Scheduler(fs, "food1");
        Scheduler b = new Scheduler(fs2, "food2");
        a.addOrder(new Order(f1, 0));
        a.addOrder(new Order(f2, 0));
        a.addOrder(new Order(f3, 1));
        a.addOrder(new Order(f3, 2));
        b.addOrder(new Order(f1, 0));
        b.addOrder(new Order(f2, 0));
        b.addOrder(new Order(f2, 0));
        b.addOrder(new Order(f1, 1));
        b.addOrder(new Order(f1, 2));
        schedulers.add(a);
        schedulers.add(b);
        /**
         * start running schedulers
         */
        for (int i=0;i<schedulers.size();i++){
            schedulers.get(i).start();
        }

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
        System.out.println("User requested recommended stand for " + foodtype);
        return recommend(foodtype);
    }

    /**
     * @return global menu in JSON format
     * sent request to localhost:8080/menu
     * This iterates all menus of the schedulers, checks for unique items,
     * and puts these in a JSON file with their price.
     * In the JSON file the keys are the menu item names and the values are the prices
     */
    @RequestMapping("/menu")
    public JSONObject requestTotalMenu() { //start with id=1 (temporary)
        System.out.println("request total menu");
        JSONObject obj = new JSONObject();
        List<String> s = new ArrayList<String>();
        for (int j = 0; j<schedulers.size();j++){
            for (int i = 0; i<schedulers.get(j).getMenu().size();i++){
                if (!s.contains(schedulers.get(j).getMenu().get(i).getType())){
                    obj.put(schedulers.get(j).getMenu().get(i).getType(),schedulers.get(j).getMenu().get(i).getPrice()); // assume prices of same product are equal
                    s.add(schedulers.get(j).getMenu().get(i).getType());
                }

            }
        }

        return obj;
    }
    /**
     * @return specific stand menu in JSON format
     * sent POST request to localhost:8080/standmenu
     * @RequestParam() String standname: post the name of a stand
     * (In Postman: Select POST, go to params, enter "standname" as KEY and enter the name of a stand as value)
     * (ex:localhost:8080/standmenu?standname=food1)
     * (in current test above: "food1" and "food2" are names of stands)
     * This iterates menu of the named schedulers,
     * and puts the menu items in a JSON file with their price.
     * In the JSON file the keys are the menu item names and the values are the prices
     */
    @RequestMapping(value ="/standmenu", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject requestStandMenu(@RequestParam() String standname) {
        System.out.println("request menu of stand " + standname);
        JSONObject obj = new JSONObject();
        int n=-1;
        for (int i = 0; i<schedulers.size();i++){
            if (schedulers.get(i).getStandname().equals(standname)){
                n=i;
            }
        }
        if (n==-1){
            System.out.println("Wrong name");
            return null;
        }
        for (int i = 0; i<schedulers.get(n).getMenu().size();i++){
            obj.put(schedulers.get(n).getMenu().get(i).getType(),schedulers.get(n).getMenu().get(i).getPrice());
        }
        return obj;
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
        List<JSONObject> bestStands = new ArrayList<>();
        String standname="";
        System.out.println("0");
        for (int i = 0; i < schedulers.size(); i++) {
            System.out.println("1");
            if (schedulers.get(i).checkType(type)) {
                System.out.println("2");
                int n = schedulers.get(i).timeSum();
                if (n < s) {
                    JSONObject newStand = new JSONObject();
                    s = n;
                    standname = schedulers.get(i).getStandname();
                    newStand.put("stand", standname);

                    System.out.println("DE STANDNAME IS : " + standname);

                }
            }
        }
        //obj.put("stand", standname);
        //obj.put("stand2", "foo");
        //obj.put("stand3", "foo");
        //obj.put("stand4", "foo");
        //obj.put("stand5", "foo");

        System.out.print(obj.toJSONString());
        return obj;
    }

}
