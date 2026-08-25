package com.dev.E_commerce.Mini.entity;

import com.dev.E_commerce.Mini.enums.PaymentMethod;
import com.dev.E_commerce.Mini.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentStatus status;

    @Column(nullable = false)
    BigDecimal amount;

    @Column(name = "transaction_id")
    String transactionId;

    @Column(name = "paid_at")
    LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    LocalDateTime createdAt;
}
