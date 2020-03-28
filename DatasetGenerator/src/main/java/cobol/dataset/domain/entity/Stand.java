package cobol.dataset.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private double longitude;

    private double latitude;

    @ManyToOne()
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Builder.Default
    @ManyToMany(mappedBy = "standList")
    private List<Food> foodList = new ArrayList<>();


    public void addBrand(Brand brand) {
        this.brand = brand;
        brand.getStandList().add(this);
    }

    public void addFood(Food food) {
        foodList.add(food);
        food.getStandList().add(this);
    }

}


