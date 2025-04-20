package shepelev.handmadeMarketplace.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import shepelev.handmadeMarketplace.entity.Category;
import shepelev.handmadeMarketplace.entity.Product;

import java.util.List;

public interface CategoryRepo extends JpaRepository<Category, Long> {
}
