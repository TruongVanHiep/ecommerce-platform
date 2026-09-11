import { useEffect, useRef, useState } from "react";
import { getPayment } from "../services/paymentService";
import { formatVND } from "../lib/formatCurrency";

const POLL_MS = 3000;
// Sau 15 phút mà chưa thấy tiền về thì ngừng hỏi server — giữ tab mở cả buổi
// thì cứ 3 giây lại một request vô ích.
const GIVE_UP_MS = 15 * 60 * 1000;

/**
 * Một dòng thông tin chuyển khoản.
 *
 * Khai báo ở ngoài SepayPaymentModal chứ không lồng bên trong: component định
 * nghĩa trong thân một component khác là một KIỂU MỚI sau mỗi lần render, nên
 * React tháo ra gắn lại từ đầu thay vì cập nhật. Modal này render lại mỗi 3 giây
 * theo nhịp hỏi server.
 */
function InfoRow({ label, value, copyable, highlight, copied, onCopy }) {
  return (
    <div className="flex justify-between items-center gap-3 py-2 border-b border-slate-100 last:border-0">
      <span className="text-slate-400 shrink-0">{label}</span>
      <span className="flex items-center gap-2 min-w-0">
        <span className={`font-bold truncate ${highlight ? "text-accent" : "text-slate-800"}`}>{value}</span>
        {copyable && (
          <button
            type="button"
            onClick={() => onCopy(label, value)}
            className="text-[10px] font-bold text-accent hover:text-accent-dark shrink-0"
          >
            {copied === label ? "Đã chép" : "Chép"}
          </button>
        )}
      </span>
    </div>
  );
}

/**
 * Hiện mã VietQR để khách chuyển khoản, rồi hỏi lại trạng thái thanh toán mỗi
 * 3 giây cho tới khi webhook SePay xác nhận tiền đã về.
 *
 * Hỏi định kỳ chứ không dùng WebSocket: thời gian chờ tính bằng phút, vài giây
 * trễ không ai để ý, mà không phải dựng thêm hạ tầng.
 */
export default function SepayPaymentModal({ orderId, payment, onPaid, onClose }) {
  const [status, setStatus] = useState(payment?.status || "PENDING");
  const [timedOut, setTimedOut] = useState(false);
  const [copied, setCopied] = useState("");

  // Mốc bắt đầu chờ. Gán trong effect chứ không phải lúc render: render phải là
  // hàm thuần, gọi Date.now() ở đó thì mỗi lần render cho một kết quả khác.
  const startedAt = useRef(null);

  // Giữ onPaid mới nhất trong ref, cập nhật SAU mỗi lần render. Component cha
  // tạo hàm mới mỗi lần render; đưa thẳng vào dependency của effect gọi onPaid
  // thì nó bị gọi lặp đi lặp lại.
  const onPaidRef = useRef(onPaid);
  useEffect(() => {
    onPaidRef.current = onPaid;
  }, [onPaid]);

  useEffect(() => {
    if (status === "SUCCESS" || timedOut) return;
    if (startedAt.current === null) startedAt.current = Date.now();

    let cancelled = false;
    const timer = setInterval(async () => {
      if (Date.now() - startedAt.current > GIVE_UP_MS) {
        clearInterval(timer);
        if (!cancelled) setTimedOut(true);
        return;
      }
      try {
        const res = await getPayment(orderId);
        if (!cancelled && res?.result?.status) setStatus(res.result.status);
      } catch {
        // Lỗi mạng thoáng qua: bỏ qua, lần hỏi sau thử lại.
      }
    }, POLL_MS);

    return () => {
      cancelled = true;
      clearInterval(timer);
    };
  }, [orderId, status, timedOut]);

  useEffect(() => {
    if (status === "SUCCESS") onPaidRef.current?.();
  }, [status]);

  const copy = async (label, value) => {
    try {
      await navigator.clipboard.writeText(String(value));
      setCopied(label);
      setTimeout(() => setCopied(""), 1500);
    } catch {
      // Trình duyệt chặn clipboard (http, iframe...): người dùng vẫn tự chọn chữ được.
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4 animate-fade-in">
      <div className="bg-white rounded-[24px] w-full max-w-sm p-6 border border-slate-200 animate-scale-up max-h-[95vh] overflow-y-auto">
        <h3 className="text-lg font-bold text-slate-900 text-center">Chuyển khoản để hoàn tất</h3>
        <p className="text-[11px] text-slate-400 text-center mt-1 mb-4">
          Đơn hàng <strong className="text-accent">#{orderId}</strong> — quét mã bằng ứng dụng ngân hàng
        </p>

        {payment?.qrUrl && (
          <img
            src={payment.qrUrl}
            alt={`Mã QR chuyển khoản cho đơn #${orderId}`}
            className="w-full max-w-[260px] mx-auto rounded-xl border border-slate-100"
          />
        )}

        <div className="bg-slate-50 rounded-xl px-4 py-1 mt-4 text-xs border border-slate-100">
          <InfoRow label="Ngân hàng" value={payment?.bankCode} copied={copied} onCopy={copy} />
          <InfoRow label="Số tài khoản" value={payment?.accountNumber} copyable copied={copied} onCopy={copy} />
          {payment?.accountName && (
            <InfoRow label="Chủ tài khoản" value={payment.accountName} copied={copied} onCopy={copy} />
          )}
          <InfoRow label="Số tiền" value={formatVND(payment?.amount)} highlight copied={copied} onCopy={copy} />
          <InfoRow
            label="Nội dung"
            value={payment?.transferContent}
            copyable
            highlight
            copied={copied}
            onCopy={copy}
          />
        </div>

        <p className="text-[11px] text-amber-600 bg-amber-50 border border-amber-100 rounded-lg px-3 py-2 mt-3">
          Nếu chuyển tay, ghi <strong>đúng nội dung {payment?.transferContent}</strong> — hệ thống dựa vào nội
          dung này để nhận ra đơn của bạn.
        </p>

        <div className="mt-4 text-center text-xs">
          {timedOut ? (
            <p className="text-slate-500">
              Chưa nhận được thanh toán. Đơn hàng vẫn được giữ — nếu bạn đã chuyển, trạng thái sẽ tự cập nhật
              trong mục Lịch sử đơn hàng.
            </p>
          ) : (
            <p className="text-slate-500 flex items-center justify-center gap-2">
              <span className="w-2 h-2 rounded-full bg-accent animate-pulse" />
              Đang chờ ngân hàng xác nhận...
            </p>
          )}
        </div>

        <button
          type="button"
          onClick={onClose}
          className="w-full mt-5 border border-slate-200 hover:bg-slate-50 text-slate-700 font-bold py-2.5 rounded-full transition-all text-xs"
        >
          ĐỂ SAU
        </button>
      </div>
    </div>
  );
}
