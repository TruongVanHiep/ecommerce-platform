package com.dev.E_commerce.Mini.controller;

import jakarta.validation.Valid;
import com.dev.E_commerce.Mini.dto.request.UpdateOrderStatusRequest;
import com.dev.E_commerce.Mini.dto.response.ApiResponse;
import com.dev.E_commerce.Mini.dto.response.OrderResponse;
import com.dev.E_commerce.Mini.service.AdminOrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminOrderController {
    AdminOrderService adminOrderService;

    @GetMapping
    public ApiResponse<List<OrderResponse>> getAllOrders() {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(adminOrderService.getAllOrders())
                .build();
    }

    @PutMapping("/{id}/status")
    public ApiResponse<OrderResponse> updateStatus(
            @PathVariable Long id, @RequestBody @Valid UpdateOrderStatusRequest request) {
        return ApiResponse.<OrderResponse>builder()
                .result(adminOrderService.updateOrderStatus(id, request))
                .build();
    }
    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable Long id) {
        return ApiResponse.<OrderResponse>builder()
                .result(adminOrderService.getOrderById(id))
                .build();
    }
}
