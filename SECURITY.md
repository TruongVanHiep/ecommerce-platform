# Báo cáo Bảo mật — E-commerce Mini

Audit toàn bộ codebase (backend Spring Boot + frontend React + hạ tầng Docker) và các biện pháp phòng thủ đã triển khai.

---

## 1. Rate limiting (chống brute force / spam)

Cài đặt tại [`RateLimitFilter.java`](backend/E-commerce-Mini/src/main/java/com/dev/E_commerce/Mini/configuration/RateLimitFilter.java) — thuật toán token bucket (thư viện Bucket4j), lưu trạng thái trong bộ nhớ tiến trình bằng Caffeine.

| Mức | Áp dụng | Giới hạn |
|---|---|---|
| **STRICT** | `POST /api/auth/login`, `POST /api/users`, `POST /api/vouchers/apply` | **5 request / 15 phút** |
| **WRITE** | Mọi `POST/PUT/DELETE` còn lại | 60 / phút |
| **GLOBAL** | Mọi request còn lại | 200 / phút |
| **Miễn trừ** | `/actuator/**` | Không giới hạn |

**Vì sao miễn trừ `/actuator`**: Docker healthcheck gọi 10 giây/lần và Prometheus scrape liên tục — nếu bị chặn, container sẽ bị đánh dấu unhealthy rồi restart liên tục.

**Vì sao khoá theo `getRemoteAddr()` chứ không phải `X-Forwarded-For`**: hiện không có reverse proxy tin cậy đứng trước backend, nên header đó do client tự đặt và giả mạo được — dùng nó sẽ khiến rate limit vô hiệu. Nếu sau này đặt backend sau nginx/cloud load balancer, phải cấu hình `server.forward-headers-strategy` rồi mới được tin header này.

Khi vượt ngưỡng: trả `429` + header `Retry-After`, `X-RateLimit-Remaining` (đã thêm vào `setExposedHeaders` của CORS để trình duyệt đọc được). Mỗi lần chặn tăng metric `ratelimit_rejected_total` → theo dõi được trên Grafana.

**Giới hạn hiện tại**: trạng thái nằm trong RAM của 1 tiến trình. Nếu scale nhiều instance backend, mỗi instance đếm riêng ⇒ cần chuyển sang store dùng chung (Redis).

---

## 2. Quản lý secrets

**Kết quả quét git**: đã grep nội dung toàn bộ commit trên mọi nhánh — **không có secret nào từng bị commit**. `.gitignore` đã chặn `.env` và `application-secrets.yaml`; `.dockerignore` đã loại secret khỏi Docker image.

**Đã thực hiện**:
- Xoá `application-secrets.yaml` — trước đây tồn tại song song với `.env` và **lệch giá trị** (DB password khác nhau, JWT key khác nhau; key trong file yaml chỉ 48 byte trong khi code ép thuật toán HS512 cần ≥64 byte ⇒ chạy local sẽ lỗi). Giờ `.env` là nguồn duy nhất.
- Xoá luôn `spring.config.import` trỏ tới file đó trong `application.yaml`.
- Xoá trang `/debug` ở frontend (in token ra console, vẫn nằm trong bundle production).
- Thêm biến `ADMIN_INIT_PASSWORD` thay cho mật khẩu admin hardcode.

**Frontend sạch**: không có secret nào trong `src/`. `.env.production` chỉ chứa `VITE_API_URL=/api`. Luồng Google OAuth chạy hoàn toàn phía backend nên client secret không bao giờ lọt vào bundle.

### ⚠️ Việc bạn phải tự làm (không tự động hoá được)

Hai secret sau đang **còn hiệu lực** và từng nằm plaintext ở 2 file local — nên coi như đã lộ và **thu hồi/tạo lại tại nhà cung cấp**:

1. **Google OAuth client secret** — vào Google Cloud Console → Credentials → tạo lại secret, cập nhật `.env`.
2. **Gmail app password** — vào Google Account → Security → App passwords → thu hồi cái cũ, tạo cái mới.

### Chạy backend bằng IntelliJ sau khi gộp secret

Do đã xoá `application-secrets.yaml`, IntelliJ cần được nạp biến môi trường từ `.env`. Chọn 1 trong 2:

