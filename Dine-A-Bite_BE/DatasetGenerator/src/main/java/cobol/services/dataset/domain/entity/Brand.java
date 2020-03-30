package cobol.services.dataset.domain.entity;

import cobol.services.dataset.domain.json.BrandDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@JsonDeserialize(using = BrandDeserializer.class)
@NoArgsConstructor
public class Brand implements Serializable {

    @Id
    private String name;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL)
    @JsonProperty("food")
    private List<Stand> standList = new ArrayList<>();

    public Brand(String name) {
        this.name = name;
        standList = new ArrayList<>();
    }

}
