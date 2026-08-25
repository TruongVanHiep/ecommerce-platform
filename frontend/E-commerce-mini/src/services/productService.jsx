import axiosClient from "../axiosClient";

export const getProducts = async (params = {}) => {
    return axiosClient.get("/products", { params });
};

export const getProductById = async (productId) => {
    try {
        const response = await axiosClient.get(`/products/${productId}`);
        return response.result || null;
    } catch (err) {
        if (err.response?.status === 404) return null;
        throw err;
    }
};

export const createProduct = async (data) => {
    return axiosClient.post("/products", data);
};

export const updateProduct = async (productId, data) => {
    return axiosClient.put(`/products/${productId}`, data);
};

export const deleteProduct = async (productId) => {
    return axiosClient.delete(`/products/${productId}`);
};
