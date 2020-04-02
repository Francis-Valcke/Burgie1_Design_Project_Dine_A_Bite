package cobol.services.ordermanager.domain.repository;



// This will be AUTO IMPLEMENTED by Spring into a Bean called food_priceRepository
// CRUD refers Create, Read, Update, Delete

import cobol.services.ordermanager.domain.entity.Food;
import cobol.services.ordermanager.domain.entity.Stand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Food.FoodId> {
    @Query("Select f from Food f where f.foodId.name=?1 and f.foodId.stand.standId.name=?2 and f.foodId.stand.standId.brand.name=?3")
    Optional<Food> findFoodById(String foodName, String standName, String brandName);
}