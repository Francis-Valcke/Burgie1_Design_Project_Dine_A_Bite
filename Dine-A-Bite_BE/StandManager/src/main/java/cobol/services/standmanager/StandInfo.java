package cobol.services.standmanager;

import java.util.HashMap;
import java.util.Map;

public class StandInfo {
    private Map<String, int[]> menu = new HashMap<>();
    private int id;
    private String name;
    private String brand;
    private Long lat;
    private Long lon;

    public StandInfo(int id, String name, String brand, Long lat, Long lon){
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.lat = lat;
        this.lon = lon;
    }

    public void addMenuItem(String name, int price, int prepTime){
        int [] info = {price, prepTime};
        menu.put(name, info);
    }

    public Map<String, int[]> getMenu(){
        return this.menu;
    }

    public int getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public String getBrand(){
        return this.brand;
    }

    public Long getLon(){
        return this.lon;
    }

    public Long getLat(){
        return this.lat;
    }
}
