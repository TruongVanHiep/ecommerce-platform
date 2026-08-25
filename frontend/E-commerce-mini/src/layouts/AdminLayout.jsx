import { NavLink, Outlet, Link } from "react-router-dom";

const navItems = [
  { to: "/admin/products", label: "Sản phẩm", icon: "📦" },
  { to: "/admin/orders", label: "Đơn hàng", icon: "🧾" },
  { to: "/admin/vouchers", label: "Voucher", icon: "🎟️" },
];

export default function AdminLayout() {
  return (
    <div className="min-h-screen bg-bg-light flex">
      <aside className="w-60 shrink-0 bg-bg-dark text-white min-h-screen p-5">
        <Link to="/" className="flex items-center gap-2 mb-8">
          <div className="w-9 h-9 rounded-xl bg-accent flex items-center justify-center text-white font-bold text-lg">
            M
          </div>
          <span className="font-semibold text-lg">Admin</span>
        </Link>
        <nav className="space-y-1">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-semibold transition-colors ${
                  isActive ? "bg-accent text-white" : "text-white/60 hover:bg-white/5"
                }`
              }
            >
              <span>{item.icon}</span>
              {item.label}
            </NavLink>
          ))}
        </nav>
        <Link
          to="/"
          className="flex items-center gap-3 px-3 py-2.5 mt-8 rounded-xl text-sm font-semibold text-white/40 hover:bg-white/5 transition-colors"
        >
          ← Về trang chủ
        </Link>
      </aside>
      <main className="flex-1 p-8 min-w-0">
        <Outlet />
      </main>
    </div>
  );
}
