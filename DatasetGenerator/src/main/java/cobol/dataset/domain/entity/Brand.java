package cobol.dataset.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Brand implements Serializable {

    @Id
    private String name;

    @Builder.Default
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL)
    private List<Food> foodList = new ArrayList<>();

    @Builder.Default
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL)
    private List<Stand> standList = new ArrayList<>();

    public Brand(String name) {
        this.name = name;
    }

    public void addStand(Stand stand){
        this.standList.add(stand);
        stand.setBrand(this);
    }

    public void addStandList(List<Stand> standList){
        standList.forEach(this::addStand);
    }

    public void addFood(Food food){
        this.foodList.add(food);
        food.setBrand(this);
    }

    public void addFoodList(List<Food> foodList){
        foodList.forEach(this::addFood);
    }
}
