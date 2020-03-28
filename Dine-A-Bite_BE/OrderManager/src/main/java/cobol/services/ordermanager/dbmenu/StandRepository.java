package cobol.services.ordermanager.dbmenu;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


// This will be AUTO IMPLEMENTED by Spring into a Bean called standRepository
// CRUD refers Create, Read, Update, Delete

public interface StandRepository extends CrudRepository<Stand, Integer> {
    @Query("select s from Stand s")
    List<Stand> findStands();

    @Query("select s from Stand s where s.full_name=?1")
    Stand findStandByName(String name);

}