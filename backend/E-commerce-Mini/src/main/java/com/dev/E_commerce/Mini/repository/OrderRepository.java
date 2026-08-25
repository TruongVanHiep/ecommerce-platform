package com.dev.E_commerce.Mini.repository;

import com.dev.E_commerce.Mini.entity.Cart;
import com.dev.E_commerce.Mini.entity.Order;
import com.dev.E_commerce.Mini.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findAllByUser(User user);
    Optional<Order> findByIdAndUser(Long id, User user);

    Optional<Order> findByIdempotencyKey(String idempotencyKey);
}
