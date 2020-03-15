package cobol.services.ordermanager;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final public class MenuHandler implements Runnable{
    protected final static MenuHandler handler = new MenuHandler();
    private ArrayList<Stand> stands = new ArrayList<Stand>();
    private Map<String, JSONObject> standmenus = new HashMap<>();
    private JSONObject totalmenu;
    private MenuHandler() {
    }

    public static MenuHandler getMenuHandler(){
        return handler;
    }

    public JSONObject getStandnames(){
        JSONObject obj = new JSONObject();
        for (int i =0;i<stands.size();i++){
            obj.put(i ,stands.get(i).getStandname());
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
    public void setStandMenu(String standname){
        for (int i = 0;i<stands.size();i++){
            if (stands.get(i).getStandname().equals(standname)){
                stands.get(i).fetchMenu();
                standmenus.put(standname,stands.get(i).setMenu());
            }
        }

    }
    public void setMenu(){
        JSONObject obj = new JSONObject();
        List<String> s = new ArrayList<String>();
        for (int j = 0; j<stands.size();j++){
            for (int i = 0; i<stands.get(j).getMenu().size();i++){
                if (!s.contains(stands.get(j).getMenu().get(i).getType())){
                    obj.put(stands.get(j).getMenu().get(i).getType(),stands.get(j).getMenu().get(i).getPrice()); // assume prices of same product are equal
                    s.add(stands.get(j).getMenu().get(i).getType());
                }

            }
        }

        this.totalmenu=obj;
    }
    private void update(){

    }
    @Override
    public void run() {
        //Event standmenu change
        boolean b = false;
        if (b){
            String n="";//standname from event
            setStandMenu(n);
            setMenu();
        }
        //Event new stand added
        if (b){
            String n="";//standname from event
            stands.add(new Stand("n"));
            setStandMenu(n);
            setMenu();
        }
    }


}
