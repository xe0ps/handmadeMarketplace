package shepelev.handmadeMarketplace.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shepelev.handmadeMarketplace.entity.Review;
import shepelev.handmadeMarketplace.entity.User;
import shepelev.handmadeMarketplace.repo.ReviewRepo;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepo reviewRepo;
    @Autowired
    private UserService userService;

    @Transactional
    public void addReview(Long userId, String username, String text) {
        User user = userService.getUserById(userId);
        User reviewer = userService.getUserByEmail(username);
        Review review = new Review();
        review.setUser(user);
        review.setReviewer(reviewer);
        review.setText(text);
        review.setReviewDate(LocalDateTime.now());
        reviewRepo.save(review);
    }

    @Transactional
    public List<Review> getUserReviews(Long userId) {
        User user = userService.getUserById(userId);
        return reviewRepo.findByUser(user);
    }
}