package shepelev.handmadeMarketplace.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import shepelev.handmadeMarketplace.dto.ProductDto;
import shepelev.handmadeMarketplace.entity.User;
import shepelev.handmadeMarketplace.service.BaseService;
import shepelev.handmadeMarketplace.service.ProductService;
import shepelev.handmadeMarketplace.service.UserService;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;

/**
 * Контролер для додавання спільних атрибутів до моделі для всіх сторінок.
 * Використовується для ініціалізації загальних даних, таких як логотип, інформація про користувача та список товарів.
 */
@ControllerAdvice
public class BaseController {

    /** Сервіс для виконання базових операцій, таких як додавання логотипу та товарів. */
    @Autowired
    private BaseService baseService;

    /** Сервіс для роботи з користувачами, зокрема для отримання даних поточного користувача. */
    @Autowired
    private UserService userService;

    /**
     * Додає спільні атрибути до моделі для всіх сторінок.
     * Включає перевірку автентифікації користувача, додавання імені користувача та аватара до моделі.
     *
     * @param model Модель для передачі даних у представлення.
     * @throws IOException У разі помилки під час читання файлів (наприклад, логотипу).
     * @throws URISyntaxException У разі помилки під час обробки URI ресурсів.
     */
    @ModelAttribute
    @Transactional(readOnly = true)
    public void addCommonAttributes(Model model) throws IOException, URISyntaxException {
        // Додавання логотипу до моделі через сервіс
        baseService.addLogoToModel(model);

        // Отримання поточного контексту автентифікації
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Перевірка, чи користувач автентифікований (не анонімний)
        boolean isAuthenticated = authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken);

        // Додавання статусу автентифікації до моделі
        model.addAttribute("isAuthenticated", isAuthenticated);

        // Якщо користувач автентифікований, додаємо його дані до моделі
        if (isAuthenticated) {
            // Отримання email користувача з контексту автентифікації
            String userEmail = authentication.getName();

            // Отримання об'єкта користувача за email
            User currentUser = userService.getUserByEmail(userEmail);

            // Якщо користувач існує, додаємо його ім'я та аватар
            if (currentUser != null) {
                // Додавання імені користувача до моделі для відображення в заголовку
                model.addAttribute("headerUsername", currentUser.getUsername());

                // Перевірка наявності аватара користувача
                if (currentUser.getProfileImage() != null) {
                    byte[] avatarBytes = currentUser.getProfileImage().getData();
                    // Якщо аватар є і містить дані, конвертуємо його в Base64
                    if (avatarBytes != null && avatarBytes.length > 0) {
                        String base64Avatar = Base64.getEncoder().encodeToString(avatarBytes);
                        model.addAttribute("headerUserAvatar", base64Avatar);
                        model.addAttribute("headerUserAvatarType", currentUser.getProfileImage().getFileType());
                    }
                }
            }
        }
    }

    /**
     * Додає список товарів до моделі для всіх сторінок.
     *
     * @param model Модель для передачі даних у представлення.
     * @return Список товарів у форматі DTO.
     */
    @ModelAttribute("products")
    @Transactional(readOnly = true)
    public List<ProductDto> addProducts(Model model) {
        // Отримання списку товарів через сервіс і додавання їх до моделі
        return baseService.addProductsToModel();
    }
}