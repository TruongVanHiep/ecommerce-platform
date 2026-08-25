package com.dev.E_commerce.Mini.event;

import java.math.BigDecimal;

/**
 * Published only after the order transaction has committed successfully
 * (see OrderService#createOrder + OrderEventListener). Carries plain values,
 * not the entity, so listeners never touch a detached/lazy proxy or need
 * another DB round-trip just to build a confirmation email.
 */
public record OrderCreatedEvent(Long orderId, String userEmail, String userName, BigDecimal totalPrice) {
}
