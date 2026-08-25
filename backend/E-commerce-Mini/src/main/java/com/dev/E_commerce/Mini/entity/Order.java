package com.dev.E_commerce.Mini.entity;

import com.dev.E_commerce.Mini.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Status status;
    BigDecimal totalPrice;
    String shippingAddress;
    String phone;

    @Builder.Default
    @Column(name = "discount_amount")
    BigDecimal discountAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "shipping_fee")
    BigDecimal shippingFee = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    // Order.java — thêm cột
    @Column(name = "idempotency_key", unique = true)
    String idempotencyKey;

    @OneToMany(mappedBy = "order",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    List<OrderItem> orderItems = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    LocalDateTime createdAt;
}
