import { NavLink, Outlet, Link } from "react-router-dom";

const navItems = [
  { to: "/admin/products", label: "Sản phẩm", icon: "📦" },
  { to: "/admin/orders", label: "Đơn hàng", icon: "🧾" },
  { to: "/admin/vouchers", label: "Voucher", icon: "🎟️" },
];

export default function AdminLayout() {
  return (
    <div className="min-h-screen bg-[#f5f5f5] flex">
      <aside className="w-60 shrink-0 bg-slate-900 text-white min-h-screen p-5">
        <Link to="/" className="flex items-center gap-2 mb-8">
          <div className="w-9 h-9 rounded-lg bg-white flex items-center justify-center text-indigo-600 font-extrabold text-xl">
            E
          </div>
          <span className="font-bold text-lg">Admin</span>
        </Link>
        <nav className="space-y-1">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-semibold transition-colors ${
                  isActive ? "bg-indigo-600 text-white" : "text-slate-300 hover:bg-slate-800"
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
          className="flex items-center gap-3 px-3 py-2.5 mt-8 rounded-lg text-sm font-semibold text-slate-400 hover:bg-slate-800 transition-colors"
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
