package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.dto.request.OrderRequest;
import com.dev.E_commerce.Mini.dto.response.OrderResponse;
import com.dev.E_commerce.Mini.entity.*;
import com.dev.E_commerce.Mini.enums.Status;
import com.dev.E_commerce.Mini.exception.AppException;
import com.dev.E_commerce.Mini.exception.ErrorCode;
import com.dev.E_commerce.Mini.mapper.OrderMapper;
import com.dev.E_commerce.Mini.repository.CartRepository;
import com.dev.E_commerce.Mini.repository.OrderRepository;
import com.dev.E_commerce.Mini.repository.ProductRepository;
import com.dev.E_commerce.Mini.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {
    OrderRepository orderRepository;
    OrderMapper orderMapper;
    UserRepository userRepository;
    CartRepository cartRepository;
    VoucherService voucherService;
    OrderLookupService orderLookupService;
    ProductRepository productRepository;
    MeterRegistry meterRegistry;

    private static final BigDecimal FREE_SHIPPING_THRESHOLD = BigDecimal.valueOf(500_000);
    private static final BigDecimal STANDARD_SHIPPING_FEE = BigDecimal.valueOf(30_000);

    @Transactional
    public OrderResponse createOrder(OrderRequest request){
        if (StringUtils.hasText(request.getIdempotencyKey())) {
            Optional<Order> existing = orderRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                return orderMapper.toOrderResponse(existing.get()); // trả đơn cũ, không tạo mới
            }
        }

        try {
            return doCreateOrder(request);
        } catch (DataIntegrityViolationException e) {
            // Race: 2 request cùng idempotencyKey lọt qua check phía trên gần như đồng thời.
            // Request thua cuộc sẽ vỡ unique constraint ở đây — tra lại đơn request kia vừa tạo
            // trong một transaction MỚI (transaction hiện tại đã bị đánh dấu rollback-only).
            if (StringUtils.hasText(request.getIdempotencyKey())) {
                return orderLookupService.findByIdempotencyKeyOrThrow(request.getIdempotencyKey(), e);
            }
            throw e;
        }
    }

    private OrderResponse doCreateOrder(OrderRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_EXISTED));

        if (cart.getCartItems().isEmpty()){
            meterRegistry.counter("orders.placed", "status", "rejected").increment();
            log.warn("Order rejected: empty cart username={}", username);
            throw new AppException(ErrorCode.CART_EMPTY);
        }

        Order order = Order.builder()
                .user(user)
                .status(Status.PENDING)
                .shippingAddress(request.getShippingAddress())
                .phone(request.getPhone())
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        List<OrderItem> orderItems = new ArrayList<>();

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cart.getCartItems()){
            BigDecimal subTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            // UPDATE nguyên tử: DB tự kiểm tra đủ hàng và trừ kho trong cùng 1 câu lệnh,
            // an toàn khi nhiều đơn hàng cùng tranh mua 1 sản phẩm sắp hết (xem ProductRepository).
            int updated = productRepository.decreaseStockIfAvailable(item.getProduct().getId(), item.getQuantity());
            if (updated == 0){
                meterRegistry.counter("products.stock.rejected", "productId", String.valueOf(item.getProduct().getId())).increment();
                log.warn("Order rejected: out of stock productId={} requestedQty={} username={}",
                        item.getProduct().getId(), item.getQuantity(), username);
                throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(item.getProduct())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .build();

            orderItems.add(orderItem);
            total = total.add(subTotal);
        }

        Voucher voucher = null;
        BigDecimal discount = BigDecimal.ZERO;
        if (StringUtils.hasText(request.getVoucherCode())) {
            try {
                voucher = voucherService.getValidVoucher(request.getVoucherCode(), total, user);
            } catch (AppException e) {
                meterRegistry.counter("vouchers.rejected", "reason", e.getErrorCode().name()).increment();
                log.warn("Voucher rejected: code={} reason={} username={}", request.getVoucherCode(), e.getErrorCode(), username);
                throw e;
            }
            discount = voucherService.calculateDiscount(voucher, total);
        }

        BigDecimal totalAfterDiscount = total.subtract(discount);
        BigDecimal shippingFee = totalAfterDiscount.compareTo(FREE_SHIPPING_THRESHOLD) >= 0
                ? BigDecimal.ZERO
                : STANDARD_SHIPPING_FEE;

        order.setOrderItems(orderItems);
        order.setTotalPrice(totalAfterDiscount.add(shippingFee));
        order.setDiscountAmount(discount);
        order.setShippingFee(shippingFee);
        order.setVoucher(voucher);

        Order savedOrder = orderRepository.save(order);
        cart.getCartItems().clear();

        if (voucher != null) {
            voucherService.recordUsage(voucher, user, savedOrder);
        }

        // KHÔNG gửi email xác nhận ở đây: đơn vừa tạo chưa được thanh toán. Email đi
        // khi thanh toán chốt — COD ngay lúc tạo thanh toán, chuyển khoản khi webhook
        // SePay báo tiền về (xem OrderPaidEvent).

        meterRegistry.counter("orders.placed", "status", "success").increment();
        meterRegistry.summary("orders.value").record(savedOrder.getTotalPrice().doubleValue());
        log.info("Order created orderId={} username={} total={} itemCount={} voucherApplied={}",
                savedOrder.getId(), username, savedOrder.getTotalPrice(), orderItems.size(), voucher != null);

        return orderMapper.toOrderResponse(savedOrder);
    }

    public List<OrderResponse> getMyOrders(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return orderRepository.findAllByUser(user)
                .stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    public OrderResponse getOrderById(Long orderId){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));
        if (!order.getUser().getId().equals(user.getId())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return orderMapper.toOrderResponse(order);
    }
}
