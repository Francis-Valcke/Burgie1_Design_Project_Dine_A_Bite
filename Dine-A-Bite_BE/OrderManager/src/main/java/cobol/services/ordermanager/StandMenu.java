package cobol.services.ordermanager;
import cobol.services.ordermanager.dbmenu.*;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StandMenu {
    private String standname;
    private ArrayList<Food_item> menu;
    private String brandname;

    public StandMenu(String standname) {
        this.standname = standname;
    }
    public ArrayList<Food_item> getMenu(){
        return menu;
    }
    public JSONObject fetchMenu(){
        JSONObject obj = new JSONObject();
        for (int i = 0; i<menu.size();i++){
            List l = new ArrayList();
            l.add(menu.get(i).getPrice());
            l.add(menu.get(i).getCategory());
            l.add(menu.get(i).getDescription());
            obj.put(menu.get(i).getType(),l);
        }
        return obj;
    }
    public void setMenu(ArrayList<Food_item> menu){
        this.menu=menu;

    }
    public String getStandname() {
        return standname;
    }

    public String getBrandname() {
        return brandname;
    }

    public void setBrandname(String brandname) {
        this.brandname = brandname;
    }
}
