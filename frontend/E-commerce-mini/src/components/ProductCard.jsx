import { useNavigate } from "react-router-dom";
import { formatVND } from "../lib/formatCurrency";

// Trên điện thoại lưới sản phẩm có 2 cột, mỗi thẻ chỉ rộng khoảng 155px. Cỡ chữ
// và lề của bản máy tính (giá text-2xl, lề p-6) khiến giá "28.990.000 ₫" rộng hơn
// cả phần nội dung còn lại, đẩy thẻ phình ra ngoài cột và làm cả trang cuộn ngang.
// Nên mọi kích thước dưới đây đều có bản nhỏ cho màn hẹp, bản gốc từ sm trở lên.
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
      className="group flex flex-col h-full bg-white rounded-2xl sm:rounded-[20px] overflow-hidden border border-slate-200/70 hover:-translate-y-1 hover:border-accent/30 transition-all duration-300 cursor-pointer focus:outline-none focus:ring-2 focus:ring-accent focus:ring-offset-2"
    >
      {/* Image Container */}
      <div className="relative aspect-[4/3] overflow-hidden bg-slate-50 p-3 sm:p-6 flex items-center justify-center">
        {/* Abstract background blob for empty images */}
        <div className="absolute inset-0 bg-accent/5 opacity-0 group-hover:opacity-100 transition-opacity duration-500" />

        <img
          src={product.image || "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?q=80&w=600&auto=format&fit=crop"}
          alt={product.title || product.name}
          className="relative z-10 w-full h-full object-contain mix-blend-multiply group-hover:scale-110 transition-transform duration-500 ease-out"
        />

        {/* Badges */}
        <div className="absolute top-2 left-2 sm:top-4 sm:left-4 z-20 flex flex-col gap-2">
          {product.discount && (
            <span className="px-2 sm:px-3 py-0.5 sm:py-1 bg-white/90 backdrop-blur border border-slate-200 text-slate-700 text-[10px] sm:text-xs font-bold rounded-full">
              -{product.discount}%
            </span>
          )}
          {product.isNew && (
            <span className="px-2 sm:px-3 py-0.5 sm:py-1 bg-accent text-white text-[10px] sm:text-xs font-bold rounded-full shadow-sm">
              NEW
            </span>
          )}
        </div>

        {/* Quick actions (hover) — ẩn trên màn cảm ứng, nơi không có hover để hiện ra. */}
        <div className="absolute right-4 top-4 z-20 translate-x-8 opacity-0 group-hover:translate-x-0 group-hover:opacity-100 transition-all duration-300 hidden sm:flex flex-col gap-2">
          <button onClick={stopNavigation} aria-label="Yêu thích" className="w-10 h-10 bg-white/90 backdrop-blur rounded-full flex items-center justify-center text-slate-600 hover:text-pink-500 hover:bg-white shadow-sm transition-colors">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>
          </button>
          <button onClick={stopNavigation} aria-label="Xem nhanh" className="w-10 h-10 bg-white/90 backdrop-blur rounded-full flex items-center justify-center text-slate-600 hover:text-accent hover:bg-white shadow-sm transition-colors">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
          </button>
        </div>
      </div>

      {/* Content — min-w-0 cho phép co nhỏ hơn nội dung thay vì đẩy thẻ phình ra. */}
      <div className="p-3 sm:p-6 flex flex-col flex-grow min-w-0">
        <div className="flex items-center gap-1 text-[10px] sm:text-xs font-semibold text-accent mb-1 sm:mb-2 uppercase tracking-wider truncate">
          {product.category || "Gadget"}
        </div>

        <h3 className="text-sm sm:text-lg font-bold text-slate-900 line-clamp-2 mb-1 sm:mb-2 group-hover:text-accent transition-colors leading-snug">
          {product.title || product.name}
        </h3>

        {/* Mô tả chiếm 2 dòng mà màn điện thoại không đủ chỗ — ẩn đi, vào trang chi tiết vẫn đọc được. */}
        <p className="hidden sm:block text-slate-500 text-sm line-clamp-2 mb-6 flex-grow">
          {product.description || "Thiết kế hiện đại, tinh tế cùng hiệu năng vượt trội mang lại trải nghiệm tuyệt vời."}
        </p>

        <div className="mt-auto">
          <div className="flex items-end justify-between gap-2 mb-3 sm:mb-5">
            <div className="flex flex-col min-w-0">
              {product.originalPrice != null && (
                <span className="text-[10px] sm:text-xs text-slate-400 line-through mb-0.5">
                  {formatVND(product.originalPrice)}
                </span>
              )}
              <span className="text-base sm:text-2xl font-extrabold text-slate-900 tracking-tight">
                {formatVND(product.price)}
              </span>
            </div>
            {product.rating && (
              <div className="flex items-center gap-1 sm:gap-1.5 bg-yellow-50 px-1.5 sm:px-2 py-1 rounded-md shrink-0">
                <svg className="w-3.5 h-3.5 sm:w-4 sm:h-4 text-yellow-400 fill-current" viewBox="0 0 20 20"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"></path></svg>
                <span className="text-xs sm:text-sm font-bold text-yellow-700">{product.rating}</span>
              </div>
            )}
          </div>

          {isOutOfStock ? (
            <button
              disabled
              className="w-full bg-slate-100 text-slate-400 font-semibold py-2.5 sm:py-3.5 px-3 sm:px-4 text-sm sm:text-base rounded-full cursor-not-allowed flex items-center justify-center gap-2"
            >
              Hết hàng
            </button>
          ) : (
            <button
              onClick={(event) => {
                stopNavigation(event);
                onAddToCart(product);
              }}
              className="w-full bg-slate-900 hover:bg-accent text-white font-semibold py-2.5 sm:py-3.5 px-3 sm:px-4 text-sm sm:text-base rounded-full transition-all duration-300 transform active:scale-[0.98] flex items-center justify-center gap-2"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="hidden sm:block"><circle cx="9" cy="21" r="1"></circle><circle cx="20" cy="21" r="1"></circle><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path></svg>
              <span className="whitespace-nowrap">Thêm vào giỏ</span>
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
