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
    if (searchValue.trim()) {
      navigate(`/?q=${encodeURIComponent(searchValue)}`);
    } else {
      navigate(`/`);
    }
  };

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <header className="fixed top-0 left-0 right-0 z-50 bg-gradient-to-b from-indigo-600 to-indigo-700 shadow-md">
      {/* Top Navbar */}
      <div className="flex justify-between items-center max-w-7xl mx-auto px-4 py-1.5 text-[13px] text-indigo-100 font-medium">
        <div className="flex gap-4">
          <a href="#" className="hover:text-white transition-colors">Kênh Người Bán</a>
          <div className="w-px h-3 bg-indigo-400/50 self-center"></div>
          <a href="#" className="hover:text-white transition-colors">Trở thành Người bán MiniCommerce</a>
          <div className="w-px h-3 bg-indigo-400/50 self-center"></div>
          <a href="#" className="hover:text-white transition-colors">Tải ứng dụng</a>
          <div className="w-px h-3 bg-indigo-400/50 self-center"></div>
          <span className="flex gap-2 items-center">Kết nối</span>
        </div>
        <div className="flex gap-4 items-center">
          <a href="#" className="hover:text-white transition-colors flex items-center gap-1">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" /></svg>
            Thông báo
          </a>
          <a href="#" className="hover:text-white transition-colors flex items-center gap-1">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            Hỗ trợ
          </a>
          <a href="#" className="hover:text-white transition-colors flex items-center gap-1">
            🌐 Tiếng Việt
          </a>
          {user ? (
            <div className="relative">
              <button
                onClick={() => setShowMenu(!showMenu)}
                className="hover:text-white font-bold transition-colors flex items-center gap-2 px-3 py-1 rounded-lg hover:bg-indigo-500/30"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clipRule="evenodd" />
                </svg>
                <span>{user.fullName || user.username}</span>
              </button>
              {showMenu && (
                <div className="absolute right-0 mt-2 w-48 bg-white text-slate-900 rounded-lg shadow-lg overflow-hidden z-50">
                  <div className="px-4 py-3 border-b border-slate-100 font-semibold text-slate-700">
                    {user.fullName || user.username}
                  </div>
                  <Link
                    to="/orders"
                    onClick={() => setShowMenu(false)}
                    className="block px-4 py-2.5 hover:bg-slate-50 text-slate-700 font-medium transition-colors"
                  >
                    Đơn hàng của tôi
                  </Link>
                  <Link
                    to="/addresses"
                    onClick={() => setShowMenu(false)}
                    className="block px-4 py-2.5 hover:bg-slate-50 text-slate-700 font-medium transition-colors"
                  >
                    Sổ địa chỉ
                  </Link>
                  {isAdmin && (
                    <Link
                      to="/admin"
                      onClick={() => setShowMenu(false)}
                      className="block px-4 py-2.5 hover:bg-slate-50 text-indigo-600 font-medium transition-colors border-t border-slate-100"
                    >
                      Trang quản trị
                    </Link>
                  )}
                  <button
                    onClick={handleLogout}
                    className="w-full text-left px-4 py-2.5 hover:bg-red-50 text-red-600 font-medium transition-colors flex items-center gap-2 border-t border-slate-100"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                    </svg>
                    Đăng xuất
                  </button>
                </div>
              )}
            </div>
          ) : (
            <>
              <Link to="/register" className="hover:text-white font-bold transition-colors ml-2">Đăng ký</Link>
              <div className="w-px h-3 bg-indigo-400 self-center"></div>
              <Link to="/login" className="hover:text-white font-bold transition-colors">Đăng nhập</Link>
            </>
          )}
        </div>
      </div>

      {/* Main Header */}
      <div className="max-w-7xl mx-auto px-4 h-24 flex items-center gap-10">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-3 group shrink-0 mb-4">
          <div className="w-12 h-12 rounded-xl bg-white flex items-center justify-center text-indigo-600 font-extrabold text-3xl shadow-lg transition-transform group-hover:scale-105">
            E
          </div>
          <div className="flex flex-col justify-center">
            <span className="font-extrabold text-3xl text-white tracking-tight leading-none group-hover:text-indigo-100 transition-colors">
              MiniCommerce
            </span>
          </div>
        </Link>

        {/* Search Bar */}
        <div className="flex-1 flex flex-col justify-center relative top-[-4px]">
          <form onSubmit={handleSearch} className="flex bg-white rounded-sm p-1 shadow-inner h-11 w-full">
            <input
              type="text"
              value={searchValue}
              onChange={(e) => setSearchValue(e.target.value)}
              placeholder="MiniCommerce bao rẻ - Miễn phí vận chuyển"
              className="flex-1 px-3 outline-none text-slate-700 text-sm placeholder-slate-400"
            />
            <button type="submit" className="bg-indigo-600 hover:bg-indigo-700 w-16 flex items-center justify-center rounded-sm transition-colors shrink-0">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>
            </button>
          </form>
          {/* Quick links */}
          <div className="flex gap-4 text-[11px] text-indigo-100 mt-2 overflow-hidden whitespace-nowrap">
            <a href="#" className="hover:text-white">Áo thun</a>
            <a href="#" className="hover:text-white">Ốp lưng iPhone</a>
            <a href="#" className="hover:text-white">Tai nghe Bluetooth</a>
            <a href="#" className="hover:text-white">Quần áo nam</a>
            <a href="#" className="hover:text-white">Giày thể thao</a>
            <a href="#" className="hover:text-white">Đồng hồ</a>
          </div>
        </div>

        {/* Cart */}
        <div className="w-16 flex justify-center shrink-0 mb-4">
          <Link to="/cart" className="relative text-white hover:text-indigo-100 transition-colors">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" /></svg>
            {cartCount > 0 && (
              <span className="absolute -top-1 -right-2 bg-red-500 text-white text-[11px] font-bold px-1.5 rounded-full border-2 border-indigo-600 animate-pulse">
                {cartCount}
              </span>
            )}
            {cartCount === 0 && (
              <span className="absolute -top-1 -right-2 bg-slate-500/80 text-white text-[11px] font-bold px-1.5 rounded-full border-2 border-indigo-600">
                0
              </span>
            )}
          </Link>
        </div>
      </div>
    </header>
  );
}
