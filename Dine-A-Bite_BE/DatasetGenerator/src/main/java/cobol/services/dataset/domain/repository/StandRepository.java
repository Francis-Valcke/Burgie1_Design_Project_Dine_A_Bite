package cobol.services.dataset.domain.repository;

import cobol.services.dataset.domain.entity.Stand;
import org.springframework.data.jpa.repository.JpaRepository;


// This will be AUTO IMPLEMENTED by Spring into a Bean called standRepository
// CRUD refers Create, Read, Update, Delete

public interface StandRepository extends JpaRepository<Stand, Long> {

}