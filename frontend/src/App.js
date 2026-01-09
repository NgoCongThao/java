import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import AdminLayout from './layouts/AdminLayout';
import GuestLayout from './layouts/GuestLayout';

// Tạo vài component rỗng để test router (Sau này các bạn Dev sẽ vào đây viết code thật)
const GuestHome = () => <h1>Trang chủ Khách hàng (Dev 1 làm ở đây)</h1>;
const AdminDashboard = () => <h1>Thống kê Doanh thu (Dev 5 làm ở đây)</h1>;
const StaffOrder = () => <h1>Danh sách Đơn hàng (Dev 3 làm ở đây)</h1>;

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* KHU VỰC KHÁCH HÀNG (Dùng GuestLayout) */}
        <Route path="/" element={<GuestLayout />}>
          <Route index element={<GuestHome />} />
          <Route path="menu" element={<h1>Trang Menu</h1>} />
        </Route>

        {/* KHU VỰC QUẢN TRỊ & NHÂN VIÊN (Dùng AdminLayout) */}
        <Route path="/" element={<AdminLayout />}>
           {/* Admin */}
          <Route path="admin/dashboard" element={<AdminDashboard />} />
          
           {/* Staff */}
          <Route path="staff/orders" element={<StaffOrder />} />
        </Route>

      </Routes>
    </BrowserRouter>
  );
}

export default App;