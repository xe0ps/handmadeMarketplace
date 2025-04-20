package shepelev.handmadeMarketplace.dto;

import lombok.Data;
import shepelev.handmadeMarketplace.entity.Order;
import shepelev.handmadeMarketplace.entity.Product;

import java.math.BigDecimal; // Імпорт BigDecimal
import java.time.LocalDateTime; // Імпорт LocalDateTime
import java.util.List;

@Data
public class ProductDto {
    private Product product;
    private List<String> base64Images;
    private String base64ProfileImage;
    private String profileImageType;
    private Long id;
    private LocalDateTime orderDate;
    private String buyerFirstName;
    private String buyerLastName;
    private String buyerPhone;
    private String shippingAddress; // Комбінована адреса для простоти

    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            this.id = product.getID();
        }
    }

    // Метод для зручного формування адреси
    public void setShippingDetailsFromOrder(Order order) {
        if (order != null) {
            this.orderDate = order.getOrderDate();
            this.buyerFirstName = order.getShippingFirstName();
            this.buyerLastName = order.getShippingLastName();
            this.buyerPhone = order.getShippingPhone();

            // Формуємо рядок адреси
            StringBuilder sb = new StringBuilder();
            if (order.getShippingCountry() != null) sb.append(order.getShippingCountry());
            if (order.getShippingCity() != null) sb.append(", ").append(order.getShippingCity());
            if (order.getShippingAddressDetails() != null && !order.getShippingAddressDetails().isEmpty()) {
                sb.append(" (").append(order.getShippingAddressDetails()).append(")");
            }
            this.shippingAddress = sb.toString();
        }
    }
}