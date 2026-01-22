import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';

// 1. IMPORT CÁC LAYOUT & AUTH
import AdminLayout from './layouts/AdminLayout';
import GuestLayout from './layouts/GuestLayout';
import Login from './pages/auth/Login';

// ✅ SỬA LẠI ĐÚNG ĐƯỜNG DẪN CỦA BẠN (trong folder auth)
import PrivateRoute from './pages/auth/PrivateRoute';
import KitchenBoard from './pages/staff/KitchenBoard';
// 2. IMPORT CÁC TRANG CHỨC NĂNG
import TableMap from './pages/staff/TableMap';
import StaffOrder from './pages/staff/OrderList';
import CreateOrder from './pages/staff/CreateOrder';

// ✅ NẾU BẠN VẪN MUỐN GIỮ TRANG LOYALTY (Thì phải đảm bảo đã tạo file Loyalty.jsx)
// Nếu không muốn làm trang này thì xóa dòng import này đi
import Loyalty from './pages/staff/Loyalty'; 

// Các trang giả (Placeholder)
const GuestHome = () => <h1>Trang chủ Khách hàng (Dev 1 làm ở đây)</h1>;
const AdminDashboard = () => <h1>Thống kê Doanh thu (Dev 5 làm ở đây)</h1>;

function App() {
  return (
    <BrowserRouter>
      <Routes>
        
        {/* TRANG LOGIN */}
        <Route path="/login" element={<Login />} />

        {/* KHU VỰC CÓ BẢO VỆ (Private) */}
        <Route element={<PrivateRoute />}>
            <Route path="/" element={<AdminLayout />}>
              <Route path="admin/dashboard" element={<AdminDashboard />} />
                <Route path="/staff/kitchen" element={<KitchenBoard />} />
              <Route path="staff/tables" element={<TableMap />} />
              <Route path="staff/order/create/:id" element={<CreateOrder />} />
              <Route path="staff/orders" element={<StaffOrder />} />
              
              {/* Nếu bỏ trang Loyalty thì xóa dòng này */}
              <Route path="staff/loyalty" element={<Loyalty />} />
            </Route>
        </Route>

        {/* KHU VỰC KHÁCH HÀNG */}
        <Route path="/" element={<GuestLayout />}>
          <Route index element={<GuestHome />} />
          <Route path="menu" element={<h1>Trang Menu</h1>} />
        </Route>

      </Routes>
    </BrowserRouter>
  );
}

export default App;