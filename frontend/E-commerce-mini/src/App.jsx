import { Routes, Route, Navigate } from 'react-router-dom'
import MainLayout from './layouts/MainLayout'
import AdminLayout from './layouts/AdminLayout'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import DebugPage from './pages/DebugPage'
import HomePage from './pages/HomePage'
import ProductDetailPage from './pages/ProductDetailPage'
import CartPage from './pages/CartPage'
import OrderHistoryPage from './pages/OrderHistoryPage'
import AddressBookPage from './pages/AddressBookPage'
import OAuth2RedirectPage from './pages/OAuth2RedirectPage'
import ProtectedRoute from './routes/ProtectedRoute'
import AdminRoute from './routes/AdminRoute'
import AdminProductsPage from './pages/admin/AdminProductsPage'
import AdminOrdersPage from './pages/admin/AdminOrdersPage'
import AdminVouchersPage from './pages/admin/AdminVouchersPage'
import { AuthProvider } from './context/AuthContext'
import { CartProvider } from './context/CartContext'

function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <Routes>
          <Route path="/" element={<MainLayout />}>
            <Route index element={<HomePage />} />
            <Route path="products/:productId" element={<ProductDetailPage />} />
            <Route element={<ProtectedRoute />}>
              <Route path="cart" element={<CartPage />} />
              <Route path="orders" element={<OrderHistoryPage />} />
              <Route path="addresses" element={<AddressBookPage />} />
            </Route>
          </Route>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/oauth2/redirect" element={<OAuth2RedirectPage />} />
          <Route path="/debug" element={<DebugPage />} />

          <Route path="/admin" element={<AdminRoute />}>
            <Route element={<AdminLayout />}>
              <Route index element={<Navigate to="products" replace />} />
              <Route path="products" element={<AdminProductsPage />} />
              <Route path="orders" element={<AdminOrdersPage />} />
              <Route path="vouchers" element={<AdminVouchersPage />} />
            </Route>
          </Route>
        </Routes>
      </CartProvider>
    </AuthProvider>
  )
}

export default App
