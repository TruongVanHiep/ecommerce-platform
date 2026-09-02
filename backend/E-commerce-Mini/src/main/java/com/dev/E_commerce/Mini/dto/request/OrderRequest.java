package com.dev.E_commerce.Mini.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {
    @NotBlank(message = "ADDRESS_INVALID")
    @Size(max = 255, message = "ADDRESS_INVALID")
    String shippingAddress;

    @NotBlank(message = "PHONE_INVALID")
    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "PHONE_INVALID")
    String phone;

    @Size(max = 50, message = "VOUCHER_CODE_INVALID")
    String voucherCode;

    // Khoá chống trùng đơn do client sinh (UUID) — giới hạn độ dài để không
    // ai nhét chuỗi khổng lồ vào cột unique.
    @Size(max = 100, message = "INVALID_INPUT")
    String idempotencyKey;
}
