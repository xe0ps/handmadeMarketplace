package shepelev.handmadeMarketplace.service;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PayPalService {
    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;


    public Payment createPayment(
            BigDecimal totalAmount,
            String currency,
            String method,
            String intent,
            String cancelUrl,
            String successUrl
    ) throws PayPalRESTException {
        APIContext apiContext = new APIContext(getAccessToken());
        apiContext.setConfigurationMap(getConfigurationMap());

        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal(totalAmount.toString());

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod(method);

        Payment payment = new Payment();
        payment.setIntent(intent);
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);
        return payment.create(apiContext);
    }

    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        APIContext apiContext = new APIContext(getAccessToken());
        apiContext.setConfigurationMap(getConfigurationMap());
        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecute = new PaymentExecution();
        paymentExecute.setPayerId(payerId);
        if (payment.getPayer() != null && payment.getPayer().getPayerInfo() != null && payment.getPayer().getPayerInfo().getBillingAddress() != null) {
            payment.getPayer().getPayerInfo().getBillingAddress().getCity();
        }
        return payment.execute(apiContext, paymentExecute);
    }

    private String getAccessToken() throws PayPalRESTException {
        OAuthTokenCredential credential = new OAuthTokenCredential(clientId, clientSecret, getConfigurationMap());
        return credential.getAccessToken();
    }


    private Map<String, String> getConfigurationMap() {
        Map<String, String> configMap = new java.util.HashMap<>();
        configMap.put("mode", mode);
        return configMap;
    }
}