- **Cách 1 (khuyên dùng)**: cài plugin **EnvFile** → Run Configuration → tab EnvFile → tick *Enable EnvFile* → Add → chọn `.env` ở gốc repo.
- **Cách 2**: Run Configuration → *Environment variables* → dán chuỗi:
  `DB_PASSWORD=...;JWT_SIGNER_KEY=...;GOOGLE_CLIENT_ID=...;GOOGLE_CLIENT_SECRET=...;MAIL_USERNAME=...;MAIL_PASSWORD=...;ADMIN_INIT_PASSWORD=...`

Chạy bằng Docker Compose thì không cần làm gì — Compose tự đọc `.env`.

---

## 3. Validate & làm sạch dữ liệu đầu vào

**Trước audit**: 17/19 DTO không có một annotation validation nào; chỉ 2 endpoint dùng `@Valid`; không có giới hạn kích thước request; không có cap page size.

**Đã thực hiện**:
- Thêm ràng buộc cho **toàn bộ DTO** trong `dto/request/` (`@NotBlank`, `@Size`, `@Email`, `@Min/@Max`, `@Positive`, `@Pattern`...). Đáng chú ý: `rating` đánh giá giờ bắt buộc 1–5 (trước đây nhận cả số âm lẫn 9999); URL ảnh chỉ chấp nhận `http/https` (chặn lưu `javascript:` / `data:` URI).
- Thêm `@Valid` vào **tất cả** `@RequestBody` trong mọi controller — thiếu annotation này thì mọi ràng buộc ở trên đều không chạy.
- Cap phân trang: `size` tối đa 100, `page` ≥ 0 cho `/api/products` và `/api/reviews/product/{id}` (trước đây `?size=1000000` sẽ quét toàn bảng).
- Giới hạn kích thước request: Tomcat 256KB (form/swallow), header 16KB, multipart 2MB; nginx `client_max_body_size 256k`.
- Bổ sung 5 exception handler còn thiếu (`ConstraintViolation`, `HttpMessageNotReadable`, `MaxUploadSizeExceeded`, `DataIntegrityViolation`, và lưới an toàn `Exception`) — trước đây các lỗi này rơi ra `/error` mặc định với format khác hẳn API, hoặc trả 500.
- Sửa NPE tiềm ẩn trong handler validation khi ràng buộc đặt ở mức class.
- Sửa trùng mã lỗi: `USERNAME_INVALID` và `UNAUTHORIZED` cùng dùng mã `1018`.

**SQL Injection: đã an toàn từ trước** — 100% truy vấn qua Spring Data JPA/JPQL có tham số hoá, không có `nativeQuery`, không nối chuỗi SQL. Không cần sửa.

**XSS**: dữ liệu người dùng (bình luận, mô tả) được lưu nguyên văn, không escape ở tầng API. Hiện an toàn vì React tự escape khi render và codebase không dùng `dangerouslySetInnerHTML`. Nếu sau này có client khác (mobile app, SSR) tiêu thụ API này thì cần escape ở tầng hiển thị của client đó.

---

## 4. Lỗ hổng phát hiện & trạng thái xử lý

### 🔴 Nghiêm trọng — ĐÃ SỬA

| # | Lỗ hổng | Ảnh hưởng | Cách sửa |
|---|---|---|---|
| 1 | **Leo thang đặc quyền**: `PUT /api/users/{id}` không kiểm tra quyền, lại nhận `roles` từ client | Bất kỳ user đăng nhập nào cũng tự cấp `ADMIN` cho mình, hoặc sửa hồ sơ/mật khẩu người khác — chiếm toàn quyền hệ thống bằng **1 request** | Bỏ `roles` khỏi `UserUpdateRequest`; chỉ chính chủ hoặc ADMIN mới được cập nhật |
| 2 | **Mật khẩu lưu dạng plaintext** khi cập nhật user: code map chuỗi thô vào entity trước, hash vào object request (rồi vứt đi) sau | Mọi user từng đổi hồ sơ đều có mật khẩu nằm trần trong DB; và họ không đăng nhập lại được vì hash không khớp | Bỏ map `password` trong mapper, hash rồi mới gán vào entity, chỉ đổi khi client thực sự gửi |
| 3 | **API trả về hash BCrypt của mật khẩu** trong `UserResponse` | Mọi endpoint trả user đều lộ hash cho client, mang đi crack offline được | Xoá field `password` khỏi `UserResponse` |
| 4 | `/api/roles`, `/api/permissions` **mở cho mọi user đã đăng nhập** | Tự tạo/xoá role và permission tuỳ ý | Thêm `@PreAuthorize("hasAuthority('SCOPE_ADMIN')")` ở cấp class |
| 5 | Tài khoản mặc định **`admin` / `admin`** seed mỗi lần khởi động | Ai biết địa chỉ server đều vào được trang quản trị | Đọc từ `ADMIN_INIT_PASSWORD`; không cấu hình thì **không seed** |

