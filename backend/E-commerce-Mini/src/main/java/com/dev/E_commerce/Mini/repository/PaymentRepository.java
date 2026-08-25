package com.dev.E_commerce.Mini.repository;

import com.dev.E_commerce.Mini.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder_Id(Long orderId);
    boolean existsByOrder_Id(Long orderId);
}
