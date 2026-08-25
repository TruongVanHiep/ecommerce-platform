import axiosClient from "../axiosClient";

export const createOrder = async ({ shippingAddress, phone, voucherCode, idempotencyKey }) => {
  return axiosClient.post("/orders", { shippingAddress, phone, voucherCode, idempotencyKey });
};

export const getMyOrders = async () => {
  return axiosClient.get("/orders/my-orders");
};

export const getOrderById = async (orderId) => {
  return axiosClient.get(`/orders/${orderId}`);
};

// Admin
export const getAllOrdersAdmin = async () => {
  return axiosClient.get("/admin/orders");
};

export const updateOrderStatusAdmin = async (orderId, status) => {
  return axiosClient.put(`/admin/orders/${orderId}/status`, { status });
};
