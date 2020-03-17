package cobol.services.ordermanager.dbmenu;

import org.springframework.data.repository.CrudRepository;



// This will be AUTO IMPLEMENTED by Spring into a Bean called stockRepository
// CRUD refers Create, Read, Update, Delete

public interface StockRepository extends CrudRepository<Stock, Integer> {

}