package com.dev.E_commerce.Mini.repository;

import com.dev.E_commerce.Mini.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByNameContainingIgnoreCase(String keyWord ,Pageable pageable);
    Page<Product> findByCategory_Name(String categoryName ,Pageable pageable);
    Page<Product> findByNameContainingIgnoreCaseAndCategory_Name(String keyWord, String categoryName,Pageable pageable);

    /**
     * Trừ kho nguyên tử ngay trong 1 câu UPDATE — DB tự đảm bảo không có 2
     * transaction nào cùng đọc-rồi-ghi đè lên nhau (tránh bán vượt tồn kho).
     * Trả về số dòng bị ảnh hưởng: 0 nghĩa là không đủ hàng tại thời điểm ghi.
     *
     * Cố ý KHÔNG dùng clearAutomatically=true: nó sẽ detach mọi entity đang
     * quản lý trong transaction hiện tại (cart, user, ...), phá vỡ các thao
     * tác dựa trên dirty-checking gọi sau đó (vd cart.getCartItems().clear()).
     * An toàn vì code gọi hàm này không đọc lại Product.stock sau khi update.
     */
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :qty WHERE p.id = :id AND p.stock >= :qty")
    int decreaseStockIfAvailable(@Param("id") Long id, @Param("qty") int qty);
}




