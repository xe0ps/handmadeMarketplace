package shepelev.handmadeMarketplace.controller;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import shepelev.handmadeMarketplace.dto.CountriesResponse;
import shepelev.handmadeMarketplace.dto.ProductDto;
import shepelev.handmadeMarketplace.service.CartService;
import shepelev.handmadeMarketplace.service.CheckoutService;
import shepelev.handmadeMarketplace.service.CountryCityService;
import shepelev.handmadeMarketplace.service.PayPalService;
import shepelev.handmadeMarketplace.dto.ShippingDetailsDto; // Імпортуємо DTO
import org.springframework.web.bind.annotation.ModelAttribute; // Для DTO

import java.util.List;
import java.util.Map;

@Controller
public class CartController {
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);
    @Value("${paypal.currency}")
    private String currency;
    @Value("${paypal.method}")
    private String method;
    @Value("${paypal.intent}")
    private String intent;
    @Value("${paypal.cancelUrl}")
    private String cancelUrl;
    @Value("${paypal.successUrl}")
    private String successUrl;
    @Autowired
    private CartService cartService;
    @Autowired
    private PayPalService payPalService;
    @Autowired
    private CheckoutService checkoutService;
    @Autowired
    private CountryCityService countryCityService;


    @GetMapping("/cart")
    public String showCart(Model model, HttpSession session) {
        List<ProductDto> productDtos = cartService.getCartProductDtos(session);
        model.addAttribute("products", productDtos);
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("id") Long productId,
                            @RequestParam("quantity") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        cartService.addToCart(session, productId, quantity);
        redirectAttributes.addFlashAttribute("successMessage", "Товар додано в корзину");
        return "redirect:/product/view?id=" + productId;
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam("id") Long productId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        cartService.removeFromCart(session, productId);
        redirectAttributes.addFlashAttribute("successMessage", "Товар видалено з корзини");
        return "redirect:/cart";
    }

    @PostMapping("/cart/checkout")
    public String checkout(HttpSession session, Model model) {
        // Перевірка чи кошик не порожній
        Map<Long, Integer> cart = cartService.getCart(session);
        if (cart == null || cart.isEmpty()) {
            // Якщо кошик порожній, перенаправляємо назад до кошика з повідомленням
            return "redirect:/cart";
        }
        CountriesResponse countriesResponse = countryCityService.getCountriesAndCities();
        model.addAttribute("countries", countriesResponse);
        model.addAttribute("shippingDetails", new ShippingDetailsDto()); // Додаємо порожній DTO для форми
        return "cartDetails"; // Показуємо форму деталей
    }

    @PostMapping("/cart/payment")
    public String payment(@ModelAttribute ShippingDetailsDto shippingDetails, // Отримуємо дані з форми
                          HttpSession session,
                          RedirectAttributes redirectAttributes,
                          Model model) throws PayPalRESTException {

        logger.info("Отримано деталі доставки: {}", shippingDetails); // Логуємо отримані дані

        // 1. Зберігаємо деталі доставки в сесію
        session.setAttribute("shippingDetails", shippingDetails);
        logger.info("Деталі доставки збережено в сесію.");

        // 2. Створюємо платіж PayPal
        Payment payment = checkoutService.processCheckout(session, currency, method, intent, cancelUrl, successUrl, payPalService);
        logger.info("Виконую checkout для PayPal");

        if (payment == null) {
            // Видаляємо деталі з сесії, якщо платіж не створено
            session.removeAttribute("shippingDetails");
            redirectAttributes.addFlashAttribute("errorMessage", "Помилка створення платежу. Можливо, кошик порожній?");
            return "redirect:/cart";
        }

        // 3. Перенаправляємо на PayPal для оплати
        for (Links link : payment.getLinks()) {
            if (link.getRel().equals("approval_url")) {
                logger.info("Перенаправлення на PayPal: {}", link.getHref());
                return "redirect:" + link.getHref();
            }
        }

        // Якщо URL для оплати не знайдено
        session.removeAttribute("shippingDetails");
        logger.error("Не знайдено approval_url в PayPal payment response.");
        redirectAttributes.addFlashAttribute("errorMessage", "Помилка перенаправлення на оплату.");
        return "redirect:/cart";
    }


    @GetMapping("/cart/paymentSuccess")
    public String paymentSuccess(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId, HttpSession session, RedirectAttributes redirectAttributes) throws PayPalRESTException {
        logger.info("Виконую paymentSuccess для paymentId={}", paymentId);
        // Викликаємо сервіс, який має дістати деталі з сесії та передати їх далі
        boolean isSuccess = checkoutService.processPaymentSuccess(paymentId, payerId, session, redirectAttributes, payPalService, cartService);
        if (isSuccess) {
            logger.info("Оплата успішна для paymentId={}. Перенаправлення на /profile", paymentId);
            return "redirect:/profile";
        }
        logger.warn("Оплата не успішна або сталася помилка для paymentId={}. Перенаправлення на /cart", paymentId);
        return "redirect:/cart";
    }
}