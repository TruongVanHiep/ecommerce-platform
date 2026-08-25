import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  getMyAddresses,
  createAddress,
  updateAddress,
  deleteAddress,
} from "../services/addressService";

const emptyForm = { receiverName: "", phone: "", addressLine: "", isDefault: false };

export default function AddressBookPage() {
  const [addresses, setAddresses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [formError, setFormError] = useState("");
  const [saving, setSaving] = useState(false);

  const loadAddresses = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await getMyAddresses();
      setAddresses(res.result || []);
    } catch (err) {
      console.error("Error loading addresses:", err);
      setError("Không thể tải sổ địa chỉ.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAddresses();
  }, []);

  const openCreateForm = () => {
    setEditingId(null);
    setForm(emptyForm);
    setFormError("");
    setShowForm(true);
  };

  const openEditForm = (address) => {
    setEditingId(address.id);
    setForm({
      receiverName: address.receiverName,
      phone: address.phone,
      addressLine: address.addressLine,
      isDefault: address.isDefault,
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
    if (!form.receiverName.trim() || !form.phone.trim() || !form.addressLine.trim()) {
      setFormError("Vui lòng điền đầy đủ thông tin.");
      return;
    }

    setSaving(true);
    setFormError("");
    try {
      if (editingId) {
        await updateAddress(editingId, form);
      } else {
        await createAddress(form);
      }
      setShowForm(false);
      await loadAddresses();
    } catch (err) {
      console.error("Error saving address:", err);
      setFormError(err.response?.data?.message || "Không thể lưu địa chỉ.");
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!confirm("Xóa địa chỉ này?")) return;
    try {
      await deleteAddress(id);
      await loadAddresses();
    } catch (err) {
      console.error("Error deleting address:", err);
      alert(err.response?.data?.message || "Không thể xóa địa chỉ.");
    }
  };

  const handleSetDefault = async (address) => {
    try {
      await updateAddress(address.id, { ...address, isDefault: true });
      await loadAddresses();
    } catch (err) {
      console.error("Error setting default address:", err);
    }
  };

  return (
    <div className="min-h-screen bg-bg-light py-8">
      <div className="max-w-3xl mx-auto px-4 sm:px-6">
        <div className="mb-4 text-xs text-slate-500 flex items-center gap-1.5">
          <Link to="/" className="hover:text-accent transition-colors">Trang chủ</Link>
          <span>/</span>
          <span className="text-slate-700">Sổ địa chỉ</span>
        </div>

        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-bold text-slate-900">Sổ địa chỉ</h1>
          <button
            type="button"
            onClick={openCreateForm}
            className="bg-accent hover:bg-accent-dark text-white font-bold text-sm px-5 py-2.5 rounded-full transition-colors"
          >
            + Thêm địa chỉ
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

        {!loading && !error && addresses.length === 0 && (
          <div className="bg-white rounded-2xl border border-slate-200/70 text-center py-16 px-4">
            <p className="text-slate-500">Bạn chưa có địa chỉ nào được lưu.</p>
          </div>
        )}

        <div className="space-y-3">
          {addresses.map((addr) => (
            <div key={addr.id} className="bg-white rounded-2xl border border-slate-200/70 p-4 flex justify-between items-start gap-4">
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <span className="font-bold text-slate-800">{addr.receiverName}</span>
                  <span className="text-slate-300">|</span>
                  <span className="text-slate-600 text-sm">{addr.phone}</span>
                  {addr.isDefault && (
                    <span className="text-[10px] font-bold text-accent border border-accent/30 bg-accent/10 px-1.5 py-0.5 rounded uppercase">
                      Mặc định
                    </span>
                  )}
                </div>
                <p className="text-sm text-slate-500">{addr.addressLine}</p>
              </div>
              <div className="flex flex-col items-end gap-2 shrink-0 text-xs font-semibold">
                <button type="button" onClick={() => openEditForm(addr)} className="text-accent hover:underline">
                  Sửa
                </button>
                {!addr.isDefault && (
                  <button type="button" onClick={() => handleSetDefault(addr)} className="text-slate-500 hover:underline">
                    Đặt làm mặc định
                  </button>
                )}
                <button type="button" onClick={() => handleDelete(addr.id)} className="text-red-500 hover:underline">
                  Xóa
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4">
          <div className="bg-white rounded-[24px] w-full max-w-md overflow-hidden border border-slate-200">
            <div className="p-5 border-b border-slate-100 flex justify-between items-center bg-slate-50">
              <h3 className="font-bold text-slate-900 text-base">
                {editingId ? "Sửa địa chỉ" : "Thêm địa chỉ mới"}
              </h3>
              <button
                type="button"
                onClick={() => setShowForm(false)}
                className="text-slate-400 hover:text-slate-600 p-1.5 rounded-full hover:bg-slate-100 transition-colors"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <form onSubmit={handleSubmit} className="p-5 space-y-4 text-sm">
              {formError && (
                <div className="bg-red-50 text-red-600 p-3 rounded-lg text-xs font-medium border border-red-100">
                  {formError}
                </div>
              )}

              <div>
                <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">Tên người nhận</label>
                <input
                  type="text"
                  name="receiverName"
                  value={form.receiverName}
                  onChange={handleChange}
                  className="w-full px-3 py-2.5 border border-slate-200 rounded-xl text-sm outline-none focus:border-accent transition-all"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">Số điện thoại</label>
                <input
                  type="text"
                  name="phone"
                  value={form.phone}
                  onChange={handleChange}
                  className="w-full px-3 py-2.5 border border-slate-200 rounded-xl text-sm outline-none focus:border-accent transition-all"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">Địa chỉ chi tiết</label>
                <textarea
                  name="addressLine"
                  rows="3"
                  value={form.addressLine}
                  onChange={handleChange}
                  className="w-full px-3 py-2.5 border border-slate-200 rounded-xl text-sm outline-none focus:border-accent transition-all resize-none"
                />
              </div>

              <label className="flex items-center gap-2 cursor-pointer text-sm text-slate-600">
                <input
                  type="checkbox"
                  name="isDefault"
                  checked={form.isDefault}
                  onChange={handleChange}
                  className="accent-accent w-4 h-4"
                />
                Đặt làm địa chỉ mặc định
              </label>

              <div className="flex gap-3 border-t border-slate-100 pt-4">
                <button
                  type="button"
                  onClick={() => setShowForm(false)}
                  disabled={saving}
                  className="flex-1 py-2.5 border border-slate-200 hover:bg-slate-50 text-slate-600 font-bold rounded-full transition-colors disabled:opacity-60"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="flex-1 py-2.5 bg-accent hover:bg-accent-dark text-white font-bold rounded-full transition-colors disabled:opacity-60"
                >
                  {saving ? "Đang lưu..." : "Lưu địa chỉ"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
