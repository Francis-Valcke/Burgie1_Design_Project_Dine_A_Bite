package cobol.services.dataset.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Stand implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @JsonProperty("lon")
    private double longitude;

    @JsonProperty("lat")
    private double latitude;

    @ManyToOne()
    @JoinColumn(name = "brand_name")
    private Brand brand;

    @Transient
    List<Food> food = new ArrayList<>();

    //@Builder.Default
    //@OneToMany(mappedBy = "stand")
    @Transient
    private List<Stock> stock = new ArrayList<>();

    public Stand(String name, double longitude, double latitude, List<Stock> stockList) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        //this.stockList = stockList;
        //
        //for (Food food : brand.getFoodList()) {
        //
        //}
    }
}


