package com.dev.E_commerce.Mini.controller;

import com.dev.E_commerce.Mini.dto.request.CartItemRequest;
import com.dev.E_commerce.Mini.dto.request.UpdateCartItemRequest;
import com.dev.E_commerce.Mini.dto.response.ApiResponse;
import com.dev.E_commerce.Mini.dto.response.CartResponse;
import com.dev.E_commerce.Mini.service.CartService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartController {
    CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> getMyCart(){
        return ApiResponse.<CartResponse>builder()
                .result(cartService.getMyCart())
                .build();
    }

    @PostMapping("/items")
    public ApiResponse<CartResponse> addToCart(@RequestBody CartItemRequest request){
        return ApiResponse.<CartResponse>builder()
                .result(cartService.addToCart(request))
                .build();
    }

    @PutMapping("/items/{cartItemId}")
    public ApiResponse<CartResponse> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestBody UpdateCartItemRequest request){
        return ApiResponse.<CartResponse>builder()
                .result(cartService.updateCartItem(cartItemId,request))
                .build();
    }

    @DeleteMapping("/items/{cartItemId}")
    public ApiResponse<String> removeCartItem(@PathVariable Long cartItemId){
        cartService.removeCartItem(cartItemId);
        return ApiResponse.<String>builder()
                .result("Cart item removed successfully")
                .build();
    }

}
