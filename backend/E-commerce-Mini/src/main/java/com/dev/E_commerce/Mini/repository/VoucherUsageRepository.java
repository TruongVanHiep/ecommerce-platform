package com.dev.E_commerce.Mini.repository;

import com.dev.E_commerce.Mini.entity.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Long> {
    boolean existsByVoucher_IdAndUser_Id(Long voucherId, Long userId);
}
