package cobol.services.ordermanager.dbmenu;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Food {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
    private float price;
    private int preptime;
    private String name;
    private String description;
    private String brandname;
    @ElementCollection(fetch=FetchType.EAGER)
    @Column(name = "category_category")
    private List<String> category = new ArrayList<>();
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

    public String getBrandname() {
        return brandname;
    }

    public void setBrandname(String brandname) {
        this.brandname = brandname;
    }

    public List<String> getCategory() {
        return category;
    }

    public void setCategory(List<String> category) {
        this.category = category;
    }
    public void addCategory(String category){
        this.category.add(category);
    }
}

