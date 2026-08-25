import { useEffect, useState, useContext } from "react";
import { useSearchParams } from "react-router-dom";
import { getProducts } from "../services/productService";
import ProductCard from "../components/ProductCard";
import { CartContext } from "../context/CartContext";

export default function HomePage() {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [sortBy, setSortBy] = useState("name");
    const [categoryFilter, setCategoryFilter] = useState("");
    const [currentSlide, setCurrentSlide] = useState(0);


    const categoryOptions = [
        { value: "1", label: "Điện tử" },
        { value: "2", label: "Trang sức" },
        { value: "3", label: "Thời trang nam" },
        { value: "4", label: "Thời trang nữ" },
        { value: "5", label: "Phụ kiện" },
    ];

    const bannerSlides = [
        {
            src: "https://img7.thuthuatphanmem.vn/uploads/2023/05/25/anh-bia-facebook-ban-quan-ao-nam_101940967.png",
            alt: "Banner thời trang nam",
        },
        {
            src: "https://thuthuatnhanh.com/wp-content/uploads/2022/06/Hinh-anh-sale-dep-nhat.png",
            alt: "Banner sale",
        },
        {
            src: "https://cloudify.vn/wp-content/uploads/2022/01/thuong-mai-dien-tu.png",
            alt: "Banner thương mại điện tử",
        },
        {
            src: "https://owa.bestprice.vn/images/articles/uploads/huong-dan-cach-mua-do-dien-tu-o-thai-lan-chat-luong-nhat-gia-tot-nhat-5e821e1c413fb.jpg",
            alt: "Banner mua đồ điện tử",
        },
        {
            src: "https://cdn.tgdd.vn/Files/2022/02/23/1416901/caidat.jpg",
            alt: "Banner cài đặt",
        },
        {
            src: "https://file.hstatic.net/1000381168/file/1920x820px_9a7b0e87a5da4b178cf2aacb00736015.jpg",
            alt: "Banner khuyến mãi",
        },
        {
            src: "https://kytoc.vn/wp-content/uploads/2025/03/bagg-min.png",
            alt: "Banner bag",
        },
    ];

    const voucherImage = "https://png.pngtree.com/png-clipart/20231203/ourmid/pngtree-sale-20-percent-off-design-png-image_10866185.png";
    const freeshipImage = "https://vimi.com.vn/wp-content/uploads/2022/04/Freeship-la-gi-2_vimi.com_.vn_.jpg";

    // Đọc query param từ URL để lọc
    const [searchParams] = useSearchParams();
    const searchTerm = searchParams.get("q") || "";

    const fetchProducts = async () => {
        try {
            setLoading(true);
            setError(null);
            const response = await getProducts();
            setProducts(response.result?.content || []);
        } catch (error) {
            console.error("Error fetching products:", error);
            setError("Không thể tải sản phẩm. Vui lòng thử lại.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const timeoutId = window.setTimeout(() => {
            void fetchProducts();
        }, 0);
        return () => window.clearTimeout(timeoutId);
    }, []);

    useEffect(() => {
        if (bannerSlides.length <= 1) return undefined;

        const interval = window.setInterval(() => {
            setCurrentSlide((previous) => (previous + 1) % bannerSlides.length);
        }, 4000);

        return () => window.clearInterval(interval);
    }, [bannerSlides.length]);

    const { addToCart } = useContext(CartContext);

    const handleAddToCart = (product) => {
        addToCart(product, 1);
    };

    const filteredProducts = products.filter((p) =>
        (p.title || p.name)?.toLowerCase().includes(searchTerm.toLowerCase()) &&
        (!categoryFilter || String(p.categoryId ?? "") === categoryFilter)
    );

    const sortedProducts = [...filteredProducts].sort((a, b) => {
        if (sortBy === "price-asc") return a.price - b.price;
        if (sortBy === "price-desc") return b.price - a.price;
        return (a.title || a.name)?.localeCompare(b.title || b.name);
    });

    return (
        <div className="min-h-screen bg-[#f5f5f5] relative overflow-hidden">
            {/* Main Content Area */}
            <main className="max-w-7xl mx-auto px-4 sm:px-6 pb-24 mt-8 relative z-10">

                {/* Banner Section (Optional Shopee-style carousel placeholder) */}
                <div className="w-full bg-white rounded-xl shadow-sm p-4 mb-6 flex flex-col lg:flex-row gap-4 h-auto lg:h-[300px] animate-fade-in-up">
                    <div className="flex-1 relative min-h-[240px] lg:h-full overflow-hidden rounded-xl bg-slate-100">
                        {bannerSlides.map((slide, index) => (
                            <img
                                key={slide.src}
                                src={slide.src}
                                alt={slide.alt}
                                className={`absolute inset-0 h-full w-full object-cover transition-all duration-700 ease-out ${index === currentSlide ? "opacity-100 scale-100" : "opacity-0 scale-105"}`}
                            />
                        ))}

                        <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/55 to-transparent p-4 sm:p-6">
                            <div className="flex items-center justify-between gap-4">
                                <div>
                                    <p className="text-xs sm:text-sm uppercase tracking-[0.3em] text-white/70">Siêu Sale Hàng Tháng</p>
                                    <h2 className="mt-1 text-2xl sm:text-3xl font-black text-white">Ưu đãi nổi bật mỗi ngày</h2>
                                </div>
                                <div className="hidden sm:flex items-center gap-2">
                                    {bannerSlides.map((slide, index) => (
                                        <button
                                            key={slide.src}
                                            type="button"
                                            onClick={() => setCurrentSlide(index)}
                                            className={`h-2.5 rounded-full transition-all ${index === currentSlide ? "w-8 bg-white" : "w-2.5 bg-white/50 hover:bg-white/75"}`}
                                            aria-label={`Chuyển đến banner ${index + 1}`}
                                        />
                                    ))}
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="w-full lg:w-1/3 flex flex-row lg:flex-col gap-4 min-h-[220px] lg:min-h-0">
                        <div className="flex-1 rounded-xl overflow-hidden bg-slate-100 shadow-sm">
                            <img
                                src={voucherImage}
                                alt="Voucher 50K"
                                className="h-full w-full object-cover"
                            />
                        </div>
                        <div className="flex-1 rounded-xl overflow-hidden bg-slate-100 shadow-sm">
                            <img
                                src={freeshipImage}
                                alt="Freeship Xtra"
                                className="h-full w-full object-cover"
                            />
                        </div>
                    </div>
                </div>

                {/* Loading State */}
                {loading && (
                    <div className="flex justify-center items-center py-32 animate-fade-in-up">
                        <div className="relative">
                            <div className="w-16 h-16 border-4 border-indigo-100 rounded-full"></div>
                            <div className="w-16 h-16 border-4 border-indigo-600 rounded-full border-t-transparent animate-spin absolute top-0 left-0"></div>
                        </div>
                    </div>
                )}

                {/* Error State */}
                {error && !loading && (
                    <div className="max-w-md mx-auto bg-white border border-red-200/50 text-red-600 p-6 rounded-sm shadow-sm text-center animate-fade-in-up mt-8">
                        <div className="w-12 h-12 bg-red-50 text-red-500 rounded-full flex items-center justify-center mx-auto mb-4">
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                        </div>
                        <p className="font-semibold mb-4">{error}</p>
                        <button
                            onClick={fetchProducts}
                            className="px-6 py-2 bg-red-50 hover:bg-red-100 text-red-700 rounded-sm font-medium transition-colors"
                        >
                            Thử lại
                        </button>
                    </div>
                )}

                {/* Shopee-style Sort Bar */}
                {!loading && !error && (
                    <div className="bg-white p-3 rounded-sm shadow-sm mb-6 flex items-center justify-between animate-fade-in-up">
                        <div className="flex items-center gap-4 text-sm">
                            <span className="text-slate-500 mr-2">Sắp xếp theo</span>
                            <button
                                onClick={() => setSortBy("name")}
                                className={`px-4 py-2 rounded-sm transition-colors ${sortBy === 'name' ? 'bg-indigo-600 text-white' : 'bg-slate-100 hover:bg-slate-200 text-slate-700'}`}
                            >
                                Phổ biến
                            </button>
                            <button
                                className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-sm transition-colors"
                            >
                                Mới nhất
                            </button>
                            <button
                                className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-sm transition-colors"
                            >
                                Bán chạy
                            </button>

                            <select
                                value={sortBy.startsWith("price") ? sortBy : ""}
                                onChange={(e) => setSortBy(e.target.value)}
                                className={`px-4 py-2 rounded-sm outline-none border border-slate-200 cursor-pointer ${sortBy.startsWith('price') ? 'text-indigo-600' : 'text-slate-700'}`}
                            >
                                <option value="" disabled>Giá</option>
                                <option value="price-asc">Giá: Thấp đến Cao</option>
                                <option value="price-desc">Giá: Cao đến Thấp</option>
                            </select>

                            <select
                                value={categoryFilter}
                                onChange={(e) => setCategoryFilter(e.target.value)}
                                className={`px-4 py-2 rounded-sm outline-none border border-slate-200 cursor-pointer ${categoryFilter ? 'text-indigo-600' : 'text-slate-700'}`}
                            >
                                <option value="">Tất cả danh mục</option>
                                {categoryOptions.map((option) => (
                                    <option key={option.value} value={option.value}>
                                        {option.label}
                                    </option>
                                ))}
                            </select>
                        </div>

                        {searchTerm && (
                            <div className="text-sm text-slate-600">
                                Kết quả tìm kiếm cho: <span className="text-indigo-600 font-bold">"{searchTerm}"</span>
                            </div>
                        )}
                    </div>
                )}

                {/* Products Grid */}
                {!loading && sortedProducts.length > 0 && (
                    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-3">
                        {sortedProducts.map((product, index) => (
                            <div key={product.id || index} style={{ animationDelay: `${index * 0.05}s`, animationFillMode: 'both' }} className="animate-fade-in-up">
                                <ProductCard product={product} onAddToCart={handleAddToCart} />
                            </div>
                        ))}
                    </div>
                )}

                {/* Empty State */}
                {!loading && sortedProducts.length === 0 && !error && (
                    <div className="bg-white text-center py-24 rounded-sm shadow-sm animate-fade-in-up">
                        <div className="text-6xl mb-6 opacity-50">🔍</div>
                        <h3 className="text-xl font-bold text-slate-800 mb-2">Không tìm thấy sản phẩm</h3>
                        <p className="text-slate-500 mb-6 max-w-md mx-auto text-sm">
                            Hãy thử sử dụng các từ khóa chung chung hơn hoặc xóa bộ lọc.
                        </p>
                        <a
                            href="/"
                            className="px-6 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-sm font-semibold transition-all inline-block"
                        >
                            Trở lại trang chủ
                        </a>
                    </div>
                )}
            </main>

        </div>
    );
}






