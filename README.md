# E-commerce Mini

Nền tảng thương mại điện tử full-stack: Spring Boot + React, kèm bộ observability
ba trụ cột (metrics / logs / traces) và các lớp bảo mật thường thấy ở hệ thống
chạy thật — refresh token xoay vòng, rate limiting nhiều tầng, validate đầu vào.

Toàn bộ hệ thống — 8 container — khởi động bằng **một lệnh**.

```bash
cp .env.example .env    # điền secret
docker compose up -d --build
```

| | |
|---|---|
| Giao diện | http://localhost:5173 |
| API | http://localhost:8080 |
| Grafana | http://localhost:3000 |

---

## Ảnh chụp màn hình

<!-- Thêm ảnh vào docs/images/ rồi bỏ chú thích 3 dòng dưới đây -->
<!-- ![Trang chủ](docs/images/home.png) -->
<!-- ![Dashboard Grafana](docs/images/grafana.png) -->
<!-- ![Trace và log tương quan](docs/images/trace.png) -->

## Tính năng

**Người mua** — duyệt sản phẩm theo danh mục, tìm kiếm full-text, xem chi tiết,
giỏ hàng, sổ địa chỉ, áp voucher, đặt hàng, xem lịch sử đơn, viết đánh giá
(chỉ cho sản phẩm đã thực sự mua).

**Quản trị** — quản lý sản phẩm, đơn hàng, voucher; phân quyền theo role và
permission ở tầng method (`@PreAuthorize`).

**Tài khoản** — đăng ký, đăng nhập, đăng nhập Google (OAuth2), access token
ngắn hạn kèm refresh token xoay vòng.

Quy mô: 14 controller, 18 service, 15 entity.

## Kiến trúc

```
Browser → frontend (nginx :5173) ──/api──► backend (Spring Boot :8080) → MySQL
                                              │
              ┌───────────────────────────────┼──────────────────────────┐
              │                               │                          │
     /actuator/prometheus              OTLP traces              JSON logs (stdout)
              │                               │                          │
        Prometheus :9090                Tempo :3200          Promtail → Loki
              │                               │                          │
              └───────────────┬───────────────┴──────────────────────────┘
                              │
                        Grafana :3000
              dashboards • alert rules • tương quan trace ↔ log
```

## Công nghệ

| Lớp | Thành phần |
|---|---|
| Backend | Java 17, Spring Boot 3.5.16, Spring Security 6, Spring Data JPA, MapStruct, Lombok |
| Xác thực | JWT (Nimbus HS512), OAuth2 Resource Server + Google OAuth2 Login |
| Database | MySQL 8.4 |
| Frontend | React 19, Vite 8, Tailwind CSS 4, React Router 7, Axios |
| Observability | Micrometer, Prometheus, Loki + Promtail, Tempo (OpenTelemetry OTLP), Grafana |
| Rate limiting | Bucket4j + Caffeine |
| Hạ tầng | Docker Compose, Caddy (HTTPS tự động) |

## Điểm kỹ thuật đáng chú ý

### Refresh token xoay vòng, có phát hiện tái sử dụng

Access token hết hạn sau 15 phút; client tự gia hạn bằng refresh token (7 ngày).
Mỗi lần gia hạn, token cũ bị thu hồi và cấp token mới. Nếu một token **đã dùng
rồi** xuất hiện lần nữa — dấu hiệu bị đánh cắp — toàn bộ phiên đăng nhập của
tài khoản đó bị thu hồi.

Phần thu hồi chạy trong transaction riêng (`Propagation.REQUIRES_NEW`). Đây là
một bug thật: ban đầu việc thu hồi nằm chung transaction với lệnh `throw` báo
lỗi, nên exception cuốn luôn cả lệnh thu hồi vào rollback — token bị đánh cắp
vẫn dùng được. Test dùng mock vẫn pass; chỉ chạy thật với database mới lộ ra.

Phía client, `axiosClient` tự gọi gia hạn khi gặp 401 và xếp hàng các request
đang chờ, tránh nhiều request cùng lúc kích hoạt nhiều lần gia hạn song song.

### Rate limiting ba tầng

| Tầng | Phạm vi | Giới hạn |
|---|---|---|
| STRICT | đăng nhập, đăng ký, áp voucher | 5 lần / 15 phút |
| WRITE | mọi POST/PUT/DELETE khác | 60 / phút |
| GLOBAL | phần còn lại | 200 / phút |

Trả về `429` kèm header `Retry-After` và `X-RateLimit-Remaining` (đã khai báo
trong `exposedHeaders` của CORS, nếu không trình duyệt không đọc được).

