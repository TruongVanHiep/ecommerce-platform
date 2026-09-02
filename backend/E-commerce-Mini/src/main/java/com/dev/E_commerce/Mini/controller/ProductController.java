package com.dev.E_commerce.Mini.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import com.dev.E_commerce.Mini.dto.request.ProductRequest;
import com.dev.E_commerce.Mini.dto.response.ApiResponse;
import com.dev.E_commerce.Mini.dto.response.ProductResponse;
import com.dev.E_commerce.Mini.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/products")
public class ProductController {
    ProductService productService;

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PostMapping
    public ApiResponse<ProductResponse> createProduct(@RequestBody @Valid ProductRequest request){
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
    public ApiResponse<ProductResponse> updateProduct(@PathVariable Long id,@RequestBody @Valid ProductRequest request){
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

    // Chặn ?size=1000000 (quét toàn bảng) và ?page=-1 (ném IllegalArgumentException → 500).
    @GetMapping
    public ApiResponse<Page<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "INVALID_INPUT") int page,
            @RequestParam(defaultValue = "39") @Min(value = 1, message = "INVALID_INPUT")
            @Max(value = 100, message = "INVALID_INPUT") int size,
            @RequestParam(required = false) @Size(max = 100, message = "INVALID_INPUT") String keyword,
            @RequestParam(required = false) @Size(max = 100, message = "INVALID_INPUT") String category
    ){
        return ApiResponse.<Page<ProductResponse>>builder()
                .result(productService.getProducts( page, size, keyword, category))
                .build();
    }
}
