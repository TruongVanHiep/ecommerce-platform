package com.dev.E_commerce.Mini.repository;

import com.dev.E_commerce.Mini.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.E_commerce.Mini.enums.PaymentStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder_Id(Long orderId);
    boolean existsByOrder_Id(Long orderId);

    /** Chống xử lý trùng: SePay gửi lại webhook tới 7 lần nếu lần trước không nhận được phản hồi. */
    boolean existsByTransactionId(String transactionId);

    /**
     * Chuyển PENDING -> SUCCESS một cách nguyên tử.
     *
     * <p>Không làm kiểu "đọc trạng thái rồi ghi": hai webhook trùng nhau tới gần
     * như cùng lúc sẽ cùng đọc thấy PENDING và cùng ghi SUCCESS — metric đếm đôi,
     * đơn hàng bị cập nhật hai lần. Điều kiện {@code status = PENDING} nằm ngay
     * trong câu UPDATE nên chỉ một request thắng; request kia nhận về 0.
     * Cùng cách với VoucherRepository#incrementUsageIfAvailable.
     */
    @Modifying
    @Query("UPDATE Payment p SET p.status = :success, p.transactionId = :transactionId, p.paidAt = :paidAt " +
            "WHERE p.id = :id AND p.status = :pending")
    int markPaidIfPending(@Param("id") Long id,
                          @Param("transactionId") String transactionId,
                          @Param("paidAt") LocalDateTime paidAt,
                          @Param("pending") PaymentStatus pending,
                          @Param("success") PaymentStatus success);
}
