import React, { useEffect, useState } from "react";
import axiosClient from "./api/axiosClient";

import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Tooltip,
  Legend
} from "chart.js";
import { Bar } from "react-chartjs-2";

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Tooltip,
  Legend
);

function Dashboard() {
  const [stats, setStats] = useState({
    menuCount: 0,
    staffCount: 0,
    bookingCount: 0,
    todayRevenue: 0,
    yesterdayRevenue: 0
  });

  useEffect(() => {
    const fetchStats = async () => {
      const today = new Date();
      const todayStr = today.toISOString().split("T")[0];

      const yesterday = new Date(today);
      yesterday.setDate(today.getDate() - 1);
      const yesterdayStr = yesterday.toISOString().split("T")[0];

      const [
        menuRes,
        staffRes,
        bookingRes,
        todayRevenueRes,
        yesterdayRevenueRes
      ] = await Promise.all([
        axiosClient.get("/api/admin/menu"),
        axiosClient.get("/api/admin/users"),
        axiosClient.get("/api/admin/bookings"),
        axiosClient.get("/api/admin/bills/revenue", {
          params: { from: todayStr, to: todayStr }
        }),
        axiosClient.get("/api/admin/bills/revenue", {
          params: { from: yesterdayStr, to: yesterdayStr }
        })
      ]);

      setStats({
        menuCount: menuRes.data.length,
        staffCount: staffRes.data.length,
        bookingCount: bookingRes.data.length,
        todayRevenue: todayRevenueRes.data.revenue || 0,
        yesterdayRevenue: yesterdayRevenueRes.data.revenue || 0
      });
    };

    fetchStats();
  }, []);

  /* ===== DATA CHO BIỂU ĐỒ ===== */
  const barData = {
    labels: ["Hôm qua", "Hôm nay"],
    datasets: [
      {
        label: "Doanh thu (VND)",
        data: [stats.yesterdayRevenue, stats.todayRevenue],
        backgroundColor: ["#90caf9", "#42a5f5"]
      }
    ]
  };

  const barOptions = {
    responsive: true,
    plugins: {
      legend: { display: false }
    }
  };

  return (
    <div className="dashboard">
      <h1>📊 Dashboard Tổng Quan</h1>

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
          <p>{stats.todayRevenue.toLocaleString("vi-VN")} VND</p>
        </div>
      </div>

      {/* ===== BIỂU ĐỒ ===== */}
      <div style={{ marginTop: 40, background: "#fff", padding: 20, borderRadius: 8 }}>
        <h3>📈 So sánh doanh thu</h3>
        <Bar data={barData} options={barOptions} />
      </div>
    </div>
  );
}

export default Dashboard;