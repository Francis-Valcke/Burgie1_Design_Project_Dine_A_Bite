package cobol.services.ordermanager.domain.entity;

import cobol.commons.CommonFood;
import cobol.services.ordermanager.domain.SpringContext;
import cobol.services.ordermanager.domain.repository.BrandRepository;
import cobol.services.ordermanager.domain.repository.CategoryRepository;
import cobol.services.ordermanager.domain.repository.StandRepository;
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
public class Food {

    @JsonIgnore
    @EmbeddedId
    private FoodId foodId = new FoodId();

    private int stock;

    private String description;
    private float price;
    private int preparationTime;

    @ManyToMany(fetch = FetchType.EAGER)
    @Cascade({org.hibernate.annotations.CascadeType.PERSIST, org.hibernate.annotations.CascadeType.REFRESH})
    @JoinTable(name = "food_category",
            joinColumns = {
                    @JoinColumn(referencedColumnName = "name", name = "food_name", foreignKey = @ForeignKey(name = "food_category_food_fk")),
                    @JoinColumn(referencedColumnName = "stand_name", name = "stand_name"),
                    @JoinColumn(referencedColumnName = "brand_name", name = "brand_name")
            },
            inverseJoinColumns = {
                    @JoinColumn(referencedColumnName = "category", name = "category_category", foreignKey = @ForeignKey(name = "food_category_category_fk"))
            }
    )

    private List<Category> category = new ArrayList<>();



    public Food() {

    }

    public Food(String name, String standName, String brandName) {

        StandRepository standRepository = SpringContext.getBean(StandRepository.class);
        Stand stand = standRepository.findStandById(standName, brandName).orElse(new Stand(standName, brandName));
        this.foodId = new Food.FoodId(name, stand);
    }

    public void updateGlobalProperties(Food cf) {
        this.description = cf.getDescription();
        this.price = cf.getPrice();
        this.preparationTime = cf.getPreparationTime();

        this.category.clear();
        CategoryRepository categoryRepository = SpringContext.getBean(CategoryRepository.class);
        this.category.addAll(cf.getCategory());
    }

    /**
     * Transform a CommonFood Object to a Food Object
     * - Used to update Stand menu (compare with food)
     * @param cf CommonFood oject
     */
    public Food(CommonFood cf){

        //Setting general fields
        this.description = cf.getDescription();
        this.price = cf.getPrice().floatValue();
        this.preparationTime = cf.getPreparationTime();
        this.stock = cf.getStock();

        //Setting categories
        CategoryRepository categoryRepository = SpringContext.getBean(CategoryRepository.class);
        cf.getCategory().forEach(c -> category.add(categoryRepository.findById(c).orElse(categoryRepository.save(new Category(c)))));

        //Setting foodId
        StandRepository standRepository = SpringContext.getBean(StandRepository.class);
        Stand stand = standRepository.findStandById(cf.getStandName(), cf.getBrandName())
                .orElse(new Stand(cf.getStandName(), cf.getBrandName()));
        this.foodId = new FoodId(cf.getName(), stand);

    }

    public Food update(CommonFood cf){

        //Setting general fields
        this.description = cf.getDescription().equals("") ? this.description : cf.getDescription();
        this.price = cf.getPrice().floatValue() < 0 ? this.price : cf.getPrice().floatValue();
        this.preparationTime = cf.getPreparationTime() < 0 ? this.preparationTime : cf.getPreparationTime();
        //Stock will be 0 if no stock has changed
        this.stock += cf.getStock();

        // When categories is empty the categories should remain unchanged
        if (!cf.getCategory().isEmpty()){
            //Setting categories
            this.category.clear();
            CategoryRepository categoryRepository = SpringContext.getBean(CategoryRepository.class);
            cf.getCategory().forEach(c -> category.add(categoryRepository.findById(c).orElse(categoryRepository.save(new Category(c)))));
        }

        return this;
    }

    /**
     * Transform a CommonFood object to a Food Object AND attach to Stand entity
     * - Used when making a new Stand
     *
     * @param cf CommonFood object
     * @param stand Stand object
     */
    public Food(CommonFood cf, Stand stand) {

        this.foodId = new FoodId(cf.getName(), stand);

        // First look if this food item exists already in other stands of this brand
        Optional<Food> optionalFood = stand.getBrand().getStandList().stream()
                .flatMap(s -> s.getFoodList().stream())
                .filter(f -> cf.getName().equals(f.getName()))
                .findFirst();

        // If such an item has been found, copy the fields
        if (optionalFood.isPresent()){
            Food food = optionalFood.get();
            this.description = food.getDescription();
            this.price = food.getPrice();
            this.category.addAll(food.getCategory());

        } else { // It is a new item
            this.description = cf.getDescription();
            this.price = cf.getPrice().floatValue();

            CategoryRepository categoryRepository = SpringContext.getBean(CategoryRepository.class);
            for (String s : cf.getCategory()) {
                Category cat = categoryRepository.findById(s).orElse(categoryRepository.save(new Category(s)));
                this.category.add(cat);
            }
        }

        // Preparation time and stock should be able to be unique for a new stand
        this.preparationTime = cf.getPreparationTime();
        this.stock = cf.getStock();
    }

