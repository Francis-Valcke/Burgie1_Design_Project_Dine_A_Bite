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
import java.util.Objects;

@Entity
@Data
@JsonDeserialize(using = BrandDeserializer.class)
public class Brand implements Serializable {

    @Id
    private String name;

    @OneToMany(mappedBy = "standId.brand", cascade = CascadeType.ALL)
    @JsonProperty("food")
    private List<Stand> standList = new ArrayList<>();

    public Brand() {
    }

    public Brand(String name) {
        this.name = name;
        standList = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Brand brand = (Brand) o;
        return Objects.equals(name, brand.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
