import { useContext } from "react";
import { Navigate, Outlet, useLocation } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";

export default function AdminRoute() {
  const { user, token, loading } = useContext(AuthContext);
  const location = useLocation();

  if (loading) return null;

  if (!token) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  const isAdmin = user?.scope?.split(" ").includes("ADMIN");
  if (!isAdmin) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
