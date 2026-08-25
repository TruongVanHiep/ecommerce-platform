import { createContext, useState, useEffect, useContext, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "./AuthContext";
import * as cartService from "../services/cartService";

export const CartContext = createContext();

export function CartProvider({ children }) {
  const { token } = useContext(AuthContext);
  const navigate = useNavigate();

  const [cart, setCart] = useState(null);
  const [toast, setToast] = useState({ show: false, message: "", type: "success" });

  const triggerToast = (message, type = "success") => {
    setToast({ show: true, message, type });
  };

  useEffect(() => {
    if (toast.show) {
      const timer = setTimeout(() => {
        setToast((prev) => ({ ...prev, show: false }));
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [toast.show]);

  const refreshCart = useCallback(async () => {
    if (!token) {
      setCart(null);
      return;
    }
    try {
      const res = await cartService.getCart();
      setCart(res.result);
    } catch (err) {
      console.error("Error loading cart:", err);
    }
  }, [token]);

  useEffect(() => {
    refreshCart();
  }, [refreshCart]);

  const requireLogin = () => {
    triggerToast("Vui lòng đăng nhập để dùng giỏ hàng!", "error");
    navigate("/login");
  };

  const addToCart = async (product, quantity = 1) => {
    if (!token) {
      requireLogin();
      return;
    }
    if (!product?.id) return;

    try {
      const res = await cartService.addItem(product.id, quantity);
      setCart(res.result);
      triggerToast(`Đã thêm "${product.title || product.name}" vào giỏ hàng!`);
    } catch (err) {
      console.error("Error adding to cart:", err);
      triggerToast(err.response?.data?.message || "Không thể thêm vào giỏ hàng.", "error");
    }
  };

  const updateQuantity = async (cartItemId, quantity) => {
    if (quantity < 1) return;
    try {
      const res = await cartService.updateItem(cartItemId, quantity);
      setCart(res.result);
    } catch (err) {
      console.error("Error updating cart item:", err);
      triggerToast(err.response?.data?.message || "Không thể cập nhật số lượng.", "error");
    }
  };

  const removeFromCart = async (cartItemId) => {
    try {
      await cartService.removeItem(cartItemId);
      await refreshCart();
      triggerToast("Đã xóa sản phẩm khỏi giỏ hàng!", "info");
    } catch (err) {
      console.error("Error removing cart item:", err);
      triggerToast(err.response?.data?.message || "Không thể xóa sản phẩm.", "error");
    }
  };

  const cartItems = cart?.items || [];
  const cartCount = cart?.totalItems || 0;
  const cartTotal = cart?.totalPrice || 0;

  return (
    <CartContext.Provider
      value={{
        cartItems,
        cartCount,
        cartTotal,
        addToCart,
        updateQuantity,
        removeFromCart,
        refreshCart,
        triggerToast,
      }}
    >
      {children}

      {/* Floating Toast Notification */}
      {toast.show && (
        <div className="fixed bottom-6 right-6 z-50 animate-fade-in-up">
          <div className={`flex items-center gap-3 px-5 py-4 rounded-2xl shadow-xl backdrop-blur-md border border-white/20 text-white font-medium min-w-[280px] max-w-sm ${
            toast.type === "error"
              ? "bg-red-500/90 text-white"
              : toast.type === "info"
              ? "bg-indigo-500/90 text-white"
              : "bg-slate-900/90 text-white"
          }`}>
            {toast.type === "error" ? (
              <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-red-100 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            ) : toast.type === "info" ? (
              <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-indigo-100 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            ) : (
              <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-emerald-400 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
              </svg>
            )}
            <span className="text-sm">{toast.message}</span>
          </div>
        </div>
      )}
    </CartContext.Provider>
  );
}
