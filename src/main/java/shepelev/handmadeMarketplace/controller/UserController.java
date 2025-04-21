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

@Controller // Контролер обробляє запити користувачів
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class); // Логер для ведення журналу подій

    @Autowired
    private UserService userService; // Сервіс для роботи з користувачами

    @Autowired
    private ProductService productService; // Сервіс для роботи з продуктами

    @Autowired
    private OrderService orderService; // Сервіс для роботи із замовленнями

    @Autowired
    private ReviewService reviewService; // Сервіс для роботи з відгуками

    @GetMapping("/user/{userId}") // Обробка GET-запиту на перегляд профілю користувача
    public String showUserProfile(@PathVariable Long userId, Model model) {
        User user = userService.getUserById(userId); // Отримання користувача за ID
        if (user == null) {
            return "redirect:/"; // Якщо користувач не знайдений — редірект на головну
        }
        if (user.getProfileImage() != null) { // Якщо є зображення профілю — конвертуємо у base64
            String base64ProfileImage = Base64.getEncoder().encodeToString(user.getProfileImage().getData());
            model.addAttribute("profileImage", base64ProfileImage); // Передача зображення в модель
            model.addAttribute("fileType", user.getProfileImage().getFileType()); // Тип файлу
        }
        List<ProductDto> productDtos = productService.getUserProductDtosById(userId); // Отримання продуктів користувача
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // Поточна автентифікація
        String currentUsername = authentication.getName(); // Отримання імені користувача
        User currentUser = userService.getUserByEmail(currentUsername); // Поточний авторизований користувач
        boolean canReview = false; // Флаг можливості залишити відгук
        if (currentUser != null) {
            canReview = orderService.canReview(currentUser, userId, productDtos); // Перевірка чи можна залишити відгук
            logger.info("Кінцеве значення canReview для користувача з id {} та продавця з id {}: {}", currentUser.getID(), userId, canReview);
        }
        model.addAttribute("canReview", canReview); // Додавання флагу в модель
        model.addAttribute("user", user); // Додавання користувача в модель
        model.addAttribute("products", productDtos); // Додавання продуктів користувача в модель
        model.addAttribute("reviews", reviewService.getUserReviews(userId)); // Додавання відгуків про користувача
        return "userProfile"; // Повернення назви представлення профілю
    }

    @PostMapping("/user/{userId}/addReview") // Обробка POST-запиту на додавання відгуку
    public String addReview(@PathVariable Long userId, @RequestParam("text") String text, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // Отримання автентифікації
        String currentUsername = authentication.getName(); // Отримання імені поточного користувача
        reviewService.addReview(userId, currentUsername, text); // Додавання відгуку
        redirectAttributes.addFlashAttribute("successMessage", "Відгук успішно додано"); // Повідомлення про успіх
        return "redirect:/user/" + userId; // Редірект назад на сторінку профілю
    }
}
