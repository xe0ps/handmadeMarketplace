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

@Service
public class CartService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private FavoriteProductRepo favoriteProductRepo;


    private static final String CART_SESSION_KEY = "cart";

    public Map<Long, Integer> getCart(HttpSession session) {
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    public void addToCart(HttpSession session, Long productId, int quantity) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        if (!productService.isProductOwner(productId, currentUsername)) {
            Map<Long, Integer> cart = getCart(session);
            cart.put(productId, cart.getOrDefault(productId, 0) + quantity);
            session.setAttribute(CART_SESSION_KEY, cart);
        }

    }

    public void removeFromCart(HttpSession session, Long productId) {
        Map<Long, Integer> cart = getCart(session);
        cart.remove(productId);
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public List<Product> getCartProducts(HttpSession session) {
        Map<Long, Integer> cart = getCart(session);
        List<Product> products = new ArrayList<>();
        for (Long productId : cart.keySet()) {
            Product product = productService.getProductById(productId);
            if (product != null) {
                products.add(product);
            }
        }
        return products;
    }

    public List<ProductDto> getCartProductDtos(HttpSession session) {
        return getCartProducts(session).stream()
                .map(product -> {
                    ProductDto productDto = new ProductDto();
                    productDto.setProduct(product);
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

    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    public void removeProductsFromCartAndDb(HttpSession session) {
        Map<Long, Integer> cart = getCart(session);
        if (cart != null && !cart.isEmpty()) {
            logger.info("Starting to hide products after purchase: {}", cart.keySet());
            for (Long productId : cart.keySet()) {
                Product product = productService.getProductById(productId);
                if (product != null) {
                    product.setVisible(false);
                    productRepo.save(product);
                    logger.info("Product {} is now invisible", product.getName());
                }
            }
            session.removeAttribute(CART_SESSION_KEY);
            logger.info("Cart clear successful");
        } else {
            logger.info("Cart is already clear");
        }
    }

    public boolean isProductInCart(HttpSession session, Long productId) {
        Map<Long, Integer> cart = getCart(session);
        return cart.containsKey(productId);
    }

    public Payment processCheckout(HttpSession session, String currency, String method, String intent, String cancelUrl, String successUrl, PayPalService payPalService) throws PayPalRESTException {
        Map<Long, Integer> cart = getCart(session);
        if (cart.isEmpty()) {
            return null;
        }
        List<Product> cartProducts = getCartProducts(session);
        BigDecimal totalAmount = cartProducts.stream().map(product -> product.getPrice().multiply(BigDecimal.valueOf(cart.get(product.getID())))).reduce(BigDecimal.ZERO, BigDecimal::add);
        return payPalService.createPayment(totalAmount, currency, method, intent, cancelUrl, successUrl);

    }
}