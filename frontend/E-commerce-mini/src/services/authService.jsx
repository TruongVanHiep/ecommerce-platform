import axiosClient from "../axiosClient";

// Hàm decode JWT để lấy user info từ token
export const decodeToken = (token) => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join('')
    );
    return JSON.parse(jsonPayload);
  } catch (err) {
    console.error("Error decoding token:", err);
    return null;
  }
};

export const login = async (data) => {
  const response = await axiosClient.post("/auth/login", data);
  return response;
};

export const register = async (data) => {
  const response = await axiosClient.post("/users", data);
  return response;
};

// Fetch user profile từ token
export const getUserProfile = async () => {
  const response = await axiosClient.get("/auth/profile");
  return response;
};




// Đổi refresh token lấy cặp token mới. Bình thường axiosClient tự gọi khi gặp
// 401, hàm này để dùng thủ công khi cần.
export const refreshAccessToken = async (refreshToken) => {
  const response = await axiosClient.post("/auth/refresh", { refreshToken });
  return response;
};

// Đăng xuất: báo backend thu hồi refresh token trong DB. Nếu chỉ xoá
// localStorage thì token vẫn còn hiệu lực với server cho tới khi hết hạn.
export const logoutApi = async (refreshToken) => {
  const response = await axiosClient.post("/auth/logout", { refreshToken });
  return response;
};
