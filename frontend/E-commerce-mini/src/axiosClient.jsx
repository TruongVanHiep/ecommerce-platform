import axios from "axios";

const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

const axiosClient = axios.create({
    baseURL: BASE_URL,
    headers: {
        "Content-Type": "application/json",
    },
});

// Instance riêng KHÔNG gắn interceptor, dùng để gọi /auth/refresh.
// Nếu dùng chung axiosClient thì lỗi 401 của chính request refresh sẽ lại
// kích hoạt interceptor refresh → lặp vô hạn.
const refreshClient = axios.create({
    baseURL: BASE_URL,
    headers: { "Content-Type": "application/json" },
});

// --- Hàng đợi khi đang refresh ---------------------------------------------
// Nhiều request cùng nhận 401 một lúc (vd trang chủ gọi 5 API song song).
// Nếu mỗi request tự gọi refresh thì token bị xoay vòng nhiều lần liên tiếp,
// các token trung gian bị thu hồi và backend sẽ coi là "dùng lại token" →
// huỷ sạch phiên đăng nhập. Vì vậy chỉ cho MỘT request đi refresh, số còn lại
// xếp hàng chờ kết quả.
let isRefreshing = false;
let pendingQueue = [];

const resolveQueue = (error, newToken = null) => {
    pendingQueue.forEach(({ resolve, reject }) => {
        if (error) reject(error);
        else resolve(newToken);
    });
    pendingQueue = [];
};

const clearSession = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
};

const redirectToLogin = () => {
    if (window.location.pathname !== "/login") {
        window.location.href = "/login";
    }
};

// Tự động gắn access token vào mọi request
axiosClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("token");
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

axiosClient.interceptors.response.use(
    // Backend trả về ApiResponse { code, result, message } — bóc sẵn .data để
    // component chỉ cần dùng response.result
    (response) => response.data,

    async (error) => {
        const originalRequest = error.config;
        const status = error.response?.status;

        // Chỉ xử lý 401, và mỗi request chỉ thử lại đúng 1 lần (_retry) để
        // không rơi vào vòng lặp nếu token mới vẫn bị từ chối.
        if (status !== 401 || !originalRequest || originalRequest._retry) {
            return Promise.reject(error);
        }

        const refreshToken = localStorage.getItem("refreshToken");
        if (!refreshToken) {
            clearSession();
            redirectToLogin();
            return Promise.reject(error);
        }

        // Đã có request khác đang refresh → xếp hàng chờ token mới
        if (isRefreshing) {
            return new Promise((resolve, reject) => {
                pendingQueue.push({ resolve, reject });
            })
                .then((newToken) => {
                    originalRequest._retry = true;
                    originalRequest.headers.Authorization = `Bearer ${newToken}`;
                    return axiosClient(originalRequest);
                });
        }

        isRefreshing = true;
        originalRequest._retry = true;

        try {
            const { data } = await refreshClient.post("/auth/refresh", { refreshToken });
            const newToken = data?.result?.token;
            const newRefreshToken = data?.result?.refreshToken;

            if (!newToken) throw new Error("Refresh không trả về token mới");

            localStorage.setItem("token", newToken);
            // Backend xoay vòng refresh token: mỗi lần refresh là cấp chuỗi mới,
            // chuỗi cũ bị thu hồi ngay nên bắt buộc phải lưu đè.
            if (newRefreshToken) {
                localStorage.setItem("refreshToken", newRefreshToken);
            }

            resolveQueue(null, newToken);

            originalRequest.headers.Authorization = `Bearer ${newToken}`;
            return axiosClient(originalRequest);
        } catch (refreshError) {
            // Refresh token hết hạn hoặc đã bị thu hồi → phải đăng nhập lại
            resolveQueue(refreshError, null);
            clearSession();
            redirectToLogin();
            return Promise.reject(refreshError);
        } finally {
            isRefreshing = false;
        }
    }
);

export default axiosClient;
