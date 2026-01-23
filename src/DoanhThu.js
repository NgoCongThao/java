import React, { useEffect, useState } from "react";
import axiosClient from "./api/axiosClient";

function RevenueReport() {
  const [startDate, setStartDate] = useState(new Date().toISOString().split('T')[0]);
  const [endDate, setEndDate] = useState(new Date().toISOString().split('T')[0]);
  const [revenue, setRevenue] = useState(0);

  const fetchRevenue = () => {
    axiosClient.get("/api/revenue", { params: { start_date: startDate, end_date: endDate } })
      .then((res) => setRevenue(res.data.total));
  };

  useEffect(fetchRevenue, [startDate, endDate]);

  return (
    <div className="revenue-report">
      <h1>Báo cáo Doanh thu</h1>
      <div className="filters">
        <div>
          <label>Từ ngày:</label>
          <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
        </div>
        <div>
          <label>Đến ngày:</label>
          <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
        </div>
        <button onClick={fetchRevenue}>Tải lại</button>
      </div>
      <div className="result">
        <h2>Tổng doanh thu: {revenue.toLocaleString()} VND</h2>
      </div>
    </div>
  );
}

export default RevenueReport;
