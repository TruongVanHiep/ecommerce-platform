# Deploy lên Northflank

Hướng dẫn này dành cho nền tảng quản lý sẵn (PaaS). Nếu deploy lên VPS tự quản
thì đọc [DEPLOYMENT.md](DEPLOYMENT.md) — hai cách khác nhau khá nhiều.

## Chạy gì và bỏ gì

Trên VPS, `docker-compose.yml` chạy 8 container. Trên Northflank chỉ deploy 3:

| Thành phần | Trên Northflank | Lý do |
|---|---|---|
| backend | ✅ Service từ Dockerfile | |
| frontend | ✅ Service từ Dockerfile | Đồng thời là cổng public duy nhất |
| MySQL | ✅ Addon database của nền tảng | Không tự chạy container, để nền tảng lo backup |
| Caddy | ❌ Bỏ | Northflank đã có SSL + custom domain sẵn |
| Prometheus, Grafana, Tempo, Loki | ❌ Bỏ | Phải build 4 image riêng chỉ để nhét file config vào, vì PaaS không bind-mount được từ repo |
| Promtail | ❌ Không thể | Nó cần `/var/run/docker.sock` của máy chủ — không nền tảng nào cấp quyền đó |

Observability vẫn chạy đầy đủ ở local bằng `docker compose up -d`. Xem
[OBSERVABILITY.md](OBSERVABILITY.md).

## Chuẩn bị

1. Code đã merge vào `main` và push lên GitHub — Northflank build từ Git, cái
   gì trên GitHub thì cái đó được deploy.
2. Có sẵn domain (hoặc dùng domain Northflank cấp).
3. Chuỗi ngẫu nhiên cho `JWT_SIGNER_KEY`:
   ```bash
   openssl rand -base64 64 | tr -d '\n'
   ```

## Bước 1 — Tạo MySQL

Tạo addon MySQL 8. Sau khi tạo, nền tảng cho biết host, port, tên database,
user và password. Ghi lại — bước 2 cần.

## Bước 2 — Deploy backend

Tạo service build từ Git:

- **Build context**: `backend/E-commerce-Mini`
- **Dockerfile**: `backend/E-commerce-Mini/Dockerfile`
- **Port**: `8080`
- **Public**: **KHÔNG**. Backend chỉ cần truy cập nội bộ, frontend proxy vào.
- **Health check**: `/actuator/health`

Biến môi trường:

| Biến | Giá trị | Ghi chú |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `docker` | Tắt log SQL, hạ log Security xuống INFO |
| `DB_URL` | `jdbc:mysql://<host>:<port>/<db>?useSSL=true&serverTimezone=UTC` | Lấy từ addon ở bước 1 |
| `DB_USERNAME` | từ addon | |
| `DB_PASSWORD` | từ addon | Để dạng secret |
| `JWT_SIGNER_KEY` | chuỗi đã sinh ở phần chuẩn bị | Để dạng secret |
| `GOOGLE_CLIENT_ID` | từ Google Cloud Console | |
| `GOOGLE_CLIENT_SECRET` | từ Google Cloud Console | Để dạng secret |
| `MAIL_USERNAME` | email gửi thông báo | |
| `MAIL_PASSWORD` | app password của Gmail | Để dạng secret |
| `ADMIN_INIT_PASSWORD` | mật khẩu admin khởi tạo | Bỏ trống thì không seed tài khoản admin nào |
| `APP_PUBLIC_URL` | `https://<domain>` | Dùng dựng redirect URI của Google |
| `APP_CORS_ORIGINS` | `https://<domain>` | |
| `SERVER_FORWARD_HEADERS_STRATEGY` | `framework` | **Bắt buộc**, xem phần cuối |
| `TRACING_SAMPLE_RATE` | `0` | **Bắt buộc**, xem phần cuối |
| `SEPAY_API_KEY` | API key webhook ở SePay | Để dạng secret. Bỏ trống thì tắt thanh toán chuyển khoản |
| `SEPAY_BANK_CODE` | vd `MBBank`, `Vietcombank` | Tên viết tắt ngân hàng theo VietQR |
| `SEPAY_ACCOUNT_NUMBER` | số tài khoản nhận tiền | Phải trùng tài khoản đã liên kết SePay |
| `SEPAY_ACCOUNT_NAME` | tên chủ tài khoản | Chỉ để hiển thị cho khách đối chiếu |

