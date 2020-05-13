package cobol.services.ordermanager.domain.entity;

import cobol.commons.domain.CommonBrand;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Data
public class Brand implements Serializable {

    @Id
    private String name;

    @OneToMany(mappedBy = "standId.brand", cascade = CascadeType.ALL)
    @JsonProperty("stand")
    private List<Stand> standList = new ArrayList<>();

    public Brand() {

    }

    public Brand(String name) {
        this.name = name;
    }

    public Brand(CommonBrand commonBrand) {

        //Set the brandname of the underlying stands
        commonBrand.getStandList().forEach(commonStand -> commonStand.setBrandName(commonBrand.getName()));

        this.name = commonBrand.getName();
        standList.addAll(commonBrand.getStandList().stream().map(Stand::new).collect(Collectors.toList()));
    }


    // --- GETTERS & SETTERS ---

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Stand> getStandList() {
        return standList;
    }

    public void setStandList(List<Stand> standList) {
        standList.forEach(s -> s.setBrand(this));

        this.standList = standList;
    }

    // --- EXTRA ---

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


    @Override
    public String toString() {
        return "Brand{" +
                "name='" + name +
                '}';
    }
}
