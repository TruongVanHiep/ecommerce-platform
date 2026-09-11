package com.dev.E_commerce.Mini.dto.response;

import com.dev.E_commerce.Mini.enums.PaymentMethod;
import com.dev.E_commerce.Mini.enums.PaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {
    Long id;
    Long orderId;
    PaymentMethod method;
    PaymentStatus status;
    BigDecimal amount;
    String transactionId;
    LocalDateTime paidAt;
    LocalDateTime createdAt;

    // Thông tin chuyển khoản — chỉ có giá trị khi method = SEPAY và chưa thanh
    // toán, để frontend hiện mã QR. Các trường hợp khác đều null.
    // Không lưu trong bảng payments: số tài khoản đổi thì mọi đơn cũ phải trỏ
    // theo, nên tính lại từ cấu hình mỗi lần trả về (xem SepayService).
    String bankCode;
    String accountNumber;
    String accountName;
    String transferContent;
    String qrUrl;
}
