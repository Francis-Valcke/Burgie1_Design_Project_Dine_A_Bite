package cobol.services.recommender;

import org.springframework.web.bind.annotation.*;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
public class RecommenderController {
    private List<Scheduler> schedulers = new ArrayList<Scheduler>();

    @RequestMapping(value ="/post", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject postOrder(@RequestBody String foodtype) {
        System.out.println(foodtype);
        return recommend(foodtype);
    }
    @RequestMapping("/start")
    public void start(){
        //test
        String ft1= "apple";
        String ft2= "burger";
        String ft3= "pizza";
        Food f1 = new Food("apple", 10);
        Food f2 = new Food("burger", 15);
        Food f3 = new Food("pizza", 20);
        List<String> fs = new ArrayList<String>();
        fs.add(ft1);
        fs.add(ft2);
        fs.add(ft3);
        List<String> fs2 = new ArrayList<String>();
        fs2.add(ft1);
        fs2.add(ft2);
        Scheduler a = new Scheduler(fs);
        Scheduler b = new Scheduler(fs2);
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
        //endtest
        for (int i=0;i<schedulers.size();i++){
            schedulers.get(i).start();
        }

    }

    public JSONObject recommend(String type) {
        JSONObject obj = new JSONObject();
        int s = 1000;
        int standid = 0;
        for (int i = 0; i < schedulers.size(); i++) {
            if (schedulers.get(i).checkType(type)) {
                int n = schedulers.get(i).timeSum();
                if (n < s) {
                    s = n;
                    standid = i;
                }
            }
        }
        obj.put("stand", standid);
        //obj.put("stand2", "foo");
        //obj.put("stand3", "foo");
        //obj.put("stand4", "foo");
        //obj.put("stand5", "foo");

        System.out.print(obj);
        return obj;
    }


}
