package cobol.services.dataset.domain.entity;

import cobol.services.dataset.domain.json.BrandDeserializer;
import cobol.services.dataset.domain.json.StandDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
//@JsonDeserialize(using = StandDeserializer.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
public class Stand implements Serializable {

    @EmbeddedId
    private StandId standId;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JsonProperty("lon")
    private double longitude;

    @JsonProperty("lat")
    private double latitude;

    @OneToMany(mappedBy = "foodId.stand")
    @JsonProperty("food")
    List<Food> foodList = new ArrayList<>();

    @JsonProperty("name")
    public String getName() {
        return standId.name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        standId = (standId == null) ? new StandId() : standId;
        this.standId.name = name;
    }

    @JsonProperty("brandName")
    public Brand getBrand() {
        return standId.brand;
    }

    @JsonProperty("brandName")
    public void setBrand(Brand brand) {
        standId = (standId == null) ? new StandId() : standId;
        this.standId.brand = brand;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class StandId implements Serializable{

        private String name;

        @ManyToOne(cascade = CascadeType.DETACH)
        @JoinColumn(name = "brand_name")
        private Brand brand;


    }
}


