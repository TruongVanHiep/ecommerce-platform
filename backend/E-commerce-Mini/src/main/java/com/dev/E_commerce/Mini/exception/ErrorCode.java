package com.dev.E_commerce.Mini.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
@Getter
public enum ErrorCode {
    USER_UNCATEGORIZED(9999,"Uncategorized ERROR",HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOT_EXISTED(1001, "User not existed", HttpStatus.NOT_FOUND),
    ROLE_NOT_EXISTED(1023, "Role not existed", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1022, "User existed", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1002,"Invalid token", HttpStatus.UNAUTHORIZED),
    UNAUTHENTICATED(1017,"Unauthorized !", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1018,"You don't have permission !", HttpStatus.FORBIDDEN),

    PRODUCT_NOT_EXISTED(1010, "Product not existed", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_EXISTED(1011, "Category not existed", HttpStatus.NOT_FOUND),
    CART_NOT_EXISTED(1012, "Cart not existed", HttpStatus.NOT_FOUND),
    ORDER_NOT_EXISTED(1015, "Order not existed", HttpStatus.NOT_FOUND),
    CARTITEM_NOT_EXISTED(1013, "Cart not existed", HttpStatus.NOT_FOUND),
    INVALID_QUANTITY(1014, "Quantity must be greater than 0", HttpStatus.BAD_REQUEST),
    CART_EMPTY(1016, "Cart empty", HttpStatus.BAD_REQUEST),
    PRODUCT_OUT_OF_STOCK(1041, "Product does not have enough stock", HttpStatus.BAD_REQUEST),

    USERNAME_INVALID(1018, "Username must be at least {min} characters",HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1019, "Password must be at least {min} characters",HttpStatus.BAD_REQUEST),
    INVALID_DOB(1020,"Your age must be at least {min}",HttpStatus.BAD_REQUEST),
    INVALID_KEY(1021,"Invalid message key",HttpStatus.BAD_REQUEST),

    ADDRESS_NOT_EXISTED(1030, "Address not existed", HttpStatus.NOT_FOUND),

    VOUCHER_NOT_EXISTED(1031, "Voucher not existed", HttpStatus.NOT_FOUND),
    VOUCHER_CODE_EXISTED(1040, "Voucher code already exists", HttpStatus.BAD_REQUEST),
    VOUCHER_INACTIVE(1032, "Voucher is not active or has expired", HttpStatus.BAD_REQUEST),
    VOUCHER_MIN_ORDER_NOT_MET(1033, "Order total does not meet voucher minimum value", HttpStatus.BAD_REQUEST),
    VOUCHER_USAGE_LIMIT_REACHED(1034, "Voucher usage limit reached", HttpStatus.BAD_REQUEST),
    VOUCHER_ALREADY_USED(1035, "You have already used this voucher", HttpStatus.BAD_REQUEST),

    PAYMENT_NOT_EXISTED(1036, "Payment not existed", HttpStatus.NOT_FOUND),

    REVIEW_NOT_EXISTED(1038, "Review not existed", HttpStatus.NOT_FOUND),
    REVIEW_ALREADY_EXISTED(1039, "You already reviewed this item", HttpStatus.BAD_REQUEST),
    ;
    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
    int code;
    String message;
    HttpStatusCode statusCode;
}
