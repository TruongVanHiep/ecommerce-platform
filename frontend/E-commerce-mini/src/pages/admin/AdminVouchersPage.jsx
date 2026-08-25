import { useEffect, useState } from "react";
import {
  getAllVouchers,
  createVoucher,
  updateVoucher,
  deleteVoucher,
} from "../../services/voucherService";
import { formatVND } from "../../lib/formatCurrency";

const emptyForm = {
  code: "",
  description: "",
  discountType: "PERCENT",
  discountValue: "",
  minOrderValue: "0",
  maxDiscountAmount: "",
  usageLimit: "",
  startDate: "",
  endDate: "",
  active: true,
};

export default function AdminVouchersPage() {
  const [vouchers, setVouchers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [formError, setFormError] = useState("");
  const [saving, setSaving] = useState(false);

  const loadVouchers = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await getAllVouchers();
      setVouchers(res.result || []);
    } catch (err) {
      console.error("Error loading vouchers:", err);
      setError("Không thể tải danh sách voucher.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadVouchers();
  }, []);

  const openCreateForm = () => {
    setEditingId(null);
    setForm(emptyForm);
    setFormError("");
    setShowForm(true);
  };

  const openEditForm = (v) => {
    setEditingId(v.id);
    setForm({
      code: v.code,
      description: v.description || "",
      discountType: v.discountType,
      discountValue: v.discountValue,
      minOrderValue: v.minOrderValue,
      maxDiscountAmount: v.maxDiscountAmount || "",
      usageLimit: v.usageLimit || "",
      startDate: v.startDate?.slice(0, 16) || "",
      endDate: v.endDate?.slice(0, 16) || "",
      active: v.active,
    });
    setFormError("");
    setShowForm(true);
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm((prev) => ({ ...prev, [name]: type === "checkbox" ? checked : value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.code.trim() || !form.discountValue || !form.startDate || !form.endDate) {
      setFormError("Vui lòng nhập mã, giá trị giảm và khoảng thời gian áp dụng.");
      return;
    }

    const payload = {
      code: form.code.trim().toUpperCase(),
      description: form.description,
      discountType: form.discountType,
      discountValue: Number(form.discountValue),
      minOrderValue: Number(form.minOrderValue) || 0,
      maxDiscountAmount: form.maxDiscountAmount ? Number(form.maxDiscountAmount) : null,
      usageLimit: form.usageLimit ? Number(form.usageLimit) : null,
      startDate: form.startDate,
      endDate: form.endDate,
      active: form.active,
    };

    setSaving(true);
    setFormError("");
    try {
      if (editingId) {
        await updateVoucher(editingId, payload);
      } else {
        await createVoucher(payload);
      }
      setShowForm(false);
      await loadVouchers();
    } catch (err) {
      console.error("Error saving voucher:", err);
      setFormError(err.response?.data?.message || "Không thể lưu voucher.");
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!confirm("Xóa voucher này?")) return;
    try {
      await deleteVoucher(id);
      await loadVouchers();
    } catch (err) {
      console.error("Error deleting voucher:", err);
      alert(err.response?.data?.message || "Không thể xóa voucher.");
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-slate-900">Quản lý voucher</h1>
        <button
          type="button"
          onClick={openCreateForm}
          className="bg-accent hover:bg-accent-dark text-white font-bold text-sm px-5 py-2.5 rounded-lg transition-colors"
        >
          + Tạo voucher
        </button>
      </div>

      {loading && (
        <div className="flex justify-center py-20">
          <div className="w-10 h-10 border-4 border-accent/15 border-t-accent rounded-full animate-spin" />
        </div>
      )}

      {error && !loading && (
        <div className="bg-white rounded-2xl border border-slate-200/70 p-8 text-center text-red-600">{error}</div>
      )}

      {!loading && !error && (
        <div className="bg-white rounded-2xl border border-slate-200/70 border border-slate-100 overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-100 text-left text-xs text-slate-500 uppercase">
                <th className="p-3">Mã</th>
                <th className="p-3">Giảm giá</th>
                <th className="p-3">Đơn tối thiểu</th>
                <th className="p-3">Đã dùng</th>
                <th className="p-3">Hiệu lực</th>
                <th className="p-3">Trạng thái</th>
                <th className="p-3 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
              {vouchers.map((v) => (
                <tr key={v.id} className="hover:bg-slate-50">
                  <td className="p-3 font-bold text-slate-800">{v.code}</td>
                  <td className="p-3 text-slate-600">
                    {v.discountType === "PERCENT" ? `${v.discountValue}%` : formatVND(v.discountValue)}
                    {v.maxDiscountAmount && v.discountType === "PERCENT" && (
                      <span className="text-slate-400"> (tối đa {formatVND(v.maxDiscountAmount)})</span>
                    )}
                  </td>
                  <td className="p-3 text-slate-600">{formatVND(v.minOrderValue)}</td>
                  <td className="p-3 text-slate-600">
                    {v.usedCount}{v.usageLimit ? ` / ${v.usageLimit}` : ""}
                  </td>
                  <td className="p-3 text-xs text-slate-500">
                    {v.startDate?.slice(0, 10)} → {v.endDate?.slice(0, 10)}
                  </td>
                  <td className="p-3">
                    <span className={`text-xs font-bold px-2 py-1 rounded-full ${v.active ? "bg-emerald-50 text-emerald-700" : "bg-slate-100 text-slate-500"}`}>
                      {v.active ? "Đang bật" : "Đã tắt"}
                    </span>
                  </td>
                  <td className="p-3 text-right space-x-3 text-xs font-semibold">
                    <button type="button" onClick={() => openEditForm(v)} className="text-accent hover:underline">Sửa</button>
                    <button type="button" onClick={() => handleDelete(v.id)} className="text-red-500 hover:underline">Xóa</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {vouchers.length === 0 && (
            <p className="text-center text-slate-400 py-10 text-sm">Chưa có voucher nào.</p>
          )}
        </div>
      )}

      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4 overflow-y-auto">
          <div className="bg-white rounded-[24px] w-full max-w-lg overflow-hidden border border-slate-200 my-8">
            <div className="p-5 border-b border-slate-100 flex justify-between items-center bg-slate-50">
              <h3 className="font-bold text-slate-900 text-base">{editingId ? "Sửa voucher" : "Tạo voucher mới"}</h3>
              <button type="button" onClick={() => setShowForm(false)} className="text-slate-400 hover:text-slate-600 p-1.5 rounded-full hover:bg-slate-100">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <form onSubmit={handleSubmit} className="p-5 space-y-4 text-sm">
              {formError && (
                <div className="bg-red-50 text-red-600 p-3 rounded-lg text-xs font-medium border border-red-100">{formError}</div>
              )}

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">Mã voucher</label>
                  <input type="text" name="code" value={form.code} onChange={handleChange} disabled={!!editingId}
                    className="w-full px-3 py-2.5 border border-slate-200 rounded text-sm outline-none focus:border-accent uppercase disabled:bg-slate-50" />
                </div>
                <div>
                  <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">Loại giảm giá</label>
                  <select name="discountType" value={form.discountType} onChange={handleChange}
                    className="w-full px-3 py-2.5 border border-slate-200 rounded text-sm outline-none focus:border-accent bg-white">
                    <option value="PERCENT">Phần trăm (%)</option>
                    <option value="FIXED_AMOUNT">Số tiền cố định</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">Mô tả</label>
                <input type="text" name="description" value={form.description} onChange={handleChange}
                  className="w-full px-3 py-2.5 border border-slate-200 rounded text-sm outline-none focus:border-accent" />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">
                    Giá trị giảm {form.discountType === "PERCENT" ? "(%)" : "(VND)"}
                  </label>
                  <input type="number" name="discountValue" value={form.discountValue} onChange={handleChange} min="0"
                    className="w-full px-3 py-2.5 border border-slate-200 rounded text-sm outline-none focus:border-accent" />
                </div>
                <div>
                  <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">Giảm tối đa (VND)</label>
                  <input type="number" name="maxDiscountAmount" value={form.maxDiscountAmount} onChange={handleChange} min="0"
                    placeholder="Không giới hạn"
                    className="w-full px-3 py-2.5 border border-slate-200 rounded text-sm outline-none focus:border-accent" />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">Đơn tối thiểu (VND)</label>
                  <input type="number" name="minOrderValue" value={form.minOrderValue} onChange={handleChange} min="0"
                    className="w-full px-3 py-2.5 border border-slate-200 rounded text-sm outline-none focus:border-accent" />
                </div>
                <div>
                  <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">Giới hạn lượt dùng</label>
                  <input type="number" name="usageLimit" value={form.usageLimit} onChange={handleChange} min="0"
                    placeholder="Không giới hạn"
                    className="w-full px-3 py-2.5 border border-slate-200 rounded text-sm outline-none focus:border-accent" />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">Bắt đầu</label>
                  <input type="datetime-local" name="startDate" value={form.startDate} onChange={handleChange}
                    className="w-full px-3 py-2.5 border border-slate-200 rounded text-sm outline-none focus:border-accent" />
                </div>
                <div>
                  <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">Kết thúc</label>
                  <input type="datetime-local" name="endDate" value={form.endDate} onChange={handleChange}
                    className="w-full px-3 py-2.5 border border-slate-200 rounded text-sm outline-none focus:border-accent" />
                </div>
              </div>

              <label className="flex items-center gap-2 cursor-pointer text-sm text-slate-600">
                <input type="checkbox" name="active" checked={form.active} onChange={handleChange} className="accent-accent w-4 h-4" />
                Kích hoạt voucher
              </label>

              <div className="flex gap-3 border-t border-slate-100 pt-4">
                <button type="button" onClick={() => setShowForm(false)} disabled={saving}
                  className="flex-1 py-2.5 border border-slate-200 hover:bg-slate-50 text-slate-600 font-bold rounded transition-colors disabled:opacity-60">
                  Hủy
                </button>
                <button type="submit" disabled={saving}
                  className="flex-1 py-2.5 bg-accent hover:bg-accent-dark text-white font-bold rounded transition-colors disabled:opacity-60">
                  {saving ? "Đang lưu..." : "Lưu voucher"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
