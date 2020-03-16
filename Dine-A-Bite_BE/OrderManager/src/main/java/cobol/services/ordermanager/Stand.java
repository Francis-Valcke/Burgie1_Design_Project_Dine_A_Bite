package cobol.services.ordermanager;
import cobol.services.ordermanager.dbmenu.*;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class Stand {
    private String standname;
    private ArrayList<Food> menu;

    public Stand(String standname) {
        this.standname = standname;
    }
    public ArrayList<Food> getMenu(){
        return menu;
    }
    public JSONObject setMenu(){
        JSONObject obj = new JSONObject();
        for (int i = 0; i<menu.size();i++){
            obj.put(menu.get(i).getType(),menu.get(i).getPrice());
        }
        return obj;
    }
    public void fetchMenu(){

    }
    public String getStandname() {
        return standname;
    }
}
