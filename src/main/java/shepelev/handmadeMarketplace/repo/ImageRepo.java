package shepelev.handmadeMarketplace.repo;

import shepelev.handmadeMarketplace.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepo extends JpaRepository<Image, Long> {
}