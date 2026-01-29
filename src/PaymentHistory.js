import React, { useEffect, useState } from "react";
import axiosClient from "./api/axiosClient";

function PaymentHistory() {
    const [bills, setBills] = useState([]);

    const loadBills = () => {
        axiosClient.get("/api/admin/bills")
            .then(res => setBills(res.data))
            .catch(err => console.error("Lỗi tải hóa đơn:", err));
    };

    useEffect(() => {
        loadBills();
    }, []);

    // ❌ xóa 1 bill (giữ nguyên)
    const deleteBill = async (id) => {
        if (!window.confirm("Bạn có chắc chắn muốn xóa hóa đơn này không?")) return;

        try {
            await axiosClient.delete(`/api/admin/bills/${id}`);
            alert("🗑️ Đã xóa hóa đơn");
            loadBills();
        } catch (err) {
            alert("❌ Lỗi xóa hóa đơn");
        }
    };

    const totalRevenue = bills.reduce(
        (sum, bill) => sum + (bill.totalAmount || 0),
        0
    );

    return (
        <div style={{ padding: "20px" }}>
            <h1>💰 Lịch sử Thanh toán</h1>

            <div style={{
                background: "#e3f2fd",
                padding: "15px",
                borderRadius: "8px",
                marginBottom: "20px"
            }}>
                <h3 style={{ margin: 0, color: "#1976d2" }}>
                    Tổng doanh thu: {totalRevenue.toLocaleString("vi-VN")} VNĐ
                </h3>
            </div>

            <table border="1" style={{
                width: "100%",
                borderCollapse: "collapse",
                background: "white"
            }}>
                <thead>
                    <tr style={{ background: "#f5f5f5" }}>
                        <th>ID</th>
                        <th>Ngày</th>
                        <th>Nội dung</th>
                        <th>Số tiền</th>
                        <th>Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    {bills.map(bill => (
                        <tr key={bill.id} style={{ textAlign: "center" }}>
                            <td>#{bill.id}</td>
                            <td>{Array.isArray(bill.date) ? bill.date.join("-") : bill.date}</td>
                            <td>{bill.note}</td>
                            <td style={{ fontWeight: "bold", color: "green" }}>
                                {bill.totalAmount?.toLocaleString("vi-VN")}
                            </td>
                            <td>
                                <button
                                    onClick={() => deleteBill(bill.id)}
                                    style={{
                                        background: "#dc3545",
                                        color: "white",
                                        border: "none",
                                        padding: "5px 10px",
                                        cursor: "pointer"
                                    }}
                                >
                                    🗑️ Xóa
                                </button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {bills.length === 0 && (
                <p style={{ textAlign: "center", marginTop: "20px" }}>
                    Chưa có dữ liệu thanh toán.
                </p>
            )}
        </div>
    );
}

export default PaymentHistory;