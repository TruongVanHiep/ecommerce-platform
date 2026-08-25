import { useContext, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { CartContext } from "../context/CartContext";
import { AuthContext } from "../context/AuthContext";
import { formatVND } from "../lib/formatCurrency";
import { applyVoucher } from "../services/voucherService";
import { createOrder } from "../services/orderService";
import { createPayment } from "../services/paymentService";

const FREE_SHIPPING_THRESHOLD = 500000;
const STANDARD_SHIPPING_FEE = 30000;

export default function CartPage() {
  const { cartItems, removeFromCart, updateQuantity, cartTotal, refreshCart, triggerToast } =
    useContext(CartContext);
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();

  // Voucher state
  const [couponCode, setCouponCode] = useState("");
  const [appliedVoucher, setAppliedVoucher] = useState(null); // { code, discountAmount }
  const [couponError, setCouponError] = useState("");
  const [couponLoading, setCouponLoading] = useState(false);

  // Checkout modal state
  const [showCheckoutModal, setShowCheckoutModal] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [placingOrder, setPlacingOrder] = useState(false);
  const [orderError, setOrderError] = useState("");
  const [completedOrder, setCompletedOrder] = useState(null);
  const [shippingInfo, setShippingInfo] = useState({
    fullName: user?.fullName || user?.username || "",
    phone: "",
    address: "",
    paymentMethod: "COD",
  });
  const [formErrors, setFormErrors] = useState({});
  const [idempotencyKey, setIdempotencyKey] = useState(null);

  const discountAmount = appliedVoucher?.discountAmount || 0;
  const isFreeShipping = cartTotal - discountAmount >= FREE_SHIPPING_THRESHOLD;
  const shippingFee = cartTotal > 0 && !isFreeShipping ? STANDARD_SHIPPING_FEE : 0;
  const grandTotal = Math.max(0, cartTotal - discountAmount + shippingFee);

  const handleApplyCoupon = async (e) => {
    e.preventDefault();
    setCouponError("");

    if (!couponCode.trim()) {
      setCouponError("Vui lòng nhập mã giảm giá.");
      return;
    }

    setCouponLoading(true);
    try {
      const res = await applyVoucher(couponCode.trim().toUpperCase(), cartTotal);
      setAppliedVoucher(res.result);
      triggerToast(`Đã áp dụng mã ${res.result.code}!`);
      setCouponCode("");
    } catch (err) {
      setCouponError(err.response?.data?.message || "Mã giảm giá không hợp lệ hoặc đã hết hạn.");
    } finally {
      setCouponLoading(false);
    }
  };

  const handleRemoveCoupon = () => {
    setAppliedVoucher(null);
    triggerToast("Đã gỡ bỏ mã giảm giá.", "info");
  };

  const handleCheckoutClick = () => {
    if (cartItems.length === 0) return;
    setOrderError("");
    // 1 key cho mỗi lần mở modal checkout — giữ nguyên qua các lần bấm "Xác nhận" bị lỗi/retry,
    // để backend nhận biết đó là cùng 1 lần đặt hàng chứ không phải đơn mới.
    setIdempotencyKey(crypto.randomUUID());
    setShowCheckoutModal(true);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setShippingInfo((prev) => ({ ...prev, [name]: value }));
    if (formErrors[name]) {
      setFormErrors((prev) => ({ ...prev, [name]: "" }));
    }
  };

  const validateForm = () => {
    const errors = {};
    if (!shippingInfo.fullName.trim()) errors.fullName = "Họ và tên là bắt buộc.";
    if (!shippingInfo.phone.trim()) {
      errors.phone = "Số điện thoại là bắt buộc.";
    } else if (!/^(0[3|5|7|8|9])+([0-9]{8})$/.test(shippingInfo.phone.trim())) {
      errors.phone = "Số điện thoại không đúng định dạng.";
    }
    if (!shippingInfo.address.trim()) errors.address = "Địa chỉ nhận hàng là bắt buộc.";
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleConfirmOrder = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setPlacingOrder(true);
    setOrderError("");
    try {
      const fullShippingAddress = `${shippingInfo.fullName} - ${shippingInfo.address}`;
      const orderRes = await createOrder({
        shippingAddress: fullShippingAddress,
        phone: shippingInfo.phone,
        voucherCode: appliedVoucher?.code || null,
        idempotencyKey,
      });
      const order = orderRes.result;

      await createPayment(order.orderId, shippingInfo.paymentMethod);

      setCompletedOrder(order);
      setShowCheckoutModal(false);
      setShowSuccessModal(true);
      setAppliedVoucher(null);
      setIdempotencyKey(null); // lần đặt hàng tiếp theo phải là 1 key mới
      await refreshCart();
    } catch (err) {
      console.error("Checkout error:", err);
      setOrderError(err.response?.data?.message || "Đặt hàng thất bại. Vui lòng thử lại.");
    } finally {
      setPlacingOrder(false);
    }
  };

  const handleFinishSuccess = () => {
    setShowSuccessModal(false);
    setCompletedOrder(null);
    navigate("/");
  };

  return (
    <div className="min-h-screen bg-bg-light pb-32 pt-6">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6">
        {/* Breadcrumb */}
        <div className="mb-4 text-xs text-slate-500 flex items-center gap-1.5">
          <Link to="/" className="hover:text-accent transition-colors">
            Trang chủ
          </Link>
          <span>/</span>
          <span className="text-slate-700">Giỏ hàng</span>
        </div>

        {cartItems.length === 0 ? (
          <div className="bg-white rounded-2xl border border-slate-200/70 text-center py-20 px-4 max-w-lg mx-auto animate-fade-in-up mt-10">
            <div className="w-24 h-24 bg-accent/10 text-accent rounded-full flex items-center justify-center mx-auto mb-6">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
            </div>
            <h2 className="text-lg font-bold text-slate-800 mb-2">Giỏ hàng của bạn còn trống</h2>
            <p className="text-slate-400 text-xs mb-6 max-w-xs mx-auto">
              Hãy bấm vào nút bên dưới để chọn ngay các sản phẩm ưng ý nhất từ cửa hàng của chúng tôi!
            </p>
            <Link
              to="/"
              className="inline-flex items-center justify-center px-6 py-2.5 bg-accent hover:bg-accent-dark text-white font-bold text-sm rounded-full transition-all"
            >
              MUA SẮM NGAY
            </Link>
          </div>
        ) : (
          <div className="space-y-4">
            {/* Items List */}
            <div className="bg-white rounded-2xl overflow-hidden border border-slate-200/70">
              <div className="p-4 border-b border-slate-100 font-bold text-slate-800 text-sm">
                Giỏ hàng của bạn ({cartItems.length} sản phẩm)
              </div>
              <div className="divide-y divide-slate-100">
                {cartItems.map((item) => (
                  <div
                    key={item.cartItemId}
                    className="p-4 grid grid-cols-1 md:grid-cols-12 gap-4 items-center text-xs sm:text-sm"
                  >
                    <div className="col-span-6 flex gap-3 items-center">
                      <div className="w-16 h-16 shrink-0 bg-slate-50 border border-slate-100 rounded-xl p-1 flex items-center justify-center">
                        <img
                          src={item.image || "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?q=80&w=300&auto=format&fit=crop"}
                          alt={item.productName}
                          className="w-full h-full object-contain mix-blend-multiply"
                        />
                      </div>
                      <Link
                        to={`/products/${item.productId}`}
                        className="font-bold text-slate-800 hover:text-accent transition-colors line-clamp-2 leading-tight max-w-sm"
                      >
                        {item.productName}
                      </Link>
                    </div>

                    <div className="col-span-2 text-left md:text-center">
                      <span className="text-slate-400 md:hidden font-semibold mr-2">Đơn giá:</span>
                      <span className="font-bold text-slate-700">{formatVND(item.unitPrice)}</span>
                    </div>

                    <div className="col-span-2 flex md:justify-center items-center justify-between">
                      <span className="text-slate-400 md:hidden font-semibold">Số lượng</span>
                      <div className="flex items-center border border-slate-200 rounded-full overflow-hidden">
                        <button
                          type="button"
                          onClick={() => updateQuantity(item.cartItemId, item.quantity - 1)}
                          disabled={item.quantity <= 1}
                          className="w-7 h-7 bg-slate-50 hover:bg-slate-100 text-slate-600 disabled:text-slate-300 transition-colors text-sm font-semibold"
                        >
                          -
                        </button>
                        <span className="w-8 text-center text-xs font-bold text-slate-800">
                          {item.quantity}
                        </span>
                        <button
                          type="button"
                          onClick={() => updateQuantity(item.cartItemId, item.quantity + 1)}
                          className="w-7 h-7 bg-slate-50 hover:bg-slate-100 text-slate-600 transition-colors text-sm font-semibold"
                        >
                          +
                        </button>
                      </div>
                    </div>

                    <div className="col-span-1 text-right">
                      <span className="text-slate-400 md:hidden font-semibold mr-2">Thành tiền:</span>
                      <span className="font-bold text-accent">{formatVND(item.subTotal)}</span>
                    </div>

                    <div className="col-span-1 text-right md:text-center">
                      <button
                        type="button"
                        onClick={() => removeFromCart(item.cartItemId)}
                        className="text-slate-500 hover:text-red-500 transition-colors text-xs font-medium cursor-pointer"
                      >
                        Xóa
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Voucher and Billing Summary */}
            <div className="bg-white p-6 rounded-2xl border border-slate-200/70 grid md:grid-cols-12 gap-6 items-start">
              <div className="md:col-span-7 space-y-4">
                <div className="flex items-center gap-2 border-b border-slate-100 pb-3">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-accent" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v13m0-13V6a2 2 0 112 2h-2zm0 0V5.5A2.5 2.5 0 109.5 8H12zm-7 4h14M5 12a2 2 0 110-4h14a2 2 0 110 4M5 12v7a2 2 0 002 2h10a2 2 0 002-2v-7" />
                  </svg>
                  <span className="font-bold text-slate-800 text-sm">MiniCommerce Voucher</span>
                </div>

                {appliedVoucher ? (
                  <div className="flex items-center justify-between bg-accent/10 border border-accent/30 px-4 py-3 rounded-lg max-w-md">
                    <div className="flex items-center gap-3">
                      <div className="w-9 h-9 bg-accent rounded-lg flex items-center justify-center text-white font-extrabold text-[10px] uppercase shrink-0">
                        Vé
                      </div>
                      <div>
                        <span className="text-[10px] font-bold text-accent uppercase block">Đang áp dụng</span>
                        <span className="font-extrabold text-accent text-sm">{appliedVoucher.code}</span>
                        <span className="text-xs text-accent-dark block mt-0.5">
                          Giảm {formatVND(appliedVoucher.discountAmount)}
                        </span>
                      </div>
                    </div>
                    <button
                      type="button"
                      onClick={handleRemoveCoupon}
                      className="text-slate-400 hover:text-slate-600 transition-colors p-1"
                      aria-label="Hủy áp dụng"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </button>
                  </div>
                ) : (
                  <form onSubmit={handleApplyCoupon} className="flex gap-2 max-w-md">
                    <input
                      type="text"
                      placeholder="Nhập mã voucher..."
                      value={couponCode}
                      onChange={(e) => {
                        setCouponCode(e.target.value);
                        setCouponError("");
                      }}
                      className={`flex-1 px-3 py-2 text-xs border rounded-xl outline-none placeholder-slate-400 transition-all ${
                        couponError ? "border-red-500 bg-red-50/20" : "border-slate-200 focus:border-accent"
                      }`}
                    />
                    <button
                      type="submit"
                      disabled={couponLoading}
                      className="bg-slate-900 hover:bg-accent text-white font-bold text-xs px-4 rounded-full transition-colors disabled:opacity-60"
                    >
                      {couponLoading ? "Đang kiểm tra..." : "Áp dụng"}
                    </button>
                  </form>
                )}

                {couponError && (
                  <p className="text-red-500 text-xs font-semibold">{couponError}</p>
                )}
              </div>

              {/* Bill Details */}
              <div className="md:col-span-5 bg-slate-50 p-4 rounded-2xl border border-slate-200/50 space-y-3.5 text-xs text-slate-600">
                <span className="font-extrabold text-slate-800 text-sm block border-b border-slate-200/80 pb-2">Chi tiết thanh toán</span>
                <div className="flex justify-between">
                  <span>Tổng tiền hàng</span>
                  <span className="font-semibold text-slate-800">{formatVND(cartTotal)}</span>
                </div>
                <div className="flex justify-between">
                  <span>Phí vận chuyển</span>
                  <span className={`font-semibold ${isFreeShipping ? "text-emerald-600" : "text-slate-800"}`}>
                    {isFreeShipping ? "Miễn phí" : formatVND(STANDARD_SHIPPING_FEE)}
                  </span>
                </div>
                {discountAmount > 0 && (
                  <div className="flex justify-between text-accent font-bold">
                    <span>Voucher giảm giá</span>
                    <span>-{formatVND(discountAmount)}</span>
                  </div>
                )}
                <div className="border-t border-slate-200 pt-3 flex justify-between items-center text-sm font-bold text-slate-800">
                  <span>Tổng thanh toán</span>
                  <span className="text-lg text-accent font-black">{formatVND(grandTotal)}</span>
                </div>
              </div>
            </div>

            {/* STICKY BOTTOM CHECKOUT BAR */}
            <div className="fixed bottom-0 left-0 right-0 z-40 bg-white border-t border-slate-200 shadow-[0_-5px_15px_rgba(0,0,0,0.06)] py-4">
              <div className="max-w-[1200px] mx-auto px-4 sm:px-6 flex flex-col sm:flex-row justify-between items-center gap-3">
                <span className="text-xs text-slate-500 font-medium">
                  {cartItems.length} sản phẩm trong giỏ hàng
                </span>
                <div className="flex items-center gap-6">
                  <div className="text-right text-xs">
                    <div className="flex items-baseline justify-end gap-1.5">
                      <span className="text-slate-600">Tổng thanh toán:</span>
                      <span className="text-xl font-black text-accent">{formatVND(grandTotal)}</span>
                    </div>
                  </div>
                  <button
                    type="button"
                    onClick={handleCheckoutClick}
                    className="bg-accent hover:bg-accent-dark text-white font-bold text-sm px-10 py-3 rounded-full transition-all transform active:scale-[0.98]"
                  >
                    Mua Hàng
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* CHECKOUT INFORMATION MODAL */}
      {showCheckoutModal && (
        <div className="fixed inset-0 z-50 overflow-y-auto flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4 animate-fade-in">
          <div className="bg-white rounded-[24px] w-full max-w-lg border border-slate-200 overflow-hidden flex flex-col max-h-[90vh]">
            <div className="p-5 border-b border-slate-100 flex justify-between items-center bg-slate-50">
              <div>
                <h3 className="font-bold text-slate-900 text-base">Thông tin nhận hàng</h3>
                <p className="text-slate-400 text-xs mt-0.5">Địa chỉ giao hàng & thanh toán cho đơn hàng của bạn</p>
              </div>
              <button
                type="button"
                onClick={() => setShowCheckoutModal(false)}
                className="text-slate-400 hover:text-slate-600 p-1.5 rounded-full hover:bg-slate-100 transition-colors"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <form onSubmit={handleConfirmOrder} className="flex-grow overflow-y-auto p-5 space-y-4 text-xs sm:text-sm">
              {orderError && (
                <div className="bg-red-50 text-red-600 p-3 rounded-lg text-xs font-medium border border-red-100">
                  {orderError}
                </div>
              )}

              <div>
                <label htmlFor="fullName" className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">Tên người nhận</label>
                <input
                  type="text"
                  id="fullName"
                  name="fullName"
                  value={shippingInfo.fullName}
                  onChange={handleInputChange}
                  placeholder="Họ và tên người nhận"
                  className={`w-full px-3 py-2.5 border rounded-xl text-xs outline-none transition-all ${
                    formErrors.fullName ? "border-red-500 bg-red-50/10" : "border-slate-200 focus:border-accent"
                  }`}
                />
                {formErrors.fullName && <p className="text-red-500 text-[10px] font-semibold mt-1">{formErrors.fullName}</p>}
              </div>

              <div>
                <label htmlFor="phone" className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">Số điện thoại</label>
                <input
                  type="text"
                  id="phone"
                  name="phone"
                  value={shippingInfo.phone}
                  onChange={handleInputChange}
                  placeholder="Số điện thoại di động"
                  className={`w-full px-3 py-2.5 border rounded-xl text-xs outline-none transition-all ${
                    formErrors.phone ? "border-red-500 bg-red-50/10" : "border-slate-200 focus:border-accent"
                  }`}
                />
                {formErrors.phone && <p className="text-red-500 text-[10px] font-semibold mt-1">{formErrors.phone}</p>}
              </div>

              <div>
                <label htmlFor="address" className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">Địa chỉ giao hàng</label>
                <textarea
                  id="address"
                  name="address"
                  rows="3"
                  value={shippingInfo.address}
                  onChange={handleInputChange}
                  placeholder="Địa chỉ chi tiết (Số nhà, Tên đường, Phường/Xã, Quận/Huyện, Tỉnh/Thành phố)"
                  className={`w-full px-3 py-2.5 border rounded-xl text-xs outline-none transition-all resize-none ${
                    formErrors.address ? "border-red-500 bg-red-50/10" : "border-slate-200 focus:border-accent"
                  }`}
                />
                {formErrors.address && <p className="text-red-500 text-[10px] font-semibold mt-1">{formErrors.address}</p>}
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">Phương thức thanh toán</label>
                <div className="grid grid-cols-2 gap-3">
                  <label className={`border rounded-xl p-3 flex items-center gap-2.5 cursor-pointer transition-all ${
                    shippingInfo.paymentMethod === "COD" ? "border-accent bg-accent/5" : "border-slate-200 hover:bg-slate-50"
                  }`}>
                    <input
                      type="radio"
                      name="paymentMethod"
                      value="COD"
                      checked={shippingInfo.paymentMethod === "COD"}
                      onChange={handleInputChange}
                      className="accent-accent"
                    />
                    <div className="text-[11px]">
                      <span className="block font-bold text-slate-800">Thanh toán COD</span>
                      <span className="text-[9px] text-slate-400 block mt-0.5">Trả tiền khi nhận hàng</span>
                    </div>
                  </label>

                  <label className={`border rounded-xl p-3 flex items-center gap-2.5 cursor-pointer transition-all ${
                    shippingInfo.paymentMethod === "VNPAY" ? "border-accent bg-accent/5" : "border-slate-200 hover:bg-slate-50"
                  }`}>
                    <input
                      type="radio"
                      name="paymentMethod"
                      value="VNPAY"
                      checked={shippingInfo.paymentMethod === "VNPAY"}
                      onChange={handleInputChange}
                      className="accent-accent"
                    />
                    <div className="text-[11px]">
                      <span className="block font-bold text-slate-800">VNPAY</span>
                      <span className="text-[9px] text-slate-400 block mt-0.5">Chờ tích hợp cổng thanh toán</span>
                    </div>
                  </label>
                </div>
              </div>

              <div className="bg-slate-50 p-3.5 rounded-xl border border-slate-100 flex justify-between items-center font-bold text-slate-700 mt-6">
                <span>Tổng thanh toán:</span>
                <span className="text-base text-accent">{formatVND(grandTotal)}</span>
              </div>

              <div className="flex gap-3 border-t border-slate-100 pt-4 mt-6">
                <button
                  type="button"
                  onClick={() => setShowCheckoutModal(false)}
                  disabled={placingOrder}
                  className="flex-1 py-2.5 border border-slate-200 hover:bg-slate-50 text-slate-600 font-bold rounded-full transition-colors disabled:opacity-60"
                >
                  Trở lại
                </button>
                <button
                  type="submit"
                  disabled={placingOrder}
                  className="flex-1 py-2.5 bg-accent hover:bg-accent-dark text-white font-bold rounded-full transition-colors disabled:opacity-60"
                >
                  {placingOrder ? "Đang xử lý..." : "Xác nhận đặt hàng"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* SUCCESS CONFIRMATION MODAL */}
      {showSuccessModal && completedOrder && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4 animate-fade-in">
          <div className="bg-white rounded-[24px] w-full max-w-sm p-6 text-center border border-slate-200 animate-scale-up">
            <div className="w-16 h-16 bg-emerald-50 text-emerald-500 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
              </svg>
            </div>

            <h3 className="text-xl font-bold text-slate-900 mb-1">Đặt Hàng Thành Công!</h3>
            <p className="text-[11px] text-slate-400 mb-5">
              Mã đơn hàng: <strong className="text-accent">#{completedOrder.orderId}</strong>
            </p>

            <div className="bg-slate-50 rounded-xl p-4 text-left text-xs space-y-2 mb-6 border border-slate-100">
              <div className="flex justify-between">
                <span className="text-slate-400">Số điện thoại:</span>
                <span className="font-bold text-slate-800">{completedOrder.phone}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Địa chỉ giao hàng:</span>
                <span className="font-bold text-slate-800 text-right max-w-[150px] truncate">{completedOrder.shippingAddress}</span>
              </div>
              {completedOrder.discountAmount > 0 && (
                <div className="flex justify-between">
                  <span className="text-slate-400">Đã giảm giá:</span>
                  <span className="font-bold text-accent">-{formatVND(completedOrder.discountAmount)}</span>
                </div>
              )}
              <div className="flex justify-between border-t border-slate-200/50 pt-2 font-bold">
                <span className="text-slate-600">Tổng thanh toán:</span>
                <span className="text-accent">{formatVND(completedOrder.totalPrice)}</span>
              </div>
            </div>

            <button
              type="button"
              onClick={handleFinishSuccess}
              className="w-full bg-slate-900 hover:bg-accent text-white font-bold py-2.5 rounded-full transition-all text-xs"
            >
              TIẾP TỤC MUA SẮM
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
