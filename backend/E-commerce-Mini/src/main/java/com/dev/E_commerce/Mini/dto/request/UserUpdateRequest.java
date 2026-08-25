package com.dev.E_commerce.Mini.dto.request;

import com.dev.E_commerce.Mini.validator.DobConstraint;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    @Size(min = 8, message = "USERNAME_INVALID")
    String username;
    String email;
    @Size(min = 8, message = "PASSWORD_INVALID")
    String password;
    String fullName;
    @DobConstraint(min = 18, message = "INVALID_DOB")
    LocalDate dob;
    String phone;
    String address;
    Set<String> roles;
}
