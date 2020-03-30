package cobol.services.dataset.domain.entity;

import cobol.services.dataset.domain.json.BrandDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Builder
@JsonDeserialize(using = BrandDeserializer.class)
public class Brand implements Serializable {

    @Id
    private String name;

    @Builder.Default
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL)
    private Set<Food> food = new HashSet<>();

    @Builder.Default
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL)
    private List<Stand> stand = new ArrayList<>();

    public Brand(String name) {
        this.name = name;
        food = new HashSet<>();
        stand = new ArrayList<>();
    }

    public Brand(String name, Set<Food> foodList, List<Stand> standList) {
        this.name = name;
        this.food = foodList;
        this.stand = standList;

        foodList.forEach(food -> food.setBrand(this));
        standList.forEach(stand -> stand.setBrand(this));
    }

}
