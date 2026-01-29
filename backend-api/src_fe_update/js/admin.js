// admin.js
const API_BASE = "http://localhost:8080/api";
const token = localStorage.getItem("token");

// 1. KIỂM TRA QUYỀN ADMIN
function checkAuth() {
  if (!token) {
    alert("Bạn chưa đăng nhập!");
    window.location.href = "authcus.html";
    return;
  }
  // Giải mã JWT đơn giản để xem Role (hoặc gọi API verify)
  const payload = JSON.parse(atob(token.split(".")[1]));
  if (payload.role !== "ADMIN") {
    alert("Bạn không có quyền truy cập trang này!");
    window.location.href = "landing.html";
  }
}
checkAuth(); // Chạy ngay khi mở trang

// 2. CHUYỂN TAB
function showSection(sec) {
  document.getElementById("sec-users").style.display =
    sec === "users" ? "block" : "none";
  document.getElementById("sec-restaurants").style.display =
    sec === "restaurants" ? "block" : "none";

  // Update Title
  document.getElementById("page-title").innerText =
    sec === "users" ? "Quản lý Người dùng" : "Quản lý Nhà hàng";

  // Load Data
  if (sec === "users") loadUsers();
  if (sec === "restaurants") loadRestaurants();
}

// ================= QUẢN LÝ USER =================
async function loadUsers() {
  try {
    const res = await fetch(`${API_BASE}/admin/users`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const users = await res.json();
    const tbody = document.getElementById("table-users");
    tbody.innerHTML = "";

    users.forEach((u) => {
      tbody.innerHTML += `
                <tr>
                    <td>${u.id}</td>
                    <td><b>${u.username}</b></td>
                    <td>${u.fullName || ""}</td>
                    <td><span class="badge ${u.role === "ADMIN" ? "bg-danger" : u.role === "KITCHEN" ? "bg-warning" : "bg-info"}">${u.role}</span></td>
                    <td>${u.restaurantId || "-"}</td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary" onclick="openEditUser(${u.id}, '${u.fullName}', '${u.role}', '${u.restaurantId}')"><i class="fas fa-edit"></i></button>
                        <button class="btn btn-sm btn-outline-danger" onclick="deleteUser(${u.id})"><i class="fas fa-trash"></i></button>
                    </td>
                </tr>
            `;
    });
  } catch (e) {
    alert("Lỗi tải danh sách user: " + e);
  }
}

function openEditUser(id, name, role, resId) {
  document.getElementById("edit-user-id").value = id;
  document.getElementById("edit-user-fullname").value =
    name === "null" ? "" : name;
  document.getElementById("edit-user-role").value = role;
  document.getElementById("edit-user-resId").value =
    resId === "null" ? "" : resId;
  new bootstrap.Modal(document.getElementById("modalUser")).show();
}

async function saveUser() {
  const id = document.getElementById("edit-user-id").value;
  const body = {
    fullName: document.getElementById("edit-user-fullname").value,
    role: document.getElementById("edit-user-role").value,
    restaurantId: document.getElementById("edit-user-resId").value || null,
    phone: "", // Giữ nguyên
  };

  try {
    const res = await fetch(`${API_BASE}/admin/users/${id}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(body),
    });
    if (res.ok) {
      alert("Cập nhật thành công!");
      location.reload();
    } else alert("Lỗi cập nhật!");
  } catch (e) {
    console.error(e);
  }
}

async function deleteUser(id) {
  if (!confirm("Bạn chắc chắn muốn xóa user này?")) return;
  await fetch(`${API_BASE}/admin/users/${id}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` },
  });
  loadUsers();
}

// ================= QUẢN LÝ NHÀ HÀNG =================
async function loadRestaurants() {
  try {
    const res = await fetch(`${API_BASE}/guest/restaurants`); // Dùng API guest để xem list
    const restaurants = await res.json();
    const tbody = document.getElementById("table-restaurants");
    tbody.innerHTML = "";

    restaurants.forEach((r) => {
      tbody.innerHTML += `
                <tr>
                    <td>${r.id}</td>
                    <td><b>${r.name}</b></td>
                    <td>${r.address}</td>
                    <td>${r.totalTables}</td>
                    <td>${r.latitude}, ${r.longitude}</td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary" onclick='openEditRes(${JSON.stringify(r)})'><i class="fas fa-edit"></i></button>
                        <button class="btn btn-sm btn-outline-danger" onclick="deleteRes(${r.id})"><i class="fas fa-trash"></i></button>
                    </td>
                </tr>
            `;
    });
  } catch (e) {
    console.error(e);
  }
}

function showModalRestaurant() {
  document.getElementById("res-id").value = "";
  document.getElementById("modalResTitle").innerText = "Thêm Nhà Hàng Mới";
  document.getElementById("res-name").value = "";
  document.getElementById("res-address").value = "";
  document.getElementById("res-tables").value = 15;
  new bootstrap.Modal(document.getElementById("modalRes")).show();
}

function openEditRes(r) {
  document.getElementById("res-id").value = r.id;
  document.getElementById("modalResTitle").innerText = "Sửa Nhà Hàng: " + r.id;
  document.getElementById("res-name").value = r.name;
  document.getElementById("res-address").value = r.address;
  document.getElementById("res-tables").value = r.totalTables;
  document.getElementById("res-lat").value = r.latitude;
  document.getElementById("res-long").value = r.longitude;
  document.getElementById("res-img").value = r.imageUrl;
  document.getElementById("res-isOpen").checked = r.isOpen;
  new bootstrap.Modal(document.getElementById("modalRes")).show();
}

async function saveRestaurant() {
  const id = document.getElementById("res-id").value;
  const isEdit = id !== "";
  const url = isEdit
    ? `${API_BASE}/admin/restaurants/${id}`
    : `${API_BASE}/admin/restaurants`;
  const method = isEdit ? "PUT" : "POST";

  const body = {
    name: document.getElementById("res-name").value,
    address: document.getElementById("res-address").value,
    totalTables: parseInt(document.getElementById("res-tables").value),
    latitude: parseFloat(document.getElementById("res-lat").value),
    longitude: parseFloat(document.getElementById("res-long").value),
    imageUrl: document.getElementById("res-img").value,
    isOpen: document.getElementById("res-isOpen").checked,
  };

  const res = await fetch(url, {
    method: method,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(body),
  });

  if (res.ok) {
    alert("Lưu thành công!");
    loadRestaurants(); // Reload lại bảng
    document.querySelector("#modalRes .btn-close").click(); // Đóng modal
  } else {
    alert("Có lỗi xảy ra!");
  }
}

async function deleteRes(id) {
  if (!confirm("Cảnh báo: Xóa nhà hàng sẽ xóa hết menu của nó! Bạn chắc chưa?"))
    return;
  await fetch(`${API_BASE}/admin/restaurants/${id}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` },
  });
  loadRestaurants();
}

function logout() {
  localStorage.removeItem("token");
  window.location.href = "authcus.html";
}

// Mặc định load users khi vào trang
loadUsers();
