package shepelev.handmadeMarketplace.service;

import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import shepelev.handmadeMarketplace.dto.ProductDto;
import shepelev.handmadeMarketplace.entity.Order;
import shepelev.handmadeMarketplace.entity.OrderItem;
import shepelev.handmadeMarketplace.entity.Product;
import shepelev.handmadeMarketplace.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import shepelev.handmadeMarketplace.repo.FavoriteProductRepo;
import shepelev.handmadeMarketplace.repo.OrderItemRepo;
import shepelev.handmadeMarketplace.repo.OrderRepo;
import shepelev.handmadeMarketplace.repo.ProductRepo;
import shepelev.handmadeMarketplace.dto.ShippingDetailsDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private UserService userService;
    @Autowired
    private OrderRepo orderRepo;
    @Autowired
    private CartService cartService;
    @Autowired
    private OrderItemRepo orderItemRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private FavoriteProductService favoriteProductService;

    @Transactional
    public boolean processPaymentSuccess(String paymentId, String payerId, HttpSession session, RedirectAttributes redirectAttributes, PayPalService payPalService, CartService cartService) { // Забрав PayPalRESTException, бо обробка всередині try-catch
        // Дістаємо деталі доставки з сесії
        ShippingDetailsDto shippingDetails = (ShippingDetailsDto) session.getAttribute("shippingDetails");

        // Перевірка, чи є деталі в сесії
        if (shippingDetails == null) {
            logger.error("Не знайдено деталей доставки в сесії для paymentId={}. Оплату не може бути завершено.", paymentId);
            redirectAttributes.addFlashAttribute("errorMessage", "Помилка обробки замовлення: відсутні деталі доставки. Спробуйте ще раз.");
            // Важливо очистити кошик PayPal, якщо можливо, або позначити платіж як невдалий
            return false;
        }

        try {
            Payment payment = payPalService.executePayment(paymentId, payerId);
            logger.info("Статус PayPal платежу для paymentId={}: {}", paymentId, payment.getState());

            if ("approved".equals(payment.getState())) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String currentUsername = authentication.getName();
                User user = userService.getUserByEmail(currentUsername); // Покупець

                if (user == null) {
                    logger.error("Користувач {} не знайдений, неможливо створити замовлення для paymentId={}", currentUsername, paymentId);
                    session.removeAttribute("shippingDetails"); // Очищаємо деталі
                    redirectAttributes.addFlashAttribute("errorMessage", "Помилка: користувача не знайдено.");
                    return false;
                }

                Map<Long, Integer> cart = cartService.getCart(session);
                if (cart == null || cart.isEmpty()) {
                    logger.warn("Кошик порожній після підтвердження оплати для paymentId={}. Замовлення не створено.", paymentId);
                    session.removeAttribute("shippingDetails"); // Очищаємо деталі
                    redirectAttributes.addFlashAttribute("errorMessage", "Кошик порожній. Замовлення не створено.");
                    return false;
                }

                List<Product> cartProducts = cartService.getCartProducts(session);
                BigDecimal totalAmount = new BigDecimal(payment.getTransactions().get(0).getAmount().getTotal()); // Беремо суму з PayPal

                // Викликаємо createOrder, передаючи деталі доставки
                createOrder(user, totalAmount, payment.getState(), cartProducts, cart, shippingDetails);

                // Приховуємо товари та очищаємо кошик
                cartService.removeProductsFromCartAndDb(session);

                // Видаляємо деталі доставки з сесії після успішного створення замовлення
                session.removeAttribute("shippingDetails");
                logger.info("Деталі доставки видалено з сесії для paymentId={}", paymentId);

                // Видалення з обраних
                for (Product product : cartProducts) {
                    if (!product.isVisible()) {
                        // Перевірка чи користувач авторизований перед видаленням з обраних
                        if (authentication != null && authentication.isAuthenticated()) {
                            favoriteProductService.deleteFromFavorites(product.getID(), currentUsername, redirectAttributes); // Передаємо redirectAttributes
                            logger.info("Товар id={} видалено з обраних для користувача {}", product.getID(), currentUsername);
                        }
                    }
                }

                redirectAttributes.addFlashAttribute("successMessage", "Замовлення успішно оплачено та оформлено!");
                return true;

            } else {
                logger.warn("Статус PayPal платежу НЕ approved ({}) для paymentId={}", payment.getState(), paymentId);
                session.removeAttribute("shippingDetails"); // Очищаємо деталі
                redirectAttributes.addFlashAttribute("errorMessage", "Оплата не була підтверджена PayPal.");
                return false;
            }
        } catch (PayPalRESTException e) {
            logger.error("Помилка виконання PayPal платежу для paymentId={}: {}", paymentId, e.getMessage());
            session.removeAttribute("shippingDetails"); // Очищаємо деталі
            redirectAttributes.addFlashAttribute("errorMessage", "Помилка підтвердження оплати: " + e.getMessage());
            return false;
        } catch (Exception e) {
            // Ловимо інші можливі помилки (наприклад, при збереженні замовлення)
            logger.error("Неочікувана помилка при обробці оплати для paymentId={}: {}", paymentId, e.getMessage(), e);
            session.removeAttribute("shippingDetails"); // Очищаємо деталі
            redirectAttributes.addFlashAttribute("errorMessage", "Виникла системна помилка при оформленні замовлення.");
            return false;
        }
    }

    @Transactional
    public void createOrder(User user, BigDecimal totalAmount, String paymentStatus, List<Product> cartProducts, Map<Long, Integer> cart, ShippingDetailsDto shippingDetails) { // Додали shippingDetails
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setPaymentStatus(paymentStatus);
        order.setShippingFirstName(shippingDetails.getFirstName());
        order.setShippingLastName(shippingDetails.getLastName());
        order.setShippingPhone(shippingDetails.getPhone());
        order.setShippingCountry(shippingDetails.getCountry());
        order.setShippingCity(shippingDetails.getCity());
        order.setShippingDeliveryType(shippingDetails.getDeliveryType());

        // Комбінуємо деталі адреси в одне поле
        String addressDetails = "";
        if ("novaposhta".equals(shippingDetails.getDeliveryType()) && shippingDetails.getNovaposhtaWarehouse() != null) {
            addressDetails = "Нова Пошта, відділення: " + shippingDetails.getNovaposhtaWarehouse();
        } else if ("meest".equals(shippingDetails.getDeliveryType()) && shippingDetails.getMeestWarehouse() != null) {
            addressDetails = "Meest Express, відділення: " + shippingDetails.getMeestWarehouse();
        }
        order.setShippingAddressDetails(addressDetails);

        Order savedOrder = orderRepo.save(order);
        logger.info("Order створено з ID: {}", savedOrder.getId());

        List<OrderItem> orderItems = new ArrayList<>();
        for (Product product : cartProducts) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder); // Пов'язуємо з збереженим замовленням
            orderItem.setProduct(product);
            orderItem.setQuantity(cart.get(product.getID()));
            orderItem.setPrice(product.getPrice()); // Зберігаємо ціну на момент покупки
            orderItems.add(orderItem);
            logger.info("Підготовлено OrderItem для product ID: {} для order ID: {}", product.getID(), savedOrder.getId());
        }
        orderItemRepo.saveAll(orderItems); // Варіант 2: зберігаємо всі разом
        savedOrder.setOrderItems(orderItems); // Встановлюємо зв'язок і в об'єкті Order

        logger.info("Order ID: {} збережено з {} позиціями.", savedOrder.getId(), orderItems.size());
    }

    @Transactional
    public List<Order> getUserOrders(Long userId) {
        User user = userService.getUserById(userId);
        List<Order> orders = orderRepo.findByUser(user);
        orders.forEach(order -> {
            orderItemRepo.findByOrder(order);
            logger.info("Отримано замовлення з id: {}, кількість товарів: {}", order.getId(), order.getOrderItems().size());
        });
        return orders;
    }

    @Transactional
    public boolean canReview(User currentUser, Long userId, List<ProductDto> productDtos) {
        if (currentUser == null) return false;
        List<Order> orders = getUserOrders(currentUser.getID());
        for (Order order : orders) {
            for (ProductDto productDto : productDtos) {
                Product product = productRepo.findById(productDto.getProduct().getID()).orElse(null);
                if (order.getOrderItems().stream().anyMatch(orderItem -> Objects.equals(orderItem.getProduct().getUser().getID(), userId) && Objects.equals(orderItem.getProduct().getID(), product.getID()))) {
                    logger.info("Знайдено замовлення для користувача з id {} та продавця з id {}. canReview = true", currentUser.getID(), userId);
                    return true;
                } else {
                    logger.info("Немає відповідних замовлень для користувача з id: {} і продавця id: {} ", currentUser.getID(), userId);
                }
            }
        }
        logger.info("Кінцеве значення canReview для користувача {} і продавця {}: false", currentUser.getID(), userId, false);
        return false;
    }
}