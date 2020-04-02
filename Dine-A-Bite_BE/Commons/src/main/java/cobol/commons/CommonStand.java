package cobol.commons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommonStand implements Serializable {
    private List<CommonFood> menu = new ArrayList<>();

    private String name;
    private String brandName;
    private double latitude;
    private double longitude;

    public CommonStand(){}

    public CommonStand(String name, String brandName, double latitude, double longitude, List<CommonFood> menu){
        this.name = name;
        this.brandName = brandName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.menu = menu;
    }

    public CommonStand(String name, String brandName, double latitude, double longitude) {
        this.name = name;
        this.brandName = brandName;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public double getLongitude(){
        return this.longitude;
    }

    public double getLatitude(){
        return this.latitude;
    }
}
