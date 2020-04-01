package cobol.commons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommonStand implements Serializable {
    private List<CommonFood> menu = new ArrayList<>();

    private String name;
    private String brandName;
    private double lat;
    private double lon;
    public CommonStand(){
        super();//needed for ObjectMapper
    }

    public CommonStand(String name, String brandName, double lat, double lon, List<CommonFood> menu){
        this.name = name;
        this.brandName = brandName;
        this.lat = lat;
        this.lon = lon;
        this.menu = menu;
    }

    public CommonStand(String name, String brandName, double lat, double lon) {
        this.name = name;
        this.brandName = brandName;
        this.lat = lat;
        this.lon = lon;
    }

    public void addMenuItem(CommonFood mi){
        menu.add(mi);
    }

    public List<CommonFood> getMenu(){
        return this.menu;
    }

    public String getName(){
        return this.name;
    }

    public String getBrandName(){
        return this.brandName;
    }

    public double getLon(){
        return this.lon;
    }

    public double getLat(){
        return this.lat;
    }
}
