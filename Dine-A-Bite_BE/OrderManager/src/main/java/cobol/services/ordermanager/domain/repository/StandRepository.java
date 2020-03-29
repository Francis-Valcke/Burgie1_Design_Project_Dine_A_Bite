package cobol.services.ordermanager.domain.repository;

import cobol.services.ordermanager.domain.entity.Stand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


// This will be AUTO IMPLEMENTED by Spring into a Bean called standRepository
// CRUD refers Create, Read, Update, Delete
@Repository
public interface StandRepository extends JpaRepository<Stand, Integer> {

    @Query("select s from Stand s")
    List<Stand> findStands();

    @Query("select s.name from Stand s where s.id=?1")
    Stand findStandById(int id);

    @Query("select s.name from Stand s where s.name=?1")
    Stand findStandByName(String name);
}