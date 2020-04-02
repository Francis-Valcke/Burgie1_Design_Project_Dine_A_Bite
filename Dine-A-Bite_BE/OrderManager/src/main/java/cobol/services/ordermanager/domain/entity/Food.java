package cobol.services.ordermanager.domain.entity;

import cobol.commons.CommonFood;
import cobol.services.ordermanager.domain.SpringContext;
import cobol.services.ordermanager.domain.repository.CategoryRepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Food {

    @JsonIgnore
    @EmbeddedId
    private FoodId foodId;

    private String description;

    private float price;

    private int preparationTime;

    private int stock;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "food_category",
            joinColumns = {
                    @JoinColumn(referencedColumnName = "name", name = "food_name", foreignKey = @ForeignKey(name = "food_category_food_fk")),
                    @JoinColumn(referencedColumnName = "stand_name", name = "stand_name", foreignKey = @ForeignKey(name = "food_category_food_fk")),
                    @JoinColumn(referencedColumnName = "brand_name", name = "brand_name", foreignKey = @ForeignKey(name = "food_category_food_fk"))
            },
            inverseJoinColumns = {
                    @JoinColumn(referencedColumnName = "category", name = "category_category", foreignKey = @ForeignKey(name = "food_category_category_fk"))
            }
    )
    private List<Category> category = new ArrayList<>();

    public Food(CommonFood cf){

        this.description = cf.getDescription();
        this.price = cf.getPrice().floatValue();
        this.preparationTime = cf.getPreparationTime();
        this.stock = cf.getStock();
        this.foodId = new FoodId(cf.getName(), new Stand(cf.getStandName(), cf.getBrandName()));
        this.category = new ArrayList<>();

        CategoryRepository categoryRepository = SpringContext.getBean(CategoryRepository.class);
        for (String s : cf.getCategory()) {
            Category cat = categoryRepository.findById(s).orElse(categoryRepository.save(new Category(s)));
            this.category.add(cat);
        }
    }

    public Food(CommonFood cf, Stand stand) {
        //Setting the food variables

        stand.getFoodList().add(this);
        this.description = cf.getDescription();
        this.price = cf.getPrice().floatValue();
        this.preparationTime = cf.getPreparationTime();
        this.stock = cf.getStock();
        this.foodId = new FoodId(cf.getName(), stand);
        this.category = new ArrayList<>();

        CategoryRepository categoryRepository = SpringContext.getBean(CategoryRepository.class);
        for (String s : cf.getCategory()) {
            Category cat = categoryRepository.findById(s).orElse(categoryRepository.save(new Category(s)));
            this.category.add(cat);
        }
    }

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

    @JsonProperty("category")
    public List<String> getCategoriesByName() {
        List<String> returnCategories = new ArrayList<>();
        for (Category category1 : category) {
            returnCategories.add(category1.getCategory());
        }

        return returnCategories;
    }


    public String getName() {
        return foodId.name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        foodId = (foodId == null) ? new Food.FoodId() : foodId;
        this.foodId.name = name;
    }

    //public String getStandName(){
    //    return foodId.stand.getName();
    //}

    @JsonIgnore
    public Stand getStand() {
        return foodId.stand;
    }

    @JsonIgnore
    public void setStand(Stand stand) {
        foodId = (foodId == null) ? new Food.FoodId() : foodId;
        this.foodId.stand = stand;
    }


    //public String getBrandName(){
    //    return this.foodId.getStand().getBrandName();
    //}

    public Food() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Food food = (Food) o;
        return Objects.equals(foodId, food.foodId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foodId);
    }


    public void update(CommonFood cf) {
        this.description = cf.getDescription();
        this.price = cf.getPrice().floatValue();
        this.preparationTime = cf.getPreparationTime();
        this.stock = cf.getStock();
        this.category = new ArrayList<>();

        CategoryRepository categoryRepository = SpringContext.getBean(CategoryRepository.class);
        for (String s : cf.getCategory()) {
            Category cat = categoryRepository.findById(s).orElse(categoryRepository.save(new Category(s)));
            this.category.add(cat);
        }
    }

    /**
     * Compares fooditems on name and brandname
     *
     * @param food Food object
     * @return returns true is same global food item
     */
    public boolean equalsGlobal(Food food) {
        return this.foodId.getName().equals(food.getName()) &&
                this.foodId.getStand().getBrand().getName().equals(food.foodId.getStand().getBrand().getName());
    }

    public void updateGlobalProperties(Food cf) {
        this.category = cf.getCategory();
        this.price = cf.getPrice();
        this.preparationTime = cf.getPreparationTime();
    }

    public void updateStock(Food cf) {
        this.stock += cf.getStock();
    }

    @Override
    public String toString() {
        return "Food{"+foodId.name + "_" + foodId.stand.getName() + "_" + foodId.stand.getBrandName()+"}";
    }

    @Data
    @AllArgsConstructor
    @Embeddable
    public static class FoodId implements Serializable {

        private String name;

        @ManyToOne(cascade = CascadeType.ALL)
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

