package com.dev.E_commerce.Mini.enums;

public enum PaymentMethod {
    COD,
    VNPAY,
    MOMO,
    ZALOPAY,
    // Chuyển khoản ngân hàng qua VietQR, SePay đọc biến động số dư và gọi
    // webhook để tự xác nhận. Cột payments.method là varchar nên thêm giá trị
    // mới không cần migration.
    SEPAY
}
