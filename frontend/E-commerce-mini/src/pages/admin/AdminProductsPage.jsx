import { useEffect, useState } from "react";
import { getAllProducts, createProduct, updateProduct, deleteProduct } from "../../services/productService";
import { getCategories } from "../../services/categoryService";
import { formatVND } from "../../lib/formatCurrency";

const emptyForm = { name: "", description: "", price: "", image: "", stock: "", categoryId: "" };
export default function AdminProductsPage() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [formError, setFormError] = useState("");
  const [saving, setSaving] = useState(false);

  // Chỉ TẢI dữ liệu, không đụng state — trả kết quả để nơi gọi tự áp vào.
  // Tách như vậy vì lúc mở trang nó được gọi từ useEffect: một hàm vừa tải vừa
  // setState thì trình lint React coi là setState đồng bộ trong effect (gây render
  // dây chuyền), dù setState thật ra nằm sau await.
  //
  // Tải hai thứ ĐỘC LẬP. Trước đây dùng Promise.all: chỉ cần lời gọi sản phẩm lỗi
  // là danh mục — dù đã tải xong — cũng bị bỏ theo, ô Danh mục trống trơn và không
  // thể thêm sản phẩm nào.
  const fetchAdminData = () => Promise.allSettled([getAllProducts(), getCategories()]);

  const applyResults = ([productsResult, categoriesResult]) => {
    if (productsResult.status === "fulfilled") {
      setProducts(productsResult.value);
    } else {
      console.error("Error loading admin products:", productsResult.reason);
    }
    if (categoriesResult.status === "fulfilled") {
      setCategories(categoriesResult.value.result || []);
    } else {
      console.error("Error loading categories:", categoriesResult.reason);
    }

    const failed = [
      productsResult.status === "rejected" && "sản phẩm",
      categoriesResult.status === "rejected" && "danh mục",
    ].filter(Boolean);
    setError(failed.length ? `Không thể tải ${failed.join(" và ")}.` : null);
    setLoading(false);
  };

  // Tải lại sau khi thêm/sửa/xoá. Lúc này đang ở trong event handler nên bật lại
  // trạng thái đang tải thoải mái.
  const reload = async () => {
    setLoading(true);
    setError(null);
    applyResults(await fetchAdminData());
  };

  useEffect(() => {
    // setState nằm trong callback của promise, không chạy đồng bộ trong effect.
    // Cờ cancelled: rời trang trước khi tải xong thì bỏ kết quả, không set state
    // lên component đã bị tháo.
    let cancelled = false;
    fetchAdminData().then((results) => {
      if (!cancelled) applyResults(results);
    });
    return () => {
      cancelled = true;
    };
  }, []);

  const categoryName = (categoryId) =>
    categories.find((c) => c.id === categoryId)?.name || "—";

  const openCreateForm = () => {
    setEditingId(null);
    setForm(emptyForm);
    setFormError("");
    setShowForm(true);
  };

  const openEditForm = (product) => {
    setEditingId(product.id);
    setForm({
      name: product.name,
      description: product.description || "",
      price: product.price,
      image: product.image || "",
      stock: product.stock,
      categoryId: product.categoryId || "",
    });
    setFormError("");
    setShowForm(true);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.name.trim() || !form.price || !form.categoryId) {
      setFormError("Vui lòng nhập tên, giá và chọn danh mục.");
      return;
    }

    const payload = {
      name: form.name,
      description: form.description,
      price: Number(form.price),
      image: form.image,
      stock: Number(form.stock) || 0,
      categoryId: Number(form.categoryId),
    };

    setSaving(true);
    setFormError("");
    try {
      if (editingId) {
        await updateProduct(editingId, payload);
      } else {
        await createProduct(payload);
      }
      setShowForm(false);
      await reload();
    } catch (err) {
      console.error("Error saving product:", err);
      setFormError(err.response?.data?.message || "Không thể lưu sản phẩm.");
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!confirm("Xóa sản phẩm này?")) return;
    try {
      await deleteProduct(id);
      await reload();
    } catch (err) {
      console.error("Error deleting product:", err);
      alert(err.response?.data?.message || "Không thể xóa sản phẩm.");
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-slate-900">Quản lý sản phẩm</h1>
        <button
          type="button"
          onClick={openCreateForm}
          className="bg-accent hover:bg-accent-dark text-white font-bold text-sm px-5 py-2.5 rounded-lg transition-colors"
        >
          + Thêm sản phẩm
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
                <th className="p-3">Sản phẩm</th>
                <th className="p-3">Danh mục</th>
                <th className="p-3">Giá</th>
                <th className="p-3">Kho</th>
                <th className="p-3 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
              {products.map((p) => (
                <tr key={p.id} className="hover:bg-slate-50">
                  <td className="p-3">
                    <div className="flex items-center gap-3">
                      <img src={p.image} alt={p.name} className="w-10 h-10 object-contain rounded bg-slate-50 border border-slate-100" />
                      <span className="font-semibold text-slate-800 max-w-xs truncate">{p.name}</span>
                    </div>
                  </td>
                  <td className="p-3 text-slate-600">{categoryName(p.categoryId)}</td>
                  <td className="p-3 font-semibold text-slate-800">{formatVND(p.price)}</td>
                  <td className="p-3">
                    <span className={p.stock === 0 ? "text-red-500 font-bold" : "text-slate-600"}>{p.stock}</span>
                  </td>
                  <td className="p-3 text-right space-x-3 text-xs font-semibold">
                    <button type="button" onClick={() => openEditForm(p)} className="text-accent hover:underline">Sửa</button>
                    <button type="button" onClick={() => handleDelete(p.id)} className="text-red-500 hover:underline">Xóa</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {products.length === 0 && (
            <p className="text-center text-slate-400 py-10 text-sm">Chưa có sản phẩm nào.</p>
          )}
        </div>
      )}

      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4 overflow-y-auto">
          <div className="bg-white rounded-[24px] w-full max-w-lg overflow-hidden border border-slate-200 my-8">
            <div className="p-5 border-b border-slate-100 flex justify-between items-center bg-slate-50">
              <h3 className="font-bold text-slate-900 text-base">{editingId ? "Sửa sản phẩm" : "Thêm sản phẩm mới"}</h3>
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

              <div>
                <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">Tên sản phẩm</label>
                <input type="text" name="name" value={form.name} onChange={handleChange}
                  className="w-full px-3 py-2.5 border border-slate-200 rounded text-sm outline-none focus:border-accent" />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">Mô tả</label>
                <textarea name="description" rows="2" value={form.description} onChange={handleChange}
                  className="w-full px-3 py-2.5 border border-slate-200 rounded text-sm outline-none focus:border-accent resize-none" />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">Giá (VND)</label>
                  <input type="number" name="price" value={form.price} onChange={handleChange} min="0"
                    className="w-full px-3 py-2.5 border border-slate-200 rounded text-sm outline-none focus:border-accent" />
                </div>
                <div>
                  <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">Tồn kho</label>
                  <input type="number" name="stock" value={form.stock} onChange={handleChange} min="0"
                    className="w-full px-3 py-2.5 border border-slate-200 rounded text-sm outline-none focus:border-accent" />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">Danh mục</label>
                <select name="categoryId" value={form.categoryId} onChange={handleChange}
                  className="w-full px-3 py-2.5 border border-slate-200 rounded text-sm outline-none focus:border-accent bg-white">
                  <option value="">-- Chọn danh mục --</option>
                  {categories.map((c) => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">URL hình ảnh</label>
                <input type="text" name="image" value={form.image} onChange={handleChange}
                  className="w-full px-3 py-2.5 border border-slate-200 rounded text-sm outline-none focus:border-accent" />
              </div>

              <div className="flex gap-3 border-t border-slate-100 pt-4">
                <button type="button" onClick={() => setShowForm(false)} disabled={saving}
                  className="flex-1 py-2.5 border border-slate-200 hover:bg-slate-50 text-slate-600 font-bold rounded transition-colors disabled:opacity-60">
                  Hủy
                </button>
                <button type="submit" disabled={saving}
                  className="flex-1 py-2.5 bg-accent hover:bg-accent-dark text-white font-bold rounded transition-colors disabled:opacity-60">
                  {saving ? "Đang lưu..." : "Lưu sản phẩm"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
