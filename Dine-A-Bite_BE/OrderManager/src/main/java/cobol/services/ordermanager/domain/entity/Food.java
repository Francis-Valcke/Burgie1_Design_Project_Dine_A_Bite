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

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Food {

    @JsonIgnore
    @EmbeddedId
    private FoodId foodId;

    private int stock;

    private String description;
    private float price;
    private int preparationTime;

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
        this.description = cf.getDescription();
        this.price = cf.getPrice().floatValue();
        this.preparationTime = cf.getPreparationTime();
        this.stock = cf.getStock();

        //Setting categories
        this.category.clear();
        CategoryRepository categoryRepository = SpringContext.getBean(CategoryRepository.class);
        cf.getCategory().forEach(c -> category.add(categoryRepository.findById(c).orElse(categoryRepository.save(new Category(c)))));

        //Setting foodId
        StandRepository standRepository = SpringContext.getBean(StandRepository.class);
        Stand stand = standRepository.findStandById(cf.getStandName(), cf.getBrandName())
                .orElse(new Stand(cf.getStandName(), cf.getBrandName()));
        this.foodId = new FoodId(cf.getName(), stand);

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
        this.description = cf.getDescription();
        this.price = cf.getPrice().floatValue();
        this.preparationTime = cf.getPreparationTime();
        this.stock = cf.getStock();
        this.foodId = new FoodId(cf.getName(), stand);

        CategoryRepository categoryRepository = SpringContext.getBean(CategoryRepository.class);
        for (String s : cf.getCategory()) {
            Category cat = categoryRepository.findById(s).orElse(categoryRepository.save(new Category(s)));
            this.category.add(cat);
        }
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

    public String getName() {
        return foodId.name;
    }

    public String getStandName(){
        return foodId.stand.getName();
    }

    public String getBrandName(){
        return foodId.stand.getBrandName();
    }

    @JsonProperty("name")
    public void setName(String name) {
        foodId = (foodId == null) ? new Food.FoodId() : foodId;
        this.foodId.name = name;
    }

    @JsonIgnore
    public Stand getStand() {
        return foodId.stand;
    }

    @JsonIgnore
    public void setStand(Stand stand) {
        foodId = (foodId == null) ? new Food.FoodId() : foodId;
        this.foodId.stand = stand;
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

