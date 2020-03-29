package cobol.services.ordermanager.domain.repository;

import cobol.services.ordermanager.domain.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


// This will be AUTO IMPLEMENTED by Spring into a Bean called food_priceRepository
// CRUD refers Create, Read, Update, Delete

@Repository
public interface FoodRepository extends JpaRepository<Food, Integer> {
    @Query("select u from Food u " +
            "inner join Stock s on s.food_id=u.id " +
            "inner join Stand st on s.stand_id=st.id where st.name = ?1")
    List<Food> findByStand(String standname);
    @Query("select u from Food u " +
            "where u.brandName = ?1")
    List<Food> findByBrand(String standname);
    @Query("select u from Food u where u.name=?1 and u.brandName=?2")
    Food findByNameAndBrand(String name, String Brand);
    @Query("select u from Food u where u.name=?1")
    List<Food> findByName(String name);
}