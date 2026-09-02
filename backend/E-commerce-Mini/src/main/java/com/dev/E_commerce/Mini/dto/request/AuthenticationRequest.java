package com.dev.E_commerce.Mini.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationRequest {
    // Chỉ chặn rỗng/quá dài, KHÔNG áp ràng buộc định dạng chi tiết ở đây:
    // thông báo lỗi khác nhau giữa "sai định dạng" và "sai mật khẩu" sẽ giúp
    // kẻ tấn công suy đoán tài khoản nào tồn tại.
    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    @Size(max = 50, message = "USERNAME_INVALID")
    String username;

    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    @Size(max = 100, message = "PASSWORD_INVALID")
    String password;
}
