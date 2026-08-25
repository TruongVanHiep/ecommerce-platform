package com.dev.E_commerce.Mini.repository;

import com.dev.E_commerce.Mini.entity.Category;
import com.dev.E_commerce.Mini.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

}
