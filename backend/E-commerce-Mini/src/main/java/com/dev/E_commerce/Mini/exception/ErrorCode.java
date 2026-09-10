package com.dev.E_commerce.Mini.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
@Getter
public enum ErrorCode {
    USER_UNCATEGORIZED(9999,"Uncategorized ERROR",HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOT_EXISTED(1001, "User not existed", HttpStatus.NOT_FOUND),
    ROLE_NOT_EXISTED(1023, "Role not existed", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1022, "Tên đăng nhập đã được sử dụng", HttpStatus.BAD_REQUEST),
    // Cột users.email có unique index. Không kiểm tra ở tầng service thì vi phạm
    // rơi xuống DB, bị handler DataIntegrityViolationException gom thành
    // INVALID_INPUT ("Dữ liệu gửi lên không hợp lệ") — người dùng không biết
    // trường nào sai nên sửa lung tung rồi bỏ cuộc.
    EMAIL_EXISTED(1074, "Email đã được sử dụng", HttpStatus.BAD_REQUEST),
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

    // 1024: sửa trùng mã — trước đây USERNAME_INVALID dùng chung mã 1018 với UNAUTHORIZED
    USERNAME_INVALID(1024, "Username must be at least {min} characters",HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1019, "Password must be at least {min} characters",HttpStatus.BAD_REQUEST),
    INVALID_DOB(1020,"Your age must be at least {min}",HttpStatus.BAD_REQUEST),
    INVALID_KEY(1021,"Invalid message key",HttpStatus.BAD_REQUEST),

    // --- Validation dữ liệu đầu vào ---
    INVALID_INPUT(1050, "Dữ liệu gửi lên không hợp lệ", HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST(1051, "Nội dung request không đọc được hoặc sai định dạng", HttpStatus.BAD_REQUEST),
    PAYLOAD_TOO_LARGE(1052, "Dữ liệu gửi lên vượt quá giới hạn cho phép", HttpStatus.PAYLOAD_TOO_LARGE),
    EMAIL_INVALID(1053, "Email không hợp lệ", HttpStatus.BAD_REQUEST),
    PHONE_INVALID(1054, "Số điện thoại không hợp lệ", HttpStatus.BAD_REQUEST),
    FULL_NAME_INVALID(1055, "Họ tên không hợp lệ", HttpStatus.BAD_REQUEST),
    ADDRESS_INVALID(1056, "Địa chỉ không hợp lệ", HttpStatus.BAD_REQUEST),
    NAME_INVALID(1057, "Tên không hợp lệ", HttpStatus.BAD_REQUEST),
    DESCRIPTION_INVALID(1058, "Mô tả không hợp lệ", HttpStatus.BAD_REQUEST),
    PRICE_INVALID(1059, "Giá phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    STOCK_INVALID(1060, "Số lượng tồn kho không hợp lệ", HttpStatus.BAD_REQUEST),
    RATING_INVALID(1061, "Đánh giá phải từ 1 đến 5 sao", HttpStatus.BAD_REQUEST),
    COMMENT_INVALID(1062, "Nội dung đánh giá quá dài", HttpStatus.BAD_REQUEST),
    IMAGE_URL_INVALID(1063, "Đường dẫn ảnh không hợp lệ", HttpStatus.BAD_REQUEST),
    VOUCHER_CODE_INVALID(1064, "Mã voucher không hợp lệ", HttpStatus.BAD_REQUEST),
    DISCOUNT_VALUE_INVALID(1065, "Giá trị giảm giá không hợp lệ", HttpStatus.BAD_REQUEST),
    REQUIRED_FIELD_MISSING(1066, "Thiếu trường bắt buộc", HttpStatus.BAD_REQUEST),

    // --- Refresh token ---
    REFRESH_TOKEN_INVALID(1071, "Refresh token không hợp lệ", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED(1072, "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_REVOKED(1073, "Phiên đăng nhập đã bị thu hồi", HttpStatus.UNAUTHORIZED),

    // --- Rate limiting ---
    TOO_MANY_REQUESTS(1070, "Quá nhiều yêu cầu. Vui lòng thử lại sau.", HttpStatus.TOO_MANY_REQUESTS),

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
