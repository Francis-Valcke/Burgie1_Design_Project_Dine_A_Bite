package cobol.services.ordermanager.dbmenu;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Food_price {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
    private float price;
    private int preptime;
    private String name;
    private String description;

    public int getId() {
        return id;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getPreptime() {
        return preptime;
    }

    public void setPreptime(int preptime) {
        this.preptime = preptime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
