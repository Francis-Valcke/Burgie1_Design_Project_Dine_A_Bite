package cobol.services.standmanager.dbmenu;

import org.springframework.data.repository.CrudRepository;



// This will be AUTO IMPLEMENTED by Spring into a Bean called standRepository
// CRUD refers Create, Read, Update, Delete

public interface StandRepository extends CrudRepository<Stand, Integer> {

}