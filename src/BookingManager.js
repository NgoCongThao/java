import React, { useEffect, useState } from "react";
import axiosClient from "./api/axiosClient";
// Nếu bạn đã chép CSS vào App.css thì bỏ dòng dưới, nếu chưa thì giữ nguyên
// import "./BookingManager.css"; 

function BookingManager() {
  const [bookings, setBookings] = useState([]);

  // Form state khớp với DTO (snake_case)
  const [form, setForm] = useState({
    customer_name: "",
    phone: "",
    email: "",
    date: "",
    time: "",
    num_guests: "",
    special_requests: "",
  });

  // Biến editing lưu object đang được sửa. Nếu null = đang ở chế độ thêm mới
  const [editing, setEditing] = useState(null);

  // 1. TẢI DANH SÁCH
  const load = () => {
    axiosClient.get("/api/admin/bookings")
      .then((res) => setBookings(res.data))
      .catch((err) => alert("Lỗi tải dữ liệu: " + err.message));
  };

  useEffect(load, []);

  // 2. TẠO BOOKING MỚI (POST)
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

  // 3. CẬP NHẬT THÔNG TIN (PUT) -> Hàm mới thêm
  const saveUpdate = async () => {
    try {
      // Gọi API PUT vào ID đang sửa
      await axiosClient.put(`/api/admin/bookings/${editing.id}`, form);
      alert("Cập nhật thông tin thành công!");
      resetForm();
      load();
    } catch (error) {
      alert("Lỗi cập nhật: " + (error.response?.data || error.message));
    }
  };

  // 4. XÓA BOOKING (DELETE) -> Hàm mới thêm
  const remove = async (id) => {
    if (window.confirm("Bạn có chắc chắn muốn xóa đơn đặt bàn này không?")) {
      try {
        await axiosClient.delete(`/api/admin/bookings/${id}`);
        load(); // Tải lại danh sách sau khi xóa
      } catch (error) {
        alert("Lỗi xóa: " + (error.response?.data || error.message));
      }
    }
  };

  // 5. CẬP NHẬT TRẠNG THÁI (Duyệt/Hủy)
  const updateStatus = async (id, status) => {
    try {
      await axiosClient.put(`/api/admin/bookings/${id}/status?status=${status}`);
      load();
    } catch (error) {
      alert("Lỗi cập nhật trạng thái");
    }
  };

  // 6. ĐỔ DỮ LIỆU LÊN FORM ĐỂ SỬA
  const edit = (item) => {
    setEditing(item);
    setForm({
      customer_name: item.customerName,
      phone: item.phone,
      email: item.email || "",
      date: item.bookingDate ? item.bookingDate.toString() : "",
      time: item.bookingTime ? item.bookingTime.toString() : "",
      num_guests: item.numGuests,
      special_requests: item.specialRequests || ""
    });
    // Cuộn lên đầu trang để người dùng thấy form
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const resetForm = () => {
    setForm({
      customer_name: "", phone: "", email: "",
      date: "", time: "", num_guests: "", special_requests: ""
    });
    setEditing(null);
  };

  // Helper: Chọn màu badge trạng thái
  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING': return 'orange';
      case 'CONFIRMED': return 'green';
      case 'CANCELLED': return 'red';
      case 'COMPLETED': return 'blue';
      default: return '#777';
    }
  };

  return (
    <div className="booking-manager" style={{ padding: "20px" }}>
      <h1>📅 Quản lý Đặt bàn</h1>

      {/* --- FORM NHẬP LIỆU --- */}
      <div className="form-container" style={{ background: "#fff", padding: "20px", borderRadius: "8px", boxShadow: "0 2px 5px rgba(0,0,0,0.1)", marginBottom: "20px" }}>
        
        {/* Đổi tiêu đề tùy theo đang Thêm hay Sửa */}
        <h2 style={{marginTop: 0, color: editing ? "#ffc107" : "#2c3e50"}}>
            {editing ? `✏️ Đang sửa: ${editing.customerName}` : "➕ Thêm Booking Mới"}
        </h2>

        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "15px" }}>
          <div className="form-group">
            <label>Tên khách:</label>
            <input className="form-control" placeholder="Tên khách" value={form.customer_name}
              onChange={(e) => setForm({ ...form, customer_name: e.target.value })} />
          </div>
          <div className="form-group">
            <label>SĐT:</label>
            <input className="form-control" placeholder="Số điện thoại" value={form.phone}
              onChange={(e) => setForm({ ...form, phone: e.target.value })} />
          </div>
          <div className="form-group">
            <label>Email:</label>
            <input className="form-control" placeholder="Email (tùy chọn)" value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })} />
          </div>
          <div className="form-group">
            <label>Số khách:</label>
            <input type="number" className="form-control" value={form.num_guests}
              onChange={(e) => setForm({ ...form, num_guests: e.target.value })} />
          </div>
          <div className="form-group">
            <label>Ngày:</label>
            <input type="date" className="form-control" value={form.date}
              onChange={(e) => setForm({ ...form, date: e.target.value })} />
          </div>
          <div className="form-group">
            <label>Giờ:</label>
            <input type="time" className="form-control" value={form.time}
              onChange={(e) => setForm({ ...form, time: e.target.value })} />
          </div>
        </div>

        <div className="form-group" style={{ marginTop: "10px" }}>
          <label>Ghi chú:</label>
          <textarea className="form-control" rows="2" placeholder="Yêu cầu đặc biệt..." value={form.special_requests}
            onChange={(e) => setForm({ ...form, special_requests: e.target.value })} />
        </div>

        <div style={{ marginTop: "15px" }}>
          {editing ? (
            <>
              {/* Nếu đang sửa thì hiện nút Lưu và Hủy */}
              <button className="btn btn-primary" onClick={saveUpdate}>💾 Lưu thay đổi</button>
              <button className="btn btn-secondary" onClick={resetForm}>Hủy bỏ</button>
            </>
          ) : (
            /* Nếu không sửa thì hiện nút Tạo mới */
            <button className="btn btn-primary" onClick={create}>+ Tạo Booking</button>
          )}
        </div>
      </div>

      {/* --- DANH SÁCH BOOKING --- */}
      <div className="booking-list">
        {bookings.map((b) => (
          <div key={b.id} className="booking-item" style={{ 
              background: "white", padding: "15px", marginBottom: "15px", 
              borderRadius: "8px", border: "1px solid #eee", position: "relative",
              boxShadow: "0 2px 4px rgba(0,0,0,0.05)"
          }}>
            
            {/* Badge Trạng thái */}
            <span style={{
              position: "absolute", top: "15px", right: "15px",
              background: getStatusColor(b.status), color: "white",
              padding: "4px 12px", borderRadius: "20px", fontSize: "12px", fontWeight: "bold"
            }}>
              {b.status}
            </span>

            <h3 style={{ margin: "0 0 10px 0", color: "#333" }}>{b.customerName}</h3>

            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "5px", fontSize: "14px", color: "#555" }}>
              <p>📞 {b.phone}</p>
              <p>📧 {b.email || "---"}</p>
              <p>📅 {b.bookingDate} lúc {b.bookingTime}</p>
              <p>👥 {b.numGuests} khách</p>
            </div>

            {b.specialRequests && (
              <div style={{ background: "#fff8e1", padding: "8px", marginTop: "10px", borderRadius: "4px", fontSize: "13px", color: "#856404" }}>
                📝 <strong>Note:</strong> {b.specialRequests}
              </div>
            )}

            {/* ACTION BUTTONS */}
            <div className="item-actions" style={{ marginTop: "15px", borderTop: "1px solid #eee", paddingTop: "10px", display: "flex", flexWrap: "wrap", gap: "5px" }}>
              
              {/* Nút Sửa: Đẩy dữ liệu lên form */}
              <button className="btn btn-warning" onClick={() => edit(b)}>
                ✏️ Sửa
              </button>

              {/* Logic nút trạng thái */}
              {b.status === 'PENDING' && (
                <>
                  <button className="btn btn-success" onClick={() => updateStatus(b.id, 'CONFIRMED')}>
                    ✅ Duyệt
                  </button>
                  <button className="btn btn-danger" onClick={() => updateStatus(b.id, 'CANCELLED')}>
                    ❌ Hủy đơn
                  </button>
                </>
              )}

              {b.status === 'CONFIRMED' && (
                <button className="btn btn-info" onClick={() => updateStatus(b.id, 'COMPLETED')}>
                  🏁 Hoàn tất
                </button>
              )}

              {/* Nút Xóa: Đẩy sang phải cùng */}
              <button className="btn btn-secondary" style={{marginLeft: "auto"}} onClick={() => remove(b.id)}>
                🗑️ Xóa
              </button>

            </div>
          </div>
        ))}
        
        {bookings.length === 0 && <p style={{textAlign: "center", color: "#999"}}>Chưa có đơn đặt bàn nào.</p>}
      </div>
    </div>
  );
}

export default BookingManager;