package cobol.services.ordermanager.dbmenu;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity

public class Food_category {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int category_number;
    private int food_id;
    private String category;

    public Food_category() {
    }

    public int getCategory_number() {
        return category_number;
    }

    public int getFood_id() {
        return food_id;
    }

    public void setFood_id(int food_id) {
        this.food_id = food_id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
