package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.entity.RefreshToken;
import com.dev.E_commerce.Mini.entity.Role;
import com.dev.E_commerce.Mini.entity.User;
import com.dev.E_commerce.Mini.repository.RefreshTokenRepository;
import com.dev.E_commerce.Mini.repository.RoleRepository;
import com.dev.E_commerce.Mini.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Mock
    OAuth2User principal;

    AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(passwordEncoder, userRepository, roleRepository, refreshTokenRepository);
        ReflectionTestUtils.setField(authenticationService, "signerKey", "1234567890123456789012345678901234567890123456789012345678901234");
        ReflectionTestUtils.setField(authenticationService, "accessTokenMinutes", 15L);
        ReflectionTestUtils.setField(authenticationService, "refreshTokenDays", 7L);
        // Repository tra ve chinh doi tuong duoc luu de test doc lai gia tri.
        lenient().when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void authenticateGoogle_shouldCreateUserWhenNotExists() {
        String email = "google.user@example.com";
        String name = "Google User";
        Role userRole = Role.builder().name("USER").build();

        when(principal.getAttribute("email")).thenReturn(email);
        when(principal.getAttribute("name")).thenReturn(name);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.findByUsername(email)).thenReturn(Optional.empty());
        when(roleRepository.findById("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = authenticationService.authenticateGoogle(principal);

        assertThat(response.isAuthenticated()).isTrue();
        assertThat(response.getToken()).isNotBlank();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo(email);
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getFullName()).isEqualTo(name);
        assertThat(savedUser.getRoles()).isEqualTo(Set.of(userRole));
    }

    @Test
    void authenticateGoogle_shouldReuseExistingUserWhenFoundByEmail() {
        String email = "existing.google.user@example.com";
        Role userRole = Role.builder().name("USER").build();
        User existingUser = User.builder()
                .username("local-username")
                .email(email)
                .password("hashed")
                .roles(Set.of(userRole))
                .build();

        when(principal.getAttribute("email")).thenReturn(email);
        when(principal.getAttribute("name")).thenReturn("Existing User");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        var response = authenticationService.authenticateGoogle(principal);

        assertThat(response.isAuthenticated()).isTrue();
        assertThat(response.getToken()).isNotBlank();
        verify(userRepository, never()).save(any(User.class));
    }
}



