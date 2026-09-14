import { Link } from "react-router-dom";
import Logo from "./Logo";

// Chỉ link tới những trang THẬT SỰ tồn tại. Footer kiểu sàn thương mại điện tử
// hay có "Trung tâm trợ giúp", "Chính sách đổi trả"... nhưng dự án chưa có các
// trang đó — để link chết thì người dùng bấm vào chỉ gặp trang trống.
const CUSTOMER_LINKS = [
  { label: "Đơn hàng của tôi", to: "/orders" },
  { label: "Giỏ hàng", to: "/cart" },
  { label: "Sổ địa chỉ", to: "/addresses" },
  { label: "Đăng nhập", to: "/login" },
  { label: "Tạo tài khoản", to: "/register" },
];

// Khớp categoryId trong database và bộ lọc ở trang chủ (đọc ?category= từ URL).
const CATEGORY_LINKS = [
  { label: "Điện tử", id: "1" },
  { label: "Trang sức", id: "2" },
  { label: "Thời trang nam", id: "3" },
  { label: "Thời trang nữ", id: "4" },
  { label: "Phụ kiện", id: "5" },
];

const linkClass = "hover:text-white transition-colors";

function FooterHeading({ children, className = "" }) {
  return (
    <h3 className={`text-[11px] font-semibold uppercase tracking-[0.12em] text-white/80 mb-3 ${className}`}>
      {children}
    </h3>
  );
}

export default function Footer() {
  return (
    <footer className="bg-bg-dark border-t border-white/10 text-xs text-text-muted-dark">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6 pt-8 pb-5">
        <div className="grid grid-cols-2 md:grid-cols-12 gap-x-6 gap-y-7">
          <div className="col-span-2 md:col-span-3">
            <Link to="/" aria-label="Shopyora — Trang chủ" className="inline-block">
              <Logo size="sm" />
            </Link>
            <p className="mt-3 leading-relaxed max-w-[260px]">
              Mua sắm trực tuyến, thanh toán khi nhận hàng hoặc chuyển khoản qua mã QR — đơn được xác nhận tự động.
            </p>
          </div>

          <nav className="md:col-span-3" aria-label="Chăm sóc khách hàng">
            <FooterHeading>Chăm sóc khách hàng</FooterHeading>
            <ul className="space-y-2">
              {CUSTOMER_LINKS.map((link) => (
                <li key={link.to}>
                  <Link to={link.to} className={linkClass}>{link.label}</Link>
                </li>
              ))}
            </ul>
          </nav>

          <nav className="md:col-span-2" aria-label="Danh mục sản phẩm">
            <FooterHeading>Danh mục</FooterHeading>
            <ul className="space-y-2">
              {CATEGORY_LINKS.map((category) => (
                <li key={category.id}>
                  {/* scrollToProducts: footer nằm cuối trang, không cuộn lên thì
                      người dùng bấm xong vẫn đứng ở footer, tưởng link không chạy. */}
                  <Link to={`/?category=${category.id}`} state={{ scrollToProducts: true }} className={linkClass}>
                    {category.label}
                  </Link>
                </li>
              ))}
            </ul>
          </nav>

          <div className="md:col-span-2">
            <FooterHeading>Thanh toán</FooterHeading>
            <div className="flex flex-wrap gap-1.5">
              {["COD", "VietQR"].map((method) => (
                <span
                  key={method}
                  className="px-2 py-1 rounded border border-white/15 bg-white/[0.04] text-[11px] font-semibold text-white/80"
                >
                  {method}
                </span>
              ))}
            </div>

            <FooterHeading className="mt-5">Vận chuyển</FooterHeading>
            {/* Khớp STANDARD_SHIPPING_FEE / FREE_SHIPPING_THRESHOLD ở OrderService. */}
            <ul className="space-y-1.5">
              <li>Phí tiêu chuẩn 30.000₫</li>
              <li>Miễn phí đơn từ 500.000₫</li>
            </ul>
          </div>

          <div className="md:col-span-2">
            <FooterHeading>Theo dõi</FooterHeading>
            <a
              href="https://github.com/TruongVanHiep/ecommerce-platform"
              target="_blank"
              rel="noopener noreferrer"
              className={`inline-flex items-center gap-2 ${linkClass}`}
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                <path d="M15 22v-4a4.8 4.8 0 0 0-1-3.5c3 0 6-2 6-5.5.08-1.25-.27-2.48-1-3.5.28-1.15.28-2.35 0-3.5 0 0-1 0-3 1.5-2.64-.5-5.36-.5-8 0C6 2 5 2 5 2c-.3 1.15-.3 2.35 0 3.5A5.403 5.403 0 0 0 4 9c0 3.5 3 5.5 6 5.5-.39.49-.68 1.05-.85 1.65-.17.6-.22 1.23-.15 1.85v4" />
                <path d="M9 18c-4.51 2-5-2-7-2" />
              </svg>
              GitHub
            </a>
          </div>
        </div>

        <div className="mt-7 pt-4 border-t border-white/10 flex flex-col sm:flex-row sm:items-center justify-between gap-2 text-[11px]">
          {/* Thanh toán chuyển khoản ở đây là tiền THẬT (SePay). Ghi rõ đây là dự
              án demo để người vào xem không chuyển tiền rồi chờ hàng. */}
          <p>© {new Date().getFullYear()} Shopyora. Dự án demo phục vụ học tập — đơn hàng không được giao thật.</p>
          <p>Quốc gia &amp; khu vực: Việt Nam</p>
        </div>
      </div>
    </footer>
  );
}
