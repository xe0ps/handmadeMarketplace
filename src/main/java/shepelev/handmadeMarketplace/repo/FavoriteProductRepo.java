package shepelev.handmadeMarketplace.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import shepelev.handmadeMarketplace.entity.FavoriteProduct;
import shepelev.handmadeMarketplace.entity.Product;
import shepelev.handmadeMarketplace.entity.User;

import java.util.List;

public interface FavoriteProductRepo extends JpaRepository<FavoriteProduct, Long> {
    List<FavoriteProduct> findByUser(User user);
    FavoriteProduct findByUserAndProduct(User user, Product product);
    List<FavoriteProduct> findByProduct(Product product);

}