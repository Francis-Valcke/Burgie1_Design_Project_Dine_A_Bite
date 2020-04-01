package cobol.services.ordermanager.domain.repository;



// This will be AUTO IMPLEMENTED by Spring into a Bean called standRepository
// CRUD refers Create, Read, Update, Delete

import cobol.services.ordermanager.domain.entity.Stand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StandRepository extends JpaRepository<Stand, Stand.StandId> {

}