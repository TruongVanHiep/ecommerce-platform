import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getMyOrders } from "../services/orderService";
import { formatVND } from "../lib/formatCurrency";

const STATUS_LABELS = {
  PENDING: { label: "Chờ xác nhận", className: "bg-amber-50 text-amber-700 border-amber-200" },
  PAID: { label: "Đã thanh toán", className: "bg-accent/10 text-accent-dark border-accent/30" },
  CONFIRMED: { label: "Đã xác nhận", className: "bg-blue-50 text-blue-700 border-blue-200" },
  SHIPPING: { label: "Đang giao", className: "bg-purple-50 text-purple-700 border-purple-200" },
  DELIVERED: { label: "Đã giao", className: "bg-emerald-50 text-emerald-700 border-emerald-200" },
  CANCELLED: { label: "Đã hủy", className: "bg-red-50 text-red-700 border-red-200" },
};

function StatusBadge({ status }) {
  const info = STATUS_LABELS[status] || { label: status, className: "bg-slate-50 text-slate-700 border-slate-200" };
  return (
    <span className={`inline-flex px-2.5 py-1 rounded-full text-xs font-bold border ${info.className}`}>
      {info.label}
    </span>
  );
}

export default function OrderHistoryPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [expandedId, setExpandedId] = useState(null);

  useEffect(() => {
    const loadOrders = async () => {
      try {
        setLoading(true);
        setError(null);
        const res = await getMyOrders();
        const sorted = [...(res.result || [])].sort((a, b) => b.orderId - a.orderId);
        setOrders(sorted);
      } catch (err) {
        console.error("Error loading orders:", err);
        setError("Không thể tải danh sách đơn hàng.");
      } finally {
        setLoading(false);
      }
    };
    loadOrders();
  }, []);

  const toggleExpand = (orderId) => {
    setExpandedId((prev) => (prev === orderId ? null : orderId));
  };

  return (
    <div className="min-h-screen bg-bg-light py-8">
      <div className="max-w-4xl mx-auto px-4 sm:px-6">
        <div className="mb-4 text-xs text-slate-500 flex items-center gap-1.5">
          <Link to="/" className="hover:text-accent transition-colors">Trang chủ</Link>
          <span>/</span>
          <span className="text-slate-700">Đơn hàng của tôi</span>
        </div>

        <h1 className="text-2xl font-bold text-slate-900 mb-6">Đơn hàng của tôi</h1>

        {loading && (
          <div className="flex justify-center py-20">
            <div className="w-10 h-10 border-4 border-accent/15 border-t-accent rounded-full animate-spin" />
          </div>
        )}

        {error && !loading && (
          <div className="bg-white rounded-2xl border border-slate-200/70 p-8 text-center text-red-600">{error}</div>
        )}

        {!loading && !error && orders.length === 0 && (
          <div className="bg-white rounded-2xl border border-slate-200/70 text-center py-20 px-4">
            <h2 className="text-lg font-bold text-slate-800 mb-2">Bạn chưa có đơn hàng nào</h2>
            <Link
              to="/"
              className="inline-flex items-center justify-center px-6 py-2.5 bg-accent hover:bg-accent-dark text-white font-bold text-sm rounded-full transition-all mt-4"
            >
              MUA SẮM NGAY
            </Link>
          </div>
        )}

        <div className="space-y-3">
          {orders.map((order) => (
            <div key={order.orderId} className="bg-white rounded-2xl border border-slate-200/70 overflow-hidden">
              <button
                type="button"
                onClick={() => toggleExpand(order.orderId)}
                className="w-full flex flex-wrap items-center justify-between gap-3 p-4 text-left hover:bg-slate-50 transition-colors"
              >
                <div className="flex items-center gap-4">
                  <span className="font-bold text-slate-800 text-sm">Đơn #{order.orderId}</span>
                  <StatusBadge status={order.status} />
                </div>
                <div className="flex items-center gap-4 text-xs text-slate-500">
                  <span>{order.createdAt ? new Date(order.createdAt).toLocaleString("vi-VN") : ""}</span>
                  <span className="font-bold text-accent text-sm">{formatVND(order.totalPrice)}</span>
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    className={`h-4 w-4 text-slate-400 transition-transform ${expandedId === order.orderId ? "rotate-180" : ""}`}
                    fill="none" viewBox="0 0 24 24" stroke="currentColor"
                  >
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                  </svg>
                </div>
              </button>

              {expandedId === order.orderId && (
                <div className="border-t border-slate-100 p-4 bg-slate-50/50 text-sm space-y-3">
                  <div className="grid sm:grid-cols-2 gap-2 text-xs text-slate-600">
                    <div><span className="text-slate-400">Người nhận / Địa chỉ:</span> {order.shippingAddress}</div>
                    <div><span className="text-slate-400">Điện thoại:</span> {order.phone}</div>
                  </div>

                  <div className="divide-y divide-slate-100 border-t border-slate-100 pt-2">
                    {order.orderItemResponses?.map((item) => (
                      <div key={item.orderItemId} className="flex justify-between items-center py-2 text-xs">
                        <div className="flex-1">
                          <span className="font-semibold text-slate-800">{item.productName}</span>
                          <span className="text-slate-400 ml-2">x{item.quantity}</span>
                        </div>
                        <span className="font-bold text-slate-700">{formatVND(item.subTotal)}</span>
                      </div>
                    ))}
                  </div>

                  <div className="border-t border-slate-200 pt-2 space-y-1 text-xs">
                    {order.discountAmount > 0 && (
                      <div className="flex justify-between text-accent font-semibold">
                        <span>Giảm giá {order.voucherCode ? `(${order.voucherCode})` : ""}</span>
                        <span>-{formatVND(order.discountAmount)}</span>
                      </div>
                    )}
                    <div className="flex justify-between text-slate-600">
                      <span>Phí vận chuyển</span>
                      <span>{order.shippingFee > 0 ? formatVND(order.shippingFee) : "Miễn phí"}</span>
                    </div>
                    <div className="flex justify-between font-bold text-slate-800 text-sm pt-1">
                      <span>Tổng cộng</span>
                      <span className="text-accent">{formatVND(order.totalPrice)}</span>
                    </div>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
