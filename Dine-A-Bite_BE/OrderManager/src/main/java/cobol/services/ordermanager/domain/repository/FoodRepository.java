package cobol.services.ordermanager.domain.repository;



// This will be AUTO IMPLEMENTED by Spring into a Bean called food_priceRepository
// CRUD refers Create, Read, Update, Delete

import cobol.services.ordermanager.domain.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Food.FoodId> {


    @Query("Select f from Food f where f.foodId.name=?1 and f.foodId.stand.standId.name=?2 and f.foodId.stand.standId.brand.name=?3")
    Optional<Food> findFoodById(String foodName, String standName, String brandName);

    @Query("Select f from Food f where f.foodId.stand.standId.brand.name=?1")
    List<Food> findFoodByBrand(String brandName);

    // This method is called in a scheduled task, therefore transactional is needed
    @Transactional
    @Modifying
    @Query("Update Food f Set f.preparationTime=?4 where f.foodId.name=?1 and f.foodId.stand.standId.name=?2 and f.foodId.stand.standId.brand.name=?3")
    void updatePreparationTime(String name, String name1, String brandName, int updatedAverage);
}