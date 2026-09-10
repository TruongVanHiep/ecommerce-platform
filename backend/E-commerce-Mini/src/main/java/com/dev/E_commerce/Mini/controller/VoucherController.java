package com.dev.E_commerce.Mini.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import com.dev.E_commerce.Mini.dto.request.ApplyVoucherRequest;
import com.dev.E_commerce.Mini.dto.request.VoucherRequest;
import com.dev.E_commerce.Mini.dto.response.ApiResponse;
import com.dev.E_commerce.Mini.dto.response.AvailableVoucherResponse;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
// Bật kiểm tra ràng buộc trên @RequestParam. Thiếu annotation này thì
// @DecimalMin ở getAvailableVouchers bị bỏ qua im lặng.
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VoucherController {
    VoucherService voucherService;
    UserRepository userRepository;

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PostMapping
    public ApiResponse<VoucherResponse> createVoucher(@RequestBody @Valid VoucherRequest request) {
        return ApiResponse.<VoucherResponse>builder()
                .result(voucherService.createVoucher(request))
                .build();
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<VoucherResponse> updateVoucher(@PathVariable Long id, @RequestBody @Valid VoucherRequest request) {
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

    /**
     * Voucher người dùng có thể chọn cho giỏ hàng hiện tại.
     *
     * <p>Khác {@code GET /api/vouchers} (chỉ ADMIN, trả về mọi voucher kể cả
     * đã tắt/hết hạn): endpoint này chỉ trả về mã còn hiệu lực, bỏ mã người
     * dùng đã dùng, và tính sẵn số tiền được giảm theo giá trị giỏ hàng.
     *
     * <p>Cần đăng nhập vì kết quả phụ thuộc lịch sử dùng voucher của từng người.
     */
    @GetMapping("/available")
    public ApiResponse<List<AvailableVoucherResponse>> getAvailableVouchers(
            @RequestParam(defaultValue = "0")
            @DecimalMin(value = "0.0", message = "INVALID_INPUT") BigDecimal orderTotal) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return ApiResponse.<List<AvailableVoucherResponse>>builder()
                .result(voucherService.getAvailableVouchers(orderTotal, user))
                .build();
    }

    @PostMapping("/apply")
    public ApiResponse<VoucherPreviewResponse> applyVoucher(@RequestBody @Valid ApplyVoucherRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return ApiResponse.<VoucherPreviewResponse>builder()
                .result(voucherService.previewVoucher(request, user))
                .build();
    }
}
