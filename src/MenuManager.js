import React, { useEffect, useState } from "react";
import axiosClient from "./api/axiosClient";

function MenuManager() {
  const [menu, setMenu] = useState([]);
  const [form, setForm] = useState({
    name: "",
    price: "",
    description: "",
    image: "",
  });
  const [editing, setEditing] = useState(null);

  const load = () => {
    axiosClient.get("/api/menu").then((res) => setMenu(res.data));
  };

  useEffect(load, []);

  const create = async () => {
    await axiosClient.post("/api/menu", form);
    setForm({ name: "", price: "", description: "", image: "" });
    load();
  };

  const update = async () => {
    await axiosClient.put(`/api/menu/${editing.id}`, form);
    setEditing(null);
    setForm({ name: "", price: "", description: "", image: "" });
    load();
  };

  const remove = async (id) => {
    await axiosClient.delete(`/api/menu/${id}`);
    load();
  };

  const edit = (item) => {
    setEditing(item);
    setForm({ name: item.name, price: item.price, description: item.description, image: item.image });
  };

  return (
    <div className="menu-manager">
      <h1>Quản lý Menu</h1>
      <div className="form">
        <h2>{editing ? "Chỉnh sửa món ăn" : "Thêm món ăn mới"}</h2>
        <div className="form-group">
          <label>Tên món:</label>
          <input placeholder="Tên món" value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })} />
        </div>
        <div className="form-group">
          <label>Giá:</label>
          <input placeholder="Giá" value={form.price}
            onChange={(e) => setForm({ ...form, price: e.target.value })} />
        </div>
        <div className="form-group">
          <label>Mô tả:</label>
          <input placeholder="Mô tả" value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </div>
        <div className="form-group">
          <label>Hình ảnh URL:</label>
          <input placeholder="Hình ảnh URL" value={form.image}
            onChange={(e) => setForm({ ...form, image: e.target.value })} />
        </div>
        {editing ? (
          <>
            <button onClick={update}>Cập nhật</button>
            <button className="btn-secondary" onClick={() => setEditing(null)}>Hủy</button>
          </>
        ) : (
          <button onClick={create}>Thêm món</button>
        )}
      </div>
      <div className="menu-list">
        {menu.map((m) => (
          <div key={m.id} className="menu-item">
            <img src={m.image} alt={m.name} />
            <div>
              <h3>{m.name}</h3>
              <p>{m.description}</p>
              <p><strong>Giá: {m.price} VND</strong></p>
              <div className="item-actions">
                <button className="btn-edit" onClick={() => edit(m)}>Sửa</button>
                <button className="btn-delete" onClick={() => remove(m.id)}>Xóa</button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default MenuManager;
