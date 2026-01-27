import React, { useEffect, useState } from "react";
import axiosClient from "./api/axiosClient";

function BookingManager() {
  const [bookings, setBookings] = useState([]);
  const [form, setForm] = useState({
    customer_name: "",
    phone: "",
    date: "",
    time: "",
    num_guests: "",
  });
  const [editing, setEditing] = useState(null);

  const load = () => {
    axiosClient.get("/api/admin/bookings").then((res) => setBookings(res.data));
  };

  useEffect(load, []);

  const create = async () => {
    await axiosClient.post("/api/admin/bookings", form);
    setForm({ customer_name: "", phone: "", date: "", time: "", num_guests: "" });
    load();
  };

  const update = async () => {
    await axiosClient.put(`/api/admin/bookings/${editing.id}`, form);
    setEditing(null);
    setForm({ customer_name: "", phone: "", date: "", time: "", num_guests: "" });
    load();
  };

  const remove = async (id) => {
    await axiosClient.delete(`/api/admin/bookings/${id}`);
    load();
  };

  const edit = (item) => {
    setEditing(item);
    setForm({
      customer_name: item.customer_name,
      phone: item.phone,
      date: item.date,
      time: item.time,
      num_guests: item.num_guests
    });
  };

  return (
    <div className="booking-manager">
      <h1>Quản lý Đặt bàn</h1>
      <div className="form">
        <h2>{editing ? "Chỉnh sửa booking" : "Thêm booking mới"}</h2>
        <div className="form-group">
          <label>Tên khách:</label>
          <input placeholder="Tên khách" value={form.customer_name}
            onChange={(e) => setForm({ ...form, customer_name: e.target.value })} />
        </div>
        <div className="form-group">
          <label>Số điện thoại:</label>
          <input placeholder="Số điện thoại" value={form.phone}
            onChange={(e) => setForm({ ...form, phone: e.target.value })} />
        </div>
        <div className="form-group">
          <label>Ngày:</label>
          <input type="date" value={form.date}
            onChange={(e) => setForm({ ...form, date: e.target.value })} />
        </div>
        <div className="form-group">
          <label>Giờ:</label>
          <input type="time" value={form.time}
            onChange={(e) => setForm({ ...form, time: e.target.value })} />
        </div>
        <div className="form-group">
          <label>Số khách:</label>
          <input placeholder="Số khách" value={form.num_guests}
            onChange={(e) => setForm({ ...form, num_guests: e.target.value })} />
        </div>
        {editing ? (
          <>
            <button onClick={update}>Cập nhật</button>
            <button className="btn-secondary" onClick={() => setEditing(null)}>Hủy</button>
          </>
        ) : (
          <button onClick={create}>Thêm booking</button>
        )}
      </div>
      <div className="booking-list">
        {bookings.map((b) => (
          <div key={b.id} className="booking-item">
            <h3>{b.customer_name}</h3>
            <p><strong>SĐT:</strong> {b.phone}</p>
            <p><strong>Ngày:</strong> {b.date}</p>
            <p><strong>Giờ:</strong> {b.time}</p>
            <p><strong>Số khách:</strong> {b.num_guests}</p>
            <div className="item-actions">
              <button className="btn-edit" onClick={() => edit(b)}>Sửa</button>
              <button className="btn-delete" onClick={() => remove(b.id)}>Xóa</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default BookingManager;
