import { useEffect, useState, useContext } from "react";
import { useSearchParams } from "react-router-dom";
import { getProducts } from "../services/productService";
import ProductCard from "../components/ProductCard";
import { CartContext } from "../context/CartContext";

const journeySteps = [
    { label: "Đặt hàng", desc: "Chọn sản phẩm, thêm vào giỏ chỉ trong vài giây." },
    { label: "Xử lý", desc: "Đơn hàng được xác nhận và chuẩn bị ngay lập tức." },
    { label: "Giao hàng", desc: "Đối tác vận chuyển tiếp nhận và theo dõi realtime." },
    { label: "Nhận hàng", desc: "Kiểm tra, xác nhận và đánh giá trải nghiệm." },
];

const tickerEvents = [
    "order #10234 confirmed",
    "voucher SALE10 applied",
    "payment COD success",
    "product #58 stock updated",
    "order #10235 shipped",
    "review 5★ submitted",
    "payment VNPAY pending",
    "order #10236 delivered",
];

export default function HomePage() {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [sortBy, setSortBy] = useState("name");
    const [categoryFilter, setCategoryFilter] = useState("");

    const categoryOptions = [
        { value: "1", label: "Điện tử" },
        { value: "2", label: "Trang sức" },
        { value: "3", label: "Thời trang nam" },
        { value: "4", label: "Thời trang nữ" },
        { value: "5", label: "Phụ kiện" },
    ];

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

    const heroProduct = products[0];

    return (
        <div className="bg-bg-light">
            {/* ================= SECTION 1 — HERO (dark) ================= */}
            <section className="bg-bg-dark pt-16 pb-24 relative overflow-hidden">
                <div className="max-w-[1200px] mx-auto px-6 grid lg:grid-cols-2 gap-12 items-center">
                    <div className="animate-fade-in-up">
                        <h1 className="text-[40px] leading-[1.05] sm:text-6xl lg:text-[68px] font-semibold text-white tracking-tight">
                            Mua sắm,<br />nhân đôi trải nghiệm
                        </h1>
                        <p className="mt-6 text-lg text-text-muted-dark max-w-md">
                            Hàng ngàn sản phẩm chính hãng, giao nhanh, thanh toán an toàn — tất cả trong một nền tảng được đo lường minh bạch.
                        </p>
                        <div className="mt-9 flex flex-wrap gap-3">
                            <a href="#san-pham" className="h-12 px-7 rounded-full bg-accent hover:bg-accent-dark text-white text-sm font-semibold flex items-center transition-colors">
                                Khám phá ngay
                            </a>
                            <a href="#san-pham" className="h-12 px-7 rounded-full border border-white/15 hover:border-white/30 text-white text-sm font-semibold flex items-center transition-colors">
                                Xem ưu đãi
                            </a>
                        </div>
                    </div>

                    <div className="relative">
                        <div className="rounded-[28px] border border-white/10 bg-white/[0.03] p-3 shadow-2xl">
                            <div className="rounded-[20px] overflow-hidden aspect-[4/3] bg-bg-dark-soft flex items-center justify-center">
                                {heroProduct?.image ? (
                                    <img src={heroProduct.image} alt={heroProduct.name} className="w-full h-full object-contain mix-blend-luminosity opacity-90 p-6" />
                                ) : (
                                    <div className="w-24 h-24 rounded-2xl bg-accent/20 animate-float" />
                                )}
                            </div>
                        </div>
                        <div className="absolute -top-4 -right-4 w-20 h-20 rounded-2xl bg-accent/20 blur-2xl" />
                    </div>
                </div>

                {/* Trust strip */}
                <div className="max-w-[1200px] mx-auto px-6 mt-16 pt-8 border-t border-white/10">
                    <p className="text-xs uppercase tracking-[0.15em] text-text-muted-dark mb-5">
                        Được tin dùng cùng các đối tác
                    </p>
                    <div className="flex flex-wrap gap-x-10 gap-y-3 text-white/40 font-semibold text-sm">
                        <span>VNPAY</span>
                        <span>COD</span>
                        <span>GHTK</span>
                        <span>Giao Hàng Nhanh</span>
                        <span>Momo</span>
                    </div>
                </div>
            </section>

            {/* ================= SECTION 2 — HÀNH TRÌNH ĐƠN HÀNG (dark, node diagram) ================= */}
            <section className="bg-bg-dark pb-28">
                <div className="max-w-[1200px] mx-auto px-6">
                    <p className="text-center text-2xl sm:text-3xl text-white/90 font-medium max-w-2xl mx-auto leading-snug">
                        Mua sắm online không khó. Nó chỉ đang bị làm cho rối.
                    </p>

                    <div className="mt-20 relative">
                        <div className="hidden sm:block absolute top-3 left-[12%] right-[12%] h-px bg-gradient-to-r from-transparent via-accent/60 to-transparent" />
                        <div className="grid grid-cols-2 sm:grid-cols-4 gap-8">
                            {journeySteps.map((step) => (
                                <div key={step.label} className="text-center">
                                    <div className="relative flex justify-center mb-5">
                                        <span className="w-3 h-3 rounded-full bg-accent animate-pulse-dot" />
                                    </div>
                                    <h3 className="text-white font-semibold text-sm mb-1.5">{step.label}</h3>
                                    <p className="text-text-muted-dark text-xs leading-relaxed px-2">{step.desc}</p>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </section>

            {/* ================= SECTION 3 — TÍNH NĂNG (light) ================= */}
            <section className="bg-bg-light py-24">
                <div className="max-w-[1200px] mx-auto px-6">
                    <div className="flex justify-center mb-6">
                        <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-accent to-accent-dark animate-float" />
                    </div>
                    <h2 className="text-center text-3xl sm:text-4xl font-semibold text-slate-900 tracking-tight">
                        Trải nghiệm mua sắm, được nâng cấp
                    </h2>

                    <div className="mt-14 grid md:grid-cols-2 gap-6">
                        <div className="rounded-[24px] bg-bg-dark p-9">
                            <h3 className="text-white text-xl font-semibold mb-5">Giao hàng thông minh</h3>
                            <ul className="space-y-3 mb-8">
                                {["Theo dõi đơn hàng realtime", "Giao nhanh trong 24h nội thành", "Đổi trả dễ dàng trong 7 ngày"].map((item) => (
                                    <li key={item} className="flex items-start gap-2.5 text-text-muted-dark text-sm">
                                        <span className="mt-1.5 w-1.5 h-1.5 rounded-full bg-accent shrink-0" />
                                        {item}
                                    </li>
                                ))}
                            </ul>
                            <a href="#san-pham" className="text-sm font-semibold text-white border-b border-white/30 hover:border-white pb-0.5 transition-colors">
                                Tìm hiểu thêm
                            </a>
                        </div>

                        <div className="rounded-[24px] bg-accent p-9">
                            <h3 className="text-white text-xl font-semibold mb-5">Ưu đãi cá nhân hoá</h3>
                            <ul className="space-y-3 mb-8">
                                {["Voucher tự động áp dụng tốt nhất", "Gợi ý sản phẩm theo sở thích", "Tích điểm đổi quà mỗi đơn hàng"].map((item) => (
                                    <li key={item} className="flex items-start gap-2.5 text-white/85 text-sm">
                                        <span className="mt-1.5 w-1.5 h-1.5 rounded-full bg-white shrink-0" />
                                        {item}
                                    </li>
                                ))}
                            </ul>
                            <a href="#san-pham" className="text-sm font-semibold text-white border-b border-white/50 hover:border-white pb-0.5 transition-colors">
                                Tìm hiểu thêm
                            </a>
                        </div>
                    </div>
                </div>
            </section>

            {/* ================= SECTION 4 — QUY TRÌNH (light/kem) ================= */}
            <section className="bg-bg-light pb-24">
                <div className="max-w-[1200px] mx-auto px-6">
                    <h2 className="text-center text-3xl sm:text-4xl font-semibold text-slate-900 tracking-tight mb-16">
                        Từ tìm kiếm đến nhận hàng, chỉ vài bước
                    </h2>
                    <div className="grid sm:grid-cols-4 gap-5">
                        {journeySteps.map((step, i) => (
                            <div key={step.label} className="bg-white rounded-2xl border border-slate-200/70 p-6">
                                <span className="text-xs font-semibold text-accent">0{i + 1}</span>
                                <h3 className="mt-2 font-semibold text-slate-900">{step.label}</h3>
                                <p className="mt-1.5 text-sm text-text-muted-light leading-relaxed">{step.desc}</p>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* ================= SẢN PHẨM (khu vực mua sắm thật) ================= */}
            <section id="san-pham" className="bg-bg-light pb-24 scroll-mt-24">
                <div className="max-w-[1200px] mx-auto px-6">
                    <h2 className="text-3xl sm:text-4xl font-semibold text-slate-900 tracking-tight mb-10">
                        Sản phẩm nổi bật
                    </h2>

                    {/* Loading State */}
                    {loading && (
                        <div className="flex justify-center items-center py-32 animate-fade-in-up">
                            <div className="relative">
                                <div className="w-14 h-14 border-4 border-accent/15 rounded-full"></div>
                                <div className="w-14 h-14 border-4 border-accent rounded-full border-t-transparent animate-spin absolute top-0 left-0"></div>
                            </div>
                        </div>
                    )}

                    {/* Error State */}
                    {error && !loading && (
                        <div className="max-w-md mx-auto bg-white border border-red-200/60 text-red-600 p-6 rounded-2xl text-center animate-fade-in-up mt-8">
                            <p className="font-semibold mb-4">{error}</p>
                            <button
                                onClick={fetchProducts}
                                className="px-6 py-2 bg-red-50 hover:bg-red-100 text-red-700 rounded-full font-medium transition-colors text-sm"
                            >
                                Thử lại
                            </button>
                        </div>
                    )}

                    {/* Sort Bar */}
                    {!loading && !error && (
                        <div className="bg-white p-3 rounded-2xl border border-slate-200/70 mb-8 flex flex-wrap items-center justify-between gap-3">
                            <div className="flex flex-wrap items-center gap-3 text-sm">
                                <span className="text-text-muted-light mr-1">Sắp xếp theo</span>
                                <button
                                    onClick={() => setSortBy("name")}
                                    className={`px-4 py-2 rounded-full transition-colors ${sortBy === 'name' ? 'bg-accent text-white' : 'bg-slate-100 hover:bg-slate-200 text-slate-700'}`}
                                >
                                    Tên A-Z
                                </button>

                                <select
                                    value={sortBy.startsWith("price") ? sortBy : ""}
                                    onChange={(e) => setSortBy(e.target.value)}
                                    className={`px-4 py-2 rounded-full outline-none border border-slate-200 cursor-pointer text-sm ${sortBy.startsWith('price') ? 'text-accent' : 'text-slate-700'}`}
                                >
                                    <option value="" disabled>Giá</option>
                                    <option value="price-asc">Giá: Thấp đến Cao</option>
                                    <option value="price-desc">Giá: Cao đến Thấp</option>
                                </select>

                                <select
                                    value={categoryFilter}
                                    onChange={(e) => setCategoryFilter(e.target.value)}
                                    className={`px-4 py-2 rounded-full outline-none border border-slate-200 cursor-pointer text-sm ${categoryFilter ? 'text-accent' : 'text-slate-700'}`}
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
                                <div className="text-sm text-text-muted-light">
                                    Kết quả cho: <span className="text-accent font-semibold">"{searchTerm}"</span>
                                </div>
                            )}
                        </div>
                    )}

                    {/* Products Grid */}
                    {!loading && sortedProducts.length > 0 && (
                        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                            {sortedProducts.map((product, index) => (
                                <div key={product.id || index} style={{ animationDelay: `${index * 0.05}s`, animationFillMode: 'both' }} className="animate-fade-in-up">
                                    <ProductCard product={product} onAddToCart={handleAddToCart} />
                                </div>
                            ))}
                        </div>
                    )}

                    {/* Empty State */}
                    {!loading && sortedProducts.length === 0 && !error && (
                        <div className="bg-white text-center py-24 rounded-2xl border border-slate-200/70 animate-fade-in-up">
                            <h3 className="text-xl font-semibold text-slate-800 mb-2">Không tìm thấy sản phẩm</h3>
                            <p className="text-text-muted-light mb-6 max-w-md mx-auto text-sm">
                                Hãy thử sử dụng các từ khóa chung chung hơn hoặc xóa bộ lọc.
                            </p>
                            <a href="/" className="px-6 py-2.5 bg-accent hover:bg-accent-dark text-white rounded-full font-semibold transition-colors inline-block text-sm">
                                Trở lại trang chủ
                            </a>
                        </div>
                    )}
                </div>
            </section>

            {/* ================= SECTION 5 — SỐ LIỆU (dark) ================= */}
            <section className="bg-bg-dark py-24">
                <div className="max-w-[1200px] mx-auto px-6 text-center">
                    <p className="text-xs uppercase tracking-[0.15em] text-accent mb-4">
                        Vận hành minh bạch, đo lường được
                    </p>
                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-10 mt-10">
                        <div>
                            <div className="text-5xl font-semibold text-white tracking-tight">{products.length || "39"}+</div>
                            <div className="mt-2 text-text-muted-dark text-sm">Sản phẩm đang bán</div>
                        </div>
                        <div>
                            <div className="text-5xl font-semibold text-white tracking-tight">98%</div>
                            <div className="mt-2 text-text-muted-dark text-sm">Khách hàng hài lòng</div>
                        </div>
                        <div>
                            <div className="text-5xl font-semibold text-white tracking-tight">24h</div>
                            <div className="mt-2 text-text-muted-dark text-sm">Thời gian giao trung bình</div>
                        </div>
                    </div>
                </div>
            </section>

            {/* ================= SECTION 6 — LIVE TICKER (đen tuyệt đối) ================= */}
            <section className="bg-black py-5 overflow-hidden border-y border-white/5">
                <div className="flex whitespace-nowrap animate-marquee font-mono text-xs text-white/35">
                    {[...tickerEvents, ...tickerEvents].map((event, i) => (
                        <span key={i} className="mx-6 flex items-center gap-2">
                            <span className="w-1 h-1 rounded-full bg-accent" />
                            {event}
                        </span>
                    ))}
                </div>
            </section>

            {/* ================= SECTION 7 — TIN CẬY (dark) ================= */}
            <section className="bg-bg-dark py-24">
                <div className="max-w-[1200px] mx-auto px-6 text-center">
                    <h2 className="text-3xl sm:text-4xl font-semibold text-white tracking-tight mb-12">
                        An toàn cho mọi giao dịch
                    </h2>
                    <div className="flex flex-wrap justify-center gap-3">
                        {["Thanh toán mã hoá SSL", "Bảo vệ người mua", "Đối tác vận chuyển uy tín", "Chính sách đổi trả rõ ràng"].map((badge) => (
                            <span key={badge} className="px-5 py-2.5 rounded-full border border-white/10 text-sm text-white/80">
                                {badge}
                            </span>
                        ))}
                    </div>
                </div>
            </section>

            {/* ================= SECTION 8 — CTA ĐÓNG TRANG (accent full-bleed) ================= */}
            <section className="bg-accent py-24 text-center">
                <a href="#san-pham" className="inline-block h-11 px-6 rounded-full bg-white text-accent text-sm font-semibold mb-8 hover:bg-white/90 transition-colors">
                    Bắt đầu mua sắm
                </a>
                <h2 className="text-white font-semibold tracking-tight text-[15vw] sm:text-[10rem] leading-none">
                    MiniCommerce
                </h2>
            </section>
        </div>
    );
}
