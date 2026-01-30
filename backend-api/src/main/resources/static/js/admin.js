const API_BASE = "/api";
const token = localStorage.getItem("token");

// --- UTILS ---
function checkAuth() {
  if (!token) {
    alert("Vui lòng đăng nhập!");
    window.location.href = "authcus.html";
    return;
  }
}
checkAuth();

function showSection(sec) {
  document.getElementById("sec-users").style.display =
    sec === "users" ? "block" : "none";
  document.getElementById("sec-restaurants").style.display =
    sec === "restaurants" ? "block" : "none";

  // Đổi màu menu active
  if (event) {
    document
      .querySelectorAll(".nav-link")
      .forEach((el) => el.classList.remove("active"));
    event.target.closest(".nav-link").classList.add("active");
  }

  document.getElementById("page-title").innerText =
    sec === "users" ? "Quản lý Người dùng" : "Quản lý Nhà hàng";

  if (sec === "users") loadUsers();
  if (sec === "restaurants") loadRestaurants();
}

function formatMoney(n) {
  return n
    ? n.toLocaleString("vi-VN", { style: "currency", currency: "VND" })
    : "0 ₫";
}

// Hàm xử lý ảnh lỗi
function imgError(image) {
  image.onerror = "";
  image.src = "../img/anhbackup.jpg";
  return true;
}

// =========================================
// 1. QUẢN LÝ USER (Giữ nguyên)
// =========================================
async function loadUsers() {
  try {
    const res = await fetch(`${API_BASE}/admin/users`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const users = await res.json();
    const tbody = document.getElementById("table-users");
    tbody.innerHTML = "";

    users.forEach((u) => {
      let roleClass =
        u.role === "ADMIN"
          ? "bg-danger"
          : u.role === "KITCHEN"
            ? "bg-warning text-dark"
            : "bg-secondary";
      tbody.innerHTML += `
                <tr>
                    <td>${u.id}</td>
                    <td class="fw-bold">${u.username}</td>
                    <td>${u.fullName || "---"}</td>
                    <td><span class="badge ${roleClass}">${u.role}</span></td>
                    <td>${u.restaurantId || ""}</td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary" onclick="openEditUser(${u.id}, '${u.fullName}', '${u.role}', '${u.restaurantId}')"><i class="fas fa-edit"></i></button>
                        <button class="btn btn-sm btn-outline-danger" onclick="deleteUser(${u.id})"><i class="fas fa-trash"></i></button>
                    </td>
                </tr>
            `;
    });
  } catch (e) {
    console.error(e);
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
      alert("Đã lưu!");
      location.reload();
    } else alert("Lỗi lưu user!");
  } catch (e) {
    alert("Lỗi kết nối!");
  }
}

async function deleteUser(id) {
  if (!confirm("Xóa user này?")) return;
  await fetch(`${API_BASE}/admin/users/${id}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` },
  });
  loadUsers();
}

