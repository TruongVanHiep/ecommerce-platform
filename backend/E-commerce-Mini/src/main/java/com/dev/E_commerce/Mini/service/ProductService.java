package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.dto.request.ProductRequest;
import com.dev.E_commerce.Mini.dto.response.ProductResponse;
import com.dev.E_commerce.Mini.entity.Category;
import com.dev.E_commerce.Mini.entity.Product;
import com.dev.E_commerce.Mini.exception.AppException;
import com.dev.E_commerce.Mini.exception.ErrorCode;
import com.dev.E_commerce.Mini.mapper.ProductMapper;
import com.dev.E_commerce.Mini.repository.CategoryRepository;
import com.dev.E_commerce.Mini.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService {
    ProductRepository productRepository;
    ProductMapper productMapper;
    CategoryRepository categoryRepository;

    public ProductResponse createProduct(ProductRequest request){
        Product product = productMapper.toProduct(request);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
        product.setCategory(category);
        return productMapper.toProductResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(Long proId, ProductRequest request){
        Product product = productRepository.findById(proId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

       productMapper.updateProduct(product,request);
        return productMapper.toProductResponse(productRepository.save(product));
    }

    public ProductResponse getProductById(Long proId){
        Product product = productRepository.findById(proId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        return productMapper.toProductResponse(product);
    }
    public List<ProductResponse> getAllProducts(){
        return productRepository.findAll().stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    public void deleteProductById(Long proId){
        Product product = productRepository.findById(proId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        productRepository.delete(product);
    }

//    Search + Pagination
//    Bước 1: Pagination trước
//    Bước 2: Search keyword
//    Bước 3: Filter category
    public Page<ProductResponse> getProducts(int page, int size, String keyword, String category){
        Pageable pageable = PageRequest.of(page, size);

        Page<Product> products;
        if (keyword != null && category != null){
            products = productRepository.findByNameContainingIgnoreCaseAndCategory_Name(
                    keyword,category,pageable);
        }
        else if (keyword != null){
            products = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else if (category != null) {
            products = productRepository.findByCategory_Name(category, pageable);
        }else {
            products = productRepository.findAll(pageable);
        }

        return products.map(productMapper::toProductResponse);
    }
}
