const API_BASE = "http://localhost:8080/api";
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
  // 1. Ẩn tất cả
  document.getElementById("sec-users").style.display = "none";
  document.getElementById("sec-restaurants").style.display = "none";
  document.getElementById("sec-approval").style.display = "none"; // ✅ Mới thêm

  // 2. Hiện phần được chọn
  if (sec === "users") document.getElementById("sec-users").style.display = "block";
  if (sec === "restaurants") document.getElementById("sec-restaurants").style.display = "block";
  if (sec === "approval") document.getElementById("sec-approval").style.display = "block"; // ✅ Mới thêm

  // 3. Đổi màu menu active
  if (event) {
    document.querySelectorAll(".nav-link").forEach((el) => el.classList.remove("active"));
    event.target.closest(".nav-link").classList.add("active");
  }

  // 4. Update Tiêu đề & Load Data
  if (sec === "users") {
      document.getElementById("page-title").innerText = "Quản lý Người dùng";
      loadUsers();
  }
  if (sec === "restaurants") {
      document.getElementById("page-title").innerText = "Quản lý Nhà hàng";
      loadRestaurants();
  }
  // ✅ Mới thêm logic load duyệt
  if (sec === "approval") {
      document.getElementById("page-title").innerText = "Duyệt Đăng Ký Mới";
      loadPendingRequests(); 
  }
}

function formatMoney(n) {
  return n ? n.toLocaleString("vi-VN", { style: "currency", currency: "VND" }) : "0 ₫";
}

// =========================================
// 1. QUẢN LÝ USER
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
      let roleClass = u.role === "ADMIN" ? "bg-danger" : u.role === "KITCHEN" ? "bg-warning text-dark" : "bg-secondary";
      tbody.innerHTML += `
                <tr>
                    <td>${u.id}</td>
                    <td class="fw-bold">${u.username}</td>
                    <td>${u.fullName || "---"}</td>
                    <td><span class="badge ${roleClass}">${u.role}</span></td>
                    <td>${u.restaurantId || ""}</td>
                    <td>
                        <button class="btn btn-sm btn-secondary" disabled title="Chức năng tạm khóa">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                </tr>
            `;
    });
  } catch (e) { console.error(e); }

}

function openEditUser(id, name, role, resId) {
  document.getElementById("edit-user-id").value = id;
  document.getElementById("edit-user-fullname").value = name === "null" ? "" : name;
  document.getElementById("edit-user-role").value = role;
  document.getElementById("edit-user-resId").value = resId === "null" ? "" : resId;
  new bootstrap.Modal(document.getElementById("modalUser")).show();
}

/* async function saveUser() {
  const id = document.getElementById("edit-user-id").value;
  const body = {
    fullName: document.getElementById("edit-user-fullname").value,
    role: document.getElementById("edit-user-role").value,
    restaurantId: document.getElementById("edit-user-resId").value || null,
  };

  try {
    const res = await fetch(`${API_BASE}/admin/users/${id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
      body: JSON.stringify(body),
    });
    if (res.ok) { alert("Đã lưu!"); location.reload(); } 
    else alert("Lỗi lưu user!");
  } catch (e) { alert("Lỗi kết nối!"); }
} */      //Tuấn đã vô hiệu hóa đoạn code này vì không có quyền chỉnh sửa user
    
async function deleteUser(id) {
  alert("Chức năng xóa User đang bị tạm khóa!");
  return;

  /* ĐOẠN CODE CŨ ĐÃ BỊ VÔ HIỆU HÓA
  if (!confirm("Xóa user này?")) return;
  await fetch(`${API_BASE}/admin/users/${id}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` },
  });
  loadUsers();
  */
}

// =========================================
// 2. QUẢN LÝ NHÀ HÀNG
// =========================================
async function loadRestaurants() {
  try {
    const res = await fetch(`${API_BASE}/guest/restaurants`); 
    const data = await res.json();
    const tbody = document.getElementById("table-restaurants");
    tbody.innerHTML = "";

    data.forEach((r) => {
      const cat = r.category ? `<span class="badge bg-info text-dark">${r.category}</span>` : "";
      
      let statusBadge = '<span class="badge bg-secondary">Unknown</span>';
      if(r.status === 'active') statusBadge = '<span class="badge bg-success">Active</span>';
      if(r.status === 'inactive') statusBadge = '<span class="badge bg-secondary">Inactive</span>';
      if(r.status === 'PENDING') statusBadge = '<span class="badge bg-warning text-dark">Pending</span>';

      const rating = `<span class="text-warning fw-bold"><i class="fas fa-star"></i> ${r.rating || 0}</span>`;

      tbody.innerHTML += `
                <tr>
                    <td>${r.id}</td>
                    <td><img src="${r.image || ""}" alt="Res" style="width:50px;height:50px;object-fit:cover" onerror="this.src='../img/anhbackup.jpg'"></td>
                    <td>
                        <div class="fw-bold">${r.name}</div>
                        <small>${rating} | ${cat}</small> <br>
                        ${statusBadge}
                    </td>
                    <td>
                        <small>${r.address}</small><br>
                        <small class="text-muted"><i class="fas fa-phone"></i> ${r.phone || '---'}</small>
                    </td>
                    <td>
                        <button class="btn btn-sm btn-warning" onclick="openMenuManager(${r.id}, '${r.name}')"><i class="fas fa-list"></i> Xem Menu</button>
                    </td>
                    <td>
                        <button class="btn btn-sm btn-primary" onclick='openEditRes(${JSON.stringify(r)})'><i class="fas fa-edit"></i></button>
                        
                        <button class="btn btn-sm btn-secondary" disabled title="Chức năng xóa đã bị khóa">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                </tr>
            `;
    });
  } catch (e) { console.error(e); }
}