    /**
     * Transform a Food object to a CommonFood object
     * - Used to send global or stand menu
     * @return CommonFood object
     */
    public CommonFood asCommonFood() {
        return new CommonFood(
                foodId.name,
                BigDecimal.valueOf(price),
                preparationTime,
                stock,
                foodId.stand.getName(),
                foodId.stand.getBrand().getName(),
                description,
                getCategoriesByName()
        );
    }



    // ---- Getters and Setters ---- //
    @JsonProperty("category")
    public List<String> getCategoriesByName() {
        List<String> returnCategories = new ArrayList<>();
        for (Category category1 : category) {
            returnCategories.add(category1.getCategory());
        }

        return returnCategories;
    }

    //public String getName() {
    //    return foodId.name;
    //}
    //
    //public String getStandName(){
    //    return foodId.stand.getName();
    //}
    //
    //public String getBrandName(){
    //    return foodId.stand.getBrandName();
    //}
    //
    //@JsonProperty("name")
    //public void setName(String name) {
    //    foodId = (foodId == null) ? new Food.FoodId() : foodId;
    //    this.foodId.name = name;
    //}
    //
    //@JsonProperty("standName")
    //public void setStandName(String name) {
    //    foodId = (foodId == null) ? new Food.FoodId() : foodId;
    //    this.foodId.getStand().setName(name);
    //}
    //
    //@JsonProperty("brandName")
    //public void setBrandName(String name) {
    //    foodId = (foodId == null) ? new Food.FoodId() : foodId;
    //    this.foodId.getStand().getBrand().setName(name);
    //}
    //
    //@JsonIgnore
    //public Stand getStand() {
    //    return foodId.stand;
    //}
    //
    //@JsonIgnore
    //public void setStand(Stand stand) {
    //    foodId = (foodId == null) ? new Food.FoodId() : foodId;
    //    this.foodId.stand = stand;
    //}


    // ---- GETTERS & SETTERS ----

    // -- food id --

    @JsonIgnore
    public FoodId getFoodId() {
        return foodId;
    }

    @JsonIgnore
    public void setFoodId(FoodId foodId) {
        this.foodId = foodId;
    }

    public String getName(){
        return foodId.name;
    }

    public void setName(String name){
        foodId.name = name;
    }

    @JsonIgnore
    public Stand getStand(){
        return foodId.stand;
    }

    @JsonIgnore
    public void setStand(Stand stand){
        foodId.stand = stand;
    }

    public String getStandName(){
        return getStand().getName();
    }

    @JsonIgnore
    public void setStandName(String standName){
        foodId.stand.setName(standName);
    }

    @JsonIgnore
    public Brand getBrand(){
        return foodId.stand.getBrand();
    }

    @JsonIgnore
    public void setBrand(Brand brand){
        foodId.stand.setBrand(brand);
    }

    public String getBrandName(){
        return getBrand().getName();
    }

    @JsonIgnore
    public void setBrandName(String brandName){
        foodId.stand.setBrandName(brandName);
    }

    // -- other fields --

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(int preparationTime) {
        this.preparationTime = preparationTime;
    }

    public List<Category> getCategory() {
        return category;
    }

    public void setCategory(Set<Category> category) {
        //Ensure that all categories are existing entities in the database
        CategoryRepository categoryRepository = SpringContext.getBean(CategoryRepository.class);
        this.category = category.stream().map(categoryRepository::save).collect(Collectors.toList());
    }


    // ---- Extra ---- //

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Food food = (Food) o;
        return Objects.equals(getName(), food.getName()) &&
                Objects.equals(getStandName(), food.getStandName()) &&
                Objects.equals(getBrandName(), food.getBrandName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getStandName(), getBrandName());
    }

    @Override
    public String toString() {
        return "Food{"+foodId.name + "_" + foodId.stand.getName() + "_" + foodId.stand.getBrandName()+"}";
    }

    public void updateStock(int stock) {
        this.stock += stock;
    }

    @Data
    @AllArgsConstructor
    @Embeddable
    public static class FoodId implements Serializable {

        private String name;

        @ManyToOne
        @JoinColumns(
                foreignKey = @ForeignKey(name = "food_stand_fk"), value = {
                @JoinColumn(referencedColumnName = "name", name = "stand_name"),
                @JoinColumn(referencedColumnName = "brand_name", name = "brand_name")
        }
        )
        private Stand stand;

        public FoodId() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FoodId foodId = (FoodId) o;
            return Objects.equals(name, foodId.name) &&
                    Objects.equals(stand, foodId.stand);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, stand);
        }
    }
}

