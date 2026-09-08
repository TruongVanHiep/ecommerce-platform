# Hướng dẫn Deploy lên VPS

Toàn bộ hệ thống chạy bằng Docker Compose, có HTTPS tự động qua Caddy.

> Deploy lên nền tảng quản lý sẵn (Northflank, Railway, Render...) thì đọc
> [DEPLOY_NORTHFLANK.md](DEPLOY_NORTHFLANK.md). Ở đó chỉ chạy 3 service và
> không dùng Caddy — nền tảng tự lo HTTPS.

## 1. Yêu cầu

| Hạng mục | Tối thiểu | Ghi chú |
|---|---|---|
| RAM | **4 GB** | 8 container: MySQL, backend, frontend, Prometheus, Grafana, Loki, Tempo, Promtail, Caddy |
| Ổ cứng | 20 GB | MySQL + dữ liệu observability |
| Hệ điều hành | Ubuntu 22.04+ | Cần cài Docker + Docker Compose plugin |
| Domain | 1 domain + 1 subdomain | VD `shop.commerce.com` và `grafana.commerce.com` |

> VPS 1 GB RAM (gói ~5$/tháng) **không đủ** chạy cả bộ observability. Nếu chỉ có 1 GB,
> xem mục "Chạy gọn nhẹ" ở cuối.

## 2. Trỏ domain

Tạo 2 bản ghi **A** trỏ về IP của VPS:

```
shop.commerce.com      A   <IP VPS>
grafana.commerce.com   A   <IP VPS>
```

Chờ DNS lan truyền (thường vài phút). Kiểm tra: `ping shop.commerce.com` phải ra đúng IP.

**Phải làm bước này TRƯỚC khi khởi động Caddy** — Let's Encrypt xác minh quyền sở hữu
domain bằng cách gọi ngược về IP, domain chưa trỏ đúng thì không cấp được chứng chỉ.

## 3. Cài Docker trên VPS

```bash
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER   # đăng xuất/đăng nhập lại để có hiệu lực
```

## 4. Lấy mã nguồn và cấu hình

```bash
git clone https://github.com/TruongVanHiep/ecommerce-platform.git
cd ecommerce-platform
cp .env.example .env
nano .env
```

Điền các giá trị sau trong `.env`:

```bash
DOMAIN=shop.commerce.com
GRAFANA_DOMAIN=grafana.commerce.com

MYSQL_ROOT_PASSWORD=<mật khẩu mạnh>
DB_USERNAME=root
DB_PASSWORD=<trùng với MYSQL_ROOT_PASSWORD>

# Sinh khoá ngẫu nhiên: openssl rand -base64 64 | tr -d '\n'
JWT_SIGNER_KEY=<chuỗi ≥64 byte>

GOOGLE_CLIENT_ID=<từ Google Cloud Console>
GOOGLE_CLIENT_SECRET=<từ Google Cloud Console>
MAIL_USERNAME=<gmail của bạn>
MAIL_PASSWORD=<app password 16 ký tự, bỏ khoảng trắng>

GRAFANA_ADMIN_PASSWORD=<mật khẩu mạnh, KHÔNG để "admin">
ADMIN_INIT_PASSWORD=<mật khẩu admin khởi tạo, dùng xong nên đổi>
```

⚠️ `JWT_SIGNER_KEY` phải **≥ 64 byte** vì code ký bằng HS512. Ngắn hơn là app không khởi động được.

## 5. Cập nhật Google OAuth cho domain mới

Vào [Google Cloud Console](https://console.cloud.google.com) → **APIs & Services → Credentials**
→ chọn OAuth client → thêm vào **Authorized redirect URIs**:

```
https://shop.commerce.com/login/oauth2/code/google
```

Thiếu bước này thì đăng nhập Google báo `redirect_uri_mismatch`.

## 6. Khởi động

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

Lần đầu build mất khoảng 5–10 phút. Caddy tự xin chứng chỉ HTTPS ngay khi khởi động.

Kiểm tra:

```bash
docker compose ps                                    # tất cả phải Up/healthy
curl -I https://shop.commerce.com                     # phải trả 200 và có HTTPS
curl https://shop.commerce.com/api/products | head    # API hoạt động
docker logs ecommerce-caddy | grep -i certificate    # xác nhận đã cấp chứng chỉ
```

## 7. Cập nhật code sau này

```bash
git pull
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

---

## Điểm khác biệt so với chạy local

| | Local (`docker-compose.yml`) | Production (thêm `docker-compose.prod.yml`) |
|---|---|---|
| Truy cập | `http://localhost:5173` | `https://shop.commerce.com` qua Caddy |
| HTTPS | Không | Có, tự động gia hạn |
| Port mở ra ngoài | 3307, 8080, 5173, 9090, 3000, 3200 | **Chỉ 80 và 443** |
| Grafana | `localhost:3000` | `https://grafana.commerce.com`, tắt đăng ký |
| CORS | `http://localhost:5173` | `https://shop.commerce.com` |
| Header bảo mật | Không | HSTS, X-Frame-Options, Referrer-Policy... |

**Vì sao production đóng hết port nội bộ**: nếu vẫn mở 8080/3306/3000 ra internet thì
người ta gọi thẳng vào backend/MySQL/Grafana, bỏ qua HTTPS và mọi lớp bảo vệ ở Caddy.

## Sao lưu dữ liệu

```bash
# Sao lưu database
docker exec ecommerce-mysql mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" \
  --single-transaction e_commerce_mini > backup-$(date +%F).sql
```

Volume `caddy_data` chứa chứng chỉ HTTPS — **đừng xoá**, xoá thì phải xin lại
(Let's Encrypt có giới hạn số lần cấp mỗi tuần).

## Chạy gọn nhẹ (VPS 1–2 GB RAM)

Bỏ bộ observability, chỉ giữ app + database:

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d \
  --build caddy mysql backend frontend
```

Grafana/Prometheus/Loki/Tempo vẫn chạy được ở máy local khi cần demo.

## Việc còn phải làm trước khi dùng thật với dữ liệu quan trọng

Xem [SECURITY.md](SECURITY.md) mục "Còn tồn tại". Đáng chú ý nhất:

- `spring.jpa.hibernate.ddl-auto: update` — Hibernate tự sửa schema, chưa có công cụ
  migration (Flyway/Liquibase). Chấp nhận được cho demo, rủi ro với dữ liệu thật.
- `/actuator` đang public — nên chặn ở Caddy hoặc yêu cầu xác thực nếu môi trường thật.

## Vì sao production cần `SERVER_FORWARD_HEADERS_STRATEGY=framework`

Khi backend chạy sau Caddy, request tới nó có địa chỉ `http://backend:8080` chứ không
phải domain thật. Nếu không bật biến này:

1. **Đăng nhập Google hỏng** — Spring sinh `redirect_uri=http://backend:8080/login/oauth2/code/google`
   gửi cho Google, Google báo `redirect_uri_mismatch`.
2. **Rate limiting hỏng** — mọi request đều mang IP của container Caddy, nên toàn bộ
   người dùng dùng chung một bucket: một người gõ sai mật khẩu 5 lần là cả hệ thống bị chặn.

Bật biến này thì Spring đọc header `X-Forwarded-*` do Caddy đặt và dựng lại URL/IP thật.
Chỉ an toàn khi backend **không mở port ra ngoài** (đúng như `docker-compose.prod.yml`),
vì khi đó chỉ Caddy gọi tới được, không ai giả mạo header được.
