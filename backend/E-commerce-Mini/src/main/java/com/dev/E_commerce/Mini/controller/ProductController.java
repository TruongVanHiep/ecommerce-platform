package com.dev.E_commerce.Mini.controller;

import com.dev.E_commerce.Mini.dto.request.ProductRequest;
import com.dev.E_commerce.Mini.dto.response.ApiResponse;
import com.dev.E_commerce.Mini.dto.response.ProductResponse;
import com.dev.E_commerce.Mini.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/products")
public class ProductController {
    ProductService productService;

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PostMapping
    public ApiResponse<ProductResponse> createProduct(@RequestBody ProductRequest request){
        return ApiResponse.<ProductResponse>builder()
                .result(productService.createProduct(request))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<ProductResponse> getProductById(@PathVariable Long id){
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getProductById(id))
                .build();
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> updateProduct(@PathVariable Long id,@RequestBody ProductRequest request){
        return ApiResponse.<ProductResponse>builder()
                .result(productService.updateProduct(id,request))
                .build();
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteProduct(@PathVariable Long id){
        productService.deleteProductById(id);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping
    public ApiResponse<Page<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "39") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category
    ){
        return ApiResponse.<Page<ProductResponse>>builder()
                .result(productService.getProducts( page, size, keyword, category))
                .build();
    }
}
