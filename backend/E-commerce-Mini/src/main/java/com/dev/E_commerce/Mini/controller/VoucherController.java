package com.dev.E_commerce.Mini.controller;

import com.dev.E_commerce.Mini.dto.request.ApplyVoucherRequest;
import com.dev.E_commerce.Mini.dto.request.VoucherRequest;
import com.dev.E_commerce.Mini.dto.response.ApiResponse;
import com.dev.E_commerce.Mini.dto.response.VoucherPreviewResponse;
import com.dev.E_commerce.Mini.dto.response.VoucherResponse;
import com.dev.E_commerce.Mini.entity.User;
import com.dev.E_commerce.Mini.exception.AppException;
import com.dev.E_commerce.Mini.exception.ErrorCode;
import com.dev.E_commerce.Mini.repository.UserRepository;
import com.dev.E_commerce.Mini.service.VoucherService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VoucherController {
    VoucherService voucherService;
    UserRepository userRepository;

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PostMapping
    public ApiResponse<VoucherResponse> createVoucher(@RequestBody VoucherRequest request) {
        return ApiResponse.<VoucherResponse>builder()
                .result(voucherService.createVoucher(request))
                .build();
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<VoucherResponse> updateVoucher(@PathVariable Long id, @RequestBody VoucherRequest request) {
        return ApiResponse.<VoucherResponse>builder()
                .result(voucherService.updateVoucher(id, request))
                .build();
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ApiResponse.<Void>builder().build();
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @GetMapping
    public ApiResponse<List<VoucherResponse>> getAllVouchers() {
        return ApiResponse.<List<VoucherResponse>>builder()
                .result(voucherService.getAllVouchers())
                .build();
    }

    @PostMapping("/apply")
    public ApiResponse<VoucherPreviewResponse> applyVoucher(@RequestBody ApplyVoucherRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return ApiResponse.<VoucherPreviewResponse>builder()
                .result(voucherService.previewVoucher(request, user))
                .build();
    }
}
