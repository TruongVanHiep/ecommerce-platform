# Observability Platform

E-commerce Mini có sẵn 1 bộ observability platform đầy đủ 3 trụ cột — **Metrics**, **Logs**, **Traces** — chạy cùng lúc với toàn bộ ứng dụng (backend + frontend + MySQL) chỉ bằng 1 lệnh Docker Compose.

## Kiến trúc

```
Browser → frontend (nginx, :5173) → /api proxy → backend (Spring Boot, :8080) → mysql
                                                       │
                          ┌────────────────────────────┼───────────────────────────┐
                          │                             │                           │
                 /actuator/prometheus              OTLP traces                 JSON logs (stdout)
                          │                             │                           │
                    Prometheus (:9090)              Tempo (:3200)      Promtail → Loki
                          │                             │                           │
                          └─────────────────┬───────────┴───────────────────────────┘
                                             │
                                       Grafana (:3000)
                              dashboards + alerts + trace↔log correlation
```

**Correlation qua `traceId`**: Micrometer Tracing (OpenTelemetry bridge) tự sinh `traceId`/`spanId` cho mỗi request, đưa vào MDC. Log JSON (`logstash-logback-encoder`) và span gửi tới Tempo đều mang cùng `traceId` — trong Grafana Explore, từ 1 dòng log có thể bấm sang xem trace tương ứng, và ngược lại (cấu hình ở `observability/grafana/provisioning/datasources/datasources.yml`).

## Chạy

```bash
cp .env.example .env   # rồi điền secret thật (DB password, JWT key, Google OAuth, mail)
docker compose up -d --build
```

Cần Docker Desktop đang chạy. Lần đầu build backend/frontend sẽ mất vài phút.

## Truy cập

| Service | URL | Ghi chú |
|---|---|---|
| Frontend | http://localhost:5173 | SPA, gọi API qua nginx proxy `/api` |
| Backend | http://localhost:8080 | REST API |
| Backend health | http://localhost:8080/actuator/health | |
| Prometheus | http://localhost:9090 | Xem target `backend` ở `/targets` |
| Grafana | http://localhost:3000 | user `admin`, password theo `GRAFANA_ADMIN_PASSWORD` trong `.env` (mặc định `admin`) |
| Tempo | http://localhost:3200 | Query API, thường dùng qua Grafana Explore |

## Dashboards có sẵn (Grafana → Dashboards → E-commerce Mini)

- **Application Overview**: HTTP request rate, tỉ lệ lỗi 5xx, uptime backend, latency p50/p95/p99, JVM heap, connection pool (HikariCP), GC pause rate.
- **Business Metrics**: đơn hàng tạo mới/phút, doanh thu/giờ, giá trị đơn hàng trung bình, số lần hết hàng, voucher bị từ chối theo lý do, thanh toán theo phương thức/trạng thái.

## Alert rules có sẵn

Cấu hình tại `observability/grafana/provisioning/alerting/rules.yml` (Grafana unified alerting, tự provision khi khởi động):

1. **High 5xx error rate** — tỉ lệ lỗi 5xx > 5% trong 5 phút.
2. **High p95 latency** — p95 latency HTTP > 1.5s trong 5 phút.
3. **Backend down** — Prometheus không scrape được `backend` trong 1 phút.

Muốn nhận thông báo thật (Slack/email/...) thì cấu hình thêm Contact Point trong Grafana UI (Alerting → Contact points) — mặc định chỉ hiện trên UI Grafana.

## Business metrics đã đưa vào code

Định nghĩa trong `OrderService`/`PaymentService` qua Micrometer `MeterRegistry`:

| Metric (Prometheus name) | Loại | Ý nghĩa |
|---|---|---|
| `orders_placed_total{status}` | Counter | Số đơn hàng tạo thành công/bị từ chối |
| `orders_value_sum` / `orders_value_count` | Summary | Tổng giá trị & số lượng đơn hàng (tính doanh thu, giá trị đơn TB) |
| `products_stock_rejected_total{productId}` | Counter | Số lần khách mua nhưng hết hàng |
| `vouchers_rejected_total{reason}` | Counter | Số lần áp voucher thất bại, theo lý do |
| `payments_processed_total{method,status}` | Counter | Số payment theo phương thức & trạng thái |

## Cấu trúc thư mục observability

```
observability/
├── prometheus/prometheus.yml       # scrape config
├── loki/loki-config.yml            # log storage
├── promtail/promtail-config.yml    # tail log container qua Docker socket
├── tempo/tempo.yml                 # nhận trace qua OTLP
└── grafana/
    ├── provisioning/datasources/   # auto-provision Prometheus + Loki + Tempo
    ├── provisioning/dashboards/    # trỏ tới thư mục dashboard JSON
    ├── provisioning/alerting/      # alert rules
    └── dashboards/*.json           # 2 dashboard nói trên
```

## Chạy backend không cần Docker (dev như cũ)

`docker-compose.yml` không bắt buộc cho việc code hằng ngày. Vẫn có thể chạy `mvnw spring-boot:run` (đọc `application.yaml` + `application-secrets.yaml` local) và `npm run dev` như trước — chỉ khi cần xem dashboard/log tập trung mới cần bật thêm observability stack qua Docker.
