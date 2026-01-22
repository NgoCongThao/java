import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';

const PrivateRoute = () => {
  // Kiểm tra xem trong kho (localStorage) đã có vé chưa
  const user = JSON.parse(localStorage.getItem('user'));

  // Nếu có vé (user tồn tại) -> Cho đi tiếp (Outlet)
  // Nếu không có vé -> Đá về trang Login
  return user ? <Outlet /> : <Navigate to="/login" />;
};

export default PrivateRoute;