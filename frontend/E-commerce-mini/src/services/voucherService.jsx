import axiosClient from "../axiosClient";

export const applyVoucher = async (code, orderTotal) => {
  return axiosClient.post("/vouchers/apply", { code, orderTotal });
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
