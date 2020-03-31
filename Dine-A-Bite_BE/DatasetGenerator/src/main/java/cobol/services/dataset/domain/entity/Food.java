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


    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @EmbeddedId
    private FoodId foodId;

    private String description;

    private float price;

    private int preparationTime;

    private int stock;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "food_category",
            joinColumns = {
                @JoinColumn(name = "food_id"),
                @JoinColumn(name = "stand_id")
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
        //@JoinTable(
        //        name = "stand_food",
        //        joinColumns = {
        //                @JoinColumn(name = "food_id")
        //        },
        //        inverseJoinColumns = {
        //                @JoinColumn(name = "stand_id")
        //        }
        //)
        @JoinColumn(name = "stand_id")
        private Stand stand;

    }
}

