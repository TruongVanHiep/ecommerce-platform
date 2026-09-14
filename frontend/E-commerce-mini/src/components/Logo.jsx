/**
 * Logo Shopyora: ba vạch nghiêng tăng dần + chữ "shopyora".
 *
 * Mọi kích thước suy ra từ bản thiết kế gốc (vạch rộng 11px, cao 26/36/46px,
 * cách nhau 5px, chữ 38px, khoảng giữa vạch và chữ 20px) rồi nhân cùng một tỉ lệ,
 * để ở kích thước nào vạch và chữ cũng giữ đúng tương quan như bản gốc.
 */
const SCALE = { sm: 0.5, md: 0.63, lg: 1 };
const BAR_HEIGHTS = [26, 36, 46];

const TONES = {
  // Cho nền tối (header, footer) — đúng màu của bản thiết kế.
  light: {
    text: "oklch(0.98 0.005 265)",
    bars: ["oklch(0.62 0.19 255)", "oklch(0.72 0.16 245)", "oklch(0.86 0.11 235)"],
  },
  // Cho nền sáng. Vạch thứ ba của bản gốc (độ sáng 0.86) đặt lên nền trắng gần
  // như biến mất, nên hạ độ sáng cả ba vạch nhưng giữ nguyên hướng màu và thứ
  // tự đậm -> nhạt, và đổi chữ sang màu tối.
  dark: {
    text: "oklch(0.18 0.03 265)",
    bars: ["oklch(0.52 0.19 260)", "oklch(0.60 0.18 250)", "oklch(0.70 0.15 240)"],
  },
};

export default function Logo({ size = "md", tone = "light", className = "" }) {
  const s = SCALE[size] ?? SCALE.md;
  const { text, bars } = TONES[tone] ?? TONES.light;

  return (
    <span className={`inline-flex items-center ${className}`} style={{ gap: 20 * s }}>
      <span aria-hidden="true" className="flex items-end" style={{ gap: 5 * s, height: 46 * s }}>
        {BAR_HEIGHTS.map((h, i) => (
          <span
            key={h}
            style={{ width: 11 * s, height: h * s, background: bars[i], transform: "skewX(-14deg)" }}
          />
        ))}
      </span>
      <span
        className="font-brand font-bold leading-none"
        style={{ fontSize: 38 * s, letterSpacing: "-0.035em", color: text }}
      >
        shopyora
      </span>
    </span>
  );
}
