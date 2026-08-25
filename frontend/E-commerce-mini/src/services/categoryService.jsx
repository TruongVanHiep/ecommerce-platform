import axiosClient from "../axiosClient";

export const getCategories = async () => {
  return axiosClient.get("/categories");
};

export const createCategory = async (data) => {
  return axiosClient.post("/categories", data);
};

export const updateCategory = async (id, data) => {
  return axiosClient.put(`/categories/${id}`, data);
};

export const deleteCategory = async (id) => {
  return axiosClient.delete(`/categories/${id}`);
};
