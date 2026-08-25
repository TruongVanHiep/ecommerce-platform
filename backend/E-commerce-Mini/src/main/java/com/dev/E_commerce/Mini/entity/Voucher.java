package com.dev.E_commerce.Mini.entity;

import com.dev.E_commerce.Mini.enums.DiscountType;
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
@Table(name = "vouchers")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false)
    String code;

    String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    DiscountType discountType;

    @Column(name = "discount_value", nullable = false)
    BigDecimal discountValue;

    @Column(name = "min_order_value", nullable = false)
    BigDecimal minOrderValue;

    @Column(name = "max_discount_amount")
    BigDecimal maxDiscountAmount;

    @Column(name = "usage_limit")
    Integer usageLimit;

    @Column(name = "used_count")
    int usedCount;

    @Column(name = "start_date", nullable = false)
    LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    LocalDateTime endDate;

    boolean active;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    LocalDateTime createdAt;
}
