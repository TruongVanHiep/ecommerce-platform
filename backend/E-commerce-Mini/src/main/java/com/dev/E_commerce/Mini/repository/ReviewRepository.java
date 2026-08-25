package com.dev.E_commerce.Mini.repository;

import com.dev.E_commerce.Mini.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findAllByProduct_Id(Long productId, Pageable pageable);
    Optional<Review> findByIdAndUser_Id(Long id, Long userId);
    boolean existsByOrderItem_Id(Long orderItemId);
}