Bucket khoá theo `getRemoteAddr()`, **không** tự đọc `X-Forwarded-For` — chạy
local không có proxy tin cậy nào phía trước thì kẻ tấn công chỉ cần đổi header
là thoát giới hạn. Khi deploy sau reverse proxy mới bật
`SERVER_FORWARD_HEADERS_STRATEGY=framework`, lúc đó backend không mở port ra
ngoài nên header không giả mạo được.

### Tương quan trace ↔ log

Micrometer Tracing sinh `traceId`/`spanId` cho mỗi request và đưa vào MDC. Log
JSON và span gửi sang Tempo mang cùng `traceId`, nên trong Grafana có thể bấm
từ một dòng log sang đúng trace của nó và ngược lại.

Ngoài metric hạ tầng còn có metric nghiệp vụ: đơn hàng theo trạng thái, doanh
thu, số lần hết hàng, voucher bị từ chối theo lý do, thanh toán theo phương thức.

Có sẵn 2 dashboard và 3 alert rule (tỉ lệ 5xx, p95 latency, backend down), tất
cả được provision tự động khi Grafana khởi động.

### Deploy được ở hai kiểu hạ tầng

- **VPS tự quản** — `docker-compose.prod.yml` + Caddy, HTTPS tự động, đóng toàn
  bộ port nội bộ, chỉ mở 80/443. Xem [DEPLOYMENT.md](DEPLOYMENT.md).
- **PaaS** (Northflank, Railway...) — 3 service, dùng database quản lý sẵn, địa
  chỉ backend và endpoint tracing cấu hình qua biến môi trường. Xem
  [DEPLOY_NORTHFLANK.md](DEPLOY_NORTHFLANK.md).

## Kiểm thử

12 unit test (JUnit 5 + Mockito) tập trung vào phần dễ sai nhất: rate limiting
(4) và vòng đời refresh token (8) — xoay vòng, phát hiện tái sử dụng, thu hồi.

```bash
cd backend/E-commerce-Mini && ./mvnw test
```

Test thứ 13, `ECommerceMiniApplicationTests.contextLoads`, là test sinh sẵn khi
tạo project. Nó nạp toàn bộ Spring context nên cần một MySQL đang chạy ở
`localhost:3306`; không có thì lệnh trên báo lỗi. 12 test kia không phụ thuộc
môi trường. Xem mục [Hạn chế đã biết](#hạn-chế-đã-biết).

## Cấu trúc thư mục

```
backend/E-commerce-Mini/     Spring Boot API
frontend/E-commerce-mini/    React SPA (nginx phục vụ bản build)
observability/               config Prometheus, Loki, Promtail, Tempo, Grafana
deploy/                      Caddyfile cho production
docker-compose.yml           stack đầy đủ cho phát triển
docker-compose.prod.yml      lớp ghi đè cho production trên VPS
```

## Tài liệu

| File | Nội dung |
|---|---|
| [OBSERVABILITY.md](OBSERVABILITY.md) | Kiến trúc logging/monitoring, dashboard, alert, metric nghiệp vụ |
| [SECURITY.md](SECURITY.md) | Kết quả audit bảo mật, các lỗ hổng đã vá và còn tồn tại |
| [DEPLOYMENT.md](DEPLOYMENT.md) | Deploy lên VPS với Caddy + HTTPS |
| [DEPLOY_NORTHFLANK.md](DEPLOY_NORTHFLANK.md) | Deploy lên nền tảng quản lý sẵn |

## Hạn chế đã biết

Ghi ra đây để minh bạch, không phải để bỏ qua:

- `spring.jpa.hibernate.ddl-auto: update` — Hibernate tự sửa schema, chưa dùng
  công cụ migration (Flyway/Liquibase). Đủ cho demo, rủi ro với dữ liệu thật.
- Index tối ưu nằm ở `backend/E-commerce-Mini/add_indexes_performance.sql`, phải
  chạy tay sau lần khởi động đầu — chưa tự động hoá.
- Rate limit lưu trong bộ nhớ tiến trình. Chạy nhiều instance backend thì mỗi
  instance đếm riêng, phải chuyển sang store dùng chung (Redis).
- Đăng nhập thành công cũng tiêu một lượt trong hạn mức 5 lần/15 phút. Đúng hơn
  là chỉ nên đếm lần **thất bại**.
- Promtail cần quyền đọc Docker socket nên không chạy được trên PaaS; ở đó
  observability dùng công cụ sẵn có của nền tảng.
- `/actuator` đang để public cho Prometheus scrape và Docker healthcheck.
- `./mvnw test` hiện **không xanh trên máy sạch**: `contextLoads` cần MySQL thật
  ở `localhost:3306`. Cách sửa đúng là dùng Testcontainers để test tự dựng
  database, hoặc bỏ hẳn test này vì nó không kiểm tra hành vi nào cụ thể.
