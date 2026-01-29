import React, { useEffect, useState } from "react";
import axiosClient from "./api/axiosClient";

function BookingManager() {
  const [bookings, setBookings] = useState([]);

  const [form, setForm] = useState({
    customer_name: "",
    phone: "",
    email: "",
    date: "",
    time: "",
    num_guests: "",
    special_requests: "",
  });

  const [editing, setEditing] = useState(null);

  // LOAD DATA
  const load = () => {
    axiosClient
      .get("/api/admin/bookings")
      .then((res) => setBookings(res.data))
      .catch((err) => alert("Lỗi tải dữ liệu: " + err.message));
  };

  useEffect(load, []);

  // CREATE
  const create = async () => {
    try {
      await axiosClient.post("/api/admin/bookings", form);
      alert("Đã thêm booking thành công!");
      resetForm();
      load();
    } catch (error) {
      alert("Lỗi thêm: " + (error.response?.data || error.message));
    }
  };

  // UPDATE
  const saveUpdate = async () => {
    try {
      await axiosClient.put(`/api/admin/bookings/${editing.id}`, form);
      alert("Cập nhật thành công!");
      resetForm();
      load();
    } catch (error) {
      alert("Lỗi cập nhật: " + (error.response?.data || error.message));
    }
  };

  // DELETE
  const remove = async (id) => {
    if (!window.confirm("Bạn chắc chắn muốn xóa?")) return;
    try {
      await axiosClient.delete(`/api/admin/bookings/${id}`);
      load();
    } catch (error) {
      alert("Lỗi xóa: " + (error.response?.data || error.message));
    }
  };

  // STATUS (Vẫn giữ hàm này để logic thanh toán hoạt động ngầm, nhưng không hiển thị ra nữa)
  const updateStatus = async (id, status) => {
    try {
      await axiosClient.put(`/api/admin/bookings/${id}/status?status=${status}`);
      load();
    } catch (error) {
      console.error(error);
    }
  };

  // PAYMENT
  const handlePayment = async (booking) => {
    const amountStr = window.prompt(
      `Thanh toán cho khách: ${booking.customerName}\nNhập số tiền (VNĐ):`
    );
    if (!amountStr) return;

    const amount = parseFloat(amountStr);
    if (isNaN(amount) || amount <= 0) {
      alert("Số tiền không hợp lệ!");
      return;
    }

    try {
      await axiosClient.post("/api/admin/bills", {
        totalAmount: amount,
        note: `Thanh toán Booking ID: ${booking.id} - ${booking.customerName}`,
        date: booking.bookingDate,
      });

      alert("✅ Thanh toán thành công!");

      // Cập nhật ngầm trạng thái thành COMPLETED trong database
      const currentStatus = booking.status ? booking.status.toUpperCase() : "";
      if (currentStatus !== "COMPLETED") {
        updateStatus(booking.id, "COMPLETED");
      }
    } catch (error) {
      alert("❌ Lỗi thanh toán: " + (error.response?.data || error.message));
    }
  };

  // EDIT
  const edit = (item) => {
    setEditing(item);
    setForm({
      customer_name: item.customerName,
      phone: item.phone,
      email: item.email || "",
      date: item.bookingDate || "",
      time: item.bookingTime || "",
      num_guests: item.numGuests,
      special_requests: item.specialRequests || "",
    });

    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const resetForm = () => {
    setForm({
      customer_name: "",
      phone: "",
      email: "",
      date: "",
      time: "",
      num_guests: "",
      special_requests: "",
    });
    setEditing(null);
  };

  return (
    <div style={{ padding: 20, maxWidth: "1000px", margin: "0 auto" }}>
      <h1>📅 Quản lý Đặt bàn</h1>

      {/* --- FORM --- */}
      <div style={{ background: "#f5f5f5", padding: 20, marginBottom: 20, borderRadius: 8 }}>
        <h2>
          {editing ? `✏️ Đang sửa: ${editing.customerName}` : "➕ Thêm Booking Mới"}
        </h2>

        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "10px" }}>
          <input
            placeholder="Tên khách hàng"
            value={form.customer_name}
            onChange={(e) => setForm({ ...form, customer_name: e.target.value })}
            style={{ padding: 8 }}
          />
          <input
            placeholder="Số điện thoại"
            value={form.phone}
            onChange={(e) => setForm({ ...form, phone: e.target.value })}
            style={{ padding: 8 }}
          />
          <input
            placeholder="Email"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            style={{ padding: 8 }}
          />
          <input
            type="number"
            placeholder="Số lượng khách"
            value={form.num_guests}
            onChange={(e) => setForm({ ...form, num_guests: e.target.value })}
            style={{ padding: 8 }}
          />
          <input
            type="date"
            value={form.date}
            onChange={(e) => setForm({ ...form, date: e.target.value })}
            style={{ padding: 8 }}
          />
          <input
            type="time"
            value={form.time}
            onChange={(e) => setForm({ ...form, time: e.target.value })}
            style={{ padding: 8 }}
          />
          <textarea
            placeholder="Yêu cầu đặc biệt (nếu có)"
            value={form.special_requests}
            onChange={(e) => setForm({ ...form, special_requests: e.target.value })}
            style={{ padding: 8, gridColumn: "span 2", minHeight: "60px" }}
          />
        </div>

        <div style={{ marginTop: 15 }}>
          <button onClick={editing ? saveUpdate : create} style={{ padding: "8px 16px", cursor: "pointer" }}>
            {editing ? "Lưu thay đổi" : "Tạo Booking"}
          </button>
          {editing && (
            <button onClick={resetForm} style={{ marginLeft: 10, padding: "8px 16px", cursor: "pointer" }}>
              Hủy
            </button>
          )}
        </div>
      </div>

      {/* --- DANH SÁCH (Đã xóa dòng trạng thái) --- */}
      <div>
        {bookings.map((b) => (
          <div key={b.id} style={{ background: "white", padding: 15, marginBottom: 10, border: "1px solid #ddd", borderRadius: 5 }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <div>
                <h3 style={{ margin: "0 0 5px 0" }}>{b.customerName} <span style={{fontSize: "0.8em", color: "#666"}}>({b.phone})</span></h3>
                <p style={{ margin: 0 }}>
                  📅 {b.bookingDate} lúc {b.bookingTime} | 👥 {b.numGuests} khách
                </p>
                {/* Đã xóa dòng hiển thị pending/resolved ở đây */}
              </div>
              
              <div style={{ display: "flex", gap: "5px" }}>
                <button onClick={() => edit(b)}>Sửa</button>
                <button onClick={() => handlePayment(b)}>Thanh toán</button>
                <button onClick={() => remove(b.id)} style={{ color: "red" }}>Xóa</button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default BookingManager;