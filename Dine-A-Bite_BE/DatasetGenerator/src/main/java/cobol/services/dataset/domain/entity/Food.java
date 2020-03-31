package cobol.services.dataset.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@EqualsAndHashCode
public class Food implements Serializable {

    @EmbeddedId
    private FoodId foodId;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Embeddable
    public static class FoodId implements Serializable {

        private String name;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(referencedColumnName = "name", name = "stand_name")
        @JoinColumn(referencedColumnName = "brand_name",name = "brand_name")
        private Stand stand;

    }
}

