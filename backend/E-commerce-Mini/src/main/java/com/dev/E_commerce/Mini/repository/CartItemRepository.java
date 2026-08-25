package com.dev.E_commerce.Mini.repository;

import com.dev.E_commerce.Mini.entity.Cart;
import com.dev.E_commerce.Mini.entity.CartItem;
import com.dev.E_commerce.Mini.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

}
