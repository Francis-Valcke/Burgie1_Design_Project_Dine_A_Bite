package cobol.services.ordermanager.domain.repository;



// This will be AUTO IMPLEMENTED by Spring into a Bean called standRepository
// CRUD refers Create, Read, Update, Delete

import cobol.services.ordermanager.domain.entity.Stand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StandRepository extends JpaRepository<Stand, Stand.StandId> {

    @Query("Select s from Stand s where s.standId.name=?1 and s.standId.brand.name=?2")
    Optional<Stand> findStandById(String standName, String brandName);

    @Query("Select s from Stand s where s.standId.brand.name=?1")
    List<Stand> findStandsByBrand(String brandName);
}