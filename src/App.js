import React from "react";
import { BrowserRouter, Routes, Route, Navigate, Link, useLocation } from "react-router-dom";
import Login from "./Login";
import Dashboard from "./Dashboard";
import MenuManager from "./MenuManager";
import StaffManager from "./StaffManager";
import BookingManager from "./BookingManager";
import RevenueReport from "./DoanhThu";
import ProtectedRoute from "./ProtectedRoute";
import "./App.css";

function AppContent() {
  const location = useLocation();
  const isLoggedIn = localStorage.getItem("token");

 // if (!isLoggedIn && location.pathname !== "/login") {
   // return <Navigate to="/login" />;
  //}

  return (
    <div className="app">
      {isLoggedIn && (
        <div className="sidebar">
          <h2>Restaurant Admin</h2>
          <ul>
            <li><Link to="/" className={location.pathname === "/" ? "active" : ""}>Dashboard</Link></li>
            <li><Link to="/menu" className={location.pathname === "/menu" ? "active" : ""}>Quản lý Menu</Link></li>
            <li><Link to="/staff" className={location.pathname === "/staff" ? "active" : ""}>Quản lý Nhân viên</Link></li>
            <li><Link to="/booking" className={location.pathname === "/booking" ? "active" : ""}>Quản lý Booking</Link></li>
            <li><Link to="/revenue" className={location.pathname === "/revenue" ? "active" : ""}>Báo cáo Doanh thu</Link></li>
          </ul>
          <button onClick={() => { localStorage.clear(); window.location.href = "/login"; }}>Đăng xuất</button>
        </div>
      )}
      <div className="main-content">
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route element={<ProtectedRoute/>}>
            <Route path="/" element={<Dashboard />} />
            <Route path="/menu" element={<MenuManager />} />
            <Route path="/staff" element={<StaffManager />} />
            <Route path="/booking" element={<BookingManager />} />
            <Route path="/revenue" element={<RevenueReport />} />
          </Route>
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </div>
    </div>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AppContent />
    </BrowserRouter>
  );
}

export default App;
