import axiosClient from "../axiosClient";

export const createPayment = async (orderId, method) => {
  return axiosClient.post(`/orders/${orderId}/payment`, { method });
};

export const getPayment = async (orderId) => {
  return axiosClient.get(`/orders/${orderId}/payment`);
};
