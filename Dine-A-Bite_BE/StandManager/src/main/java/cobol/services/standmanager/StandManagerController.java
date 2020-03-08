package cobol.services.standmanager;

import org.springframework.web.bind.annotation.*;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

@RestController
public class StandManagerController {
    private List<Scheduler> schedulers = new ArrayList<Scheduler>();

    @RequestMapping(value ="/post", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject postOrder(@RequestBody String foodtype) {
        System.out.println(foodtype);
        return recommend(foodtype);
    }
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
    @RequestMapping(value ="/standmenu", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject requestStandMenu(@RequestBody String standname) {
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
    @RequestMapping("/start")
    public void start(){
        //tempory test, delete later
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
        //endtest
        for (int i=0;i<schedulers.size();i++){
            schedulers.get(i).start();
        }

    }

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
