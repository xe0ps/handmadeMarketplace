package shepelev.handmadeMarketplace.service;

import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Service
public class CheckoutService {
    @Autowired
    private OrderService orderService;
    @Autowired
    private CartService cartService;

    public Payment processCheckout(HttpSession session, String currency, String method, String intent, String cancelUrl, String successUrl, PayPalService payPalService) throws PayPalRESTException {
        return cartService.processCheckout(session, currency, method, intent, cancelUrl, successUrl, payPalService);
    }

    public boolean processPaymentSuccess(String paymentId, String payerId, HttpSession session, RedirectAttributes redirectAttributes, PayPalService payPalService, CartService cartService) throws PayPalRESTException {
        return orderService.processPaymentSuccess(paymentId, payerId, session, redirectAttributes, payPalService, cartService);
    }
}