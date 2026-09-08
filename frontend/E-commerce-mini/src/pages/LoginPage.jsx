import { useState, useContext } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { login, decodeToken } from "../services/authService";
import { AuthContext } from "../context/AuthContext";

export default function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const location = useLocation();
  const [error, setError] = useState(location.state?.error || null);
  const navigate = useNavigate();
  const { login: contextLogin } = useContext(AuthContext);

  const handleGoogleLogin = () => {
    // Đường dẫn tương đối, KHÔNG hardcode host: nginx proxy đường này sang
    // backend (xem nginx.conf.template). Nhờ vậy chạy đúng ở cả local lẫn khi
    // deploy, nơi backend không có địa chỉ public để trỏ tới.
    window.location.href = "/oauth2/authorization/google";
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const response = await login({ username, password });
      console.log("Login Response:", response);
      
      const token = response?.result?.token;
      
      if (token) {
        // Decode token để lấy user info
        const tokenPayload = decodeToken(token);
        console.log("Token Payload:", tokenPayload);
        
        // Tạo user object từ token payload và input username
        const user = {
          username: username,
          fullName: username, // Nếu backend không cấp fullName, dùng username tạm thời
          authenticated: response?.result?.authenticated || true,
          ...tokenPayload // Merge token payload nếu có thông tin khác
        };
        
        console.log("User Object:", user);
        contextLogin(user, token, response?.result?.refreshToken);
        navigate("/");
      } else {
        console.warn("Response không có token:", response);
        setError(response?.message || "Sai tên đăng nhập hoặc mật khẩu. Vui lòng thử lại!");
      }
    } catch (err) {
      console.error("Login Error:", err);
      console.error("Error Response:", err.response?.data);

      const errorMessage =
        err.response?.data?.message ||
        err.message ||
        "Sai tên đăng nhập hoặc mật khẩu. Vui lòng thử lại!";

      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4 sm:p-6 lg:p-8">
      <div className="max-w-5xl w-full bg-white rounded-[2rem] shadow-2xl overflow-hidden flex flex-col md:flex-row min-h-[600px]">

        {/* Left Side: Graphic/Image */}
        <div className="md:w-1/2 relative bg-indigo-600 flex flex-col justify-between p-12 overflow-hidden hidden md:flex">
          {/* Abstract blobs */}
          <div className="absolute top-[-20%] left-[-10%] w-[70%] h-[70%] rounded-full bg-indigo-500/50 blur-3xl mix-blend-multiply" />
          <div className="absolute bottom-[-10%] right-[-10%] w-[60%] h-[60%] rounded-full bg-purple-500/50 blur-3xl mix-blend-multiply" />

          <div className="relative z-10">
            <Link to="/" className="flex items-center gap-2 mb-12 w-fit group">
              <div className="w-10 h-10 rounded-xl bg-white flex items-center justify-center text-indigo-600 font-extrabold text-2xl shadow-lg transition-transform group-hover:scale-105">
                E
              </div>
              <span className="font-bold text-2xl text-white tracking-tight">
                MiniCommerce
              </span>
            </Link>

            <h1 className="text-4xl lg:text-5xl font-extrabold text-white leading-tight mb-6">
              Mua sắm thả ga,<br />Không lo về giá.
            </h1>
            <p className="text-indigo-100 text-lg max-w-sm">
              Tham gia ngay cùng hàng triệu người dùng khác và tận hưởng hàng ngàn ưu đãi mỗi ngày.
            </p>
          </div>

          {/* Illustration/Pattern placeholder */}
          <div className="relative z-10 bg-white/10 backdrop-blur-md border border-white/20 rounded-2xl p-6 shadow-xl">
            <div className="flex items-center gap-4 mb-4">
              <div className="w-12 h-12 rounded-full bg-white/20 flex items-center justify-center">
                <span className="text-2xl">✨</span>
              </div>
              <div>
                <h4 className="text-white font-bold">Thành viên mới</h4>
                <p className="text-indigo-100 text-sm">Nhận ngay voucher 50k</p>
              </div>
            </div>
            <div className="w-full bg-white/20 h-2 rounded-full overflow-hidden">
              <div className="w-3/4 bg-white h-full rounded-full"></div>
            </div>
          </div>
        </div>

        {/* Right Side: Login Form */}
        <div className="md:w-1/2 flex items-center justify-center p-8 sm:p-12">
          <div className="w-full max-w-md animate-fade-in-up">
            <div className="text-center md:text-left mb-10">
              {/* Mobile logo only */}
              <Link to="/" className="flex items-center justify-center gap-2 mb-8 md:hidden">
                <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-indigo-600 to-purple-600 flex items-center justify-center text-white font-extrabold text-2xl shadow-lg">
                  E
                </div>
              </Link>
              <h2 className="text-3xl font-extrabold text-slate-900 mb-2">Chào mừng trở lại! 👋</h2>
              <p className="text-slate-500 font-medium">Vui lòng đăng nhập vào tài khoản của bạn.</p>
            </div>

            {error && (
              <div className="bg-red-50 text-red-600 p-4 rounded-xl mb-6 text-sm font-medium border border-red-100 flex items-center gap-3">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 shrink-0" viewBox="0 0 20 20" fill="currentColor"><path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" /></svg>
                {error}
              </div>
            )}

            <form onSubmit={handleLogin} className="space-y-5">
              <div>
                <label className="block text-sm font-semibold text-slate-700 mb-2">
                  Tên đăng nhập
                </label>
                <input
                  type="text"
                  placeholder="Nhập tên đăng nhập..."
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                  className="w-full px-5 py-3.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:bg-white focus:border-transparent transition-all font-medium text-slate-900 placeholder-slate-400"
                />
              </div>

              <div>
                <div className="flex justify-between items-center mb-2">
                  <label className="block text-sm font-semibold text-slate-700">
                    Mật khẩu
                  </label>
                  <a href="#" className="text-sm font-semibold text-indigo-600 hover:text-indigo-700">
                    Quên mật khẩu?
                  </a>
                </div>
                <input
                  type="password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  className="w-full px-5 py-3.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:bg-white focus:border-transparent transition-all font-medium text-slate-900 placeholder-slate-400"
                />
              </div>

              <div className="flex items-center gap-2 py-2">
                <input type="checkbox" id="remember" className="w-4 h-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500" />
                <label htmlFor="remember" className="text-sm font-medium text-slate-600 cursor-pointer">
                  Ghi nhớ đăng nhập
                </label>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full bg-slate-900 hover:bg-indigo-600 text-white font-bold py-4 px-4 rounded-xl transition-all shadow-lg hover:shadow-indigo-200 transform hover:-translate-y-0.5 active:translate-y-0 flex items-center justify-center gap-2 disabled:opacity-70 disabled:cursor-not-allowed disabled:transform-none"
              >
                {loading ? (
                  <>
                    <svg className="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
                    <span>Đang xử lý...</span>
                  </>
                ) : (
                  <span>Đăng nhập ngay</span>
                )}
              </button>
            </form>

            <div className="mt-8 flex items-center gap-4">
              <div className="flex-1 h-px bg-slate-200" />
              <span className="text-slate-400 text-sm font-medium">hoặc</span>
              <div className="flex-1 h-px bg-slate-200" />
            </div>

            <button
              type="button"
              onClick={handleGoogleLogin}
              className="mt-6 w-full flex items-center justify-center gap-3 border border-slate-200 hover:bg-slate-50 text-slate-700 font-semibold py-3.5 px-4 rounded-xl transition-all"
            >
              <svg className="h-5 w-5" viewBox="0 0 24 24">
                <path fill="#4285F4" d="M23.52 12.27c0-.85-.08-1.67-.22-2.45H12v4.64h6.47a5.54 5.54 0 0 1-2.4 3.63v3h3.89c2.27-2.09 3.56-5.17 3.56-8.82z"/>
                <path fill="#34A853" d="M12 24c3.24 0 5.95-1.07 7.93-2.91l-3.89-3c-1.08.72-2.46 1.15-4.04 1.15-3.1 0-5.73-2.09-6.67-4.9H1.32v3.09A12 12 0 0 0 12 24z"/>
                <path fill="#FBBC05" d="M5.33 14.34a7.2 7.2 0 0 1 0-4.68V6.57H1.32a12 12 0 0 0 0 10.86l4.01-3.09z"/>
                <path fill="#EA4335" d="M12 4.77c1.76 0 3.35.61 4.6 1.8l3.45-3.45C17.94 1.19 15.24 0 12 0 7.31 0 3.26 2.69 1.32 6.57l4.01 3.09C6.27 6.86 8.9 4.77 12 4.77z"/>
              </svg>
              Đăng nhập với Google
            </button>

            <div className="mt-8 pt-8 border-t border-slate-100 text-center">
              <p className="text-slate-500 font-medium">
                Chưa có tài khoản?{" "}
                <Link to="/register" className="text-indigo-600 font-bold hover:text-indigo-700">
                  Đăng ký ngay
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}