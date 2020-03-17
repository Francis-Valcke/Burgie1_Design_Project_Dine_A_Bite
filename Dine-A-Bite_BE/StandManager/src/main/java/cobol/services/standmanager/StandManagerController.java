package cobol.services.standmanager;

import cobol.services.ordermanager.Order;
import org.springframework.web.bind.annotation.*;



import cobol.services.eventchannel.Event;
import cobol.services.eventchannel.EventPublisher;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestController
public class StandManagerController {

    /**
     * The controller has a list of all schedulers.
     * More information on schedulers in class Scheduler
     */
    private List<Scheduler> schedulers = new ArrayList<Scheduler>();



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
     * @param type: type of food for which attendee seeks stand recommendation
     * @return JSON object with standname
     * Iterates over schedulers and looks for scheduler (that has the requested food item in menu) with shortest queue
     * TODO: instead of returning 1 stand: return list of stands in recommended order
     */
    public JSONObject recommend(String type) {
        //choose how many recommends you want
        int amountOfRecommends = 3;

        //group all stands (schedulers) with the correct type of food available
        ArrayList<Scheduler> goodSchedulers = new ArrayList<>();
        for (int i = 0; i < schedulers.size(); i++) {
            Scheduler currentScheduler = schedulers.get(i);
            if (currentScheduler.checkType(type)) {
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
            add.put("Standname", curScheduler.getStandname());
            add.put("Queuetime", curScheduler.timeSum());
            System.out.println(curScheduler.timeSum());
            obj.put(curScheduler.getStandname(), add);
        }


        return obj;
    }

}
