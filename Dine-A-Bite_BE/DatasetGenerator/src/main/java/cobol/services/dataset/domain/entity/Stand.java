package cobol.services.dataset.domain.entity;

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
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
public class Stand implements Serializable {

    @EmbeddedId
    private StandId standId;

    @Column(unique=true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JsonProperty("lon")
    private double longitude;

    @JsonProperty("lat")
    private double latitude;

    @OneToMany(mappedBy = "foodId.stand", cascade = CascadeType.ALL)
    @JsonProperty("food")
    List<Food> foodList = new ArrayList<>();

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class StandId implements Serializable{

        private String name;

        @ManyToOne()
        @JoinColumn(name = "brand_name")
        private Brand brand;
    }
}


