package cobol.services.ordermanager.dbmenu;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


// This will be AUTO IMPLEMENTED by Spring into a Bean called stockRepository
// CRUD refers Create, Read, Update, Delete

public interface StockRepository extends CrudRepository<Stock, Integer> {
    @Query("select s from Stock s inner join Food f on f.id=s.food_id inner join Stand st on st.id=s.stand_id where f.id=?1 and st.id=?2")
    Stock findStock(int fid, int sid);
    @Query("select s from Stock s inner join Stand st on st.id=s.stand_id where st.id=?1")
    List<Stock> findStockByStand(int sid);
    @Query("select s.stand_id from Stock s where s.food_id=?1")
    List<Integer> findStandIdByFoodId(int fid);

}