import { createContext, useState, useEffect } from "react";
import { logoutApi } from "../services/authService";

export const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  // Initialize from localStorage on mount
  useEffect(() => {
    const storedToken = localStorage.getItem("token");
    const storedUser = localStorage.getItem("user");

    if (storedToken) {
      setToken(storedToken);
    }
    if (storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch (e) {
        console.error("Failed to parse user from localStorage", e);
      }
    }

    setLoading(false);
  }, []);

  const login = (userData, authToken, refreshToken) => {
    setUser(userData);
    setToken(authToken);
    localStorage.setItem("token", authToken);
    localStorage.setItem("user", JSON.stringify(userData));
    if (refreshToken) {
      localStorage.setItem("refreshToken", refreshToken);
    }
  };

  const logout = async () => {
    const refreshToken = localStorage.getItem("refreshToken");

    // Báo backend thu hồi refresh token trước khi xoá phía client. Bọc try/catch
    // để dù gọi API lỗi (mất mạng, token đã hết hạn) thì vẫn đăng xuất được
    // ở phía trình duyệt — không để người dùng mắc kẹt.
    if (refreshToken) {
      try {
        await logoutApi(refreshToken);
      } catch (err) {
        console.warn("Không thu hồi được refresh token phía server:", err);
      }
    }

    setUser(null);
    setToken(null);
    localStorage.removeItem("token");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
