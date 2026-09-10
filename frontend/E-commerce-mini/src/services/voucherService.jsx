import axiosClient from "../axiosClient";

export const applyVoucher = async (code, orderTotal) => {
  return axiosClient.post("/vouchers/apply", { code, orderTotal });
};

/**
 * Voucher người dùng có thể chọn cho giỏ hàng hiện tại.
 *
 * Khác getAllVouchers (chỉ ADMIN gọi được, trả về mọi voucher kể cả đã tắt):
 * hàm này chỉ trả về mã còn hiệu lực, bỏ mã đã dùng, và backend tính sẵn số
 * tiền được giảm theo giá trị giỏ hàng.
 */
export const getAvailableVouchers = async (orderTotal) => {
  return axiosClient.get("/vouchers/available", { params: { orderTotal } });
};

export const getAllVouchers = async () => {
  return axiosClient.get("/vouchers");
};

export const createVoucher = async (data) => {
  return axiosClient.post("/vouchers", data);
};

export const updateVoucher = async (id, data) => {
  return axiosClient.put(`/vouchers/${id}`, data);
};

export const deleteVoucher = async (id) => {
  return axiosClient.delete(`/vouchers/${id}`);
};
