package cobol.services.ordermanager.dbmenu;

import org.springframework.data.repository.CrudRepository;



// This will be AUTO IMPLEMENTED by Spring into a Bean called food_categoryRepository
// CRUD refers Create, Read, Update, Delete

public interface Food_categoryRepository extends CrudRepository<Food_category, Integer> {

}