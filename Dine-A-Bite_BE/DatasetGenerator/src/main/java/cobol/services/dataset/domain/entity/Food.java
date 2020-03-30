package cobol.services.dataset.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Food implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private String description;

    private float price;

    private int preparationTime;

    @Transient
    private int stock;

    @Builder.Default
    @ManyToMany()
    @JoinTable(name = "food_category",
        joinColumns = { @JoinColumn(name = "food_id") },
            inverseJoinColumns = { @JoinColumn(name = "category_category") }
    )
    private List<Category> category = new ArrayList<>();

    @ManyToOne()
    @JoinColumn(name = "brand_name")
    private Brand brand;

    //@Builder.Default
    //@OneToMany()
    @Transient
    private List<Stock> stockList = new ArrayList<>();

    public Food(String name, String description, float price, int preparationTime, List<Category> category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.preparationTime = preparationTime;
        this.category = category;

        category.forEach(c -> c.getFoodList().add(this));
    }


    //@Builder.Default
    //@ManyToMany()
    //@JoinTable(name = "food_stand",
    //    joinColumns = { @JoinColumn(name = "food_id") },
    //        inverseJoinColumns = { @JoinColumn(name = "stand_id") }
    //)
    //private List<Stand> standList = new ArrayList<>();

    //@Builder.Default
    //@OneToMany(mappedBy = "food")
    //private List<Stock> stockList = new ArrayList<>();


    //public void addBrand(Brand brand) {
    //    this.brand = brand;
    //    brand.getFoodList().add(this);
    //}

    //public void addStand(Stand stand) {
    //    standList.add(stand);
    //    stand.getFoodList().add(this);
    //}


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

