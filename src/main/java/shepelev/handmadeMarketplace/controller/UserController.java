package shepelev.handmadeMarketplace.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import shepelev.handmadeMarketplace.dto.ProductDto;
import shepelev.handmadeMarketplace.entity.User;
import shepelev.handmadeMarketplace.service.OrderService;
import shepelev.handmadeMarketplace.service.ProductService;
import shepelev.handmadeMarketplace.service.UserService;
import shepelev.handmadeMarketplace.service.ReviewService;

import java.util.Base64;
import java.util.List;

@Controller
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private ProductService productService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ReviewService reviewService;


    @GetMapping("/user/{userId}")
    public String showUserProfile(@PathVariable Long userId, Model model) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return "redirect:/";
        }
        if (user.getProfileImage() != null) {
            String base64ProfileImage = Base64.getEncoder().encodeToString(user.getProfileImage().getData());
            model.addAttribute("profileImage", base64ProfileImage);
            model.addAttribute("fileType", user.getProfileImage().getFileType());
        }
        List<ProductDto> productDtos = productService.getUserProductDtosById(userId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userService.getUserByEmail(currentUsername);
        boolean canReview = false;
        if(currentUser != null){
            canReview =  orderService.canReview(currentUser, userId, productDtos);
            logger.info("Кінцеве значення canReview для користувача з id {} та продавця з id {}: {}", currentUser.getID(), userId, canReview);
        }
        model.addAttribute("canReview", canReview);
        model.addAttribute("user", user);
        model.addAttribute("products", productDtos);
        model.addAttribute("reviews", reviewService.getUserReviews(userId));
        return "userProfile";
    }
    @PostMapping("/user/{userId}/addReview")
    public String addReview(@PathVariable Long userId, @RequestParam("text") String text, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        reviewService.addReview(userId, currentUsername, text);
        redirectAttributes.addFlashAttribute("successMessage", "Відгук успішно додано");
        return "redirect:/user/" + userId;
    }
}