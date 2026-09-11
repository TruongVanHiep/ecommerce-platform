package com.dev.E_commerce.Mini.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * Payload SePay POST tới webhook mỗi khi tài khoản ngân hàng đã liên kết có
 * biến động số dư — kể cả những khoản chẳng liên quan tới đơn hàng nào (bạn bè
 * chuyển tiền, lương về...). Định dạng theo https://docs.sepay.vn/tich-hop-webhooks.html
 *
 * <p>Bỏ qua trường lạ thay vì báo lỗi: SePay thêm trường mới vào payload thì
 * webhook vẫn chạy, không đột nhiên trả 400 rồi bị gửi lại 7 lần.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SepayWebhookRequest {
    /** Mã giao dịch phía SePay, duy nhất — dùng để chống xử lý trùng. */
    Long id;
    /** Tên ngân hàng, vd "MBBank". */
    String gateway;
    String transactionDate;
    /** Tài khoản nhận tiền. */
    String accountNumber;
    /** Tài khoản ảo (VA), nếu có. */
    String subAccount;
    /** Mã thanh toán SePay tự tách từ nội dung, nếu đã cấu hình tiền tố trong SePay. */
    String code;
    /** Nội dung chuyển khoản khách ghi. */
    String content;
    /** "in" = tiền vào, "out" = tiền ra. */
    String transferType;
    String description;
    BigDecimal transferAmount;
    BigDecimal accumulated;
    String referenceCode;
}
