package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.dto.response.PaymentResponse;
import com.dev.E_commerce.Mini.mapper.PaymentMapper;
import com.dev.E_commerce.Mini.repository.PaymentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Separate bean on purpose: REQUIRES_NEW only works through Spring's proxy,
 * so this must be called from a different bean than PaymentService
 * (self-invocation — this.method() from within the same class — silently
 * ignores the annotation). Same pattern as OrderLookupService.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentLookupService {
    PaymentRepository paymentRepository;
    PaymentMapper paymentMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentResponse findByOrderIdOrThrow(Long orderId, RuntimeException fallback) {
        return paymentRepository.findByOrder_Id(orderId)
                .map(paymentMapper::toPaymentResponse)
                .orElseThrow(() -> fallback);
    }
}
