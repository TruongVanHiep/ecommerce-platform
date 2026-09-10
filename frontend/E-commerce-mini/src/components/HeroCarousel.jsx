import { useEffect, useState, useCallback, useRef } from "react";
import { useNavigate } from "react-router-dom";

const AUTOPLAY_MS = 5000;

/**
 * Carousel ảnh sản phẩm ở khu hero trang chủ.
 *
 * Ảnh lấy thẳng từ danh sách sản phẩm trong database chứ không nhúng file
 * tĩnh: admin thêm sản phẩm mới là carousel tự đổi theo, và bấm vào ảnh đi
 * thẳng tới trang chi tiết sản phẩm đó.
 */
export default function HeroCarousel({ products = [], count = 5 }) {
    const [index, setIndex] = useState(0);
    // paused: tạm dừng do rê chuột/focus vào. playing: người dùng chủ động bật
    // tắt bằng nút. Tách riêng để rê chuột không làm mất lựa chọn của họ.
    const [paused, setPaused] = useState(false);
    const [playing, setPlaying] = useState(true);
    // Ảnh sản phẩm trỏ tới website bên thứ ba, một số chặn hotlink (trả về
    // ERR_BLOCKED_BY_RESPONSE) nên không tải được. Ảnh hỏng mà vẫn giữ slide
    // thì người dùng thấy một khung trống và tưởng trang lỗi — loại nó ra.
    const [failedIds, setFailedIds] = useState(() => new Set());
    const navigate = useNavigate();

    const slides = products
        .filter((p) => !failedIds.has(p.id))
        .slice(0, count);

    // Người dùng bật "giảm chuyển động" trong hệ điều hành (Windows:
    // Accessibility > Visual effects > Animation effects).
    //
    // Trước đây gặp cờ này thì tắt luôn tự chạy. Nhưng nhiều máy bật sẵn mặc
    // định, khiến carousel đứng im và trông như hỏng. Giờ vẫn tự chuyển, chỉ bỏ
    // hiệu ứng mờ dần — thứ mà thiết lập này nhắm tới là CHUYỂN ĐỘNG, không
    // phải việc nội dung thay đổi. Ai thực sự muốn nó đứng yên thì bấm nút tạm
    // dừng bên dưới.
    const reducedMotion = useRef(
        typeof window !== "undefined" &&
        window.matchMedia?.("(prefers-reduced-motion: reduce)").matches
    ).current;

    const go = useCallback(
        (next) => {
            if (slides.length === 0) return;
            setIndex(((next % slides.length) + slides.length) % slides.length);
        },
        [slides.length]
    );

    // Danh sách sản phẩm tải xong sau carousel nên độ dài có thể đổi; kẹp lại
    // để không trỏ ra ngoài mảng.
    useEffect(() => {
        if (index >= slides.length) setIndex(0);
    }, [slides.length, index]);

    useEffect(() => {
        if (paused || !playing || slides.length < 2) return;
        const timer = setInterval(() => setIndex((i) => (i + 1) % slides.length), AUTOPLAY_MS);
        return () => clearInterval(timer);
    }, [paused, playing, slides.length]);

    if (slides.length === 0) {
        return (
            <div className="rounded-[28px] border border-white/10 bg-white/[0.03] p-3 shadow-2xl">
                <div className="rounded-[20px] overflow-hidden aspect-[4/3] bg-bg-dark-soft flex items-center justify-center">
                    <div className="w-24 h-24 rounded-2xl bg-accent/20 animate-float" />
                </div>
            </div>
        );
    }

    const current = slides[index];

    return (
        <div
            className="rounded-[28px] border border-white/10 bg-white/[0.03] p-3 shadow-2xl"
            onMouseEnter={() => setPaused(true)}
            onMouseLeave={() => setPaused(false)}
            onFocusCapture={() => setPaused(true)}
            onBlurCapture={() => setPaused(false)}
            role="region"
            aria-roledescription="carousel"
            aria-label="Sản phẩm nổi bật"
        >
            <div className="relative rounded-[20px] overflow-hidden aspect-[4/3] bg-bg-dark-soft">
                {slides.map((p, i) => (
                    <button
                        key={p.id}
                        type="button"
                        onClick={() => navigate(`/products/${p.id}`)}
                        // Tất cả slide luôn nằm trong DOM và chỉ đổi opacity: ảnh
                        // được tải sẵn nên chuyển slide không bị chớp trắng.
                        className={`absolute inset-0 w-full h-full ${
                            reducedMotion ? "" : "transition-opacity duration-700 ease-out"
                        } ${i === index ? "opacity-100" : "opacity-0 pointer-events-none"}`}
                        aria-hidden={i !== index}
                        tabIndex={i === index ? 0 : -1}
                        aria-label={`Xem chi tiết ${p.name}`}
                    >
                        <img
                            src={p.image}
                            alt={p.name}
                            // Tải sẵn hết thay vì lazy: chỉ 5 ảnh, mà lazy khiến
                            // ảnh chưa từng hiện sẽ không kịp báo lỗi để loại ra.
                            loading="eager"
                            onError={() =>
                                setFailedIds((prev) => {
                                    if (prev.has(p.id)) return prev;
                                    const next = new Set(prev);
                                    next.add(p.id);
                                    return next;
                                })
                            }
                            className="w-full h-full object-contain mix-blend-luminosity opacity-90 p-6"
                        />
                    </button>
                ))}

                {slides.length > 1 && (
                    <>
                        <button
                            type="button"
                            onClick={() => go(index - 1)}
                            aria-label="Ảnh trước"
                            className="absolute left-3 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-black/40 hover:bg-black/60 border border-white/15 text-white flex items-center justify-center transition-colors backdrop-blur-sm"
                        >
                            ‹
                        </button>
                        <button
                            type="button"
                            onClick={() => go(index + 1)}
                            aria-label="Ảnh kế tiếp"
                            className="absolute right-3 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-black/40 hover:bg-black/60 border border-white/15 text-white flex items-center justify-center transition-colors backdrop-blur-sm"
                        >
                            ›
                        </button>
                    </>
                )}

                <div className="absolute left-0 right-0 bottom-0 p-4 bg-gradient-to-t from-black/70 to-transparent pointer-events-none">
                    <p className="text-white text-sm font-semibold truncate">{current.name}</p>
                    <p className="text-white/60 text-xs mt-0.5">
                        {Number(current.price).toLocaleString("vi-VN")} ₫
                    </p>
                </div>
            </div>

            {slides.length > 1 && (
                <div className="flex justify-center items-center gap-2 pt-3">
                    <div className="flex gap-2" role="tablist" aria-label="Chọn ảnh">
                        {slides.map((p, i) => (
                            <button
                                key={p.id}
                                type="button"
                                role="tab"
                                aria-selected={i === index}
                                aria-label={`Ảnh ${i + 1} trên ${slides.length}`}
                                onClick={() => go(i)}
                                className={`h-1.5 rounded-full transition-all ${
                                    i === index ? "w-6 bg-accent" : "w-1.5 bg-white/25 hover:bg-white/40"
                                }`}
                            />
                        ))}
                    </div>

                    {/* Bắt buộc phải có khi nội dung tự chuyển: người dùng cần
                        cách dừng lại để kịp đọc, và đây là lối thoát cho ai bật
                        "giảm chuyển động" mà vẫn thấy phiền. */}
                    <button
                        type="button"
                        onClick={() => setPlaying((v) => !v)}
                        aria-label={playing ? "Tạm dừng tự chuyển ảnh" : "Tự chuyển ảnh"}
                        className="ml-2 w-5 h-5 rounded-full text-white/40 hover:text-white/80 text-[10px] leading-none flex items-center justify-center transition-colors"
                    >
                        {playing ? "❚❚" : "▶"}
                    </button>
                </div>
            )}
        </div>
    );
}
