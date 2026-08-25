import { useEffect, useState } from "react";
import { getAllOrdersAdmin, updateOrderStatusAdmin } from "../../services/orderService";
import { formatVND } from "../../lib/formatCurrency";

const STATUS_OPTIONS = ["PENDING", "PAID", "CONFIRMED", "SHIPPING", "DELIVERED", "CANCELLED"];

const STATUS_LABELS = {
  PENDING: "Chờ xác nhận",
  PAID: "Đã thanh toán",
  CONFIRMED: "Đã xác nhận",
  SHIPPING: "Đang giao",
  DELIVERED: "Đã giao",
  CANCELLED: "Đã hủy",
};

export default function AdminOrdersPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [updatingId, setUpdatingId] = useState(null);
  const [expandedId, setExpandedId] = useState(null);

  const loadOrders = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await getAllOrdersAdmin();
      const sorted = [...(res.result || [])].sort((a, b) => b.orderId - a.orderId);
      setOrders(sorted);
    } catch (err) {
      console.error("Error loading admin orders:", err);
      setError("Không thể tải danh sách đơn hàng.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadOrders();
  }, []);

  const handleStatusChange = async (orderId, status) => {
    setUpdatingId(orderId);
    try {
      await updateOrderStatusAdmin(orderId, status);
      setOrders((prev) => prev.map((o) => (o.orderId === orderId ? { ...o, status } : o)));
    } catch (err) {
      console.error("Error updating order status:", err);
      alert(err.response?.data?.message || "Không thể cập nhật trạng thái.");
    } finally {
      setUpdatingId(null);
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-slate-900 mb-6">Quản lý đơn hàng</h1>

      {loading && (
        <div className="flex justify-center py-20">
          <div className="w-10 h-10 border-4 border-accent/15 border-t-accent rounded-full animate-spin" />
        </div>
      )}

      {error && !loading && (
        <div className="bg-white rounded-2xl border border-slate-200/70 p-8 text-center text-red-600">{error}</div>
      )}

      {!loading && !error && (
        <div className="bg-white rounded-2xl border border-slate-200/70 border border-slate-100 overflow-hidden">
          {orders.map((order) => (
            <div key={order.orderId} className="border-b border-slate-50 last:border-b-0">
              <div className="p-4 flex flex-wrap items-center justify-between gap-3">
                <button
                  type="button"
                  onClick={() => setExpandedId((prev) => (prev === order.orderId ? null : order.orderId))}
                  className="text-left flex items-center gap-4"
                >
                  <span className="font-bold text-slate-800 text-sm">#{order.orderId}</span>
                  <span className="text-xs text-slate-500">
                    {order.createdAt ? new Date(order.createdAt).toLocaleString("vi-VN") : ""}
                  </span>
                  <span className="text-xs text-slate-500">User ID: {order.userId}</span>
                </button>

                <div className="flex items-center gap-4">
                  <span className="font-bold text-accent text-sm">{formatVND(order.totalPrice)}</span>
                  <select
                    value={order.status}
                    disabled={updatingId === order.orderId}
                    onChange={(e) => handleStatusChange(order.orderId, e.target.value)}
                    className="text-xs font-semibold border border-slate-200 rounded px-2 py-1.5 outline-none focus:border-accent bg-white"
                  >
                    {STATUS_OPTIONS.map((s) => (
                      <option key={s} value={s}>{STATUS_LABELS[s]}</option>
                    ))}
                  </select>
                </div>
              </div>

              {expandedId === order.orderId && (
                <div className="px-4 pb-4 bg-slate-50/50 text-xs text-slate-600 space-y-2">
                  <div><span className="text-slate-400">Địa chỉ:</span> {order.shippingAddress}</div>
                  <div><span className="text-slate-400">Điện thoại:</span> {order.phone}</div>
                  <div className="divide-y divide-slate-100 border-t border-slate-100 pt-2 mt-2">
                    {order.orderItemResponses?.map((item) => (
                      <div key={item.orderItemId} className="flex justify-between py-1.5">
                        <span>{item.productName} x{item.quantity}</span>
                        <span className="font-semibold">{formatVND(item.subTotal)}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          ))}
          {orders.length === 0 && (
            <p className="text-center text-slate-400 py-10 text-sm">Chưa có đơn hàng nào.</p>
          )}
        </div>
      )}
    </div>
  );
}
