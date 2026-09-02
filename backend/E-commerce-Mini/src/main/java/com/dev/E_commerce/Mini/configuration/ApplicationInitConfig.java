package com.dev.E_commerce.Mini.configuration;

import com.dev.E_commerce.Mini.entity.Role;
import com.dev.E_commerce.Mini.entity.User;
import com.dev.E_commerce.Mini.repository.RoleRepository;
import com.dev.E_commerce.Mini.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    /**
     * Mật khẩu admin khởi tạo, lấy từ biến môi trường ADMIN_INIT_PASSWORD.
     * Trước đây mật khẩu bị hardcode là "admin" — ai biết địa chỉ server đều
     * đăng nhập được bằng admin/admin. Bỏ trống biến này => không seed admin.
     */
    @Value("${app.admin.init-password:}")
    String adminInitPassword;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository){
        return args -> {
            Role adminRole = roleRepository.findById("ADMIN")
                    .orElseGet(() -> roleRepository.save(
                            Role.builder().name("ADMIN").description("Administrator").build()));
            roleRepository.findById("USER")
                    .orElseGet(() -> roleRepository.save(
                            Role.builder().name("USER").description("Regular user").build()));

            if (!StringUtils.hasText(adminInitPassword)) {
                log.info("ADMIN_INIT_PASSWORD chưa được cấu hình — bỏ qua bước seed tài khoản admin.");
                return;
            }

            if (userRepository.findByUsername("admin").isEmpty()){
                User user = User.builder()
                        .username("admin")
                        .email("admin@ecommerce-mini.local")
                        .password(passwordEncoder.encode(adminInitPassword))
                        .roles(Set.of(adminRole))
                        .build();
                userRepository.save(user);
                log.warn("Đã tạo tài khoản admin khởi tạo từ ADMIN_INIT_PASSWORD — hãy đổi mật khẩu sau lần đăng nhập đầu tiên.");
            }
        };
    }
}
