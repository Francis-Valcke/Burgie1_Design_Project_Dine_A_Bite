package cobol.services.ordermanager.domain.repository;

import cobol.services.ordermanager.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

}
