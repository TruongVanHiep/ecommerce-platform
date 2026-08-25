package com.dev.E_commerce.Mini.repository;

import com.dev.E_commerce.Mini.entity.Order;
import com.dev.E_commerce.Mini.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {
    Optional<OrderItem> findByIdAndOrder_User_Id(Long id, Long userId);
}
