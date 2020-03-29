package cobol.commons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StandInfo implements Serializable {
    private ArrayList<MenuItem> menu = new ArrayList<>();
    private int id;
    private String name;
    private String brand;
    private long lat;
    private long lon;
    public StandInfo(){
        super();//needed for ObjectMapper
    }
    public StandInfo(int id, String name, String brand, long lat, long lon){
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.lat = lat;
        this.lon = lon;
    }
    public StandInfo(String name, String brand, long lat, long lon, ArrayList<MenuItem> menu){
        this.id = 0;
        this.name = name;
        this.brand = brand;
        this.lat = lat;
        this.lon = lon;
        this.menu=menu;
    }

    public StandInfo(String name, String brand, long lat, long lon) {
        this.id = 0;
        this.name = name;
        this.brand = brand;
        this.lat = lat;
        this.lon = lon;
    }

    public void setId(int id){
        this.id = id;
    }
    public void addMenuItem(MenuItem mi){
        menu.add(mi);
    }

    public ArrayList<MenuItem> getMenu(){
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

    public long getLon(){
        return this.lon;
    }

    public long getLat(){
        return this.lat;
    }
}
