package com.dev.E_commerce.Mini.dto.response;

import com.dev.E_commerce.Mini.entity.Order;
import com.dev.E_commerce.Mini.entity.Product;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemResponse {
    Long orderItemId;
    Long productId;
    String productName;
    int quantity;
    BigDecimal unitPrice;
    BigDecimal subTotal;
}
