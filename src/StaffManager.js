import React, { useEffect, useState } from "react";
import axiosClient from "./api/axiosClient";

function StaffManager() {
  const [staff, setStaff] = useState([]);
  const [form, setForm] = useState({
    username: "",
    password: "",
    full_name: "",
    role: "chef",
  });
  const [editing, setEditing] = useState(null);

  const load = () => {
    axiosClient.get("/api/staff").then((res) => setStaff(res.data));
  };

  useEffect(load, []);

  const create = async () => {
    await axiosClient.post("/api/staff", form);
    setForm({ username: "", password: "", full_name: "", role: "chef" });
    load();
  };

  const update = async () => {
    await axiosClient.put(`/api/staff/${editing.id}`, form);
    setEditing(null);
    setForm({ username: "", password: "", full_name: "", role: "chef" });
    load();
  };

  const remove = async (id) => {
    await axiosClient.delete(`/api/staff/${id}`);
    load();
  };

  const edit = (item) => {
    setEditing(item);
    setForm({ username: item.username, password: "", full_name: item.full_name, role: item.role });
  };

  return (
    <div className="staff-manager">
      <h1>Quản lý Nhân viên</h1>
      <div className="form">
        <h2>{editing ? "Chỉnh sửa nhân viên" : "Thêm nhân viên mới"}</h2>
        <div className="form-group">
          <label>Username:</label>
          <input placeholder="Username" value={form.username}
            onChange={(e) => setForm({ ...form, username: e.target.value })} />
        </div>
        <div className="form-group">
          <label>Password:</label>
          <input placeholder="Password" type="password" value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })} />
        </div>
        <div className="form-group">
          <label>Họ tên:</label>
          <input placeholder="Họ tên" value={form.full_name}
            onChange={(e) => setForm({ ...form, full_name: e.target.value })} />
        </div>
        <div className="form-group">
          <label>Vai trò:</label>
          <select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
            <option value="chef">Chef</option>
            <option value="cashier">Cashier</option>
            <option value="manager">Manager</option>
          </select>
        </div>
        {editing ? (
          <>
            <button onClick={update}>Cập nhật</button>
            <button className="btn-secondary" onClick={() => setEditing(null)}>Hủy</button>
          </>
        ) : (
          <button onClick={create}>Thêm nhân viên</button>
        )}
      </div>
      <div className="staff-list">
        {staff.map((s) => (
          <div key={s.id} className="staff-item">
            <h3>{s.full_name}</h3>
            <p><strong>Username:</strong> {s.username}</p>
            <p><strong>Vai trò:</strong> {s.role}</p>
            <div className="item-actions">
              <button className="btn-edit" onClick={() => edit(s)}>Sửa</button>
              <button className="btn-delete" onClick={() => remove(s.id)}>Xóa</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default StaffManager;
