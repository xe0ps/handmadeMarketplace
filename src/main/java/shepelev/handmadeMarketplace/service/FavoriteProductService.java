package shepelev.handmadeMarketplace.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import shepelev.handmadeMarketplace.dto.ProductDto;
import shepelev.handmadeMarketplace.entity.FavoriteProduct;
import shepelev.handmadeMarketplace.entity.Product;
import shepelev.handmadeMarketplace.entity.User;
import shepelev.handmadeMarketplace.repo.FavoriteProductRepo;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

// Позначає клас як сервіс Spring-компоненту
@Service
public class FavoriteProductService {

    // Інʼєкція залежностей репозиторіїв та сервісів
    @Autowired
    private FavoriteProductRepo favoriteProductRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    /**
     * Додає товар до списку обраних для конкретного користувача.
     * Якщо товар вже є в списку — нічого не змінюється.
     *
     * @param productId ID товару
     * @param username електронна пошта користувача
     * @param redirectAttributes атрибути для передачі повідомлення при перенаправленні
     * @return об'єкт обраного товару
     */
    @Transactional
    public FavoriteProduct addProductToFavorite(Long productId, String username, RedirectAttributes redirectAttributes) {
        User user = userService.getUserByEmail(username);
        Product product = productService.getProductById(productId);
        FavoriteProduct favoriteProduct = favoriteProductRepo.findByUserAndProduct(user, product);

        // Якщо товар ще не обраний — додаємо його до списку
        if (favoriteProduct == null) {
            FavoriteProduct newFavoriteProduct = new FavoriteProduct();
            newFavoriteProduct.setUser(user);
            newFavoriteProduct.setProduct(product);
            redirectAttributes.addFlashAttribute("successMessage", "Товар додано в обране.");
            return favoriteProductRepo.save(newFavoriteProduct);
        }

        // Якщо вже є — повертаємо існуючий
        return favoriteProduct;
    }

    /**
     * Повертає список обраних товарів користувача.
     * Завантажує всі зображення кожного товару (через звернення до їх кількості).
     *
     * @param username електронна пошта користувача
     * @return список об'єктів FavoriteProduct
     */
    @Transactional
    public List<FavoriteProduct> getUserFavoriteProducts(String username) {
        User user = userService.getUserByEmail(username);
        List<FavoriteProduct> favoriteProducts = favoriteProductRepo.findByUser(user);

        // Ініціалізуємо список зображень, якщо вони є
        favoriteProducts.forEach(favoriteProduct -> {
            if (favoriteProduct.getProduct().getImages() != null) {
                favoriteProduct.getProduct().getImages().size(); // Ініціалізація proxy-об’єктів
            }
        });

        return favoriteProducts;
    }

    /**
     * Перетворює обрані товари користувача на DTO-об'єкти, додаючи зображення у форматі Base64.
     *
     * @param username електронна пошта користувача
     * @return список DTO обраних товарів
     */
    @Transactional
    public List<ProductDto> getUserFavoriteProductDtos(String username) {
        return getUserFavoriteProducts(username).stream()
                .map(favoriteProduct -> {
                    Product product = favoriteProduct.getProduct();
                    ProductDto productDto = new ProductDto();
                    productDto.setProduct(product);

                    // Додаємо зображення у форматі Base64
                    if (product.getImages() != null) {
                        List<String> base64Images = product.getImages().stream()
                                .map(image -> Base64.getEncoder().encodeToString(image.getData()))
                                .collect(Collectors.toList());
                        productDto.setBase64Images(base64Images);
                    }

                    return productDto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Видаляє товар з обраного користувача.
     *
     * @param productId ID товару
     * @param username електронна пошта користувача
     * @param redirectAttributes повідомлення для відображення після перенаправлення
     */
    @Transactional
    public void deleteFromFavorites(Long productId, String username, RedirectAttributes redirectAttributes) {
        User user = userService.getUserByEmail(username);
        Product product = productService.getProductById(productId);
        FavoriteProduct favoriteProduct = favoriteProductRepo.findByUserAndProduct(user, product);

        if (favoriteProduct != null) {
            favoriteProductRepo.delete(favoriteProduct);
            redirectAttributes.addFlashAttribute("successMessage", "Товар видалено з обраного.");
        }
    }

    /**
     * Перевіряє, чи знаходиться товар у списку обраного користувача.
     *
     * @param productId ID товару
     * @param username електронна пошта користувача
     * @return true, якщо товар є в обраному
     */
    @Transactional
    public boolean isProductInFavorites(Long productId, String username) {
        User user = userService.getUserByEmail(username);
        Product product = productService.getProductById(productId);
        FavoriteProduct favoriteProduct = favoriteProductRepo.findByUserAndProduct(user, product);
        return favoriteProduct != null;
    }
}
