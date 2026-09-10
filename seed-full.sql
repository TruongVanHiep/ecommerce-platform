-- MySQL dump 10.13  Distrib 8.4.11, for Linux (x86_64)
--
-- Host: localhost    Database: e_commerce_mini
-- ------------------------------------------------------
-- Server version	8.4.11

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `addresses`
--

DROP TABLE IF EXISTS `addresses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `addresses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `receiver_name` varchar(255) NOT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `address_line` varchar(255) DEFAULT NULL,
  `is_default` tinyint(1) NOT NULL DEFAULT '0',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_addresses_user_id` (`user_id`),
  CONSTRAINT `fk_addresses_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cart_items`
--

DROP TABLE IF EXISTS `cart_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `quantity` int NOT NULL,
  `unit_price` decimal(38,2) DEFAULT NULL,
  `cart_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cart_items_cart_product` (`cart_id`,`product_id`),
  KEY `FK1re40cjegsfvw58xrkdp6bac6` (`product_id`),
  CONSTRAINT `FK1re40cjegsfvw58xrkdp6bac6` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKpcttvuq4mxppo8sxggjtn5i2c` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `carts`
--

DROP TABLE IF EXISTS `carts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK64t7ox312pqal3p7fg9o503c2` (`user_id`),
  CONSTRAINT `FKb5o626f86h46m4s7ms6ginnop` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_categories_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `quantity` int NOT NULL,
  `unit_price` decimal(38,2) DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbioxgbv59vetrxe0ejfubep1w` (`order_id`),
  KEY `FKocimc7dtr037rh4ls4l95nlfi` (`product_id`),
  CONSTRAINT `FKbioxgbv59vetrxe0ejfubep1w` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `FKocimc7dtr037rh4ls4l95nlfi` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_at` datetime(6) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `address_id` bigint DEFAULT NULL,
  `voucher_id` bigint DEFAULT NULL,
  `discount_amount` decimal(38,2) DEFAULT NULL,
  `shipping_fee` decimal(38,2) DEFAULT NULL,
  `shipping_address` varchar(255) DEFAULT NULL,
  `status` enum('CANCELLED','CONFIRMED','DELIVERED','PAID','PENDING','SHIPPING') NOT NULL,
  `total_price` decimal(38,2) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `idempotency_key` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKd1kkvl4hi9hp3peub1umk2xeo` (`idempotency_key`),
  KEY `FK32ql8ubntj5uh44ph9659tiih` (`user_id`),
  KEY `fk_orders_address` (`address_id`),
  KEY `fk_orders_voucher` (`voucher_id`),
  CONSTRAINT `FK32ql8ubntj5uh44ph9659tiih` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_orders_address` FOREIGN KEY (`address_id`) REFERENCES `addresses` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_orders_voucher` FOREIGN KEY (`voucher_id`) REFERENCES `vouchers` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `method` varchar(30) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'PENDING',
  `amount` decimal(38,2) NOT NULL,
  `transaction_id` varchar(255) DEFAULT NULL,
  `paid_at` datetime DEFAULT NULL,
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payments_order_id` (`order_id`),
  KEY `idx_payments_transaction_id` (`transaction_id`),
  CONSTRAINT `fk_payments_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `permissions`
--

