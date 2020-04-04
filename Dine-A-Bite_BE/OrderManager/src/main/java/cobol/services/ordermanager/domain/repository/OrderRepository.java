package cobol.services.ordermanager.domain.repository;
import cobol.services.ordermanager.domain.entity.Order;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer>{
}
