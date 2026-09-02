package com.dev.E_commerce.Mini.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplyVoucherRequest {
    @NotBlank(message = "VOUCHER_CODE_INVALID")
    @Size(max = 50, message = "VOUCHER_CODE_INVALID")
    String code;

    // Đây chỉ là endpoint XEM TRƯỚC mức giảm. Tổng tiền thật của đơn hàng luôn
    // được tính lại phía server từ giỏ hàng (OrderService), nên giá trị client
    // gửi ở đây không ảnh hưởng số tiền thực thu.
    @NotNull(message = "REQUIRED_FIELD_MISSING")
    @DecimalMin(value = "0.0", message = "INVALID_INPUT")
    BigDecimal orderTotal;
}
