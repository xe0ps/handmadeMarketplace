package shepelev.handmadeMarketplace.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import shepelev.handmadeMarketplace.entity.Category;
import shepelev.handmadeMarketplace.entity.Product;
import shepelev.handmadeMarketplace.entity.User;

import java.util.List;

public interface ProductRepo extends JpaRepository<Product, Long> {
    List<Product> findByUser(User user);
    List<Product> findByUserAndIsVisibleTrue(User user);
    List<Product> findByCategoryAndIsVisibleTrue(Category category);
    List<Product> findByNameContainingIgnoreCaseAndIsVisibleTrue(String name);
    List<Product> findByIsVisibleTrue();
}