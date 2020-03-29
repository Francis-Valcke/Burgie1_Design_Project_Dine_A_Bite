package cobol.services.ordermanager.domain.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Stand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private String brandName;

    private double longitude;

    private double latitude;

    @OneToMany(mappedBy = "stand", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Food> foodList = new ArrayList<>();

    public Stand(String name, String brandName, double latitude, double longitude, List<Food> foodList) {
        this.name = name;
        this.brandName = brandName;
        this.latitude = latitude;
        this.longitude = longitude;
        foodList.forEach(f -> f.setStand(this));
        this.foodList = foodList;
    }

}


