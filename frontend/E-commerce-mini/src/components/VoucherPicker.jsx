import { useEffect, useState } from "react";
import { getAvailableVouchers } from "../services/voucherService";

const formatVND = (n) => `${Number(n || 0).toLocaleString("vi-VN")}₫`;

const describeDiscount = (v) =>
    v.discountType === "PERCENT"
        ? `Giảm ${Number(v.discountValue)}%${v.maxDiscountAmount ? ` (tối đa ${formatVND(v.maxDiscountAmount)})` : ""}`
        : `Giảm ${formatVND(v.discountValue)}`;

/**
 * Danh sách voucher để người dùng bấm chọn trong giỏ hàng.
 *
 * Trước đây chỗ này chỉ có một ô nhập mã — người dùng phải tự biết mã từ đâu đó
 * mới dùng được, nên gần như không ai dùng. Giờ mã hiện sẵn, bấm là áp.
 *
 * Voucher chưa đủ điều kiện vẫn hiện (mờ đi, kèm "mua thêm X nữa") thay vì bị
 * ẩn: đó là thông tin thúc người dùng mua thêm, giấu đi thì mất tác dụng.
 */
export default function VoucherPicker({ cartTotal, appliedCode, onSelect, onRemove, applying }) {
    const [vouchers, setVouchers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        let cancelled = false;

        (async () => {
            setLoading(true);
            setError(null);
            try {
                const res = await getAvailableVouchers(cartTotal);
                // Giỏ hàng đổi liên tục khi người dùng bấm +/- số lượng, nên
                // request cũ có thể về sau request mới. Bỏ qua kết quả của lần
                // gọi đã bị thay thế, nếu không danh sách sẽ nhấp nháy sai.
                if (!cancelled) setVouchers(res?.result || []);
            } catch (err) {
                if (!cancelled) {
                    setError(err.response?.data?.message || "Không tải được danh sách voucher.");
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        })();

        return () => { cancelled = true; };
    }, [cartTotal]);

    if (loading) {
        return <p className="text-xs text-slate-400">Đang tải ưu đãi...</p>;
    }

    if (error) {
        return <p className="text-xs text-red-500 font-semibold">{error}</p>;
    }

    if (vouchers.length === 0) {
        return <p className="text-xs text-slate-400">Hiện chưa có ưu đãi nào dành cho bạn.</p>;
    }

    return (
        <ul className="space-y-2 max-h-72 overflow-y-auto pr-1">
            {vouchers.map((v) => {
                const isApplied = appliedCode === v.code;
                const selectable = v.eligible && !applying;

                return (
                    <li key={v.id}>
                        <button
                            type="button"
                            disabled={!selectable && !isApplied}
                            onClick={() => (isApplied ? onRemove() : onSelect(v))}
                            className={`w-full text-left flex items-center gap-3 px-3 py-2.5 rounded-xl border transition-all ${
                                isApplied
                                    ? "border-accent bg-accent/10"
                                    : v.eligible
                                        ? "border-slate-200 hover:border-accent hover:bg-accent/5 cursor-pointer"
                                        : "border-slate-200/70 bg-slate-50 opacity-70 cursor-not-allowed"
                            }`}
                        >
                            <div
                                className={`w-9 h-9 rounded-lg flex items-center justify-center text-white font-extrabold text-[10px] uppercase shrink-0 ${
                                    v.eligible ? "bg-accent" : "bg-slate-300"
                                }`}
                            >
                                Vé
                            </div>

                            <div className="min-w-0 flex-1">
                                <div className="flex items-center gap-2">
                                    <span className={`font-extrabold text-sm ${v.eligible ? "text-accent" : "text-slate-500"}`}>
                                        {v.code}
                                    </span>
                                    {isApplied && (
                                        <span className="text-[10px] font-bold text-accent uppercase">Đang áp dụng</span>
                                    )}
                                </div>
                                <span className="text-xs text-slate-600 block">{describeDiscount(v)}</span>

                                {v.eligible ? (
                                    <span className="text-xs text-green-600 font-semibold block mt-0.5">
                                        Tiết kiệm {formatVND(v.estimatedDiscount)}
                                    </span>
                                ) : (
                                    <span className="text-xs text-orange-600 font-semibold block mt-0.5">
                                        Mua thêm {formatVND(v.amountNeeded)} để dùng mã này
                                    </span>
                                )}

                                {v.description && (
                                    <span className="text-[11px] text-slate-400 block mt-0.5 truncate">{v.description}</span>
                                )}
                            </div>

                            {isApplied && <span className="text-accent text-lg shrink-0">✕</span>}
                        </button>
                    </li>
                );
            })}
        </ul>
    );
}
