package com.dev.E_commerce.Mini.repository;

import com.dev.E_commerce.Mini.entity.Cart;
import com.dev.E_commerce.Mini.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart,Long> {
    boolean findCartsByUserId(Long userId);
    Optional<Cart> findByUser(User user);
}
