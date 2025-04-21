package shepelev.handmadeMarketplace.service;

import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import shepelev.handmadeMarketplace.dto.ProductDto;
import shepelev.handmadeMarketplace.entity.FavoriteProduct;
import shepelev.handmadeMarketplace.entity.Product;
import shepelev.handmadeMarketplace.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shepelev.handmadeMarketplace.repo.FavoriteProductRepo;
import shepelev.handmadeMarketplace.repo.ProductRepo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Сервіс для управління кошиком користувача, включаючи додавання, видалення товарів та обробку платежів.
 * Використовує сесію для збереження стану кошика та інтегрується з PayPal для оформлення замовлення.
 */
@Service
public class CartService {

    /** Логер для відстеження операцій із кошиком (зауваження: використовує OrderService.class). */
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    /** Сервіс для роботи з товарами. */
    @Autowired
    private ProductService productService;

    /** Репозиторій для доступу до даних товарів. */
    @Autowired
    private ProductRepo productRepo;

    /** Репозиторій для доступу до даних обраних товарів. */
    @Autowired
    private FavoriteProductRepo favoriteProductRepo;

    /** Ключ для збереження кошика в сесії. */
    private static final String CART_SESSION_KEY = "cart";

    /**
     * Отримує кошик із сесії або створює новий, якщо він відсутній.
     *
     * @param session HTTP-сесія користувача.
     * @return Мапа з ідентифікаторами товарів та їх кількістю в кошику.
     */
    public Map<Long, Integer> getCart(HttpSession session) {
        // Отримання кошика з сесії
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            // Створення нового кошика, якщо він не існує
            cart = new HashMap<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    /**
     * Додає товар до кошика, якщо користувач не є власником товару.
     *
     * @param session   HTTP-сесія користувача.
     * @param productId Ідентифікатор товару.
     * @param quantity  Кількість товару для додавання.
     */
    public void addToCart(HttpSession session, Long productId, int quantity) {
        // Отримання поточного користувача з контексту безпеки
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // Перевірка, чи користувач не є власником товару
        if (!productService.isProductOwner(productId, currentUsername)) {
            Map<Long, Integer> cart = getCart(session);
            // Додавання або оновлення кількості товару в кошику
            cart.put(productId, cart.getOrDefault(productId, 0) + quantity);
            session.setAttribute(CART_SESSION_KEY, cart);
        }
    }

    /**
     * Видаляє товар із кошика.
     *
     * @param session   HTTP-сесія користувача.
     * @param productId Ідентифікатор товару для видалення.
     */
    public void removeFromCart(HttpSession session, Long productId) {
        Map<Long, Integer> cart = getCart(session);
        // Видалення товару з кошика
        cart.remove(productId);
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    /**
     * Отримує список товарів у кошику.
     *
     * @param session HTTP-сесія користувача.
     * @return Список об'єктів товарів у кошику.
     */
    public List<Product> getCartProducts(HttpSession session) {
        Map<Long, Integer> cart = getCart(session);
        List<Product> products = new ArrayList<>();
        // Отримання кожного товару за його ідентифікатором
        for (Long productId : cart.keySet()) {
            Product product = productService.getProductById(productId);
            if (product != null) {
                products.add(product);
            }
        }
        return products;
    }

    /**
     * Отримує список DTO товарів у кошику з закодованими зображеннями у форматі Base64.
     *
     * @param session HTTP-сесія користувача.
     * @return Список DTO товарів для відображення.
     */
    public List<ProductDto> getCartProductDtos(HttpSession session) {
        return getCartProducts(session).stream()
                .map(product -> {
                    // Створення DTO для товару
                    ProductDto productDto = new ProductDto();
                    productDto.setProduct(product);
                    if (product.getImages() != null) {
                        // Кодування зображень товару у Base64
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
     * Очищає кошик, видаляючи його з сесії.
     *
     * @param session HTTP-сесія користувача.
     */
    public void clearCart(HttpSession session) {
        // Видалення кошика з сесії
        session.removeAttribute(CART_SESSION_KEY);
    }

    /**
     * Видаляє товари з кошика та приховує їх у базі даних після покупки.
     *
     * @param session HTTP-сесія користувача.
     */
    public void removeProductsFromCartAndDb(HttpSession session) {
        Map<Long, Integer> cart = getCart(session);
        if (cart != null && !cart.isEmpty()) {
            // Логування початку операції приховування товарів
            logger.info("Починаємо приховування товарів після покупки: {}", cart.keySet());
            for (Long productId : cart.keySet()) {
                // Отримання товару та встановлення статусу невидимості
                Product product = productService.getProductById(productId);
                if (product != null) {
                    product.setVisible(false);
                    productRepo.save(product);
                    logger.info("Товар {} тепер невидимий", product.getName());
                }
            }
            // Очищення кошика після приховування товарів
            session.removeAttribute(CART_SESSION_KEY);
            logger.info("Кошик успішно очищено");
        } else {
            logger.info("Кошик уже порожній");
        }
    }

    /**
     * Перевіряє, чи є товар у кошику.
     *
     * @param session   HTTP-сесія користувача.
     * @param productId Ідентифікатор товару.
     * @return true, якщо товар є в кошику, інакше false.
     */
    public boolean isProductInCart(HttpSession session, Long productId) {
        Map<Long, Integer> cart = getCart(session);
        return cart.containsKey(productId);
    }

    /**
     * Ініціює оформлення замовлення через PayPal, розраховуючи загальну суму кошика.
     *
     * @param session      HTTP-сесія користувача.
     * @param currency     Код валюти (наприклад, "USD").
     * @param method       Метод оплати (наприклад, "paypal").
     * @param intent       Тип платежу (наприклад, "sale").
     * @param cancelUrl    URL для перенаправлення у разі скасування.
     * @param successUrl   URL для перенаправлення після успіху.
     * @param payPalService Сервіс для роботи з PayPal.
     * @return Об'єкт платежу або null, якщо кошик порожній.
     * @throws PayPalRESTException У разі помилки під час створення платежу.
     */
    public Payment processCheckout(HttpSession session, String currency, String method, String intent, String cancelUrl, String successUrl, PayPalService payPalService) throws PayPalRESTException {
        Map<Long, Integer> cart = getCart(session);
        if (cart.isEmpty()) {
            // Повернення null, якщо кошик порожній
            return null;
        }
        // Отримання товарів із кошика
        List<Product> cartProducts = getCartProducts(session);
        // Розрахунок загальної суми кошика з урахуванням кількості
        BigDecimal totalAmount = cartProducts.stream()
                .map(product -> product.getPrice().multiply(BigDecimal.valueOf(cart.get(product.getID()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // Створення платежу через PayPal
        return payPalService.createPayment(totalAmount, currency, method, intent, cancelUrl, successUrl);
    }
}