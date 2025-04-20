package shepelev.handmadeMarketplace.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import shepelev.handmadeMarketplace.entity.Review;
import shepelev.handmadeMarketplace.entity.User;

import java.util.List;

public interface ReviewRepo extends JpaRepository<Review, Long> {
    List<Review> findByUser(User user);
}