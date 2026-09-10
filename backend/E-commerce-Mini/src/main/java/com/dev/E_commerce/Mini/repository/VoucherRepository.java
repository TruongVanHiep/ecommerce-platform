package com.dev.E_commerce.Mini.repository;

import com.dev.E_commerce.Mini.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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

    /**
     * Các voucher đang còn hiệu lực để hiện cho người dùng chọn: đang bật, nằm
     * trong khoảng ngày, và chưa hết lượt dùng chung.
     *
     * <p>CỐ Ý không lọc theo minOrderValue: voucher chưa đủ điều kiện vẫn được
     * trả về để giao diện hiện "mua thêm X nữa để dùng mã này". Lọc ở đây thì
     * người dùng không bao giờ biết ưu đãi đó tồn tại.
     *
     * <p>Cũng không lọc voucher người dùng đã dùng rồi — việc đó cần bảng
     * voucher_usages, xử lý ở tầng service để tránh join phức tạp trong query.
     */
    @Query("SELECT v FROM Voucher v WHERE v.active = true " +
            "AND :now BETWEEN v.startDate AND v.endDate " +
            "AND (v.usageLimit IS NULL OR v.usedCount < v.usageLimit) " +
            "ORDER BY v.minOrderValue ASC")
    List<Voucher> findRedeemable(@Param("now") LocalDateTime now);
}
