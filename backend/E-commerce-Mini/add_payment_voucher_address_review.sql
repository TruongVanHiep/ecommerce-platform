-- =====================================================
-- Bổ sung: ADDRESSES, VOUCHERS, PAYMENTS, REVIEWS
-- Chạy trên database e_commerce_mini
-- =====================================================

-- =====================
-- ADDRESSES (sổ địa chỉ giao hàng của user)
-- =====================
CREATE TABLE addresses (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    user_id        BIGINT       NOT NULL,
    receiver_name  VARCHAR(255) NOT NULL,
    phone          VARCHAR(20)  NOT NULL,
    address_line   VARCHAR(500) NOT NULL,
    is_default     TINYINT(1)   NOT NULL DEFAULT 0,
    create_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_addresses_user_id (user_id),
    CONSTRAINT fk_addresses_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- =====================
-- VOUCHERS (mã giảm giá)
-- =====================
CREATE TABLE vouchers (
    id                   BIGINT        NOT NULL AUTO_INCREMENT,
    code                 VARCHAR(50)   NOT NULL,
    description          VARCHAR(255)  NULL,
    discount_type        VARCHAR(20)   NOT NULL, -- PERCENT | FIXED_AMOUNT
    discount_value       DECIMAL(15,2) NOT NULL,
    min_order_value      DECIMAL(15,2) NOT NULL DEFAULT 0,
    max_discount_amount  DECIMAL(15,2) NULL,
    usage_limit          INT           NULL,     -- NULL = không giới hạn
    used_count           INT           NOT NULL DEFAULT 0,
    start_date           DATETIME      NOT NULL,
    end_date             DATETIME      NOT NULL,
    active               TINYINT(1)    NOT NULL DEFAULT 1,
    create_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_vouchers_code (code)
);

-- Lịch sử dùng voucher (chặn 1 user dùng lại cùng voucher nếu cần)
CREATE TABLE voucher_usages (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    voucher_id BIGINT   NOT NULL,
    user_id    BIGINT   NOT NULL,
    order_id   BIGINT   NOT NULL,
    used_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_voucher_usages_voucher_user (voucher_id, user_id), -- bỏ dòng này nếu cho phép dùng lại nhiều lần
    KEY idx_voucher_usages_order_id (order_id),
    CONSTRAINT fk_voucher_usages_voucher
        FOREIGN KEY (voucher_id) REFERENCES vouchers (id) ON DELETE CASCADE,
    CONSTRAINT fk_voucher_usages_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_voucher_usages_order
        FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

-- =====================
-- Bổ sung cột cho ORDERS (địa chỉ đã chọn, voucher áp dụng, phí ship, số tiền giảm)
-- =====================
ALTER TABLE orders
    ADD COLUMN address_id       BIGINT        NULL AFTER phone,
    ADD COLUMN voucher_id       BIGINT        NULL AFTER address_id,
    ADD COLUMN discount_amount  DECIMAL(15,2) NOT NULL DEFAULT 0 AFTER voucher_id,
    ADD COLUMN shipping_fee     DECIMAL(15,2) NOT NULL DEFAULT 0 AFTER discount_amount;

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_address
        FOREIGN KEY (address_id) REFERENCES addresses (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_orders_voucher
        FOREIGN KEY (voucher_id) REFERENCES vouchers (id) ON DELETE SET NULL;

-- =====================
-- PAYMENTS (thanh toán cho từng order)
-- =====================
CREATE TABLE payments (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    order_id       BIGINT        NOT NULL,
    method         VARCHAR(30)   NOT NULL,             -- COD | VNPAY | MOMO | ZALOPAY
    status         VARCHAR(20)   NOT NULL DEFAULT 'PENDING', -- PENDING | SUCCESS | FAILED | REFUNDED
    amount         DECIMAL(15,2) NOT NULL,
    transaction_id VARCHAR(255)  NULL,                 -- mã giao dịch từ cổng thanh toán, dùng để đối soát/chống trùng
    paid_at        DATETIME      NULL,
    create_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payments_order_id (order_id),
    KEY idx_payments_transaction_id (transaction_id),
    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

-- =====================
-- REVIEWS (đánh giá sản phẩm — khớp permission REVIEW_* đã seed sẵn)
-- =====================
CREATE TABLE reviews (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    product_id    BIGINT       NOT NULL,
    user_id       BIGINT       NOT NULL,
    order_item_id BIGINT       NULL,    -- để xác nhận "đã mua hàng mới được review", có thể NULL nếu không áp dụng
    rating        TINYINT      NOT NULL,
    comment       VARCHAR(2000) NULL,
    create_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_reviews_product_id (product_id),
    KEY idx_reviews_user_id (user_id),
    CONSTRAINT fk_reviews_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_order_item
        FOREIGN KEY (order_item_id) REFERENCES order_items (id) ON DELETE SET NULL,
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5)
);

-- =====================
-- PRODUCT_IMAGES (tuỳ chọn — nếu muốn nhiều ảnh/sản phẩm thay vì 1 cột `image`)
-- =====================
CREATE TABLE product_images (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    product_id   BIGINT       NOT NULL,
    url          VARCHAR(1000) NOT NULL,
    is_thumbnail TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_product_images_product_id (product_id),
    CONSTRAINT fk_product_images_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);
