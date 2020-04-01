package cobol.commons;

import java.io.Serializable;
import java.util.ArrayList;

public class CommonStand implements Serializable {
    private ArrayList<CommonFood> menu = new ArrayList<>();
    private int id;
    private String name;
    private String brandName;
    private long lat;
    private long lon;
    public CommonStand(){
        super();//needed for ObjectMapper
    }
    public CommonStand(int id, String name, String brandName, long lat, long lon){
        this.id = id;
        this.name = name;
        this.brandName = brandName;
        this.lat = lat;
        this.lon = lon;
    }
    public CommonStand(String name, String brandName, long lat, long lon, ArrayList<CommonFood> menu){
        this.id = 0;
        this.name = name;
        this.brandName = brandName;
        this.lat = lat;
        this.lon = lon;
        this.menu=menu;
    }

    public CommonStand(String name, String brandName, long lat, long lon) {
        this.id = 0;
        this.name = name;
        this.brandName = brandName;
        this.lat = lat;
        this.lon = lon;
    }

    public void setId(int id){
        this.id = id;
    }
    public void addMenuItem(CommonFood mi){
        menu.add(mi);
    }

    public ArrayList<CommonFood> getMenu(){
        return this.menu;
    }

    public int getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public String getBrandName(){
        return this.brandName;
    }

    public long getLon(){
        return this.lon;
    }

    public long getLat(){
        return this.lat;
    }
}
