package com.dev.E_commerce.Mini.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Long id;
    String username;
    String email;
    // CỐ Ý KHÔNG có field `password`: trước đây MapStruct map cả hash BCrypt
    // của mật khẩu ra response, tức mọi API trả UserResponse đều lộ hash
    // cho client (có thể mang đi crack offline).
    String fullName;
    LocalDate dob;
    String phone;
    String address;
    Set<RoleResponse> roles;
    LocalDateTime createdAt;
}
