package cobol.dataset.domain.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Category {

    @Id
    private String category;

    @ManyToMany(mappedBy = "category")
    List<Food> foodList = new ArrayList<>();

    public Category() {
    }

    public Category(String category) {
        this.category = category;
    }
}
