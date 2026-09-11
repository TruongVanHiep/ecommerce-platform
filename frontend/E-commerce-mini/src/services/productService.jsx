import axiosClient from "../axiosClient";

export const getProducts = async (params = {}) => {
    return axiosClient.get("/products", { params });
};

// Backend giới hạn tối đa 100 sản phẩm mỗi trang (ProductController, @Max(100))
// để chặn một request kéo cả database. Nơi nào cần TOÀN BỘ sản phẩm — trang chủ
// lọc và tìm kiếm phía client, trang quản trị — thì đi lần lượt từng trang tới hết.
//
// Trước đây mỗi nơi tự gọi getProducts theo kiểu riêng và đều hỏng theo cách riêng:
// trang chủ không truyền size nên backend dùng mặc định 39 — sản phẩm thứ 40 trở
// đi không bao giờ hiện, kể cả khi tìm kiếm; trang quản trị xin size=200 nên bị
// trả 400, kéo theo ô chọn danh mục trống trơn.
const PAGE_SIZE = 100;
const MAX_PAGES = 50; // chốt an toàn (5.000 sản phẩm), phòng API trả sai làm lặp mãi

export const getAllProducts = async () => {
    const all = [];
    for (let page = 0; page < MAX_PAGES; page++) {
        const res = await axiosClient.get("/products", { params: { page, size: PAGE_SIZE } });
        const data = res.result;
        all.push(...(data?.content || []));
        if (!data || data.last || page + 1 >= (data.totalPages ?? 0)) break;
    }
    return all;
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
