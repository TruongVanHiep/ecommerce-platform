import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../services/authService";

export default function RegisterPage() {
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    fullName: "",
    dob: "",
    phone: "",
    address: "",
  });
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    // Validation
    if (formData.username.length < 8) {
      setError("Tên đăng nhập phải có ít nhất 8 ký tự!");
      setLoading(false);
      return;
    }

    if (formData.password.length < 8) {
      setError("Mật khẩu phải có ít nhất 8 ký tự!");
      setLoading(false);
      return;
    }
    
    if (!formData.email) {
      setError("Vui lòng nhập email!");
      setLoading(false);
      return;
    }
    
    if (!formData.fullName) {
      setError("Vui lòng nhập họ tên!");
      setLoading(false);
      return;
    }

    if (!formData.dob) {
      setError("Vui lòng chọn ngày sinh!");
      setLoading(false);
      return;
    }

    // Check if user is at least 18 years old
    const birthDate = new Date(formData.dob);
    const today = new Date();
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    if (age < 18) {
      setError("Bạn phải đủ 18 tuổi để đăng ký!");
      setLoading(false);
      return;
    }

    try {
      const response = await register({
        username: formData.username,
        email: formData.email,
        password: formData.password,
        fullName: formData.fullName,
        dob: formData.dob,
        phone: formData.phone,
        address: formData.address,
        // KHÔNG gửi `roles`: backend cố tình bỏ trường này khỏi
        // UserRegisterRequest để client không tự chọn được vai trò, và luôn
        // gán cứng USER. Trước đây frontend vẫn gửi `roles: ["USER"]` — không
        // gây lỗi vì Jackson bỏ qua trường lạ, nhưng để lại dễ khiến người đọc
        // tưởng vai trò do client quyết định.
      });

      if (response?.result) {
        setSuccess(true);
        setTimeout(() => {
          navigate("/login");
        }, 2000);
      } else {
        throw new Error("Invalid response");
      }
    } catch (err) {
      console.error(err);
      setError(
        err.response?.data?.message || "Đăng ký thất bại. Vui lòng thử lại!"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4 sm:p-6 lg:p-8">
      <div className="max-w-5xl w-full bg-white rounded-[2rem] shadow-2xl overflow-hidden flex flex-col md:flex-row min-h-screen md:min-h-[700px]">
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
              Gia nhập cộng đồng<br />
              của chúng tôi!
            </h1>
            <p className="text-indigo-100 text-lg max-w-sm">
              Tạo tài khoản ngay và bắt đầu khám phá hàng ngàn sản phẩm tuyệt vời với giá cực tốt.
            </p>
          </div>

          {/* Illustration/Pattern placeholder */}
          <div className="relative z-10 bg-white/10 backdrop-blur-md border border-white/20 rounded-2xl p-6 shadow-xl">
            <div className="flex items-center gap-4 mb-4">
              <div className="w-12 h-12 rounded-full bg-white/20 flex items-center justify-center">
                <span className="text-2xl">🎁</span>
              </div>
              <div>
                <h4 className="text-white font-bold">Khuyến mãi đặc biệt</h4>
                <p className="text-indigo-100 text-sm">Voucher 50k cho thành viên mới</p>
              </div>
            </div>
            <div className="w-full bg-white/20 h-2 rounded-full overflow-hidden">
              <div className="w-3/4 bg-white h-full rounded-full"></div>
            </div>
          </div>
        </div>

        {/* Right Side: Register Form */}
        <div className="md:w-1/2 flex items-center justify-center p-6 sm:p-8 md:p-12 overflow-y-auto">
          <div className="w-full max-w-md">
            <div className="text-center md:text-left mb-8">
              {/* Mobile logo only */}
              <Link to="/" className="flex items-center justify-center gap-2 mb-8 md:hidden">
                <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-indigo-600 to-purple-600 flex items-center justify-center text-white font-extrabold text-2xl shadow-lg">
                  E
                </div>
              </Link>
              <h2 className="text-3xl font-extrabold text-slate-900 mb-2">
                Đăng ký tài khoản 🚀
              </h2>
              <p className="text-slate-500 font-medium">
                Điền thông tin để bắt đầu mua sắm.
              </p>
            </div>

            {error && (
              <div className="bg-red-50 text-red-600 p-4 rounded-xl mb-6 text-sm font-medium border border-red-100 flex items-center gap-3">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-5 w-5 shrink-0"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    fillRule="evenodd"
                    d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"
                    clipRule="evenodd"
                  />
                </svg>
                {error}
              </div>
            )}

            {success && (
              <div className="bg-green-50 text-green-600 p-4 rounded-xl mb-6 text-sm font-medium border border-green-100 flex items-center gap-3">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-5 w-5 shrink-0"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    fillRule="evenodd"
                    d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                    clipRule="evenodd"
                  />
                </svg>
                Đăng ký thành công! Chuyển hướng tới trang đăng nhập...
              </div>
            )}

            <form onSubmit={handleRegister} className="space-y-4">
              {/* Username */}
              <div>
                <label className="block text-sm font-semibold text-slate-700 mb-2">
                  Tên đăng nhập *
                </label>
                <input
                  type="text"
                  name="username"
                  value={formData.username}
                  onChange={handleChange}
                  placeholder="Tối thiểu 8 ký tự"
                  className="w-full px-4 py-3 rounded-xl border border-slate-300 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 outline-none transition text-slate-700"
                  disabled={loading}
                />
              </div>

              {/* Email */}
              <div>
                <label className="block text-sm font-semibold text-slate-700 mb-2">
                  Email *
                </label>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="example@email.com"
                  className="w-full px-4 py-3 rounded-xl border border-slate-300 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 outline-none transition text-slate-700"
                  disabled={loading}
                />
              </div>

              {/* Full Name */}
              <div>
                <label className="block text-sm font-semibold text-slate-700 mb-2">
                  Họ và tên *
                </label>
                <input
                  type="text"
                  name="fullName"
                  value={formData.fullName}
                  onChange={handleChange}
                  placeholder="Nhập họ tên của bạn"
                  className="w-full px-4 py-3 rounded-xl border border-slate-300 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 outline-none transition text-slate-700"
                  disabled={loading}
                />
              </div>

              {/* Password */}
              <div>
                <label className="block text-sm font-semibold text-slate-700 mb-2">
                  Mật khẩu *
                </label>
                <input
                  type="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="Tối thiểu 8 ký tự"
                  className="w-full px-4 py-3 rounded-xl border border-slate-300 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 outline-none transition text-slate-700"
                  disabled={loading}
                />
              </div>

              {/* Date of Birth */}
              <div>
                <label className="block text-sm font-semibold text-slate-700 mb-2">
                  Ngày sinh (Phải ≥ 18 tuổi) *
                </label>
                <input
                  type="date"
                  name="dob"
                  value={formData.dob}
                  onChange={handleChange}
                  className="w-full px-4 py-3 rounded-xl border border-slate-300 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 outline-none transition text-slate-700"
                  disabled={loading}
                />
              </div>

              {/* Phone */}
              <div>
                <label className="block text-sm font-semibold text-slate-700 mb-2">
                  Số điện thoại
                </label>
                <input
                  type="tel"
                  name="phone"
                  value={formData.phone}
                  onChange={handleChange}
                  placeholder="0123456789"
                  className="w-full px-4 py-3 rounded-xl border border-slate-300 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 outline-none transition text-slate-700"
                  disabled={loading}
                />
              </div>

              {/* Address */}
              <div>
                <label className="block text-sm font-semibold text-slate-700 mb-2">
                  Địa chỉ
                </label>
                <textarea
                  name="address"
                  value={formData.address}
                  onChange={handleChange}
                  placeholder="Nhập địa chỉ của bạn"
                  rows="3"
                  className="w-full px-4 py-3 rounded-xl border border-slate-300 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 outline-none transition text-slate-700 resize-none"
                  disabled={loading}
                />
              </div>

              {/* Submit Button */}
              <button
                type="submit"
                disabled={loading}
                className="w-full bg-gradient-to-r from-indigo-600 to-indigo-700 hover:from-indigo-700 hover:to-indigo-800 text-white font-bold py-3 rounded-xl transition-all duration-200 flex items-center justify-center gap-2 mt-6 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? (
                  <>
                    <svg className="animate-spin h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Đang đăng ký...
                  </>
                ) : (
                  "Đăng ký"
                )}
              </button>

              {/* Login Link */}
              <p className="text-center text-slate-600 text-sm mt-4">
                Đã có tài khoản?{" "}
                <Link
                  to="/login"
                  className="text-indigo-600 font-semibold hover:text-indigo-700 transition-colors"
                >
                  Đăng nhập ngay
                </Link>
              </p>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
