package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.dto.request.UpdateOrderStatusRequest;
import com.dev.E_commerce.Mini.dto.response.OrderResponse;
import com.dev.E_commerce.Mini.entity.Order;
import com.dev.E_commerce.Mini.exception.AppException;
import com.dev.E_commerce.Mini.exception.ErrorCode;
import com.dev.E_commerce.Mini.mapper.OrderMapper;
import com.dev.E_commerce.Mini.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminOrderService {
    OrderRepository orderRepository;
    OrderMapper orderMapper;

    public List<OrderResponse> getAllOrders(){
        return orderRepository.findAll().stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));
        order.setStatus(request.getStatus());
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(savedOrder);
    }

    public OrderResponse getOrderById(Long orderId){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));
        return orderMapper.toOrderResponse(order);
    }

}