DROP TABLE IF EXISTS `permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permissions` (
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_images`
--

DROP TABLE IF EXISTS `product_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `url` varchar(1000) NOT NULL,
  `is_thumbnail` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_product_images_product_id` (`product_id`),
  CONSTRAINT `fk_product_images_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `image` varchar(1000) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `price` decimal(38,2) DEFAULT NULL,
  `stock` int NOT NULL,
  `category_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKog2rp4qthbtt2lfyhfo32lsw9` (`category_id`),
  FULLTEXT KEY `idx_products_name_fulltext` (`name`),
  CONSTRAINT `FKog2rp4qthbtt2lfyhfo32lsw9` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `refresh_tokens`
--

DROP TABLE IF EXISTS `refresh_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refresh_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_at` datetime(6) DEFAULT NULL,
  `expires_at` datetime(6) NOT NULL,
  `revoked` bit(1) NOT NULL,
  `token` varchar(255) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKghpmfn23vmxfu3spu3lfg4r2d` (`token`),
  KEY `idx_refresh_tokens_user` (`user_id`),
  CONSTRAINT `FK1lih5y2npsf8u5o3vhdb9y0os` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reviews`
--

DROP TABLE IF EXISTS `reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `order_item_id` bigint DEFAULT NULL,
  `rating` int NOT NULL,
  `comment` varchar(2000) DEFAULT NULL,
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_reviews_product_id` (`product_id`),
  KEY `idx_reviews_user_id` (`user_id`),
  KEY `fk_reviews_order_item` (`order_item_id`),
  CONSTRAINT `fk_reviews_order_item` FOREIGN KEY (`order_item_id`) REFERENCES `order_items` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_reviews_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_reviews_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_reviews_rating` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role_permissions`
--

DROP TABLE IF EXISTS `role_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_permissions` (
  `role_name` varchar(255) NOT NULL,
  `permission_name` varchar(255) NOT NULL,
  PRIMARY KEY (`role_name`,`permission_name`),
  KEY `FKie537gni0kx7t0vxakg9kbur9` (`permission_name`),
  CONSTRAINT `FKie537gni0kx7t0vxakg9kbur9` FOREIGN KEY (`permission_name`) REFERENCES `permissions` (`name`),
  CONSTRAINT `FKq893o9jg7ihgdxfwtiswn0uk2` FOREIGN KEY (`role_name`) REFERENCES `roles` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_roles` (
  `user_id` bigint NOT NULL,
  `role_name` varchar(255) NOT NULL,
  PRIMARY KEY (`user_id`,`role_name`),
  KEY `FKdcdh0gl1mdce42vy0klyio6fi` (`role_name`),
  CONSTRAINT `FKdcdh0gl1mdce42vy0klyio6fi` FOREIGN KEY (`role_name`) REFERENCES `roles` (`name`),
  CONSTRAINT `FKhfh9dx7w3ubf1co1vdev94g3f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `create_at` datetime(6) DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `voucher_usages`
--

DROP TABLE IF EXISTS `voucher_usages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `voucher_usages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `voucher_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `order_id` bigint NOT NULL,
  `used_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_voucher_usages_voucher_user` (`voucher_id`,`user_id`),
  KEY `idx_voucher_usages_order_id` (`order_id`),
  KEY `fk_voucher_usages_user` (`user_id`),
  CONSTRAINT `fk_voucher_usages_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_voucher_usages_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_voucher_usages_voucher` FOREIGN KEY (`voucher_id`) REFERENCES `vouchers` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vouchers`
--

DROP TABLE IF EXISTS `vouchers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vouchers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `discount_type` varchar(20) NOT NULL,
  `discount_value` decimal(38,2) NOT NULL,
  `min_order_value` decimal(38,2) NOT NULL,
  `max_discount_amount` decimal(38,2) DEFAULT NULL,
  `usage_limit` int DEFAULT NULL,
  `used_count` int NOT NULL DEFAULT '0',
  `start_date` datetime NOT NULL,
  `end_date` datetime NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vouchers_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-09 11:48:01

-- ============ DU LIEU DANH MUC ============

-- MySQL dump 10.13  Distrib 8.4.11, for Linux (x86_64)
--
-- Host: localhost    Database: e_commerce_mini
-- ------------------------------------------------------
-- Server version	8.4.11

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` (`id`, `description`, `name`) VALUES (1,NULL,'electronics'),(2,NULL,'jewelery'),(3,NULL,'men\'s clothing'),(4,NULL,'women\'s clothing'),(5,NULL,'accessories');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` (`id`, `create_at`, `description`, `image`, `name`, `price`, `stock`, `category_id`) VALUES (1,NULL,'Chip A17 Pro, Camera 48MP, Titan tự nhiên.','https://static1.pocketnowimages.com/wordpress/wp-content/uploads/2023/09/pbi-iphone-15-pro-max.png','iPhone 15 Pro',28990000.00,48,1),(2,NULL,'Màn hình Liquid Retina 13.6 inch, RAM 8GB, SSD 256GB.','https://thehikaku.net/pc/apple/image/22macbook-air-m2/x1400.jpg','MacBook Air M2',24500000.00,29,1),(3,NULL,'Tai nghe chống ồn hàng đầu thế giới.','https://i0.wp.com/techstomper.com/wp-content/uploads/2023/04/WH-1000XM5_keyvisual_midnightblue-Large.jpg?resize=1200%2C910&ssl=1','Sony WH-1000XM5',6500000.00,99,1),(4,NULL,'Bút S-Pen tích hợp, Camera AI siêu phân giải.','https://static1.pocketnowimages.com/wordpress/wp-content/uploads/2024/01/pbi-samsung-galaxy-s24-ultra.png','Samsung Galaxy S24 Ultra',29990000.00,40,1),(5,NULL,'Laptop doanh nhân sang trọng, màn hình OLED.','https://m.media-amazon.com/images/I/71rLpZlR5hL._AC_SL1500_.jpg','Dell XPS 13 Plus',35000000.00,14,1),(6,NULL,'Theo dõi sức khỏe và nồng độ oxy trong máu.','https://www.apple.com/newsroom/images/2023/09/apple-introduces-the-advanced-new-apple-watch-series-9/article/Apple-Watch-S9-gold-stainless-steel-Sport-Band-purple-230912_inline.jpg.large_2x.jpg','Apple Watch Series 9',9500000.00,58,1),(7,NULL,'Vàng trắng 14K đính kim cương tự nhiên.','https://cdn.pnj.io/images/detailed/34/gndrwa60919.501-nhan-kim-cuong-pnj-vang-trang.png','Nhẫn Kim Cương PNJ',15000000.00,8,2),(8,NULL,'Thiết kế tối giản, tinh tế cho nữ.','https://down-vn.img.susercontent.com/file/50bd35e23386e4dc56d7354269ed71dd','Dây chuyền Bạc Ý 925',450000.00,120,2),(9,NULL,'Vòng tay bạc nguyên khối có khóa chốt.','https://cdn.vuahanghieu.com/unsafe/0x0/left/top/smart/filters:quality(90)/https://admin.vuahanghieu.com/upload/news/content/2023/01/vong-deo-tay-pandora-moments-infinity-heart-clasp-bangle-598891c00-mau-bac-anh-2-jpg-1674895312-28012023154152.jpg','Vòng tay Pandora Moments',2100000.00,80,2),(10,NULL,'Ngọc trai nước ngọt, chuôi vàng 18K.','https://product.hstatic.net/200000671715/product/4_86c8b538e1864ed7a2f34bc644058270_1024x1024.jpg','Hoa tai ngọc trai',3200000.00,25,2),(11,NULL,'Vàng 24K truyền thống, tinh xảo.','https://www.quocbaolam.com/upload/product/20251208194533-5041.jpg','Lắc chân hoa mai',8000000.00,15,2),(12,NULL,'Máy cơ Automatic, dây da cao cấp.','https://cdn.vuahanghieu.com/unsafe/0x0/left/top/smart/filters:quality(90)/https://admin.vuahanghieu.com/upload/news/content/2024/01/dong-ho-nam-tissot-automatic-chemin-des-tourelles-powermatic-80-t099-407-22-038-01-mau-bac-trang2-jpg-1704357283-04012024153443.jpg','Đồng hồ nam Tissot',12500000.00,11,2),(13,NULL,'Chất liệu cotton thoáng mát, form Slim Fit.','https://cf.shopee.vn/file/ad5d7fcd858586d9accaf2b3ef3a8eba','Áo Sơ mi Oxford',450000.00,195,3),(14,NULL,'Kiểu dáng hiện đại, bền bỉ theo thời gian.','https://pos.nvncdn.com/8f7207-62506/ps/Quan-Jean-Dai-Nam-Levi-s-511-Form-Slim-ong-dung-chat-vai-jean-cotton-co-gian-nhe-ong-17-19cm-Hang-VNXK-LV511-6.jpg?v=1755081902','Quần Jean Levi\'s 511',1850000.00,50,3),(15,NULL,'Vải nỉ mềm mại, giữ ấm tốt.','https://cdn.vuahanghieu.com/unsafe/0x900/left/top/smart/filters:quality(90)/https://admin.vuahanghieu.com/upload/product/2023/09/ao-hoodie-nike-sportswear-club-full-zip-hoodie-mau-xam-size-s-64f7ff2b5fc11-06092023112515.jpg','Áo Hoodie Nike Sportswear',1200000.00,73,3),(16,NULL,'Phong cách năng động cho mùa hè.','https://product.hstatic.net/1000253775/product/qsid0137_06f8df98b65b4ffe82851bcd5a011ff1.jpg','Quần Short Kaki',350000.00,150,3),(17,NULL,'Phù hợp đi làm và đi tiệc.','https://down-vn.img.susercontent.com/file/sg-11134201-7rfhu-m3nsy5115in69a','Áo Blazer nam Slimfit',2200000.00,18,3),(18,NULL,'Công nghệ làm mát Airism độc quyền.','https://product.hstatic.net/1000209952/product/z4333899410706_598fed9eaab90d413cad04abd3003948_5f863827e8694095a28720ab30f57c3c_master.jpg','Áo Polo Uniqlo',490000.00,298,3),(19,NULL,'Phong cách vintage nhẹ nhàng, nữ tính.','https://lamia.com.vn/storage/anh-seo/vay-hoa-nhi-4.jpg','Váy hoa nhí dáng dài',550000.00,44,4),(20,NULL,'Màu be trung tính, dễ phối đồ.','https://sakurafashion.vn/upload/sanpham/large/599313-ao-khoac-blazer-nu-thiet-ke-dang-suong-1.jpg','Áo khoác Blazer nữ',890000.00,33,4),(21,NULL,'Vải mềm rủ, tôn dáng người mặc.','https://down-vn.img.susercontent.com/file/vn-11134201-23030-bia5ufkbqyovf6','Quần ống rộng Culottes',420000.00,100,4),(22,NULL,'Giữ ấm nhẹ trong phòng máy lạnh.','https://down-vn.img.susercontent.com/file/sg-11134201-22100-1grq2eeckniv69','Áo Cardigan dệt kim',380000.00,59,4),(23,NULL,'Màu đen cơ bản, dễ kết hợp áo thun.','https://halotravel.vn/wp-content/uploads/2022/02/mix-do-chan-vay-xep-ly_5_11zon-822x1024.jpg','Chân váy xếp ly',320000.00,89,4),(24,NULL,'Thiết kế sang trọng cho các buổi tiệc tối.','https://gottwow.com/wp-content/uploads/2018/10/Picture15-745x800.png','Đầm dạ hội đuôi cá',3500000.00,10,4),(25,NULL,'Thiết kế cổ điển, ngăn chứa rộng rãi.','https://media.mia.vn/uploads/balo-herschel-little-america-eco-standard-15-backpack-s-teal-13938-21679736739.jpg','Balo Herschel Supply Co.',1950000.00,25,5),(26,NULL,'Chống tia UV tuyệt đối, gọng kim loại.','https://www.jordan1.vn/wp-content/uploads/2023/09/8053672980769_shad_fr_9f33fd9d9d1340baa5b1bbc65435c68c_4c736eba6b374efaba7761298ac0f1cd.jpeg','Kính râm Ray-Ban Aviator',4200000.00,30,5),(27,NULL,'Khóa cài hợp kim không gỉ.','https://down-vn.img.susercontent.com/file/cn-11134207-7r98o-lvd4ghkdjm5hee','Thắt lưng da bò thật',650000.00,80,5),(28,NULL,'Màu pastel ngọt ngào, nhiều ngăn tiện lợi.','https://hotgirlshop.vn/uploads/picture/08092021/News/2098205845-vi-cnk-hong-ngan-tien-loi.jpg','Ví cầm tay nữ Charles & Keith',1100000.00,40,5),(29,NULL,'Chất liệu thoáng khí, thấm hút mồ hôi.','https://down-vn.img.susercontent.com/file/f9a344ae3d0702c6d142c139c48bc6d6','Nón kết Adidas',500000.00,110,5),(30,NULL,'Họa tiết vẽ tay thủ công.','https://down-vn.img.susercontent.com/file/vn-11134207-7r98o-lphx7si68ppxed','Khăn choàng lụa tơ tằm',1250000.00,15,5),(31,NULL,'Gaming laptop Intel i7 RTX 4060','https://laptopmedia.com/wp-content/uploads/2023/01/3-4-e1672813305688.jpg','ASUS ROG Strix G16',35990000.00,10,1),(32,NULL,'Affordable gaming laptop with RTX graphics','https://gamesmix.net/wp-content/uploads/2023/09/Nitro_v15-1024x576.jpg','Acer Nitro V15',24990000.00,10,1),(33,NULL,'Mirrorless camera for content creators','https://amateurphotographer.com/wp-content/uploads/sites/7/2023/05/Canon_EOS_R50-01.jpg?resize=1240','Canon EOS R50',18990000.00,10,1),(34,NULL,'4K action camera waterproof design','https://thecamerastore.com/cdn/shop/articles/Hero12.png?v=1694182776','GoPro Hero 12',12990000.00,12,1),(35,NULL,'Compact drone with 4K stabilized camera','https://techstory.in/wp-content/uploads/2023/11/Image-Credits-Kara-Murphy-DP-Review-scaled.jpg','DJI Mini 4 Pro',22990000.00,10,1),(36,NULL,'Portable Bluetooth speaker deep bass','https://m.media-amazon.com/images/I/91wbSBJai2L._AC_SL1500_.jpg','JBL Charge 5',3990000.00,17,1),(37,NULL,'E-reader with glare-free display','https://cdn1.smartprix.com/rx-itqBlpDo6-w1200-h1200/tqBlpDo6.jpg','Kindle Paperwhite',4490000.00,14,1),(38,NULL,'WiFi 6 high speed router','https://c1.neweggimages.com/BizIntell/item/Network%20Retail/Network%20-%20Wireless%20Routers/33-704-685/2.jpg','TP-Link Archer AX55',2690000.00,20,1),(39,NULL,'Full HD smart projector with Android TV','https://eloutput.com/wp-content/uploads/2021/04/Mi-Smart-Projector-2-Pro.jpg','Xiaomi Smart Projector 2',11990000.00,9,1);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `product_images`
--

LOCK TABLES `product_images` WRITE;
/*!40000 ALTER TABLE `product_images` DISABLE KEYS */;
/*!40000 ALTER TABLE `product_images` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `vouchers`
--

LOCK TABLES `vouchers` WRITE;
/*!40000 ALTER TABLE `vouchers` DISABLE KEYS */;
INSERT INTO `vouchers` (`id`, `code`, `description`, `discount_type`, `discount_value`, `min_order_value`, `max_discount_amount`, `usage_limit`, `used_count`, `start_date`, `end_date`, `active`, `create_at`) VALUES (1,'TEST10','Test 10 percent off','PERCENT',10.00,10000.00,5000000.00,100,3,'2025-12-31 17:00:00','2026-12-31 17:00:00',1,'2026-08-23 06:38:29'),(2,'FREESHIP50','','FIXED_AMOUNT',50000.00,0.00,NULL,NULL,1,'2025-12-31 17:00:00','2026-12-31 17:00:00',1,'2026-08-23 07:30:17');
/*!40000 ALTER TABLE `vouchers` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-09 11:48:01
