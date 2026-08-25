# Redesign Spec — MiniCommerce (theo phong cách Auxia)

## Mục tiêu

Giao diện hiện tại (Shopee/Lazada-style: cam-indigo gradient, card bo tròn, mật độ thông tin dày) đổi hướng sang phong cách **SaaS cao cấp** như ảnh tham chiếu (trang chủ Auxia — nền tối/sáng xen kẽ, typography lớn, khoảng trắng rộng, 1 màu nhấn xanh điện, hiệu ứng glass/gradient tinh tế). Đây là **spec thiết kế**, chưa động vào code — dùng để lên kế hoạch implement sau bằng Tailwind v4 hiện có.

Tinh thần: **ít màu, nhiều khoảng trắng, chữ to, chuyển động tinh tế** — bỏ hẳn cảm giác "sàn TMĐT nhiều màu mè", hướng tới cảm giác "sản phẩm công nghệ cao cấp".

---

## 1. Design tokens

### Màu sắc

| Token | Giá trị | Dùng cho |
|---|---|---|
| `--bg-dark` | `#0B0B12` | Section tối (hero, footer, trust) |
| `--bg-dark-soft` | `#13131C` | Card nổi trên nền tối |
| `--bg-light` | `#F3F1EA` | Section sáng (kem ngà, không dùng trắng thuần) |
| `--bg-white` | `#FFFFFF` | Card nổi trên nền sáng |
| `--accent` | `#3D5AFE` | CTA chính, link, số liệu nhấn — **duy nhất 1 màu nhấn** |
| `--accent-soft` | `#3D5AFE1A` (10% opacity) | Nền badge, hover nhẹ |
| `--text-primary-dark` | `#F5F5F7` | Chữ chính trên nền tối |
| `--text-primary-light` | `#0B0B12` | Chữ chính trên nền sáng |
| `--text-muted` | `#9A9AA5` (nền tối) / `#6B6B76` (nền sáng) | Subtext, caption |
| `--border-subtle` | `#FFFFFF14` (nền tối) / `#0B0B1214` (nền sáng) | Viền card mảnh 1px |

Bỏ hoàn toàn cặp cam/indigo gradient hiện tại. Không dùng quá 1 màu nhấn (`accent`) — mọi màu khác chỉ là sắc độ đen/trắng/kem.

### Typography

- Font: 1 font grotesque hiện đại (vd `Inter`, `Geist`, hoặc `General Sans` qua Google Fonts) — chữ đậm, tracking âm nhẹ ở heading.
- Scale (mobile → desktop):
  - Display (hero headline): `40px/1.05` → `76px/1.02`, `font-weight: 600`, `letter-spacing: -0.02em`
  - H2 (section title): `28px` → `44px`, weight 600
  - Body: `16px` → `18px`, weight 400, `text-muted` cho subtext
  - Caption/badge: `12px`, uppercase, `letter-spacing: 0.08em`
- Heading luôn ngắn, 1–2 dòng, không nhồi nhiều chữ như bản hiện tại.

### Bo góc, đổ bóng, khoảng cách

- Radius: `12px` (button/input), `20px` (card nhỏ), `28px` (card lớn/hero mockup)
- Shadow: cực nhẹ, gần như không thấy trên nền sáng (`0 8px 30px rgba(0,0,0,0.06)`); trên nền tối dùng viền sáng mảnh thay vì shadow (`border: 1px solid var(--border-subtle)`)
- Section padding: tối thiểu `96px` trên/dưới ở desktop (hiện tại đang chật, cần nới rộng hẳn)
- Container max-width: `1200px`, căn giữa

### Nút bấm

- Primary: nền `accent`, chữ trắng, bo `9999px` (pill), padding `14px 28px`
- Secondary: viền `border-subtle`, chữ theo nền, nền trong suốt, hover mới đổi nền nhẹ
- Không dùng nút gradient nhiều màu như hiện tại

---

## 2. Cấu trúc trang chủ mới (map từ ảnh tham chiếu sang bối cảnh e-commerce)

Nguyên tắc chung: **xen kẽ section tối/sáng** để tạo nhịp thị giác, giống hệt ảnh gốc.

### Section 1 — Hero (nền tối `--bg-dark`)
- Nav tối, logo bên trái, menu giữa, nút "Đăng nhập"/"Giỏ hàng" bên phải (pill, viền mảnh)
- Headline lớn kiểu Auxia: **"Mua sắm, nhân đôi trải nghiệm"** (ví dụ tinh thần — giữ ngắn gọn, 2 dòng)
- Subtext 1 câu, nhạt màu (`text-muted`)
- 2 CTA: nút xanh "Khám phá ngay" + nút viền "Xem ưu đãi"
- Bên phải: 1 khối ảnh sản phẩm nổi bật, đặt trong khung bo góc lớn `28px`, có gradient border tinh tế thay vì banner carousel nhiều màu như hiện tại
- Dưới cùng: 1 dòng nhỏ "Được tin dùng bởi hàng nghìn khách hàng" + logo/badge các đối tác vận chuyển, cổng thanh toán (VNPAY, COD...) xám nhạt, mờ 60% opacity — thay cho category pill row hiện tại

### Section 2 — "Vì sao chọn chúng tôi" (nền tối, tiếp nối hero)
- Câu dẫn kiểu: *"Mua sắm online không khó. Nó chỉ đang bị làm cho rối."* — 1 dòng lớn, căn giữa
- Bên dưới: sơ đồ node/dot kết nối bằng đường line mảnh màu `accent`, minh hoạ hành trình đơn hàng (Đặt hàng → Xử lý → Giao hàng → Nhận hàng) — thay cho network diagram trừu tượng trong ảnh gốc, nhưng giữ đúng phong cách (chấm tròn, đường nối cong, nền tối tuyệt đối)

