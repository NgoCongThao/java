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

 const load = async () => {
  try {
    const res = await axiosClient.get("/api/admin/users");
    setStaff(Array.isArray(res.data) ? res.data : []);
  } catch (err) {
    console.error("Load staff error:", err.response?.data || err.message);
    setStaff([]);
  }
};

  useEffect(() => {
      load();
    }, []);

 const create = async () => {
  try {
    await axiosClient.post("/api/admin/users", form);
    setForm({ username: "", password: "", full_name: "", role: "chef" });
    load();
  } catch (err) {
    console.error("Create staff error:", err.response?.data || err.message);
    alert(err.response?.data?.message || "Không tạo được nhân viên");
  }
};

  const update = async () => {
  try {
    await axiosClient.put(`/api/admin/users/${editing.id}`, form);
    setEditing(null);
    setForm({ username: "", password: "", full_name: "", role: "chef" });
    load();
  } catch (err) {
    console.error("Update staff error:", err.response?.data || err.message);
  }
};

 const remove = async (id) => {
  if (!window.confirm("Xóa nhân viên này?")) return;

  try {
    await axiosClient.delete(`/api/admin/users/${id}`);
    load();
  } catch (err) {
    console.error("Delete staff error:", err.response?.data || err.message);
  }
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
