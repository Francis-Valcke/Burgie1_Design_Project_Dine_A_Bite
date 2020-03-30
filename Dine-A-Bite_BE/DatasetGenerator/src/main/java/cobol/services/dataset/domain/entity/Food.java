package cobol.services.dataset.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class Food implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinTable(
            name = "stand_food",
            joinColumns = {
                    @JoinColumn(name = "food_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "stand_id")
            }
    )
    private Stand stand;

    private String description;

    private float price;

    private int preparationTime;

    private int stock;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "food_category",
        joinColumns = { @JoinColumn(name = "food_id") },
            inverseJoinColumns = { @JoinColumn(name = "category_category") }
    )
    private List<Category> category = new ArrayList<>();



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Food food = (Food) o;
        return id == food.id &&
                Float.compare(food.price, price) == 0 &&
                preparationTime == food.preparationTime &&
                stock == food.stock &&
                Objects.equals(name, food.name) &&
                Objects.equals(description, food.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, price, preparationTime, stock);
    }
}