### Section 3 — Đổi nền sáng `--bg-light`, giới thiệu 2 nhóm tính năng
- Icon 3D/isometric nhỏ ở giữa (khối lập phương xoay, gradient xanh) — điểm nhấn duy nhất có hiệu ứng "công nghệ"
- Tiêu đề: **"Trải nghiệm mua sắm, được nâng cấp"**
- 2 card cạnh nhau (giống bản gốc có 1 card đen + 1 card xanh):
  - Card tối: **"Giao hàng thông minh"** — checklist 3–4 gạch đầu dòng (theo dõi real-time, giao nhanh, đổi trả dễ), nút "Tìm hiểu thêm"
  - Card xanh (`accent` nền): **"Ưu đãi cá nhân hoá"** — checklist (voucher tự động, gợi ý sản phẩm, tích điểm), nút trắng

### Section 4 — Hành trình mua hàng (nền kem, tiếp tục sáng)
- Tiêu đề: **"Từ tìm kiếm đến nhận hàng, chỉ vài bước"**
- Chuỗi mockup UI thật của app (ảnh chụp trang sản phẩm → giỏ hàng → checkout → theo dõi đơn) đặt zic-zac, nối bằng đường chấm mảnh có label bước ở giữa mỗi đoạn nối — tái hiện đúng bố cục "flow diagram nối screenshot" trong ảnh gốc
- Đây là chỗ nên dùng **ảnh chụp thật từ app** (ProductDetailPage, CartPage, checkout modal) thay vì hình minh hoạ, để vừa đẹp vừa trung thực

### Section 5 — Dải số liệu (nền tối, full-bleed)
- 1 dòng nhỏ phía trên: **"Vận hành minh bạch, đo lường được"**
- 3 số liệu lớn cạnh nhau, kiểu ảnh gốc (20M+, 115M, $2B+):
  - Ví dụ: `10.000+` Đơn hàng đã giao · `98%` Khách hàng hài lòng · `24h` Thời gian giao trung bình
  - *(Điểm hay: số liệu này có thể lấy thật từ observability platform đã build — `orders_placed_total`, `payments_processed_total` — biến trang chủ thành nơi "khoe" luôn hạ tầng đo lường, rất hợp CV)*

### Section 6 — Dải "log/ticker" động (nền đen tuyệt đối, chữ monospace)
- Giống dải "automated decisions served" trong ảnh gốc: 1 dòng chữ monospace nhỏ, xám, chạy ngang liệt kê sự kiện realtime — ví dụ: `order #10234 confirmed · voucher SALE10 applied · payment COD success ...`
- Hiệu ứng scroll ngang chậm, tạo cảm giác "hệ thống đang sống"

### Section 7 — Tin cậy & bảo mật (nền tối)
- Tiêu đề: **"An toàn cho mọi giao dịch"**
- Badge: Thanh toán mã hoá SSL · Bảo vệ người mua · Đối tác vận chuyển uy tín · Chính sách đổi trả rõ ràng — dạng pill viền mảnh, icon đơn sắc

### Section 8 — CTA đóng trang (nền `accent` full-bleed)
- Chữ trắng cực lớn, wordmark thương hiệu chiếm gần hết section, giống dòng "auxia" khổng lồ trong ảnh gốc
- 1 nút trắng nhỏ "Bắt đầu mua sắm" phía trên logo

---

## 3. Component cần chuẩn hoá lại

| Component hiện tại | Thay đổi |
|---|---|
| `Header.jsx` | Chuyển nền trắng/gradient → nền tối trong suốt (blur khi scroll), bỏ icon nhiều màu, chỉ giữ outline icon đơn sắc |
| `ProductCard.jsx` | Bỏ badge giảm giá màu đỏ nổi bật, thay bằng badge viền mảnh; ảnh sản phẩm bo góc `20px`, hover chỉ scale nhẹ (không đổi shadow màu) |
| Banner carousel (Home) | Bỏ hẳn — thay bằng hero tĩnh 1 sản phẩm hero + section flow như trên |
| Category pill row | Bỏ hàng pill màu, chuyển thành dropdown/tag đơn sắc trong nav |
| Nút "Thêm vào giỏ" | Đổi sang pill `accent`, bỏ gradient cam hiện tại |
| Checkout modal | Card nền trắng viền mảnh thay vì shadow đậm, radio COD/VNPAY dạng segmented control 2 nút thay vì radio tròn |

---

## 4. Việc KHÔNG đổi (giữ nguyên)

- Toàn bộ logic nghiệp vụ, API, luồng giỏ hàng/checkout/voucher — chỉ đổi lớp trình bày (CSS/JSX layout), không đổi hành vi.
- Ngôn ngữ hiển thị vẫn tiếng Việt.
- Stack kỹ thuật giữ nguyên: React + Tailwind v4 (chỉ cần bổ sung token màu/font vào `tailwind.config.js`, không cần đổi framework).

---

## 5. Gợi ý thứ tự triển khai (khi bắt tay code)

1. Thêm design tokens vào Tailwind config + import font mới.
2. Làm lại `Header.jsx` + `Footer.jsx` (khung sườn ảnh hưởng toàn site trước).
3. Làm lại `HomePage.jsx` theo 8 section ở trên (phần tốn công nhất).
4. Làm lại `ProductCard.jsx`, `ProductDetailPage.jsx`.
5. Làm lại `CartPage.jsx` + checkout modal.
6. Rà lại `AddressBookPage`, `OrderHistoryPage`, trang admin — áp token màu mới nhưng giữ layout hiện tại (không ưu tiên đổi sâu vì ít người dùng ngắm).

Bạn xác nhận hướng này rồi mình bắt đầu code theo đúng thứ tự trên.
