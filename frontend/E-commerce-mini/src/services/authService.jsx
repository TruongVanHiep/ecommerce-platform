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



