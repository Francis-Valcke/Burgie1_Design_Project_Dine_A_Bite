package cobol.services.ordermanager.domain.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Food {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    private float price;

    private int preparationTime;

    private String name;

    private String description;

    private String brandName;

    private int stock;

    @ElementCollection(fetch=FetchType.EAGER)
    @Column(name = "category_category")
    private List<String> category = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinTable(name = "food_stand",
            joinColumns = {
                    @JoinColumn(name = "food_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "stand_id")
            }
    )
    private Stand stand;

    public Food(String name, String brandName, int stock) {
        this.name = name;
        this.brandName = brandName;
        this.stock = stock;
    }


    public void addCategory(String category){
        this.category.add(category);
    }
}

