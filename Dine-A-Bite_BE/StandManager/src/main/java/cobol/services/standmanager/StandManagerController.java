package cobol.services.standmanager;

import cobol.services.ordermanager.Order;
import org.springframework.web.bind.annotation.*;
import cobol.services.ordermanager.Food;



import cobol.services.eventchannel.Event;
import cobol.services.eventchannel.EventPublisher;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
        Scheduler a = new Scheduler(fs, "food1", 1);
        Scheduler b = new Scheduler(fs2, "food2",2);
        /*a.addOrder(new Order(f1, 0));
        a.addOrder(new Order(f2, 0));
        a.addOrder(new Order(f3, 1));
        a.addOrder(new Order(f3, 2));
        b.addOrder(new Order(f1, 0));
        b.addOrder(new Order(f2, 0));
        b.addOrder(new Order(f2, 0));
        b.addOrder(new Order(f1, 1));
        b.addOrder(new Order(f1, 2));
        */

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
    //@RequestMapping(value ="/post", method = RequestMethod.POST)
    @RequestMapping(value = "/post", consumes = "application/json")
    @ResponseBody
    public JSONObject postOrder(@RequestBody() Order order) {
        System.out.println("User requested recommended stand for " + order.id);
        return recommend(order);
    }


    /**
     * @param type: type of food for which attendee seeks stand recommendation
     * @return JSON object with standname
     * Iterates over schedulers and looks for scheduler (that has the requested food item in menu) with shortest queue
     * TODO: instead of returning 1 stand: return list of stands in recommended order
     */
    public JSONObject recommend(Order order) {
        //first get the Map with all the food of the order
        Map<Food, Integer> foodMap = order.getFull_order();

        //choose how many recommends you want
        int amountOfRecommends = 3;

        //group all stands (schedulers) with the correct type of food available
        ArrayList<Scheduler> goodSchedulers = new ArrayList<>();
        for (int i = 0; i < schedulers.size(); i++) {
            Boolean validStand = true;
            Scheduler currentScheduler = schedulers.get(i);
            for (Food food : foodMap.keySet()) {
                if (currentScheduler.checkType(food.getType())) {
                    validStand = true;
                }
                else{
                    validStand = false;
                    break;
                }
            }
            if (validStand == true){
                goodSchedulers.add(currentScheduler);
            }
        }

        //sort the stands (schedulers) based on remaining time
        Collections.sort(goodSchedulers, new SchedulerComparator());

        //check if you have enough stands (for amount of recommendations you want)
        if (goodSchedulers.size() < amountOfRecommends){
            amountOfRecommends = goodSchedulers.size();
        }

        //put everything into a JSON file to give as return value
        JSONObject obj = new JSONObject();
        for (int i = 0 ; i < amountOfRecommends ; i++){
            JSONObject add = new JSONObject();
            Scheduler curScheduler = goodSchedulers.get(i);
            add.put("stand_id", curScheduler.getStandId());
            add.put("time_estimate", curScheduler.timeSum());
            System.out.println(curScheduler.timeSum());
            obj.put(curScheduler.getStandname(), add);
        }


        return obj;
    }

}