function showModalRestaurant() {
  document.getElementById("res-id").value = "";
  document.getElementById("modalResTitle").innerText = "Thêm Nhà Hàng Mới";
  document.getElementById("res-name").value = "";
  document.getElementById("res-address").value = "";
  document.getElementById("res-tables").value = 20;
  document.getElementById("res-rating").value = 4.5;
  document.getElementById("res-img").value = "";
  document.getElementById("res-desc").value = "";
  document.getElementById("res-time").value = "07:00 - 22:00";
  document.getElementById("res-phone").value = ""; 
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
  document.getElementById("res-rating").value = r.rating || 0;
  document.getElementById("res-lat").value = r.latitude;
  document.getElementById("res-long").value = r.longitude;
  document.getElementById("res-img").value = r.image;
  document.getElementById("res-isOpen").checked = r.isOpen;
  document.getElementById("res-desc").value = r.description || "";
  document.getElementById("res-time").value = r.time || "";
  document.getElementById("res-phone").value = r.phone || "";
  document.getElementById("res-category").value = r.category || "Khác";
  document.getElementById("res-status").value = r.status || "active";
  new bootstrap.Modal(document.getElementById("modalRes")).show();
}

async function saveRestaurant() {
  const id = document.getElementById("res-id").value;
  const isEdit = id !== "";
  const url = isEdit ? `${API_BASE}/admin/restaurants/${id}` : `${API_BASE}/admin/restaurants`;
  const method = isEdit ? "PUT" : "POST";

  const body = {
    name: document.getElementById("res-name").value,
    address: document.getElementById("res-address").value,
    totalTables: parseInt(document.getElementById("res-tables").value) || 0,
    rating: parseFloat(document.getElementById("res-rating").value) || 0.0,
    latitude: parseFloat(document.getElementById("res-lat").value) || 0,
    longitude: parseFloat(document.getElementById("res-long").value) || 0,
    image: document.getElementById("res-img").value,
    isOpen: document.getElementById("res-isOpen").checked,
    description: document.getElementById("res-desc").value,
    time: document.getElementById("res-time").value,
    phone: document.getElementById("res-phone").value,
    category: document.getElementById("res-category").value,
    status: document.getElementById("res-status").value,
  };

  try {
    const res = await fetch(url, {
      method: method,
      headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
      body: JSON.stringify(body),
    });

    if (res.ok) {
      alert("Lưu nhà hàng thành công!");
      loadRestaurants();
      bootstrap.Modal.getInstance(document.getElementById("modalRes")).hide();
    } else {
      alert("Lỗi khi lưu nhà hàng!");
    }
  } catch (e) { alert("Lỗi kết nối Server!"); }
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
// 3. DUYỆT ĐỐI TÁC (QUAN TRỌNG - PHẦN NÀY ĐANG THIẾU CỦA BẠN)
// =========================================
async function loadPendingRequests() {
    try {
        const res = await fetch(`${API_BASE}/admin/pending-restaurants`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        const list = await res.json();
        const tbody = document.getElementById("table-approval");
        tbody.innerHTML = "";

        // Update badge count (số lượng chờ duyệt hiển thị ở menu)
        const badge = document.getElementById("pending-count");
        if(list.length > 0) {
            badge.innerText = list.length;
            badge.style.display = "inline-block";
        } else {
            badge.style.display = "none";
        }

        if(list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">Không có yêu cầu nào đang chờ.</td></tr>`;
            return;
        }

        list.forEach(item => {
            tbody.innerHTML += `
                <tr>
                    <td>${item.id}</td>
                    <td class="fw-bold text-primary">${item.name}<br><small class="text-muted">${item.address}</small></td>
                    <td>${item.address}</td>
                    <td>
                        <div class="fw-bold">${item.ownerName}</div>
                        <small>${item.ownerPhone}</small>
                    </td>
                    <td>${item.ownerUsername}</td>
                    <td>
                        <button class="btn btn-sm btn-success me-2" onclick="approveRes(${item.id})">
                            <i class="fas fa-check"></i> Duyệt
                        </button>
                        <button class="btn btn-sm btn-danger" onclick="rejectRes(${item.id})">
                            <i class="fas fa-times"></i> Xóa
                        </button>
                    </td>
                </tr>
            `;
        });
    } catch(e) { console.error(e); }
}

async function approveRes(id) {
    if(!confirm("Xác nhận DUYỆT nhà hàng này?")) return;
    try {
        const res = await fetch(`${API_BASE}/admin/approve/${id}`, {
            method: "POST",
            headers: { "Authorization": `Bearer ${token}` }
        });
        if(res.ok) { alert("Đã duyệt thành công!"); loadPendingRequests(); }
        else alert("Lỗi khi duyệt!");
    } catch(e) { alert("Lỗi kết nối!"); }
}

async function rejectRes(id) {
    if(!confirm("Xác nhận TỪ CHỐI và XÓA yêu cầu này?")) return;
    try {
        const res = await fetch(`${API_BASE}/admin/reject/${id}`, {
            method: "POST",
            headers: { "Authorization": `Bearer ${token}` }
        });
        if(res.ok) { alert("Đã từ chối yêu cầu!"); loadPendingRequests(); }
        else alert("Lỗi khi từ chối!");
    } catch(e) { alert("Lỗi kết nối!"); }
}

// =========================================
// 4. QUẢN LÝ MENU
// =========================================
async function openMenuManager(resId, resName) {
  document.getElementById("current-res-id-for-menu").value = resId;
  document.getElementById("menuTitle").innerText = "Menu: " + resName;

  // 👇 Ẩn nút "Thêm món mới" trong Modal
  const addBtn = document.querySelector("#modalMenuManager .btn-success");
  if(addBtn) addBtn.style.display = 'none';

  try {
    const res = await fetch(`${API_BASE}/admin/menu/restaurant/${resId}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const items = await res.json();

    const tbody = document.getElementById("table-menu-items");
    tbody.innerHTML = "";
    
    if(items.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">Chưa có món ăn nào.</td></tr>`;
    }

    items.forEach((i) => {
      tbody.innerHTML += `
                <tr>
                    <td>${i.id}</td>
                    <td><img src="${i.imageUrl || ""}" alt="img" style="width:40px;height:40px" onerror="this.src='../img/anhbackup.jpg'"></td>
                    <td class="fw-bold">${i.name}</td>
                    <td class="text-success">${formatMoney(i.price)}</td>
                    <td><small>${i.description || ""}</small></td>
                    <td>
                        <span class="badge bg-secondary"><i class="fas fa-eye"></i> Chỉ xem</span>
                    </td>
                </tr>
            `;
    });
    new bootstrap.Modal(document.getElementById("modalMenuManager")).show();
  } catch (e) { alert("Lỗi tải menu: " + e); }
}

function showModalMenuItem(item = null) {
  const isEdit = item != null;
  document.getElementById("itemTitle").innerText = isEdit ? "Sửa món ăn" : "Thêm món mới";
  document.getElementById("item-id").value = isEdit ? item.id : "";
  document.getElementById("item-name").value = isEdit ? item.name : "";
  document.getElementById("item-price").value = isEdit ? item.price : "";
  document.getElementById("item-desc").value = isEdit ? item.description : "";
  document.getElementById("item-img").value = isEdit ? item.imageUrl : "";
  document.getElementById("item-category").value = isEdit ? item.category : "Món chính";
  new bootstrap.Modal(document.getElementById("modalMenuItem")).show();
}

async function saveMenuItem() {
  const resId = document.getElementById("current-res-id-for-menu").value;
  const itemId = document.getElementById("item-id").value;
  const isEdit = itemId !== "";
  const url = isEdit ? `${API_BASE}/admin/menu/${itemId}` : `${API_BASE}/admin/menu`;
  const method = isEdit ? "PUT" : "POST";

  const body = {
    name: document.getElementById("item-name").value,
    price: parseFloat(document.getElementById("item-price").value) || 0,
    description: document.getElementById("item-desc").value,
    imageUrl: document.getElementById("item-img").value,
    category: document.getElementById("item-category").value,
    isAvailable: true,
    restaurant: { id: parseInt(resId) },
  };

  try {
    const res = await fetch(url, {
      method: method,
      headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
      body: JSON.stringify(body),
    });

    if (res.ok) {
      alert("Lưu món thành công!");
      bootstrap.Modal.getInstance(document.getElementById("modalMenuItem")).hide();
      const resName = document.getElementById("menuTitle").innerText.replace("Menu: ", "");
      openMenuManager(resId, resName);
    } else {
      const errorText = await res.text();
      alert("Lỗi lưu món ăn: " + errorText);
    }
  } catch (e) { alert("Lỗi kết nối: " + e); }
}

async function deleteMenuItem(id) {
  if (!confirm("Xóa món này?")) return;
  const res = await fetch(`${API_BASE}/admin/menu/${id}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` },
  });
  if (res.ok) {
    const resId = document.getElementById("current-res-id-for-menu").value;
    const resName = document.getElementById("menuTitle").innerText.replace("Menu: ", "");
    openMenuManager(resId, resName);
  } else alert("Lỗi xóa món!");
}

function logout() {
  localStorage.removeItem("token");
  window.location.href = "authcus.html";
}

// Init view
loadUsers();