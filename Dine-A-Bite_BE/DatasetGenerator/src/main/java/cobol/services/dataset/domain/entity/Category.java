package cobol.services.dataset.domain.entity;

import cobol.services.dataset.domain.json.BrandDeserializer;
import cobol.services.dataset.domain.json.CategoryDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
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

    @ManyToMany(mappedBy = "category")
    List<Food> foodList = new ArrayList<>();

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
