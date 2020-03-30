package cobol.services.dataset.domain.repository;

import cobol.services.dataset.domain.entity.Category;
import cobol.services.dataset.domain.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
}
