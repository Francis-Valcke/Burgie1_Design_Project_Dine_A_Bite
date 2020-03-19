package cobol.services.ordermanager.dbmenu;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


// This will be AUTO IMPLEMENTED by Spring into a Bean called food_priceRepository
// CRUD refers Create, Read, Update, Delete

public interface Food_Repository extends CrudRepository<Food, Integer> {
    @Query("select u from Food u " +
            "inner join Stock s on s.food_id=u.id " +
            "inner join Stand st on s.stand_id=st.id where st.full_name = ?1")
    List<Food> findByStand(String standname);
    @Query("select u from Food u " +
            "where u.brandname = ?1")
    List<Food> findByBrand(String standname);
}