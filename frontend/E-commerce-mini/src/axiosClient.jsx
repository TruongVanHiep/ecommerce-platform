import axios from "axios";

const axiosClient = axios.create({
    baseURL: "http://localhost:8080/api",
    headers: {
        "Content-Type": "application/json",
    },
});

// Thiết lập Interceptor: Tự động gắn Token vào header trước khi gửi request
axiosClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("token"); // Lấy token đã lưu khi login thành công

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Thiết lập Interceptor: Xử lý dữ liệu trả về hoặc bắt lỗi tập trung (như lỗi 401, 403)
axiosClient.interceptors.response.use(
    (response) => {
        // Backend của bạn trả về ApiResponse { code, result, message }
        // Ở đây mình lấy luôn .data để ở component chỉ cần dùng response.result
        return response.data;
    },
    (error) => {
        if (error.response && error.response.status === 401) {
            // Token hết hạn hoặc không hợp lệ: xoá phiên và đưa về trang login
            console.error("Phiên đăng nhập hết hạn!");
            localStorage.removeItem("token");
            localStorage.removeItem("user");
            if (window.location.pathname !== "/login") {
                window.location.href = "/login";
            }
        }
        return Promise.reject(error);
    }
);




export default axiosClient;