Nếu MySQL của nền tảng báo lỗi `Public Key Retrieval is not allowed`, thêm
`&allowPublicKeyRetrieval=true` vào cuối `DB_URL`.

## Bước 3 — Deploy frontend

- **Build context**: `frontend/E-commerce-mini`
- **Dockerfile**: `frontend/E-commerce-mini/Dockerfile`
- **Port**: `80`
- **Public**: **CÓ**. Đây là cổng vào duy nhất.

Biến môi trường:

| Biến | Giá trị |
|---|---|
| `BACKEND_URL` | `http://<địa chỉ nội bộ của service backend>:8080` |

Địa chỉ nội bộ lấy trong phần networking của service backend trên Northflank.
Không có `/` ở cuối.

nginx sẽ proxy 3 nhóm đường dẫn sang backend: `/api/`, `/oauth2/`,
`/login/oauth2/`. Phần còn lại trả về SPA.

## Bước 4 — Gắn domain

Gắn domain vào **service frontend** (không phải backend). Northflank tự cấp
chứng chỉ HTTPS.

Xong thì quay lại backend, kiểm tra `APP_PUBLIC_URL` và `APP_CORS_ORIGINS` đã
đúng domain thật chưa, rồi restart backend.

## Bước 5 — Cập nhật Google OAuth

Vào Google Cloud Console → Credentials → OAuth 2.0 Client ID, thêm:

- **Authorized JavaScript origins**: `https://<domain>`
- **Authorized redirect URIs**: `https://<domain>/login/oauth2/code/google`

Đúng chính xác chuỗi đó, kể cả `https`. Sai một ký tự là `redirect_uri_mismatch`.

## Thứ tự deploy

MySQL → backend (đợi healthy) → frontend.

**Không đảo thứ tự.** nginx phân giải DNS của backend ngay lúc khởi động, không
phải lúc có request. Backend chưa sẵn sàng thì frontend thoát với
`host not found in upstream` và lặp restart mãi.

## Kiểm tra sau khi deploy

```bash
curl -s -o /dev/null -w "%{http_code}\n" https://<domain>/
```
→ `200`

```bash
curl -s -o /dev/null -w "%{http_code}\n" https://<domain>/api/products
```
→ `200`

```bash
curl -s -o /dev/null -w "%{redirect_url}\n" https://<domain>/oauth2/authorization/google
```
→ phải ra link `accounts.google.com` và trong đó
`redirect_uri=https://<domain>/login/oauth2/code/google`.

Nếu thấy `redirect_uri=http://...` (không phải https) thì
`SERVER_FORWARD_HEADERS_STRATEGY` chưa được set.

Cuối cùng đăng nhập thử bằng Google trên trình duyệt thật.

## Hai biến dễ quên nhất

### `SERVER_FORWARD_HEADERS_STRATEGY=framework`

Bảo Spring tin các header `X-Forwarded-*` do lớp proxy phía trước đặt. Thiếu nó
thì Spring dựng URL từ request nội bộ, sinh ra
`http://<tên-service-nội-bộ>:8080/login/oauth2/code/google` — Google từ chối
ngay. Ngoài ra `RateLimitFilter` khoá bucket theo `getRemoteAddr()`, thiếu dòng
này thì mọi request đều mang IP của lớp proxy và cả hệ thống dùng chung một
bucket 5 lần/15 phút.

An toàn vì backend không public: chỉ frontend gọi tới được nên header không giả
mạo được từ bên ngoài.

### `TRACING_SAMPLE_RATE=0`

Trên Northflank không có Tempo. Không tắt thì backend cứ 100% request lại cố
gửi trace tới địa chỉ không tồn tại, log đầy lỗi export và tốn tài nguyên vô ích.

## Cập nhật code sau này

Push lên `main`, Northflank tự build và deploy lại. Không cần SSH, không cần
`docker compose pull`.

## Việc chưa làm

- Chưa có backup tự động cho database — kiểm tra addon MySQL của nền tảng có
  bật sẵn không.
- `ddl-auto: update` vẫn đang bật. Dùng thật với dữ liệu quan trọng thì nên
  chuyển sang Flyway/Liquibase để kiểm soát thay đổi schema.
