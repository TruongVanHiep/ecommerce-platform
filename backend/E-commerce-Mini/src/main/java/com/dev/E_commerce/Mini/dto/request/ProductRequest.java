package com.dev.E_commerce.Mini.dto.request;

import com.dev.E_commerce.Mini.entity.CartItem;
import com.dev.E_commerce.Mini.entity.Category;
import com.dev.E_commerce.Mini.entity.OrderItem;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequest {
    String name;
    String description;
    BigDecimal price;
    String image;
    int stock;
    Long categoryId;
}
