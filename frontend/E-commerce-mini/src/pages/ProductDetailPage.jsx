import { useEffect, useState, useContext } from "react";
import { Link, useParams } from "react-router-dom";
import { getProductById } from "../services/productService";
import { formatVND } from "../lib/formatCurrency";
import { CartContext } from "../context/CartContext";
import ProductReviews from "../components/ProductReviews";

export default function ProductDetailPage() {
  const { productId } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    let active = true;
    
    const loadProduct = async () => {
      try {
        setLoading(true);
        setError(null);

        const result = await getProductById(productId);

        if (!active) return;

        if (!result) {
          setProduct(null);
          setError("Không tìm thấy sản phẩm.");
          return;
        }

        setProduct(result);
      } catch (fetchError) {
        if (!active) return;
        console.error("Error loading product detail:", fetchError);
        setError("Không thể tải chi tiết sản phẩm. Vui lòng thử lại.");
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    loadProduct();

    return () => {
      active = false;
    };
  }, [productId]);

  const stockCount = Number(product?.stock);
  const hasStockLimit = Number.isFinite(stockCount) && stockCount > 0;
  const isOutOfStock = product?.inStock === false || stockCount === 0;
  const stockLabel = hasStockLimit
    ? `${stockCount} sản phẩm`
    : product?.inStock === false
      ? "Hết hàng"
      : "Đang cập nhật";

  const { addToCart } = useContext(CartContext);

  const handleAddToCart = () => {
    if (!product) return;

    addToCart(product, quantity);
  };

  return (
    <div className="min-h-screen bg-[#f5f5f5] py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6">
        <div className="mb-6 text-sm text-slate-500">
          <Link to="/" className="hover:text-indigo-600 transition-colors">Trang chủ</Link>
          <span className="mx-2">/</span>
          <span className="text-slate-700">Chi tiết sản phẩm</span>
        </div>

        {loading && (
          <div className="bg-white rounded-3xl shadow-sm p-8 animate-pulse">
            <div className="grid lg:grid-cols-2 gap-8">
              <div className="aspect-square rounded-3xl bg-slate-100" />
              <div className="space-y-4">
                <div className="h-6 w-1/3 bg-slate-100 rounded" />
                <div className="h-10 w-2/3 bg-slate-100 rounded" />
                <div className="h-12 w-1/2 bg-slate-100 rounded" />
                <div className="h-24 w-full bg-slate-100 rounded-2xl" />
                <div className="h-12 w-full bg-slate-100 rounded-2xl" />
              </div>
            </div>
          </div>
        )}

        {error && !loading && (
          <div className="bg-white rounded-3xl shadow-sm p-8 text-center">
            <h1 className="text-2xl font-bold text-slate-900 mb-2">Chi tiết sản phẩm</h1>
            <p className="text-slate-600 mb-6">{error}</p>
            <Link
              to="/"
              className="inline-flex items-center justify-center px-6 py-3 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-semibold transition-colors"
            >
              Quay lại trang chủ
            </Link>
          </div>
        )}

        {!loading && product && (
          <div className="bg-white rounded-3xl shadow-sm overflow-hidden">
            <div className="grid lg:grid-cols-2 gap-0">
              <div className="bg-slate-50 p-6 sm:p-10 flex items-center justify-center">
                <div className="w-full aspect-square max-w-[620px] rounded-3xl overflow-hidden bg-white shadow-inner flex items-center justify-center p-8">
                  <img
                    src={product.image || "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?q=80&w=1200&auto=format&fit=crop"}
                    alt={product.title || product.name}
                    className="w-full h-full object-contain mix-blend-multiply"
                  />
                </div>
              </div>

              <div className="p-6 sm:p-10 lg:p-12 flex flex-col gap-6">
                <div>
                  <p className="text-sm font-semibold uppercase tracking-[0.3em] text-indigo-600 mb-3">
                    {product.category || "Gadget"}
                  </p>
                  <h1 className="text-3xl sm:text-4xl font-black text-slate-900 leading-tight">
                    {product.title || product.name}
                  </h1>
                </div>

                <div className="flex items-end justify-between gap-4">
                  <div>
                    <p className="text-sm text-slate-400 mb-1">Giá bán</p>
                    <div className="text-4xl sm:text-5xl font-black text-slate-900 tracking-tight">
                      {formatVND(product.price)}
                    </div>
                    {product.originalPrice != null && (
                      <p className="text-sm text-slate-400 line-through mt-2">
                        {formatVND(product.originalPrice)}
                      </p>
                    )}
                  </div>

                  <div className="text-right">
                    <p className="text-sm text-slate-400 mb-1">Stock</p>
                    <p className="text-lg font-bold text-slate-900">{stockLabel}</p>
                  </div>
                </div>

                <div className="rounded-2xl bg-slate-50 p-5 sm:p-6">
                  <p className="text-sm font-semibold text-slate-500 mb-3 uppercase tracking-wider">Mô tả</p>
                  <p className="text-slate-700 leading-7">
                    {product.description || "Thiết kế hiện đại, tinh tế cùng hiệu năng vượt trội mang lại trải nghiệm tuyệt vời."}
                  </p>
                </div>

                <div className="flex items-center justify-between gap-4 rounded-2xl border border-slate-200 p-5">
                  <div>
                    <p className="text-sm font-semibold text-slate-500 uppercase tracking-wider mb-2">Số lượng</p>
                    <div className="flex items-center rounded-xl border border-slate-200 overflow-hidden w-fit">
                      <button
                        type="button"
                        onClick={() => setQuantity((current) => Math.max(1, current - 1))}
                        disabled={isOutOfStock || quantity <= 1}
                        className="w-12 h-12 bg-slate-50 hover:bg-slate-100 text-slate-700 text-xl font-semibold transition-colors disabled:text-slate-300 disabled:hover:bg-slate-50"
                        aria-label="Giảm số lượng"
                      >
                        -
                      </button>
                      <div className="w-16 h-12 flex items-center justify-center font-bold text-slate-900">
                        {quantity}
                      </div>
                      <button
                        type="button"
                        onClick={() => setQuantity((current) => {
                          if (!hasStockLimit) return current + 1;
                          return Math.min(current + 1, stockCount);
                        })}
                        disabled={isOutOfStock || (hasStockLimit && quantity >= stockCount)}
                        className="w-12 h-12 bg-slate-50 hover:bg-slate-100 text-slate-700 text-xl font-semibold transition-colors disabled:text-slate-300 disabled:hover:bg-slate-50"
                        aria-label="Tăng số lượng"
                      >
                        +
                      </button>
                    </div>
                  </div>

                  <div className="text-right">
                    <p className="text-sm font-semibold text-slate-500 uppercase tracking-wider mb-2">Trạng thái</p>
                    <span className={`inline-flex px-3 py-1 rounded-full text-sm font-semibold ${isOutOfStock ? "bg-red-50 text-red-600" : "bg-emerald-50 text-emerald-600"}`}>
                      {isOutOfStock ? "Hết hàng" : "Còn hàng"}
                    </span>
                  </div>
                </div>

                <div className="flex flex-col sm:flex-row gap-3 pt-2">
                  <button
                    type="button"
                    onClick={handleAddToCart}
                    disabled={isOutOfStock}
                    className="flex-1 inline-flex items-center justify-center px-6 py-4 rounded-2xl bg-slate-900 hover:bg-indigo-600 disabled:bg-slate-200 disabled:text-slate-400 text-white font-semibold transition-colors"
                  >
                    {isOutOfStock ? "Hết hàng" : "Add to cart"}
                  </button>
                  <Link
                    to="/"
                    className="inline-flex items-center justify-center px-6 py-4 rounded-2xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold transition-colors"
                  >
                    Tiếp tục mua sắm
                  </Link>
                </div>
              </div>
            </div>
          </div>
        )}

        {!loading && product && <ProductReviews productId={product.id} />}
      </div>
    </div>
  );
}