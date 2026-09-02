package com.dev.E_commerce.Mini.dto.request;

import com.dev.E_commerce.Mini.validator.DobConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRegisterRequest {
    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    @Size(min = 8, max = 50, message = "USERNAME_INVALID")
    String username;

    @NotBlank(message = "EMAIL_INVALID")
    @Email(message = "EMAIL_INVALID")
    @Size(max = 255, message = "EMAIL_INVALID")
    String email;

    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    @Size(min = 8, max = 100, message = "PASSWORD_INVALID")
    String password;

    @Size(max = 100, message = "FULL_NAME_INVALID")
    String fullName;

    @DobConstraint(min = 18, message = "INVALID_DOB")
    LocalDate dob;

    @Pattern(regexp = "^$|^(0[3|5|7|8|9])+([0-9]{8})$", message = "PHONE_INVALID")
    String phone;

    @Size(max = 255, message = "ADDRESS_INVALID")
    String address;

    // CỐ Ý KHÔNG có field `roles`: người đăng ký không được tự chọn vai trò.
    // UserService luôn gán cứng vai trò USER khi tạo tài khoản mới.
}
