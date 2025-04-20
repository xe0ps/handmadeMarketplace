package shepelev.handmadeMarketplace.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shepelev.handmadeMarketplace.entity.Order;
import shepelev.handmadeMarketplace.entity.User;

import java.util.List;

public interface OrderRepo extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.user = :user")
    List<Order> findByUser(@Param("user") User user);
}