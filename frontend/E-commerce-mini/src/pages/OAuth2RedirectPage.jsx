import { useContext, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";
import { decodeToken } from "../services/authService";

export default function OAuth2RedirectPage() {
  const navigate = useNavigate();
  const { login: contextLogin } = useContext(AuthContext);
  const handled = useRef(false);

  useEffect(() => {
    if (handled.current) return;
    handled.current = true;

    const params = new URLSearchParams(window.location.hash.replace(/^#/, ""));
    const token = params.get("token");
    const refreshToken = params.get("refreshToken");
    const error = params.get("error");

    if (error) {
      navigate("/login", { state: { error: "Đăng nhập Google thất bại. Vui lòng thử lại!" } });
      return;
    }

    if (!token) {
      navigate("/login", { state: { error: "Không nhận được token từ Google." } });
      return;
    }

    const tokenPayload = decodeToken(token);
    const user = {
      username: tokenPayload?.sub,
      fullName: tokenPayload?.sub,
      authenticated: true,
      ...tokenPayload,
    };

    contextLogin(user, token, refreshToken);
    navigate("/");
  }, [contextLogin, navigate]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50">
      <div className="flex flex-col items-center gap-4">
        <svg className="animate-spin h-10 w-10 text-accent" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        <p className="text-slate-500 font-medium">Đang đăng nhập bằng Google...</p>
      </div>
    </div>
  );
}
