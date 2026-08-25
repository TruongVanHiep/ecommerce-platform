import axiosClient from "../axiosClient";

export const getReviewsByProduct = async (productId, page = 0, size = 10) => {
  return axiosClient.get(`/reviews/product/${productId}`, { params: { page, size } });
};

export const createReview = async (data) => {
  return axiosClient.post("/reviews", data);
};

export const updateReview = async (id, data) => {
  return axiosClient.put(`/reviews/${id}`, data);
};

export const deleteReview = async (id) => {
  return axiosClient.delete(`/reviews/${id}`);
};
