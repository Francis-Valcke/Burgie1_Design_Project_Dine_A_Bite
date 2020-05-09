package cobol.services.ordermanager.domain.repository;

import cobol.commons.domain.CommonOrder;
import cobol.services.ordermanager.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer>{

    @Query("select o from Order o join fetch o.orderItems where o.id=?1")
    Optional<Order> findFullOrderById(int id);

    // This method is called in a scheduled task, therefore transactional is needed
    @Transactional
    @Modifying
    @Query("update Order o set o.orderState=?2 where o.id = ?1")
    void updateState(int id, CommonOrder.State orderState);
}
