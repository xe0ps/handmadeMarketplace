package shepelev.handmadeMarketplace.entity;

import jakarta.persistence.*;
import lombok.*; // Переконайся, що lombok підключено

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "orderItems")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String paymentStatus;

    @Column(length = 100)
    private String shippingFirstName;

    @Column(length = 100)
    private String shippingLastName;

    @Column(length = 20)
    private String shippingPhone;

    @Column(length = 100)
    private String shippingCountry;

    @Column(length = 100)
    private String shippingCity;

    @Column(length = 50)
    private String shippingDeliveryType;

    @Column(length = 255)
    private String shippingAddressDetails;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;
}