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
public class Food {

    private static int idCounter = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    //private String brandName;

    private String description;

    private float price;

    private int preparationTime;

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
    //@ManyToMany()
    //@JoinTable(name = "food_stand",
    //    joinColumns = { @JoinColumn(name = "food_id") },
    //        inverseJoinColumns = { @JoinColumn(name = "stand_id") }
    //)
    //private List<Stand> standList = new ArrayList<>();

    //@Builder.Default
    //@OneToMany(mappedBy = "food")
    //private List<Stock> stockList = new ArrayList<>();


    public void addBrand(Brand brand) {
        this.brand = brand;
        brand.getFoodList().add(this);
    }

    //public void addStand(Stand stand) {
    //    standList.add(stand);
    //    stand.getFoodList().add(this);
    //}
}

