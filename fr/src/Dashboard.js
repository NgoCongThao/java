import React, { useEffect, useState } from "react";
import axiosClient from "./api/axiosClient";

function Dashboard() {
  const [stats, setStats] = useState({
    menuCount: 0,
    staffCount: 0,
    bookingCount: 0,
    revenue: 0
  });

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const [menuRes, staffRes, bookingRes, revenueRes] = await Promise.all([
          axiosClient.get("/api/menu"),
          axiosClient.get("/api/staff"),
          axiosClient.get("/api/bookings"),
          axiosClient.get("/api/revenue", { params: { start_date: new Date().toISOString().split('T')[0], end_date: new Date().toISOString().split('T')[0] } })
        ]);
        setStats({
          menuCount: menuRes.data.length,
          staffCount: staffRes.data.length,
          bookingCount: bookingRes.data.length,
          revenue: revenueRes.data.total
        });
      } catch (error) {
        console.error("Error fetching stats", error);
      }
    };
    fetchStats();
  }, []);

  return (
    <div className="dashboard">
      <h1>Dashboard Tổng Quan</h1>
      <div className="stats">
        <div className="stat-card">
          <h3>Số món ăn</h3>
          <p>{stats.menuCount}</p>
        </div>
        <div className="stat-card">
          <h3>Số nhân viên</h3>
          <p>{stats.staffCount}</p>
        </div>
        <div className="stat-card">
          <h3>Số booking</h3>
          <p>{stats.bookingCount}</p>
        </div>
        <div className="stat-card">
          <h3>Doanh thu hôm nay</h3>
          <p>{stats.revenue} VND</p>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
