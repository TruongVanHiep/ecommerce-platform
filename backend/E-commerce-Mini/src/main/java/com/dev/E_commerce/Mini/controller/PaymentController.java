package com.dev.E_commerce.Mini.controller;

import com.dev.E_commerce.Mini.dto.request.CreatePaymentRequest;
import com.dev.E_commerce.Mini.dto.response.ApiResponse;
import com.dev.E_commerce.Mini.dto.response.PaymentResponse;
import com.dev.E_commerce.Mini.service.PaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders/{orderId}/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {
    PaymentService paymentService;

    @PostMapping
    public ApiResponse<PaymentResponse> createPayment(
            @PathVariable Long orderId,
            @RequestBody CreatePaymentRequest request
    ) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.createPaymentForOrder(orderId, request))
                .build();
    }

    @GetMapping
    public ApiResponse<PaymentResponse> getPayment(@PathVariable Long orderId) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.getPaymentByOrder(orderId))
                .build();
    }
}
