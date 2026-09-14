import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { useState, useContext } from "react";
import { AuthContext } from "../context/AuthContext";
import { CartContext } from "../context/CartContext";
import Logo from "./Logo";

const SearchIcon = ({ className = "h-4 w-4" }) => (
  <svg xmlns="http://www.w3.org/2000/svg" className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
  </svg>
);

export default function Header() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { user, logout } = useContext(AuthContext);
  const { cartCount } = useContext(CartContext);
  const [showMenu, setShowMenu] = useState(false);
  // Trên điện thoại ô tìm kiếm thu thành một nút, bấm vào mới mở ra thành hàng
  // riêng. Để ô tìm kiếm cố định trong header như trên máy tính thì nó chiếm
  // hết chiều ngang và đẩy giỏ hàng, nút đăng nhập ra ngoài màn hình.
  const [showMobileSearch, setShowMobileSearch] = useState(false);
  const isAdmin = user?.scope?.split(" ").includes("ADMIN");
  const currentQuery = searchParams.get("q") || "";

  const handleSearch = (e) => {
    e.preventDefault();
    const value = String(new FormData(e.currentTarget).get("q") || "").trim();
    setShowMobileSearch(false);
    // scrollToProducts: trang chủ cuộn thẳng tới kết quả thay vì đứng ở banner.
    navigate(value ? `/?q=${encodeURIComponent(value)}` : "/", { state: { scrollToProducts: Boolean(value) } });
  };

  // logout giờ là async vì phải gọi backend thu hồi refresh token trước khi
  // xoá phiên ở trình duyệt.
  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  // Dùng chung cho ô tìm kiếm trên máy tính và trên điện thoại. Input không kiểm
  // soát (defaultValue) và đổi key theo từ khoá trên URL: điều hướng sang từ khoá
  // khác thì ô tự điền lại, không cần đồng bộ state bằng effect.
  const renderSearchForm = (className, autoFocus = false) => (
    <form onSubmit={handleSearch} className={className} role="search">
      <div className="flex items-center h-11 rounded-full bg-white/[0.06] border border-white/10 focus-within:border-accent/60 transition-colors px-4">
        <SearchIcon className="h-4 w-4 text-white/40 shrink-0" />
        <input
          key={currentQuery}
          name="q"
          type="search"
          defaultValue={currentQuery}
          autoFocus={autoFocus}
          placeholder="Tìm sản phẩm..."
          aria-label="Tìm sản phẩm"
          className="flex-1 min-w-0 bg-transparent outline-none text-sm text-white placeholder-white/40 px-3"
        />
      </div>
    </form>
  );

  return (
    <header className="fixed top-0 left-0 right-0 z-50 bg-bg-dark/90 backdrop-blur-xl border-b border-white/10">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6 h-16 md:h-20 flex items-center gap-3 md:gap-8">
        <Link to="/" aria-label="Shopyora — Trang chủ" className="shrink-0 transition-opacity hover:opacity-90">
          <Logo size="sm" />
        </Link>

        {renderSearchForm("hidden md:block flex-1 max-w-md")}

        <div className="flex items-center gap-1 sm:gap-3 shrink-0 ml-auto">
          <button
            type="button"
            onClick={() => setShowMobileSearch((open) => !open)}
            aria-label="Tìm kiếm"
            aria-expanded={showMobileSearch}
            className="md:hidden w-10 h-10 rounded-full flex items-center justify-center text-white/80 hover:text-white hover:bg-white/10 transition-colors"
          >
            <SearchIcon className="h-5 w-5" />
          </button>

          {/* Cart */}
          <Link
            to="/cart"
            aria-label="Giỏ hàng"
            className="relative w-10 h-10 rounded-full flex items-center justify-center text-white/80 hover:text-white hover:bg-white/10 transition-colors"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
            {cartCount > 0 && (
              <span className="absolute -top-0.5 -right-0.5 bg-accent text-white text-[10px] font-semibold min-w-[18px] h-[18px] px-1 rounded-full flex items-center justify-center">
                {cartCount}
              </span>
            )}
          </Link>

          {user ? (
            <div className="relative">
              <button
                onClick={() => setShowMenu(!showMenu)}
                aria-label="Tài khoản"
                aria-expanded={showMenu}
                className="flex items-center gap-2 pl-1.5 pr-1.5 sm:pr-3 h-10 rounded-full bg-white/[0.06] border border-white/10 hover:border-white/20 transition-colors text-white text-sm font-medium"
              >
                <span className="w-7 h-7 rounded-full bg-accent/20 text-accent flex items-center justify-center text-xs font-semibold">
                  {(user.fullName || user.username || "U").charAt(0).toUpperCase()}
                </span>
                {/* Trên điện thoại chỉ hiện avatar: tên dài (vd một địa chỉ email)
                    đủ sức đẩy cả hàng header tràn khỏi màn hình. */}
                <span className="hidden sm:inline max-w-[160px] truncate">{user.fullName || user.username}</span>
              </button>
              {showMenu && (
                <div className="absolute right-0 mt-2 w-52 bg-bg-dark-soft border border-white/10 rounded-2xl overflow-hidden shadow-2xl">
                  <div className="px-4 py-3 border-b border-white/10 text-xs text-text-muted-dark uppercase tracking-wide truncate">
                    {user.fullName || user.username}
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
                className="h-9 sm:h-10 px-3 sm:px-4 flex items-center rounded-full border border-white/15 text-white text-sm font-medium hover:border-white/30 transition-colors whitespace-nowrap"
              >
                Đăng nhập
              </Link>
              {/* Ẩn trên màn nhỏ để không tràn — trang Đăng nhập đã có link Đăng ký. */}
              <Link
                to="/register"
                className="hidden sm:flex h-10 px-4 items-center rounded-full bg-accent text-white text-sm font-medium hover:bg-accent-dark transition-colors whitespace-nowrap"
              >
                Đăng ký
              </Link>
            </div>
          )}
        </div>
      </div>

      {showMobileSearch && (
        <div className="md:hidden border-t border-white/10 px-4 py-3">
          {renderSearchForm("w-full", true)}
        </div>
      )}
    </header>
  );
}
