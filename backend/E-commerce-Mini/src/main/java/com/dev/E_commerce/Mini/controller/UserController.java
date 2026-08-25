package com.dev.E_commerce.Mini.controller;

import com.dev.E_commerce.Mini.dto.request.UserRegisterRequest;
import com.dev.E_commerce.Mini.dto.request.UserUpdateRequest;
import com.dev.E_commerce.Mini.dto.response.ApiResponse;
import com.dev.E_commerce.Mini.dto.response.UserResponse;
import com.dev.E_commerce.Mini.entity.User;
import com.dev.E_commerce.Mini.mapper.UserMapper;
import com.dev.E_commerce.Mini.repository.UserRepository;
import com.dev.E_commerce.Mini.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/users")
public class UserController {
    UserRepository userRepository;
    UserService userService;
    UserMapper userMapper;

    @PostMapping
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserRegisterRequest request){
        User user = userService.createUser(request);
        return ApiResponse.<UserResponse>builder()
                .result(userMapper.toUserResponse(user))
                .build();
    }

    @PutMapping ("/{userId}")
    public ApiResponse<UserResponse> createUser(
            @PathVariable Long userId,
            @RequestBody @Valid UserUpdateRequest request){
        UserResponse user = userService.updateUser(userId,request);
        return ApiResponse.<UserResponse>builder()
                .result(user)
                .build();
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getAllUsers(){
        return ApiResponse.<List<UserResponse>>builder()
                .result(userRepository.findAll().stream()
                        .map(userMapper::toUserResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    @GetMapping("/myInfo")
    public ApiResponse<UserResponse> getMyInformation(){
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInformation())
                .build();
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId){
        userService.deleteUser(userId);
    }

}
