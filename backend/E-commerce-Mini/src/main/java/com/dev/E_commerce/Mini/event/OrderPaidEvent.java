package com.dev.E_commerce.Mini.event;

import com.dev.E_commerce.Mini.entity.Order;

import java.math.BigDecimal;

/**
 * Phát ra khi một đơn hàng đã THANH TOÁN XONG — là thời điểm gửi email xác nhận.
 *
 * <p>Thay cho OrderCreatedEvent cũ, vốn phát ngay lúc tạo đơn. Hậu quả của cách
 * cũ: email "xác nhận đơn hàng" bay đi trước cả bước thanh toán. Khách chọn
 * chuyển khoản mà chưa chuyển, hoặc bước tạo thanh toán lỗi, vẫn nhận xác nhận
 * cho một đơn chưa ai trả tiền.
 *
 * <p>Hai nơi phát:
 * <ul>
 *   <li>PaymentService — thanh toán COD, được chốt ngay lúc tạo.</li>
 *   <li>SepayService — khi webhook SePay báo tiền đã về.</li>
 * </ul>
 *
 * <p>Chỉ mang giá trị thuần chứ không mang entity: listener chạy ở luồng khác
 * sau khi transaction đã đóng, chạm vào entity lúc đó sẽ gặp proxy lazy đã tách
 * khỏi session.
 */
public record OrderPaidEvent(Long orderId, String userEmail, String userName, BigDecimal totalPrice) {

    /** Gọi trong transaction còn mở, khi order.getUser() vẫn tải được. */
    public static OrderPaidEvent of(Order order) {
        var user = order.getUser();
        return new OrderPaidEvent(
                order.getId(),
                user != null ? user.getEmail() : null,
                user != null ? user.getFullName() : null,
                order.getTotalPrice());
    }
}
