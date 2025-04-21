package shepelev.handmadeMarketplace.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import shepelev.handmadeMarketplace.dto.ProductDto;
import shepelev.handmadeMarketplace.entity.User;
import shepelev.handmadeMarketplace.service.AdminService;
import shepelev.handmadeMarketplace.service.ProductService;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Контролер для управління діями адміністратора.
 * Дозволяє переглядати список користувачів і товарів, а також видаляти їх.
 */
@Controller
public class AdminController {

    /** Логер для відстеження дій адміністратора. */
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    /** Сервіс для роботи з діями адміністратора, такими як видалення користувачів. */
    @Autowired
    private AdminService adminService;

    /** Сервіс для роботи з товарами, зокрема їх видаленням. */
    @Autowired
    private ProductService productService;

    /**
     * Відображає сторінку адміністратора з інформацією про всіх користувачів і товари.
     *
     * @param model Модель для передачі даних у представлення.
     * @return Назва шаблону сторінки ("admin").
     */
    @GetMapping("/admin")
    public String showAdminPage(Model model) {
        // Отримання поточного користувача з контексту безпеки
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // Логування спроби доступу до сторінки адміністратора
        logger.info("Admin user {} is attempting to access admin page", currentUsername);

        // Отримання списку всіх користувачів
        List<User> users = adminService.getAllUsers();
        model.addAttribute("users", users);

        // Отримання списку всіх товарів у форматі DTO
        List<ProductDto> productDtos = productService.getAllProductDtos();
        model.addAttribute("products", productDtos);

        // Повернення назви шаблону для відображення сторінки
        return "admin";
    }

    /**
     * Видаляє користувача за його ідентифікатором.
     *
     * @param userId Ідентифікатор користувача, якого потрібно видалити.
     * @param redirectAttributes Об'єкт для додавання повідомлень після перенаправлення.
     * @return URL для перенаправлення на сторінку адміністратора.
     */
    @PostMapping("/admin/deleteUser")
    public String deleteUser(@RequestParam("id") Long userId, RedirectAttributes redirectAttributes) {
        // Виклик сервісу для видалення користувача
        adminService.deleteUser(userId, redirectAttributes);

        // Перенаправлення на сторінку адміністратора після видалення
        return "redirect:/admin";
    }

    /**
     * Видаляє товар за його ідентифікатором.
     *
     * @param productId Ідентифікатор товару, який потрібно видалити.
     * @param redirectAttributes Об'єкт для додавання повідомлень після перенаправлення.
     * @return URL для перенаправлення на сторінку адміністратора.
     */
    @PostMapping("/admin/deleteProduct")
    public String deleteProduct(@RequestParam("id") Long productId, RedirectAttributes redirectAttributes) {
        // Виклик сервісу для видалення товару
        productService.deleteProduct(productId, redirectAttributes);

        // Перенаправлення на сторінку адміністратора після видалення
        return "redirect:/admin";
    }
}