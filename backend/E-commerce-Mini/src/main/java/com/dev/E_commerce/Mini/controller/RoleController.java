package com.dev.E_commerce.Mini.controller;

import com.dev.E_commerce.Mini.dto.request.RoleRequest;
import com.dev.E_commerce.Mini.dto.response.ApiResponse;
import com.dev.E_commerce.Mini.dto.response.RoleResponse;
import com.dev.E_commerce.Mini.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Toàn bộ API quản lý vai trò chỉ dành cho ADMIN — trước đây mọi user đã
// đăng nhập đều tạo/xoá được role, tức tự trao quyền cho chính mình.
@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/roles")
public class RoleController {
    RoleService roleService;

    @PostMapping
    public ApiResponse<RoleResponse> createRole(@RequestBody @Valid RoleRequest request){
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<RoleResponse>> getAllRoles(){
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getAllRoles())
                .build();
    }

    @DeleteMapping("/{role}")
    ApiResponse<Void> delete(@PathVariable String role){
        roleService.deleteRole(role);
        return ApiResponse.<Void>builder().build();
    }

}
