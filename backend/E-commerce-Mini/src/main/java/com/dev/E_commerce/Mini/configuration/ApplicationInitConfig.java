package com.dev.E_commerce.Mini.configuration;

import com.dev.E_commerce.Mini.entity.Role;
import com.dev.E_commerce.Mini.entity.User;
import com.dev.E_commerce.Mini.repository.RoleRepository;
import com.dev.E_commerce.Mini.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;
    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository){
        return args -> {
            Role adminRole = roleRepository.findById("ADMIN")
                    .orElseGet(() -> roleRepository.save(
                            Role.builder().name("ADMIN").description("Administrator").build()));
            roleRepository.findById("USER")
                    .orElseGet(() -> roleRepository.save(
                            Role.builder().name("USER").description("Regular user").build()));

            if (userRepository.findByUsername("admin").isEmpty()){
                User user = User.builder()
                        .username("admin")
                        .email("admin@gamil.com")
                        .password(passwordEncoder.encode("admin"))
                        .roles(Set.of(adminRole))
                        .build();
                userRepository.save(user);
            }
        };
    }
}
