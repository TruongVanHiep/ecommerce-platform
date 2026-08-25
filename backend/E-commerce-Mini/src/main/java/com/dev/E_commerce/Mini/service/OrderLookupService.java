package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.dto.response.OrderResponse;
import com.dev.E_commerce.Mini.mapper.OrderMapper;
import com.dev.E_commerce.Mini.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Separate bean on purpose: REQUIRES_NEW only works through Spring's proxy,
 * so this must be called from a different bean than OrderService (self-invocation
 * — this.method() from within the same class — silently ignores the annotation).
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderLookupService {
    OrderRepository orderRepository;
    OrderMapper orderMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderResponse findByIdempotencyKeyOrThrow(String idempotencyKey, RuntimeException fallback) {
        return orderRepository.findByIdempotencyKey(idempotencyKey)
                .map(orderMapper::toOrderResponse)
                .orElseThrow(() -> fallback);
    }
}
