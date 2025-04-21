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

/**
 * Сервіс для обробки платежів через PayPal, включаючи створення та виконання платежів.
 * Інтегрується з API PayPal для обробки транзакцій у Handmade Marketplace.
 */
@Service
public class PayPalService {

    /** Ідентифікатор клієнта PayPal, отриманий із конфігураційних властивостей. */
    @Value("${paypal.client.id}")
    private String clientId;

    /** Секретний ключ клієнта PayPal, отриманий із конфігураційних властивостей. */
    @Value("${paypal.client.secret}")
    private String clientSecret;

    /** Режим роботи PayPal (наприклад, "sandbox" або "live"), отриманий із конфігураційних властивостей. */
    @Value("${paypal.mode}")
    private String mode;

    /**
     * Створює платіж у PayPal із зазначеними параметрами.
     *
     * @param totalAmount Загальна сума платежу.
     * @param currency    Код валюти (наприклад, "USD").
     * @param method      Метод оплати (наприклад, "paypal").
     * @param intent      Тип платежу (наприклад, "sale").
     * @param cancelUrl   URL для перенаправлення у разі скасування платежу.
     * @param successUrl  URL для перенаправлення після успішного платежу.
     * @return Об'єкт створеного платежу.
     * @throws PayPalRESTException У разі помилки під час створення платежу.
     */
    public Payment createPayment(
            BigDecimal totalAmount,
            String currency,
            String method,
            String intent,
            String cancelUrl,
            String successUrl
    ) throws PayPalRESTException {
        // Ініціалізація контексту API PayPal із токеном доступу та конфігурацією
        APIContext apiContext = new APIContext(getAccessToken());
        apiContext.setConfigurationMap(getConfigurationMap());

        // Налаштування суми та валюти платежу
        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal(totalAmount.toString());

        // Створення транзакції з вказаною сумою
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);

        // Додавання транзакції до списку
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        // Налаштування інформації про платника
        Payer payer = new Payer();
        payer.setPaymentMethod(method);

        // Ініціалізація платежу з типом, платником і транзакціями
        Payment payment = new Payment();
        payment.setIntent(intent);
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        // Налаштування URL для перенаправлення у разі скасування або успіху
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);

        // Створення платежу через API PayPal
        return payment.create(apiContext);
    }

    /**
     * Виконує платіж у PayPal за ідентифікатором платежу та платника.
     *
     * @param paymentId Ідентифікатор платежу, отриманий від PayPal.
     * @param payerId   Ідентифікатор платника, отриманий від PayPal.
     * @return Об'єкт виконаного платежу.
     * @throws PayPalRESTException У разі помилки під час виконання платежу.
     */
    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        // Ініціалізація контексту API PayPal
        APIContext apiContext = new APIContext(getAccessToken());
        apiContext.setConfigurationMap(getConfigurationMap());

        // Налаштування об'єкта платежу з ідентифікатором
        Payment payment = new Payment();
        payment.setId(paymentId);

        // Налаштування виконання платежу з ідентифікатором платника
        PaymentExecution paymentExecute = new PaymentExecution();
        paymentExecute.setPayerId(payerId);

        // Перевірка наявності інформації про адресу платника (може бути необов'язковою)
        if (payment.getPayer() != null && payment.getPayer().getPayerInfo() != null && payment.getPayer().getPayerInfo().getBillingAddress() != null) {
            payment.getPayer().getPayerInfo().getBillingAddress().getCity();
        }

        // Виконання платежу через API PayPal
        return payment.execute(apiContext, paymentExecute);
    }

    /**
     * Отримує токен доступу для авторизації в API PayPal.
     *
     * @return Токен доступу.
     * @throws PayPalRESTException У разі помилки під час отримання токена.
     */
    private String getAccessToken() throws PayPalRESTException {
        // Створення облікових даних для отримання токена
        OAuthTokenCredential credential = new OAuthTokenCredential(clientId, clientSecret, getConfigurationMap());
        return credential.getAccessToken();
    }

    /**
     * Повертає конфігураційну мапу для API PayPal із режимом роботи.
     *
     * @return Мапа конфігураційних параметрів.
     */
    private Map<String, String> getConfigurationMap() {
        // Налаштування конфігурації з режимом (sandbox або live)
        Map<String, String> configMap = new java.util.HashMap<>();
        configMap.put("mode", mode);
        return configMap;
    }
}