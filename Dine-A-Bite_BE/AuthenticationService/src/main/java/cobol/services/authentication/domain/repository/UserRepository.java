package cobol.services.authentication.domain.repository;

import cobol.services.authentication.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Indicates to spring JPA that User class can be used as an entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
