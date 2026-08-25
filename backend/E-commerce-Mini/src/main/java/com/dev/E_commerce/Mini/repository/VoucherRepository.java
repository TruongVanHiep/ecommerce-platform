package com.dev.E_commerce.Mini.repository;

import com.dev.E_commerce.Mini.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);
    boolean existsByCode(String code);

    /**
     * Tăng usedCount nguyên tử — cùng lý do với ProductRepository#decreaseStockIfAvailable:
     * tránh 2 request cùng lọt qua kiểm tra usageLimit trước khi ai kịp ghi.
     * Trả về 0 nếu voucher đã đạt giới hạn NGAY tại thời điểm ghi.
     */
    @Modifying
    @Query("UPDATE Voucher v SET v.usedCount = v.usedCount + 1 " +
            "WHERE v.id = :id AND (v.usageLimit IS NULL OR v.usedCount < v.usageLimit)")
    int incrementUsageIfAvailable(@Param("id") Long id);
}
