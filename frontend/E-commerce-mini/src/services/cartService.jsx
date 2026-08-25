import axiosClient from "../axiosClient";

export const getCart = async () => {
  return axiosClient.get("/cart");
};

export const addItem = async (productId, quantity) => {
  return axiosClient.post("/cart/items", { productId, quantity });
};

export const updateItem = async (cartItemId, quantity) => {
  return axiosClient.put(`/cart/items/${cartItemId}`, { quantity });
};

export const removeItem = async (cartItemId) => {
  return axiosClient.delete(`/cart/items/${cartItemId}`);
};
