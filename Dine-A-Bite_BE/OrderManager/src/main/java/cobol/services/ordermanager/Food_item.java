package cobol.services.ordermanager;

import java.util.List;

/**
 * Menu Item
 */
public class Food_item {
    private String name;
    private int preptime;
    private double price;
    private String description;
    private List<String> category;

    public Food_item(String name, int preptime, double price) {
        this.name = name;
        this.preptime = preptime;
        this.price = price;
        this.description=null;
        this.category=null;

    }
    public Food_item(String name, int preptime, double price, String description, List<String> category) {
        this.name = name;
        this.preptime = preptime;
        this.price = price;
        this.description=description;
        this.category=category;
    }
    /**
     *  TODO: preptime changes dynamically depending on average time between "preparation" and "done" flags
     */
    public void setPreptime(int nieuwpreptime) {
        this.preptime=nieuwpreptime;
    }
    public int getTime() {
        return preptime;
    }

    public String getType(){
        return name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getCategory() {
        return category;
    }
}
