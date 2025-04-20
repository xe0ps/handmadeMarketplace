package shepelev.handmadeMarketplace.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shepelev.handmadeMarketplace.entity.Order;
import shepelev.handmadeMarketplace.entity.OrderItem;
import shepelev.handmadeMarketplace.entity.Product;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepo extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByProduct(Product product);
    List<OrderItem> findByOrder(Order order);

    // Знаходить останній (за ID) OrderItem для заданого продукту.
    @Query("SELECT oi FROM OrderItem oi WHERE oi.product = :product ORDER BY oi.id DESC LIMIT 1")
    Optional<OrderItem> findLastOrderItemByProduct(@Param("product") Product product);
}