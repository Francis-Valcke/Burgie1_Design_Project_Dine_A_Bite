package cobol.services.authentication.domain.repository;


import cobol.services.authentication.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Indicates to spring JPA that Role class can be used as an entity.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

}
