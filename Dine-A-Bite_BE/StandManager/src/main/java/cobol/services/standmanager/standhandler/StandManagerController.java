package cobol.services.standmanager.standhandler;




import cobol.services.eventchannel.Event;
import cobol.services.eventchannel.EventPublisher;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
public class StandManagerController extends EventPublisher {

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
