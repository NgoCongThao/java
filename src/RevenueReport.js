import React, { useEffect, useState } from "react";
import axiosClient from "./api/axiosClient";

function RevenueReport() {
  const today = new Date().toISOString().split('T')[0];
  const [startDate, setStartDate] = useState(today);
  const [endDate, setEndDate] = useState(today);
  const [revenue, setRevenue] = useState(0);

  // State cho form thu tiền nhanh
  const [newBill, setNewBill] = useState({ totalAmount: "", note: "" });

  // 1. Hàm xem doanh thu
  const fetchRevenue = () => {
    axiosClient.get("/api/admin/bills/revenue", {
      params: { from: startDate, to: endDate }
    })
    .then((res) => {
      // Xử lý linh hoạt dữ liệu trả về
      const value = res.data.revenue !== undefined ? res.data.revenue : res.data;
      setRevenue(value || 0);
    })
    .catch((err) => {
      console.error("Lỗi:", err);
      // Không set về 0 vội, để giữ số cũ nếu chỉ lỗi mạng nhẹ
    });
  };

  // 2. Hàm nạp tiền
  const handleCreateBill = async () => {
    if (!newBill.totalAmount) return alert("Vui lòng nhập số tiền!");
    try {
      await axiosClient.post("/api/admin/bills", {
        totalAmount: parseFloat(newBill.totalAmount),
        note: newBill.note
      });
      alert("✅ Thu tiền thành công!");
      setNewBill({ totalAmount: "", note: "" }); // Reset form
      fetchRevenue(); // Tải lại số tổng ngay
    } catch (error) {
      alert("❌ Lỗi: " + (error.response?.data || error.message));
    }
  };

  useEffect(() => {
    fetchRevenue();
  }, [startDate, endDate]);

  return (
    <div className="revenue-report" style={{ padding: "30px", maxWidth: "800px", margin: "0 auto" }}>
      <h1 style={{ borderBottom: "2px solid #ddd", paddingBottom: "10px", color: "#333" }}>📊 Báo cáo Doanh thu</h1>

      {/* --- FORM THU TIỀN NHANH --- */}
      <div style={{ background: "#e8f5e9", padding: "20px", borderRadius: "10px", marginBottom: "30px", border: "1px solid #c8e6c9" }}>
        <h3 style={{ marginTop: 0, color: "#2e7d32" }}>➕ Thu tiền nhanh</h3>
        <div style={{ display: "flex", gap: "10px" }}>
          <input 
            type="number" className="form-control" placeholder="Nhập số tiền..." 
            value={newBill.totalAmount}
            onChange={(e) => setNewBill({...newBill, totalAmount: e.target.value})}
            style={{ padding: "10px", flex: 1, borderRadius: "5px", border: "1px solid #ccc" }}
          />
          <input 
            type="text" className="form-control" placeholder="Ghi chú..." 
            value={newBill.note}
            onChange={(e) => setNewBill({...newBill, note: e.target.value})}
            style={{ padding: "10px", flex: 2, borderRadius: "5px", border: "1px solid #ccc" }}
          />
          <button onClick={handleCreateBill} style={{ padding: "10px 20px", background: "#2e7d32", color: "white", border: "none", borderRadius: "5px", cursor: "pointer", fontWeight: "bold" }}>Lưu</button>
        </div>
      </div>

      {/* --- BỘ LỌC NGÀY --- */}
      <div style={{ display: "flex", gap: "20px", marginBottom: "30px", alignItems: "flex-end" }}>
        <div>
          <label style={{ display: "block", fontWeight: "bold", marginBottom: "5px" }}>Từ ngày:</label>
          <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} style={{ padding: "8px", borderRadius: "4px", border: "1px solid #ccc" }} />
        </div>
        <div>
          <label style={{ display: "block", fontWeight: "bold", marginBottom: "5px" }}>Đến ngày:</label>
          <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} style={{ padding: "8px", borderRadius: "4px", border: "1px solid #ccc" }} />
        </div>
        <button onClick={fetchRevenue} style={{ padding: "8px 20px", background: "#007bff", color: "white", border: "none", borderRadius: "4px", cursor: "pointer" }}>🔄 Tải lại</button>
      </div>

      {/* --- KẾT QUẢ --- */}
      <div style={{ background: "linear-gradient(135deg, #28a745, #218838)", color: "white", padding: "40px", borderRadius: "15px", textAlign: "center", boxShadow: "0 4px 15px rgba(0,0,0,0.1)" }}>
        <h2 style={{ margin: 0, opacity: 0.9 }}>TỔNG DOANH THU THỰC TẾ</h2>
        <div style={{ fontSize: "3.5rem", fontWeight: "bold", margin: "15px 0" }}>
            {Number(revenue).toLocaleString('vi-VN')} VND
        </div>
      </div>
    </div>
  );
}

// KHÔNG ĐƯỢC XÓA DÒNG NÀY HOẶC SỬA THÀNH { RevenueReport }
export default RevenueReport;