package cobol.services.ordermanager.domain.entity;

import cobol.commons.CommonStand;
import cobol.services.ordermanager.domain.SpringContext;
import cobol.services.ordermanager.domain.repository.BrandRepository;
import cobol.services.ordermanager.domain.repository.CategoryRepository;
import cobol.services.ordermanager.domain.repository.StandRepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

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
    private double longitude;
    private double latitude;

    @OneToMany(mappedBy = "foodId.stand", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonProperty("menu")
    List<Food> foodList = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "stand")
    List<Order> orderList = new ArrayList<>();


    public Stand() {
    }

    public Stand(String name, String brandName) {
        BrandRepository brandRepository = SpringContext.getBean(BrandRepository.class);
        Brand brand = brandRepository.findById(brandName).orElse(new Brand(brandName));
        this.standId = new StandId(name, brand);
    }

    public Stand(CommonStand cs){

        //Update general fields
        this.longitude = cs.getLongitude();
        this.latitude = cs.getLatitude();

        //Set the brand in the standId
        BrandRepository brandRepository = SpringContext.getBean(BrandRepository.class);
        Brand brand = brandRepository.findById(cs.getBrandName()).orElse(new Brand(cs.getBrandName()));
        this.standId = new StandId(cs.getName(), brand);

        //Update food list
        cs.getMenu().forEach(cf -> {
            //TODO: should not be nescessary if everything is sent correctly
            cf.setBrandName(cs.getBrandName());
            cf.setStandName(cs.getName());

            Food food = new Food(cf);
            foodList.add(food);
            food.setStand(this);
        });

    }

    /**
     * Transform a CommonStand object to a Stand object and attach it with a Brand object
     * - used to add Stand to database
     *
     * @param commonStand CommonStand object
     * @param brand Brand object
     */
    public Stand(CommonStand commonStand, Brand brand) {
        this.standId = new StandId(commonStand.getName(), brand);
        this.latitude = commonStand.getLatitude();
        this.longitude = commonStand.getLongitude();
        commonStand.getMenu().forEach(cf -> {
            Food food = new Food(cf, this);
            foodList.add(food); //Map bidirectional relationship
            food.setStand(this);
        });
    }


    /**
     * Transform Stand object to CommonStand object
     * - Used to send Stand to StandManager
     *
     * @return CommonStand object
     */
    public CommonStand asCommonStand() {
        return new CommonStand(
                this.standId.getName(),
                this.getBrandName(),
                this.latitude,
                this.longitude,
                this.getFoodList().stream().map(Food::asCommonFood).collect(Collectors.toList())
        );
    }

    // ---- Updaters ---- //
    public void update(CommonStand commonStand) {
        this.longitude = commonStand.getLongitude();
        this.latitude = commonStand.getLatitude();
    }


    // ---- Getters and Setters ----//

    public String getName(){
        return standId.name;
    }

    public String getBrandName(){
        return standId.brand.getName();
    }


    @JsonProperty("name")
    public void setName(String name) {
        standId = (standId == null) ? new StandId() : standId;
        this.standId.name = name;
    }

    @JsonProperty("brandName")
    public void setBrand(Brand brand) {
        standId = (standId == null) ? new StandId() : standId;
        this.standId.brand = brand;
    }

    @JsonIgnore
    public Brand getBrand() {
        return standId.brand;
    }

    // ---- Extra ---- //
    @Override
    public String toString() {
        return "Stand{" +
                getName() +
                "_" +
                getBrandName() +
                "}";
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


    // ---- Composite Id ----//

    @Embeddable
    @Data
    @AllArgsConstructor
    public static class StandId implements Serializable {

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

        @Override
        public String toString() {
            return name + "_" + brand.getName();
        }
    }
}


