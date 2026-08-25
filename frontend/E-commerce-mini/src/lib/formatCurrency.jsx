export const formatVND = (value) => {
  const amount = Number(value) || 0;

  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(amount);
};