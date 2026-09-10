package com.dev.E_commerce.Mini.dto.response;

import com.dev.E_commerce.Mini.enums.DiscountType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Một voucher người dùng có thể thấy trong giỏ hàng.
 *
 * <p>Khác {@link VoucherResponse} (dành cho trang quản trị) ở chỗ đã tính sẵn
 * theo giá trị giỏ hàng hiện tại: dùng được hay chưa, được giảm bao nhiêu, còn
 * thiếu bao nhiêu nữa mới đủ điều kiện. Tính ở server thay vì để frontend tự
 * tính lại — logic giảm giá (phần trăm, trần giảm, không vượt quá tổng đơn) chỉ
 * nên tồn tại ở một chỗ.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AvailableVoucherResponse {
    Long id;
    String code;
    String description;
    DiscountType discountType;
    BigDecimal discountValue;
    BigDecimal minOrderValue;
    BigDecimal maxDiscountAmount;
    LocalDateTime endDate;

    /** Giỏ hàng hiện tại đã đủ điều kiện dùng mã này chưa. */
    boolean eligible;

    /** Số tiền được giảm nếu áp ngay bây giờ. Bằng 0 khi chưa đủ điều kiện. */
    BigDecimal estimatedDiscount;

    /** Còn thiếu bao nhiêu nữa mới đạt minOrderValue. Bằng 0 khi đã đủ. */
    BigDecimal amountNeeded;
}
