package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.dto.request.CartItemRequest;
import com.dev.E_commerce.Mini.dto.request.UpdateCartItemRequest;
import com.dev.E_commerce.Mini.dto.response.CartResponse;
import com.dev.E_commerce.Mini.entity.Cart;
import com.dev.E_commerce.Mini.entity.CartItem;
import com.dev.E_commerce.Mini.entity.Product;
import com.dev.E_commerce.Mini.entity.User;
import com.dev.E_commerce.Mini.exception.AppException;
import com.dev.E_commerce.Mini.exception.ErrorCode;
import com.dev.E_commerce.Mini.mapper.CartMapper;
import com.dev.E_commerce.Mini.repository.CartItemRepository;
import com.dev.E_commerce.Mini.repository.CartRepository;
import com.dev.E_commerce.Mini.repository.ProductRepository;
import com.dev.E_commerce.Mini.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartService {
    CartRepository cartRepository;
    UserRepository userRepository;
    ProductRepository productRepository;
    CartItemRepository cartItemRepository;
    CartMapper cartMapper;

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    /*
        thêm sản phẩm
        đổi số lượng
        xóa sản phẩm
        xem giỏ hàng
        checkout

        Nếu chưa có cart -> tạo cart
        Nếu sản phẩm đã có -> cộng quantity
        Nếu chưa có -> tạo cartItem mới
        */

    @Transactional
    public CartResponse addToCart(CartItemRequest request){
        User user = currentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart,product).orElse(null);

        if (cartItem != null) {
            if (cartItem.getUnitPrice() == null) {
                cartItem.setUnitPrice(product.getPrice());
            }
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        } else {
            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
        }
        cartItemRepository.save(cartItem);
        Cart updateCart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_EXISTED));

        return cartMapper.toCartResponse(updateCart);
    }

    @Transactional
    public CartResponse getMyCart(){
        User user = currentUser();
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
        return cartMapper.toCartResponse(cart);
    }

    @Transactional
    public CartResponse updateCartItem(Long cartItemId, UpdateCartItemRequest request){
        User user = currentUser();
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CARTITEM_NOT_EXISTED));
        if (!cartItem.getCart().getUser().getId().equals(user.getId())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if(request.getQuantity() <= 0){
            throw new AppException(ErrorCode.INVALID_QUANTITY);
        }
        cartItem.setQuantity(request.getQuantity());
        Cart cart = cartItemRepository.save(cartItem).getCart();
        return cartMapper.toCartResponse(cart);
    }

    @Transactional
    public void removeCartItem(Long cartItemId){
        User user = currentUser();
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CARTITEM_NOT_EXISTED));
        if (!cartItem.getCart().getUser().getId().equals(user.getId())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        cartItemRepository.deleteById(cartItemId);
    }
}

