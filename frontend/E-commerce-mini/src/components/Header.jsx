import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { useState, useEffect, useContext } from "react";
import { AuthContext } from "../context/AuthContext";
import { CartContext } from "../context/CartContext";

export default function Header() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [searchValue, setSearchValue] = useState(searchParams.get("q") || "");
  const { user, logout } = useContext(AuthContext);
  const { cartCount } = useContext(CartContext);
  const [showMenu, setShowMenu] = useState(false);
  const isAdmin = user?.scope?.split(" ").includes("ADMIN");

  useEffect(() => {
    setSearchValue(searchParams.get("q") || "");
  }, [searchParams]);

  const handleSearch = (e) => {
    e.preventDefault();
    navigate(searchValue.trim() ? `/?q=${encodeURIComponent(searchValue)}` : "/");
  };

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <header className="fixed top-0 left-0 right-0 z-50 bg-bg-dark/90 backdrop-blur-xl border-b border-white/10">
      <div className="max-w-[1200px] mx-auto px-6 h-20 flex items-center gap-8">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-2.5 shrink-0 group">
          <div className="w-9 h-9 rounded-xl bg-accent flex items-center justify-center text-white font-bold text-lg">
            M
          </div>
          <span className="font-semibold text-lg text-white tracking-tight">
            MiniCommerce
          </span>
        </Link>

        {/* Search */}
        <form onSubmit={handleSearch} className="flex-1 max-w-md">
          <div className="flex items-center h-11 rounded-full bg-white/[0.06] border border-white/10 focus-within:border-accent/60 transition-colors px-4">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-white/40 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input
              type="text"
              value={searchValue}
              onChange={(e) => setSearchValue(e.target.value)}
              placeholder="Tìm sản phẩm..."
              className="flex-1 bg-transparent outline-none text-sm text-white placeholder-white/40 px-3"
            />
          </div>
        </form>

        <div className="flex items-center gap-3 shrink-0 ml-auto">
          {/* Cart */}
          <Link
            to="/cart"
            className="relative w-10 h-10 rounded-full flex items-center justify-center text-white/80 hover:text-white hover:bg-white/10 transition-colors"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
            {cartCount > 0 && (
              <span className="absolute -top-0.5 -right-0.5 bg-accent text-white text-[10px] font-semibold w-4.5 h-4.5 min-w-[18px] px-1 rounded-full flex items-center justify-center">
                {cartCount}
              </span>
            )}
          </Link>

          {user ? (
            <div className="relative">
              <button
                onClick={() => setShowMenu(!showMenu)}
                className="flex items-center gap-2 pl-1.5 pr-3 h-10 rounded-full bg-white/[0.06] border border-white/10 hover:border-white/20 transition-colors text-white text-sm font-medium"
              >
                <span className="w-7 h-7 rounded-full bg-accent/20 text-accent flex items-center justify-center text-xs font-semibold">
                  {(user.fullName || user.username || "U").charAt(0).toUpperCase()}
                </span>
                {user.fullName || user.username}
              </button>
              {showMenu && (
                <div className="absolute right-0 mt-2 w-52 bg-bg-dark-soft border border-white/10 rounded-2xl overflow-hidden shadow-2xl">
                  <div className="px-4 py-3 border-b border-white/10 text-xs text-text-muted-dark uppercase tracking-wide">
                    Tài khoản
                  </div>
                  <Link to="/orders" onClick={() => setShowMenu(false)} className="block px-4 py-2.5 text-sm text-white/90 hover:bg-white/5 transition-colors">
                    Đơn hàng của tôi
                  </Link>
                  <Link to="/addresses" onClick={() => setShowMenu(false)} className="block px-4 py-2.5 text-sm text-white/90 hover:bg-white/5 transition-colors">
                    Sổ địa chỉ
                  </Link>
                  {isAdmin && (
                    <Link to="/admin" onClick={() => setShowMenu(false)} className="block px-4 py-2.5 text-sm text-accent hover:bg-white/5 transition-colors border-t border-white/10">
                      Trang quản trị
                    </Link>
                  )}
                  <button
                    onClick={handleLogout}
                    className="w-full text-left px-4 py-2.5 text-sm text-red-400 hover:bg-white/5 transition-colors border-t border-white/10"
                  >
                    Đăng xuất
                  </button>
                </div>
              )}
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Link
                to="/login"
                className="h-10 px-4 flex items-center rounded-full border border-white/15 text-white text-sm font-medium hover:border-white/30 transition-colors"
              >
                Đăng nhập
              </Link>
              <Link
                to="/register"
                className="h-10 px-4 flex items-center rounded-full bg-accent text-white text-sm font-medium hover:bg-accent-dark transition-colors"
              >
                Đăng ký
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
