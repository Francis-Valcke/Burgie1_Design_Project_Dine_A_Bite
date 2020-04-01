package cobol.services.ordermanager.domain.entity;

import cobol.services.ordermanager.domain.json.CategoryDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Entity
@JsonDeserialize(using = CategoryDeserializer.class)
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
