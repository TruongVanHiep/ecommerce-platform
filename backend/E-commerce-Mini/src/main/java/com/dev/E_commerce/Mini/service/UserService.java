package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.dto.request.UserRegisterRequest;
import com.dev.E_commerce.Mini.dto.request.UserUpdateRequest;
import com.dev.E_commerce.Mini.dto.response.UserResponse;
import com.dev.E_commerce.Mini.entity.Role;
import com.dev.E_commerce.Mini.entity.User;
import com.dev.E_commerce.Mini.exception.AppException;
import com.dev.E_commerce.Mini.exception.ErrorCode;
import com.dev.E_commerce.Mini.mapper.UserMapper;
import com.dev.E_commerce.Mini.repository.RoleRepository;
import com.dev.E_commerce.Mini.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    PasswordEncoder passwordEncoder;
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;

    public User createUser(UserRegisterRequest request){
        if (userRepository.existsUserByUsername(request.getUsername())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        Role userRole = roleRepository.findById("USER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        User user = userMapper.toUser(request);
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(userRole));

        return userRepository.save(user);
    }

    public UserResponse updateUser(Long id,UserUpdateRequest request){
        User user = userRepository.findById(id).orElseThrow(() ->
                new AppException(ErrorCode.USER_NOT_EXISTED));
        userMapper.updateUser(user, request);
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        var role = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(role));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    public UserResponse getMyInformation(){
        var contextHolder = SecurityContextHolder.getContext().getAuthentication();
        String username = contextHolder.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public void deleteUser(Long userId){
        userRepository.deleteById(userId);
    }

}
