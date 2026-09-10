package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.dto.request.ApplyVoucherRequest;
import com.dev.E_commerce.Mini.dto.request.VoucherRequest;
import com.dev.E_commerce.Mini.dto.response.AvailableVoucherResponse;
import com.dev.E_commerce.Mini.dto.response.VoucherPreviewResponse;
import com.dev.E_commerce.Mini.dto.response.VoucherResponse;
import com.dev.E_commerce.Mini.entity.Order;
import com.dev.E_commerce.Mini.entity.User;
import com.dev.E_commerce.Mini.entity.Voucher;
import com.dev.E_commerce.Mini.entity.VoucherUsage;
import com.dev.E_commerce.Mini.enums.DiscountType;
import com.dev.E_commerce.Mini.exception.AppException;
import com.dev.E_commerce.Mini.exception.ErrorCode;
import com.dev.E_commerce.Mini.mapper.VoucherMapper;
import com.dev.E_commerce.Mini.repository.VoucherRepository;
import com.dev.E_commerce.Mini.repository.VoucherUsageRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VoucherService {
    VoucherRepository voucherRepository;
    VoucherUsageRepository voucherUsageRepository;
    VoucherMapper voucherMapper;

    @Transactional
    public VoucherResponse createVoucher(VoucherRequest request) {
        if (voucherRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.VOUCHER_CODE_EXISTED);
        }
        Voucher voucher = voucherMapper.toVoucher(request);
        return voucherMapper.toVoucherResponse(voucherRepository.save(voucher));
    }

    @Transactional
    public VoucherResponse updateVoucher(Long id, VoucherRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_EXISTED));
        voucherMapper.updateVoucher(voucher, request);
        return voucherMapper.toVoucherResponse(voucherRepository.save(voucher));
    }

    @Transactional
    public void deleteVoucher(Long id) {
        if (!voucherRepository.existsById(id)) {
            throw new AppException(ErrorCode.VOUCHER_NOT_EXISTED);
        }
        voucherRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<VoucherResponse> getAllVouchers() {
        return voucherRepository.findAll().stream()
                .map(voucherMapper::toVoucherResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VoucherPreviewResponse previewVoucher(ApplyVoucherRequest request, User user) {
        Voucher voucher = getValidVoucher(request.getCode(), request.getOrderTotal(), user);
        BigDecimal discount = calculateDiscount(voucher, request.getOrderTotal());
        return VoucherPreviewResponse.builder()
                .code(voucher.getCode())
                .discountAmount(discount)
                .finalTotal(request.getOrderTotal().subtract(discount))
                .build();
    }

    /**
     * Danh sách voucher hiện cho người dùng chọn trong giỏ hàng, đã tính sẵn
     * theo giá trị giỏ hàng hiện tại.
     *
     * <p>Trước đây người dùng phải TỰ BIẾT mã rồi gõ tay — không có chỗ nào
     * hiển thị mã đang có, nên tính năng voucher gần như vô dụng với người dùng
     * thật. Endpoint này là thứ khiến nó dùng được.
     *
     * <p>Voucher chưa đạt giá trị tối thiểu vẫn được trả về (eligible = false)
     * kèm amountNeeded, để giao diện gợi ý mua thêm thay vì giấu ưu đãi đi.
     */
    @Transactional(readOnly = true)
    public List<AvailableVoucherResponse> getAvailableVouchers(BigDecimal orderTotal, User user) {
        BigDecimal total = orderTotal == null ? BigDecimal.ZERO : orderTotal;

        return voucherRepository.findRedeemable(LocalDateTime.now()).stream()
                // Mỗi người chỉ dùng được một voucher một lần (bảng voucher_usages
                // có unique voucher_id + user_id). Đã dùng rồi thì hiện ra chỉ gây
                // bực bội vì bấm vào sẽ báo lỗi.
                .filter(v -> user == null
                        || !voucherUsageRepository.existsByVoucher_IdAndUser_Id(v.getId(), user.getId()))
                .map(v -> toAvailableResponse(v, total))
                // Dùng được xếp trước, trong đó mã giảm nhiều nhất lên đầu. Nhóm
                // chưa đủ điều kiện xếp sau, ưu tiên mã cần mua thêm ít nhất.
                .sorted(Comparator
                        .comparing(AvailableVoucherResponse::isEligible).reversed()
                        .thenComparing(r -> r.isEligible()
                                ? r.getEstimatedDiscount().negate()
                                : r.getAmountNeeded()))
                .toList();
    }

    private AvailableVoucherResponse toAvailableResponse(Voucher voucher, BigDecimal orderTotal) {
        boolean eligible = orderTotal.compareTo(voucher.getMinOrderValue()) >= 0;

        return AvailableVoucherResponse.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .description(voucher.getDescription())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .minOrderValue(voucher.getMinOrderValue())
                .maxDiscountAmount(voucher.getMaxDiscountAmount())
                .endDate(voucher.getEndDate())
                .eligible(eligible)
                .estimatedDiscount(eligible ? calculateDiscount(voucher, orderTotal) : BigDecimal.ZERO)
                .amountNeeded(eligible ? BigDecimal.ZERO : voucher.getMinOrderValue().subtract(orderTotal))
                .build();
    }

    /**
     * Validates a voucher code against expiry/active/usage-limit/min-order rules and,
     * if a user is supplied, whether that user has already used it.
     */
    @Transactional(readOnly = true)
    public Voucher getValidVoucher(String code, BigDecimal orderTotal, User user) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_EXISTED));

        LocalDateTime now = LocalDateTime.now();
        boolean withinWindow = !now.isBefore(voucher.getStartDate()) && !now.isAfter(voucher.getEndDate());
        if (!voucher.isActive() || !withinWindow) {
            throw new AppException(ErrorCode.VOUCHER_INACTIVE);
        }
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new AppException(ErrorCode.VOUCHER_USAGE_LIMIT_REACHED);
        }
        if (orderTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new AppException(ErrorCode.VOUCHER_MIN_ORDER_NOT_MET);
        }
        if (user != null && voucherUsageRepository.existsByVoucher_IdAndUser_Id(voucher.getId(), user.getId())) {
            throw new AppException(ErrorCode.VOUCHER_ALREADY_USED);
        }
        return voucher;
    }

    public BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderTotal) {
        BigDecimal discount = voucher.getDiscountType() == DiscountType.PERCENT
                ? orderTotal.multiply(voucher.getDiscountValue()).divide(BigDecimal.valueOf(100))
                : voucher.getDiscountValue();

        if (voucher.getMaxDiscountAmount() != null && discount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
            discount = voucher.getMaxDiscountAmount();
        }
        if (discount.compareTo(orderTotal) > 0) {
            discount = orderTotal;
        }
        return discount;
    }

    /**
     * Records that a user consumed a voucher for a given order, and bumps the usage counter.
     * Call this only after the order has actually been persisted.
     *
     * getValidVoucher() already checked usageLimit earlier, but that check happened before
     * the whole order (stock decrement, etc.) was built — another concurrent order could have
     * used up the last slot in the meantime. incrementUsageIfAvailable() is the real,
     * race-safe gate: if it returns 0, the limit was hit right at write time and this order
     * must fail (throwing here rolls back the whole order transaction).
     */
    @Transactional
    public void recordUsage(Voucher voucher, User user, Order order) {
        int updated = voucherRepository.incrementUsageIfAvailable(voucher.getId());
        if (updated == 0) {
            throw new AppException(ErrorCode.VOUCHER_USAGE_LIMIT_REACHED);
        }
        voucherUsageRepository.save(VoucherUsage.builder()
                .voucher(voucher)
                .user(user)
                .order(order)
                .build());
    }
}
