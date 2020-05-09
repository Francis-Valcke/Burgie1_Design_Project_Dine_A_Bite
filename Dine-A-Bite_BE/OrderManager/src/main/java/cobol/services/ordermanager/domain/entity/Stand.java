package cobol.services.ordermanager.domain.entity;

import cobol.commons.domain.CommonStand;
import cobol.services.ordermanager.domain.SpringContext;
import cobol.services.ordermanager.domain.repository.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Stand implements Serializable {

    @JsonIgnore
    @EmbeddedId
    private StandId standId = new StandId();

    private double longitude;

    private double latitude;

    private BigDecimal revenue = BigDecimal.ZERO;

    @OneToMany(mappedBy = "foodId.stand", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonProperty("menu")
    List<Food> foodList = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "stand", cascade = CascadeType.ALL)
    List<Order> orderList = new ArrayList<>();

    @ManyToMany
    @Cascade({org.hibernate.annotations.CascadeType.PERSIST, org.hibernate.annotations.CascadeType.REFRESH})
    @JoinTable(name = "stand_user",
            joinColumns = {
                    @JoinColumn(referencedColumnName = "name", name = "stand_name", foreignKey = @ForeignKey(name = "stand_user_stand_fk")),
                    @JoinColumn(referencedColumnName = "brand_name", name = "brand_name", foreignKey = @ForeignKey(name = "stand_user_stand_fk"))
            },
            inverseJoinColumns = {
                    @JoinColumn(referencedColumnName = "username", name = "user_username", foreignKey = @ForeignKey(name = "stand_user_user_fk"))
            }
    )
    Set<User> owners = new HashSet<>();

    public Stand() {

    }

    public Stand(String name, String brandName) {
        BrandRepository brandRepository = SpringContext.getBean(BrandRepository.class);
        Brand brand = brandRepository.findById(brandName).orElse(new Brand(brandName));
        this.standId = new StandId(name, brand);
        this.revenue = BigDecimal.ZERO;
    }

    public Stand(CommonStand cs){

        //Update general fields
        this.longitude = cs.getLongitude();
        this.latitude = cs.getLatitude();
        this.revenue = cs.getRevenue();

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

    public Stand update(CommonStand cs){

        //Update general fields
        this.longitude = cs.getLongitude() < 0 ? this.longitude : cs.getLongitude();
        this.latitude = cs.getLatitude() < 0 ? this.latitude : cs.getLatitude();

        // A map that allows search by hashcode
        Map<Food, Food> originalMenu = foodList.stream().collect(Collectors.toMap(f -> f, f -> f));
        // Clear the original menu
        foodList.clear();
        // Build the new menu
        cs.getMenu().forEach(cf -> {

            //TODO: should not be nescessary if everything is sent correctly
            cf.setBrandName(cs.getBrandName());
            cf.setStandName(cs.getName());

            //Create a new Food object based on the common food to use as key
            Food newFood = new Food(cf);

            //The food item already exists and needs to be updated
            if (originalMenu.containsKey(newFood)){
                newFood = originalMenu.get(newFood);
                newFood.update(cf);
            }

            //As the list was cleared, every food item, new or old and adjusted needs to be added back to the foodList
            foodList.add(newFood);
            newFood.setStand(this);

        });

        return this;
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
        this.revenue = commonStand.getRevenue();
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
                this.getFoodList().stream().map(Food::asCommonFood).collect(Collectors.toList()),
                this.revenue
        );
    }

    public Map<String, Double> getLocation() {
        Map<String, Double> location = new HashMap<>();
        location.put("latitude", this.latitude);
        location.put("longitude", this.longitude);
        return location;
    }

    // ---- Getters and Setters ----//

    // -- food id --

    @JsonIgnore
    public StandId getStandId() {
        return standId;
    }

    @JsonIgnore
    public void setStandId(StandId standId) {
        this.standId = standId;
    }

    public String getName(){
        return standId.name;
    }

    public void setName(String name){
        standId.name = name;
    }

    @JsonIgnore
    public Brand getBrand(){
        return standId.brand;
    }

    @JsonIgnore
    public void setBrand(Brand brand){
        standId.brand = brand;
    }

    public String getBrandName(){
        return standId.brand.getName();
    }

    @JsonIgnore
    public void setBrandName(String brandName){
        standId.brand.setName(brandName);
    }


    // -- other fields --

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getRevenue() { return revenue; }

    public void addToRevenue(BigDecimal addedRevenue) { this.revenue = this.revenue.add(addedRevenue); }

    public void setRevenue(BigDecimal newRevenue) { this.revenue = newRevenue; }

    public List<Food> getFoodList() {
        return foodList;
    }

    public void setFoodList(List<Food> foodList) {
        foodList.forEach(f -> f.setStand(this));

        this.foodList = foodList;
    }

    @JsonIgnore
    public List<Order> getOrderList() {
        return orderList;
    }

    @JsonIgnore
    public void setOrderList(List<Order> orderList) {
        this.orderList = orderList;
    }

    public Set<User> getOwners() {
        return owners;
    }

    public void setOwners(List<User> newOwners) {
        UserRepository userRepository = SpringContext.getBean(UserRepository.class);
        Set<User> owners = newOwners.stream().map(userRepository::save).collect(Collectors.toSet());

        this.owners = owners;
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
        return Objects.equals(getName(), stand.getName()) &&
                Objects.equals(getBrandName(), stand.getBrandName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getBrandName());
    }


    // ---- Composite Id ----//

    @Embeddable
    @Data
    @AllArgsConstructor
    public static class StandId implements Serializable {

        private String name;

        @JsonIgnore
        @ManyToOne
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


