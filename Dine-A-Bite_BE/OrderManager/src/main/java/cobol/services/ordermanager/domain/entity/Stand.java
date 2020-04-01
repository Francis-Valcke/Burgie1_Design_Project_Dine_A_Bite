package cobol.services.ordermanager.domain.entity;

import cobol.commons.CommonStand;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Stand implements Serializable {

    @JsonIgnore
    @EmbeddedId
    private StandId standId;

    @JsonProperty("lon")
    private double longitude;

    @JsonProperty("lat")
    private double latitude;

    @OneToMany(mappedBy = "foodId.stand", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
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


    //public String getBrandName(){
    //    return standId.brand.getName();
    //}

    @JsonProperty("brandName")
    public void setBrand(Brand brand) {
        standId = (standId == null) ? new StandId() : standId;
        this.standId.brand = brand;
    }

    @JsonIgnore
    public Brand getBrand() {
        return standId.brand;
    }

    public Stand() {
    }

    public Stand(CommonStand commonStand, Brand brand){
        brand.getStandList().add(this);
        this.standId= new StandId(commonStand.getName(), brand);
        this.latitude=commonStand.getLat();
        this.longitude=commonStand.getLon();
        Stand thisStand= this;
        this.foodList= commonStand.getMenu().stream().map(cf -> new Food(cf, thisStand)).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stand stand = (Stand) o;
        return Objects.equals(standId, stand.standId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(standId);
    }

    @Embeddable
    @Data
    @AllArgsConstructor
    public static class StandId implements Serializable{

        private String name;

        @JsonIgnore
        @ManyToOne(cascade = CascadeType.ALL)
        @JoinColumns(
                foreignKey = @ForeignKey(name = "stand_brand_fk"), value = {
                @JoinColumn(referencedColumnName = "name", name = "brand_name")
        }
        )
        private Brand brand;

        public StandId() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StandId standId = (StandId) o;
            return Objects.equals(name, standId.name) &&
                    Objects.equals(brand, standId.brand);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, brand);
        }
    }
}


