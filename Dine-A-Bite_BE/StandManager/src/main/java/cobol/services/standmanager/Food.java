package cobol.services.standmanager;

/**
 * Menu Item
 */
public class Food {
    private String name;
    private int preptime;
    private double price;

    public Food(String name, int preptime, double price) {
        this.name = name;
        this.preptime = preptime;
        this.price = price;
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
}
