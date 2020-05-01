package cobol.services.ordermanager.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.*;

@Data
@Entity
//@JsonDeserialize(using = CategoryDeserializer.class)
public class Category {

    @Id
    private String category;

    @JsonIgnore
    @ManyToMany(mappedBy = "category")
    private List<Food> foodList = new ArrayList<>();

    public Category() {
    }

    public Category(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Category{" +
                category +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category1 = (Category) o;
        return category.equals(category1.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category);
    }
}
