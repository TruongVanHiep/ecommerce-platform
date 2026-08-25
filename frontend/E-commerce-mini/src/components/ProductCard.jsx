import { useNavigate } from "react-router-dom";
import { formatVND } from "../lib/formatCurrency";

export default function ProductCard({ product, onAddToCart }) {
  const navigate = useNavigate();
  const isOutOfStock = product.inStock === false;

  const handleOpenDetail = () => {
    if (product?.id == null) return;

    navigate(`/products/${product.id}`);
  };

  const stopNavigation = (event) => {
    event.stopPropagation();
  };

  return (
    <div
      role="link"
      tabIndex={0}
      onClick={handleOpenDetail}
      onKeyDown={(event) => {
        if (event.key === "Enter" || event.key === " ") {
          event.preventDefault();
          handleOpenDetail();
        }
      }}
      className="group flex flex-col bg-white rounded-3xl overflow-hidden border border-slate-100 shadow-[0_2px_10px_rgb(0,0,0,0.02)] hover:shadow-[0_20px_40px_rgb(0,0,0,0.06)] hover:-translate-y-1 transition-all duration-300 cursor-pointer focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
    >
      {/* Image Container */}
      <div className="relative aspect-[4/3] overflow-hidden bg-slate-50 p-6 flex items-center justify-center">
        {/* Abstract background blob for empty images */}
        <div className="absolute inset-0 bg-gradient-to-tr from-indigo-50 to-purple-50 opacity-0 group-hover:opacity-100 transition-opacity duration-500" />

        <img
          src={product.image || "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?q=80&w=600&auto=format&fit=crop"}
          alt={product.title || product.name}
          className="relative z-10 w-full h-full object-contain mix-blend-multiply group-hover:scale-110 transition-transform duration-500 ease-out"
        />
        
        {/* Badges */}
        <div className="absolute top-4 left-4 z-20 flex flex-col gap-2">
          {product.discount && (
            <span className="px-3 py-1 bg-red-500 text-white text-xs font-bold rounded-full shadow-sm">
              -{product.discount}%
            </span>
          )}
          {product.isNew && (
            <span className="px-3 py-1 bg-indigo-500 text-white text-xs font-bold rounded-full shadow-sm">
              NEW
            </span>
          )}
        </div>

        {/* Quick actions (hover) */}
        <div className="absolute right-4 top-4 z-20 translate-x-8 opacity-0 group-hover:translate-x-0 group-hover:opacity-100 transition-all duration-300 flex flex-col gap-2">
          <button onClick={stopNavigation} className="w-10 h-10 bg-white/90 backdrop-blur rounded-full flex items-center justify-center text-slate-600 hover:text-pink-500 hover:bg-white shadow-sm transition-colors">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>
          </button>
          <button onClick={stopNavigation} className="w-10 h-10 bg-white/90 backdrop-blur rounded-full flex items-center justify-center text-slate-600 hover:text-indigo-600 hover:bg-white shadow-sm transition-colors">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
          </button>
        </div>
      </div>

      {/* Content */}
      <div className="p-6 flex flex-col flex-grow">
        <div className="flex items-center gap-1 text-xs font-semibold text-indigo-600 mb-2 uppercase tracking-wider">
          {product.category || "Gadget"}
        </div>

        <h3 className="text-lg font-bold text-slate-900 line-clamp-2 mb-2 group-hover:text-indigo-600 transition-colors leading-snug">
          {product.title || product.name}
        </h3>

        <p className="text-slate-500 text-sm line-clamp-2 mb-6 flex-grow">
          {product.description || "Thiết kế hiện đại, tinh tế cùng hiệu năng vượt trội mang lại trải nghiệm tuyệt vời."}
        </p>

        <div className="mt-auto">
          <div className="flex items-end justify-between mb-5">
            <div className="flex flex-col">
              {product.originalPrice != null && (
                <span className="text-xs text-slate-400 line-through mb-0.5">
                  {formatVND(product.originalPrice)}
                </span>
              )}
              <span className="text-2xl font-extrabold text-slate-900 tracking-tight">
                {formatVND(product.price)}
              </span>
            </div>
            {product.rating && (
              <div className="flex items-center gap-1.5 bg-yellow-50 px-2 py-1 rounded-md">
                <svg className="w-4 h-4 text-yellow-400 fill-current" viewBox="0 0 20 20"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"></path></svg>
                <span className="text-sm font-bold text-yellow-700">{product.rating}</span>
              </div>
            )}
          </div>

          {isOutOfStock ? (
            <button
              disabled
              className="w-full bg-slate-100 text-slate-400 font-semibold py-3.5 px-4 rounded-xl cursor-not-allowed flex items-center justify-center gap-2"
            >
              Hết hàng
            </button>
          ) : (
            <button
              onClick={(event) => {
                stopNavigation(event);
                onAddToCart(product);
              }}
              className="w-full bg-slate-900 hover:bg-indigo-600 text-white font-semibold py-3.5 px-4 rounded-xl transition-all duration-300 transform active:scale-[0.98] flex items-center justify-center gap-2 shadow-md hover:shadow-indigo-200"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="9" cy="21" r="1"></circle><circle cx="20" cy="21" r="1"></circle><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path></svg>
              <span>Thêm vào giỏ</span>
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
