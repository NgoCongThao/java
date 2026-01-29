import React, { useEffect, useState } from "react";
import axiosClient from "./api/axiosClient";
import "./MenuManager.css";

function MenuManager() {
  const [menu, setMenu] = useState([]);
  const [form, setForm] = useState({
    name: "",
    price: "",
    description: "",
    image: "",
  });
  const [editing, setEditing] = useState(null);
  const [imageError, setImageError] = useState(false);

  const load = () => {
    axiosClient
      .get("/api/admin/menu")
      .then((res) => setMenu(res.data))
      .catch((err) => console.error("LỖI LOAD MENU:", err));
  };

  useEffect(() => {
    load();
  }, []);

  const create = async () => {
    if (!form.name || !form.price || !form.image) {
      alert("❌ Vui lòng nhập đầy đủ tên món, giá và hình ảnh");
      return;
    }

    if (isNaN(form.price)) {
      alert("❌ Giá phải là số");
      return;
    }

    try {
      await axiosClient.post("/api/admin/menu", {
        ...form,
        price: Number(form.price),
      });
      setForm({ name: "", price: "", description: "", image: "" });
      setImageError(false);
      load();
    } catch {
      alert("❌ Không thể thêm món");
    }
  };

  const update = async () => {
    await axiosClient.put(`/api/admin/menu/${editing.id}`, form);
    setEditing(null);
    setForm({ name: "", price: "", description: "", image: "" });
    setImageError(false);
    load();
  };

  const remove = async (id) => {
    if (!window.confirm("Xóa món này?")) return;
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
    setImageError(false);
  };

  return (
    <div className="menu-manager">
      <h1>🍽️ Quản lý Menu</h1>

      <div className="menu-container">
        {/* FORM */}
        <div className="menu-form">
          <h2>{editing ? "✏️ Chỉnh sửa món" : "➕ Thêm món mới"}</h2>

          <input
            placeholder="Tên món"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
          />

          <input
            placeholder="Giá (VND)"
            value={form.price}
            onChange={(e) => setForm({ ...form, price: e.target.value })}
          />

          <input
            placeholder="Mô tả"
            value={form.description}
            onChange={(e) =>
              setForm({ ...form, description: e.target.value })
            }
          />

          <input
            placeholder="Hình ảnh URL"
            value={form.image}
            onChange={(e) => {
              setForm({ ...form, image: e.target.value });
              setImageError(false);
            }}
          />

          {/* PREVIEW IMAGE */}
          {form.image && !imageError && (
            <div className="image-preview">
              <img
                src={form.image}
                alt="Preview"
                onError={() => setImageError(true)}
              />
            </div>
          )}

          {imageError && (
            <p className="image-error">❌ Link ảnh không hợp lệ</p>
          )}

          <div className="form-actions">
            {editing ? (
              <>
                <button className="btn primary" onClick={update}>
                  Cập nhật
                </button>
                <button className="btn" onClick={() => setEditing(null)}>
                  Hủy
                </button>
              </>
            ) : (
              <button className="btn primary" onClick={create}>
                Thêm món
              </button>
            )}
          </div>
        </div>

        {/* LIST */}
        <div className="menu-list">
          {menu.map((m) => (
            <div className="menu-card" key={m.id}>
              <img src={m.image} alt={m.name} />

              <div className="menu-info">
                <h3>{m.name}</h3>
                <p>{m.description}</p>
                <p className="price">{m.price} VND</p>
              </div>

              <div className="card-actions">
                <button className="btn small" onClick={() => edit(m)}>
                  Sửa
                </button>
                <button
                  className="btn small danger"
                  onClick={() => remove(m.id)}
                >
                  Xóa
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default MenuManager;