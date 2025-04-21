package shepelev.handmadeMarketplace.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import shepelev.handmadeMarketplace.dto.ProductDto;
import shepelev.handmadeMarketplace.service.FavoriteProductService;

import java.util.List;

/**
 * Контролер для управління обраними товарами користувача.
 * Дозволяє переглядати, додавати та видаляти товари з обраного.
 */
@Controller
public class FavoriteProductController {

    /** Сервіс для роботи з обраними товарами. */
    @Autowired
    private FavoriteProductService favoriteProductService;

    /**
     * Відображає сторінку з обраними товарами поточного користувача.
     *
     * @param model Модель для передачі даних у представлення.
     * @return Назва шаблону сторінки ("favoriteProducts").
     */
    @GetMapping("/favorites")
    public String showFavoriteProducts(Model model) {
        // Отримання поточного користувача з контексту безпеки
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // Отримання списку DTO обраних товарів
        List<ProductDto> productDtos = favoriteProductService.getUserFavoriteProductDtos(currentUsername);

        // Додавання списку товарів до моделі для відображення
        model.addAttribute("products", productDtos);
        return "favoriteProducts";
    }

    /**
     * Додає товар до списку обраних для поточного користувача.
     *
     * @param productId           Ідентифікатор товару.
     * @param redirectAttributes Об'єкт для додавання повідомлень після перенаправлення.
     * @return URL для перенаправлення на сторінку товару.
     */
    @PostMapping("/product/addFavorite")
    public String addProductToFavorites(@RequestParam("id") Long productId, RedirectAttributes redirectAttributes) {
        // Отримання поточного користувача з контексту безпеки
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // Додавання товару до обраних через сервіс
        favoriteProductService.addProductToFavorite(productId, currentUsername, redirectAttributes);

        // Перенаправлення на сторінку товару
        return "redirect:/product/view?id=" + productId;
    }

    /**
     * Видаляє товар зі списку обраних для поточного користувача.
     *
     * @param productId           Ідентифікатор товару.
     * @param redirectAttributes Об'єкт для додавання повідомлень після перенаправлення.
     * @return URL для перенаправлення на сторінку обраних товарів.
     */
    @PostMapping("/favorites/delete")
    public String deleteFromFavorites(@RequestParam("id") Long productId, RedirectAttributes redirectAttributes) {
        // Отримання поточного користувача з контексту безпеки
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // Видалення товару з обраних через сервіс
        favoriteProductService.deleteFromFavorites(productId, currentUsername, redirectAttributes);

        // Перенаправлення на сторінку обраних товарів
        return "redirect:/favorites";
    }
}