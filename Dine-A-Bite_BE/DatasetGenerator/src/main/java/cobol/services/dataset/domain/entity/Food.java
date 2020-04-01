package cobol.services.dataset.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;


import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Food implements Serializable {

    @EmbeddedId
    private FoodId foodId;

    private String description;

    private float price;

    private int preparationTime;

    private int stock;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "food_category",
            joinColumns = {
                @JoinColumn(referencedColumnName = "name", name = "food_name"),
                @JoinColumn(referencedColumnName = "stand_name", name = "stand_name"),
                @JoinColumn(referencedColumnName = "brand_name", name = "brand_name")
            },
            inverseJoinColumns = {@JoinColumn(name = "category_category")}
    )
    private List<Category> category = new ArrayList<>();

    public String getName() {
        return foodId.name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        foodId = (foodId == null) ? new Food.FoodId() : foodId;
        this.foodId.name = name;
    }

    public Stand getStand() {
        return foodId.stand;
    }


    public void setStand(Stand stand) {
        foodId = (foodId == null) ? new Food.FoodId() : foodId;
        this.foodId.stand = stand;
    }

    public Food() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Food food = (Food) o;
        return Objects.equals(foodId, food.foodId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foodId);
    }

    @Data
    @AllArgsConstructor
    @Embeddable
    public static class FoodId implements Serializable {

        private String name;

        @ManyToOne()
        @JoinColumn(referencedColumnName = "name", name = "stand_name")
        @JoinColumn(referencedColumnName = "brand_name",name = "brand_name")
        private Stand stand;

        public FoodId() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FoodId foodId = (FoodId) o;
            return Objects.equals(name, foodId.name) &&
                    Objects.equals(stand, foodId.stand);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, stand);
        }
    }
}

