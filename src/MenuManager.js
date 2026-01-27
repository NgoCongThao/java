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

  // 🔥 SỬA Ở ĐÂY
  const load = () => {
    axiosClient.get("/api/admin/menu")
      .then((res) => setMenu(res.data))
      .catch(err => console.error("LỖI LOAD MENU:", err));
  };

  useEffect(() => {
    load();
  }, []);

  const create = async () => {
    await axiosClient.post("/api/admin/menu", form);
    setForm({ name: "", price: "", description: "", image: "" });
    load();
  };

  const update = async () => {
    await axiosClient.put(`/api/admin/menu/${editing.id}`, form);
    setEditing(null);
    setForm({ name: "", price: "", description: "", image: "" });
    load();
  };

  const remove = async (id) => {
    await axiosClient.delete(`/api/admin/menu/${id}`);
    load();
  };

  const edit = (item) => {
    setEditing(item);
    setForm({
      name: item.name,
      price: item.price,
      description: item.description,
      image: item.image,
    });
  };

  return (
    <div className="menu-manager">
      <h1>Quản lý Menu</h1>

      <div className="form">
        <h2>{editing ? "Chỉnh sửa món ăn" : "Thêm món ăn mới"}</h2>

        <input
          placeholder="Tên món"
          value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })}
        />

        <input
          placeholder="Giá"
          value={form.price}
          onChange={(e) => setForm({ ...form, price: e.target.value })}
        />

        <input
          placeholder="Mô tả"
          value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
        />

        <input
          placeholder="Hình ảnh URL"
          value={form.image}
          onChange={(e) => setForm({ ...form, image: e.target.value })}
        />

        {editing ? (
          <>
            <button onClick={update}>Cập nhật</button>
            <button onClick={() => setEditing(null)}>Hủy</button>
          </>
        ) : (
          <button onClick={create}>Thêm món</button>
        )}
      </div>

      <div className="menu-list">
        {menu.map((m) => (
          <div key={m.id}>
            <img src={m.image} alt={m.name} width="120" />
            <h3>{m.name}</h3>
            <p>{m.description}</p>
            <p><b>{m.price} VND</b></p>
            <button onClick={() => edit(m)}>Sửa</button>
            <button onClick={() => remove(m.id)}>Xóa</button>
          </div>
        ))}
      </div>
    </div>
  );
}

export default MenuManager;