### 🟠 Trung bình — ĐÃ SỬA

| # | Lỗ hổng | Cách sửa |
|---|---|---|
| 6 | `GET /api/users` trả **toàn bộ danh sách user** cho bất kỳ ai đăng nhập | Giới hạn chỉ ADMIN |
| 7 | **User enumeration**: username sai trả 404, mật khẩu sai trả 401 ⇒ dò được username hợp lệ | Cả 2 trường hợp trả cùng `UNAUTHENTICATED` |
| 8 | Log DEBUG của Spring Security + `show-sql: true` bật cả khi chạy Docker ⇒ chi tiết cơ chế xác thực và câu SQL chứa dữ liệu người dùng đổ vào Loki | Hạ về INFO / tắt `show-sql` trong `application-docker.yaml` |
| 9 | `@EnableMethodSecurity` đặt nhầm trên một `@RestController` | Chuyển về `SecurityConfig` |

### 🟡 Còn tồn tại — CHƯA SỬA (cần thiết kế riêng, ghi nhận để xử lý sau)

| Lỗ hổng | Rủi ro | Hướng xử lý |
|---|---|---|
| **Không có logout / thu hồi token** | JWT sống 24h, lộ token = mất tài khoản trọn 1 ngày, không cách nào vô hiệu hoá | Thêm refresh token ngắn hạn + bảng blacklist `jti` |
| **JWT lưu trong `localStorage`** | Nếu có lỗ hổng XSS, script đọc được token | Chuyển sang cookie `HttpOnly` + `SameSite` (kèm CSRF token) |
| **Không có refresh token** | Buộc phải để TTL dài (24h) mới đủ tiện dùng | Access token 15 phút + refresh token xoay vòng |
| **CORS hardcode `http://localhost:5173`** | Deploy môi trường khác là hỏng; sửa vội dễ thành `*` (rất nguy hiểm khi bật `allowCredentials`) | Đưa origin vào biến môi trường |
| **Thiếu security headers** (CSP, HSTS, Referrer-Policy) | Giảm khả năng phòng thủ nhiều lớp trước XSS/clickjacking | Cấu hình `httpSecurity.headers(...)` |
| **`/actuator` public + `show-details: always`** | Lộ trạng thái DB, thông tin nội bộ cho bất kỳ ai | Đặt actuator sang port quản trị riêng, hoặc yêu cầu xác thực |
| **`ddl-auto: update`** | Hibernate tự sửa schema production, không rollback được | Chuyển sang Flyway/Liquibase |
| **Chưa có xác thực email khi đăng ký** | Đăng ký bằng email người khác; luồng Google OAuth ghép tài khoản theo email nên có thể bị chiếm | Gửi mail xác thực trước khi kích hoạt tài khoản |
| **Session không stateless** | `oauth2Login` tạo `JSESSIONID`, API vốn dùng bearer token nên không cần session | `.sessionManagement(STATELESS)` cho các route `/api/**` |
| **JWT không kiểm tra `issuer`/`audience`** | Token ký bằng cùng khoá từ hệ thống khác vẫn dùng được | Thêm validator issuer/audience |

---

## 5. Cách kiểm chứng nhanh

```bash
docker compose up -d --build
```

```bash
# Rate limit: lần thứ 6 phải trả 429
for i in 1 2 3 4 5 6; do curl -s -o /dev/null -w "$i: %{http_code}\n" \
  -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"sai-mat-khau"}'; done
```

```bash
# Actuator KHÔNG bị chặn (nếu 429 là container sẽ restart liên tục)
for i in $(seq 1 20); do curl -s -o /dev/null -w "%{http_code} " http://localhost:8080/actuator/health; done
```

```bash
# Validation: rating ngoài khoảng 1-5 phải bị từ chối
curl -s -X POST http://localhost:8080/api/reviews -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" -d '{"productId":1,"rating":99,"comment":"x"}'
```
