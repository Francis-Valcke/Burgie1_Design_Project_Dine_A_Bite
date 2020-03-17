package cobol.services.ordermanager.dbmenu;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


// This will be AUTO IMPLEMENTED by Spring into a Bean called food_priceRepository
// CRUD refers Create, Read, Update, Delete

public interface Food_priceRepository extends CrudRepository<Food_price, Integer> {
    @Query("select u.name, u.price, u.preptime, u.description, fc.category from Food_price u " +
            "inner join Stock s on s.food_id=u.id inner join Food_category fc on fc.food_id=u.id " +
            "inner join Stand st on s.stand_id=st.id where st.full_name = ?1")
    List<String[]> findByStand(String standname);
}