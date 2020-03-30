package cobol.services.dataset.domain.repository;

import cobol.services.dataset.domain.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;


// This will be AUTO IMPLEMENTED by Spring into a Bean called food_priceRepository
// CRUD refers Create, Read, Update, Delete

public interface FoodRepository extends JpaRepository<Food, Long> {

}