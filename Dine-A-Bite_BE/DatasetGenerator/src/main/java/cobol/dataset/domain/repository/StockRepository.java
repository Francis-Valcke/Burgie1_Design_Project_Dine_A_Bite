package cobol.dataset.domain.repository;

import cobol.dataset.domain.entity.Brand;
import cobol.dataset.domain.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, Stock.StockId> {
}