// =========================================
// 2. QUẢN LÝ NHÀ HÀNG (SỬA LẠI: Lấy đủ trường)
// =========================================
async function loadRestaurants() {
  try {
    const res = await fetch(`${API_BASE}/guest/restaurants`);
    const data = await res.json();
    const tbody = document.getElementById("table-restaurants");
    tbody.innerHTML = "";

    data.forEach((r) => {
      const cat = r.category
        ? `<span class="badge bg-info text-dark">${r.category}</span>`
        : "";
      const status =
        r.status === "active"
          ? '<span class="text-success">● Active</span>'
          : '<span class="text-muted">● Inactive</span>';
      // Hiển thị Rating màu vàng
      const rating = `<span class="text-warning fw-bold"><i class="fas fa-star"></i> ${r.rating || 0}</span>`;

      tbody.innerHTML += `
                <tr>
                    <td>${r.id}</td>
                    <td><img src="${r.image || ""}" alt="Res" style="width:50px;height:50px;object-fit:cover" onerror="this.src='../img/anhbackup.jpg'"></td>
                    <td>
                        <div class="fw-bold">${r.name}</div>
                        <small>${rating} | ${cat}</small>
                    </td>
                    <td><small>${r.address}</small></td>
                    <td>
                        <button class="btn btn-sm btn-warning" onclick="openMenuManager(${r.id}, '${r.name}')"><i class="fas fa-list"></i> Menu</button>
                    </td>
                    <td>
                        <button class="btn btn-sm btn-primary" onclick='openEditRes(${JSON.stringify(r)})'><i class="fas fa-edit"></i></button>
                        <button class="btn btn-sm btn-danger" onclick="deleteRes(${r.id})"><i class="fas fa-trash"></i></button>
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
  document.getElementById("res-tables").value = 20;

  // Reset rating về 5.0 hoặc 4.5 tùy bạn
  document.getElementById("res-rating").value = 4.5;

  document.getElementById("res-img").value = "";
  document.getElementById("res-desc").value = "";
  document.getElementById("res-time").value = "07:00 - 22:00";
  document.getElementById("res-category").value = "Cơm";
  document.getElementById("res-status").value = "active";

  new bootstrap.Modal(document.getElementById("modalRes")).show();
}
function openEditRes(r) {
  document.getElementById("res-id").value = r.id;
  document.getElementById("modalResTitle").innerText = "Sửa: " + r.name;

  document.getElementById("res-name").value = r.name;
  document.getElementById("res-address").value = r.address;
  document.getElementById("res-tables").value = r.totalTables;

  // Điền rating cũ
  document.getElementById("res-rating").value = r.rating || 0;

  document.getElementById("res-lat").value = r.latitude;
  document.getElementById("res-long").value = r.longitude;
  document.getElementById("res-img").value = r.image;
  document.getElementById("res-isOpen").checked = r.isOpen;
  document.getElementById("res-desc").value = r.description || "";
  document.getElementById("res-time").value = r.time || "";
  document.getElementById("res-category").value = r.category || "Khác";
  document.getElementById("res-status").value = r.status || "active";

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
    totalTables: parseInt(document.getElementById("res-tables").value) || 0,

    // Lấy giá trị rating từ ô input
    rating: parseFloat(document.getElementById("res-rating").value) || 0.0,

    latitude: parseFloat(document.getElementById("res-lat").value) || 0,
    longitude: parseFloat(document.getElementById("res-long").value) || 0,
    image: document.getElementById("res-img").value,
    isOpen: document.getElementById("res-isOpen").checked,
    description: document.getElementById("res-desc").value,
    time: document.getElementById("res-time").value,
    category: document.getElementById("res-category").value,
    status: document.getElementById("res-status").value,
  };

  try {
    const res = await fetch(url, {
      method: method,
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(body),
    });

    if (res.ok) {
      alert("Lưu nhà hàng thành công!");
      loadRestaurants();
      bootstrap.Modal.getInstance(document.getElementById("modalRes")).hide();
    } else {
      alert("Lỗi khi lưu nhà hàng!");
    }
  } catch (e) {
    alert("Lỗi kết nối Server!");
  }
}
async function deleteRes(id) {
  if (!confirm("CẢNH BÁO: Xóa nhà hàng sẽ xóa toàn bộ menu của nó!")) return;
  const res = await fetch(`${API_BASE}/admin/restaurants/${id}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` },
  });
  if (res.ok) loadRestaurants();
  else alert("Không thể xóa nhà hàng này!");
}

// =========================================
// 3. QUẢN LÝ MENU (SỬA LẠI: Fix lỗi lưu món)
// =========================================
async function openMenuManager(resId, resName) {
  document.getElementById("current-res-id-for-menu").value = resId;
  document.getElementById("menuTitle").innerText = "Menu: " + resName;

  try {
    const res = await fetch(`${API_BASE}/admin/menu/restaurant/${resId}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const items = await res.json();

    const tbody = document.getElementById("table-menu-items");
    tbody.innerHTML = "";
    items.forEach((i) => {
      tbody.innerHTML += `
                <tr>
                    <td>${i.id}</td>
                    <td><img src="${i.imageUrl || ""}" alt="img" style="width:40px;height:40px" onerror="this.src='../img/anhbackup.jpg'"></td>
                    <td class="fw-bold">${i.name}</td>
                    <td class="text-success">${formatMoney(i.price)}</td>
                    <td><small>${i.description || ""}</small></td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary" onclick='showModalMenuItem(${JSON.stringify(i)})'><i class="fas fa-edit"></i></button>
                        <button class="btn btn-sm btn-outline-danger" onclick="deleteMenuItem(${i.id})"><i class="fas fa-trash"></i></button>
                    </td>
                </tr>
            `;
    });
    new bootstrap.Modal(document.getElementById("modalMenuManager")).show();
  } catch (e) {
    alert("Lỗi tải menu: " + e);
  }
}

function showModalMenuItem(item = null) {
  const isEdit = item != null;
  document.getElementById("itemTitle").innerText = isEdit
    ? "Sửa món ăn"
    : "Thêm món mới";
  document.getElementById("item-id").value = isEdit ? item.id : "";
  document.getElementById("item-name").value = isEdit ? item.name : "";
  document.getElementById("item-price").value = isEdit ? item.price : "";
  document.getElementById("item-desc").value = isEdit ? item.description : "";
  document.getElementById("item-img").value = isEdit ? item.imageUrl : "";
  document.getElementById("item-category").value = isEdit
    ? item.category
    : "Món chính";

  new bootstrap.Modal(document.getElementById("modalMenuItem")).show();
}

async function saveMenuItem() {
  const resId = document.getElementById("current-res-id-for-menu").value;
  const itemId = document.getElementById("item-id").value;
  const isEdit = itemId !== "";

  const url = isEdit
    ? `${API_BASE}/admin/menu/${itemId}`
    : `${API_BASE}/admin/menu`;
  const method = isEdit ? "PUT" : "POST";

  const body = {
    name: document.getElementById("item-name").value,
    price: parseFloat(document.getElementById("item-price").value) || 0,
    description: document.getElementById("item-desc").value,
    imageUrl: document.getElementById("item-img").value,
    category: document.getElementById("item-category").value,
    isAvailable: true,
    // Quan trọng: Gửi ID nhà hàng đúng format object
    restaurant: { id: parseInt(resId) },
  };

  try {
    const res = await fetch(url, {
      method: method,
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(body),
    });

    if (res.ok) {
      alert("Lưu món thành công!");
      bootstrap.Modal.getInstance(
        document.getElementById("modalMenuItem"),
      ).hide();
      // Refresh list
      const resName = document
        .getElementById("menuTitle")
        .innerText.replace("Menu: ", "");
      openMenuManager(resId, resName);
    } else {
      const errorText = await res.text();
      alert("Lỗi lưu món ăn: " + errorText);
    }
  } catch (e) {
    alert("Lỗi kết nối: " + e);
  }
}

async function deleteMenuItem(id) {
  if (!confirm("Xóa món này?")) return;
  const res = await fetch(`${API_BASE}/admin/menu/${id}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` },
  });
  if (res.ok) {
    const resId = document.getElementById("current-res-id-for-menu").value;
    const resName = document
      .getElementById("menuTitle")
      .innerText.replace("Menu: ", "");
    openMenuManager(resId, resName);
  } else alert("Lỗi xóa món!");
}

function logout() {
  localStorage.removeItem("token");
  window.location.href = "authcus.html";
}

// Init view
loadUsers();
