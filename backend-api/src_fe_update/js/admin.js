/**
 * ADMIN SYSTEM (SAAS) - JAVASCRIPT
 * Nhiệm vụ: Quản lý Tenant (Nhà hàng) & User hệ thống
 * KHÔNG BAO GỒM: Quản lý Menu, Order (Việc của Manager)
 */

const API_BASE = "http://localhost:8080/api";
const token = localStorage.getItem("token");

// --- 1. UTILS & AUTH ---
function checkAuth() {
  if (!token) {
    alert("Phiên đăng nhập hết hạn hoặc không tồn tại!");
    window.location.href = "admin-login.html"; // Chuyển về cổng nội bộ
    return;
  }
  // Check role sơ bộ (Backend sẽ check kỹ hơn)
  const role = localStorage.getItem("role");
  if(role !== "ADMIN") {
    alert("Bạn không có quyền truy cập trang Quản Trị Hệ Thống!");
    window.location.href = "admin-login.html";
  }
}
checkAuth();

// Hàm chuyển tab (User vs Restaurant)
function showSection(sec) {
  document.getElementById("sec-users").style.display = sec === "users" ? "block" : "none";
  document.getElementById("sec-restaurants").style.display = sec === "restaurants" ? "block" : "none";

  // Update active class cho menu sidebar
  if (event) {
    document.querySelectorAll(".nav-link").forEach((el) => el.classList.remove("active"));
    event.target.closest(".nav-link").classList.add("active");
  }

  // Đổi tiêu đề trang
  const titles = {
    'users': 'Quản lý Người dùng Hệ thống',
    'restaurants': 'Quản lý Đối tác Nhà hàng (Tenants)'
  };
  document.getElementById("page-title").innerText = titles[sec];

  if (sec === "users") loadUsers();
  if (sec === "restaurants") loadRestaurants();
}

// =========================================
// 2. QUẢN LÝ NHÀ HÀNG (TENANT MANAGEMENT)
// =========================================

// Load danh sách nhà hàng
async function loadRestaurants() {
  try {
    // Gọi API lấy tất cả nhà hàng (Admin xem được hết)
    const res = await fetch(`${API_BASE}/guest/restaurants`);
    const data = await res.json();
    const tbody = document.getElementById("table-restaurants");
    tbody.innerHTML = "";

    data.forEach((r) => {
      // Hiển thị trạng thái đẹp mắt
      const statusBadge = r.isOpen
          ? '<span class="badge bg-success"><i class="fas fa-check-circle"></i> Hoạt động</span>'
          : '<span class="badge bg-danger"><i class="fas fa-ban"></i> Đã khóa/Đóng</span>';

      tbody.innerHTML += `
                <tr>
                    <td><span class="fw-bold text-muted">#${r.id}</span></td>
                    <td>
                        <div class="fw-bold text-primary" style="font-size: 1.1rem">${r.name}</div>
                        <small class="text-muted"><i class="fas fa-tag"></i> ${r.category || 'Chưa phân loại'}</small>
                    </td>
                    <td>
                        <div class="small"><i class="fas fa-map-marker-alt text-danger"></i> ${r.address}</div>
                        <div class="small"><i class="fas fa-phone text-success"></i> ${r.phone || '---'}</div>
                    </td>
                    <td>${statusBadge}</td>
                    <td>
                        ${r.isOpen ?
          `<button class="btn btn-sm btn-outline-danger" onclick="lockRestaurant(${r.id})">
                                <i class="fas fa-lock"></i> Khóa quán
                             </button>` :
          `<button class="btn btn-sm btn-outline-success" onclick="unlockRestaurant(${r.id})">
                                <i class="fas fa-unlock"></i> Mở lại
                             </button>`
      }
                    </td>
                </tr>
            `;
    });
  } catch (e) {
    console.error("Lỗi load nhà hàng:", e);
    alert("Không thể tải danh sách nhà hàng.");
  }
}

// --- LOGIC TẠO TENANT MỚI (PROVISIONING) ---
// Chức năng quan trọng nhất: Tạo Quán + Tạo Chủ Quán
function openCreateTenantModal() {
  // Reset form cũ
  document.getElementById("formCreateTenant").reset();
  // Hiện Modal
  const modal = new bootstrap.Modal(document.getElementById("modalCreateTenant"));
  modal.show();
}

async function submitCreateTenant() {
  // 1. Lấy dữ liệu từ form
  const payload = {
    // Info Quán
    restaurantName: document.getElementById("new-res-name").value,
    address: document.getElementById("new-res-address").value,
    phone: document.getElementById("new-res-phone").value,

    // Info Chủ quán (Manager)
    managerUsername: document.getElementById("new-mgr-username").value,
    managerPassword: document.getElementById("new-mgr-password").value,
    managerFullName: document.getElementById("new-mgr-fullname").value
  };

  // 2. Validate sơ bộ
  if(!payload.restaurantName || !payload.managerUsername || !payload.managerPassword) {
    alert("Vui lòng điền đầy đủ các trường bắt buộc (*)");
    return;
  }

  // 3. Gọi API AdminController (Backend đã viết ở bài trước)
  try {
    const res = await fetch(`${API_BASE}/admin/create-tenant`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
      body: JSON.stringify(payload)
    });

    if (res.ok) {
      alert(`🎉 Thành công!\nĐã tạo quán "${payload.restaurantName}" và tài khoản quản lý "${payload.managerUsername}".`);

      // Ẩn modal
      const modalEl = document.getElementById("modalCreateTenant");
      const modalInstance = bootstrap.Modal.getInstance(modalEl);
      modalInstance.hide();

      // Reload danh sách
      loadRestaurants();
    } else {
      const errText = await res.text();
      alert("Lỗi từ hệ thống: " + errText);
    }
  } catch (e) {
    console.error(e);
    alert("Lỗi kết nối Server! Vui lòng kiểm tra lại Backend.");
  }
}

// Logic Khóa nhà hàng (Dành cho quán bùng tiền hoặc vi phạm)
async function lockRestaurant(id) {
  if(!confirm("⚠️ CẢNH BÁO:\nBạn có chắc muốn KHÓA nhà hàng này?\n- Khách sẽ không thể đặt món.\n- Chủ quán không thể truy cập.")) return;

  try {
    const res = await fetch(`${API_BASE}/admin/lock/${id}`, {
      method: "PUT",
      headers: { "Authorization": `Bearer ${token}` }
    });

    if(res.ok) {
      alert("Đã khóa nhà hàng.");
      loadRestaurants();
    } else {
      alert("Lỗi khi khóa nhà hàng. Kiểm tra lại API.");
    }
  } catch(e) { console.error(e); }
}

async function unlockRestaurant(id) {
  alert("Tính năng mở khóa đang phát triển (Cần cập nhật isOpen = true trong DB).");
  // Em có thể tự viết thêm API unlock bên backend tương tự API lock
}

// =========================================
// 3. QUẢN LÝ USER (SYSTEM WIDE)
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
      // Style badge role
      let badgeClass = "bg-secondary";
      if (u.role === "ADMIN") badgeClass = "bg-danger";     // Đỏ
      if (u.role === "MANAGER") badgeClass = "bg-primary";  // Xanh dương
      if (u.role === "KITCHEN") badgeClass = "bg-warning text-dark"; // Vàng
      if (u.role === "STAFF") badgeClass = "bg-info text-dark";    // Xanh nhạt

      tbody.innerHTML += `
                <tr>
                    <td>${u.id}</td>
                    <td class="fw-bold">${u.username}</td>
                    <td>${u.fullName || "---"}</td>
                    <td><span class="badge ${badgeClass}">${u.role}</span></td>
                    <td>
                        ${u.restaurantId
          ? `<span class="badge bg-light text-dark border">Res #${u.restaurantId}</span>`
          : '<span class="text-muted fst-italic">System</span>'}
                    </td>
                    <td>
                        <button class="btn btn-sm btn-outline-dark" onclick="openEditUser(${u.id}, '${u.username}')">
                            <i class="fas fa-cog"></i> Tác vụ
                        </button>
                    </td>
                </tr>
            `;
    });
  } catch (e) {
    console.error(e);
  }
}

// Mở modal sửa user (chỉ để Admin reset pass hoặc xem thông tin)
function openEditUser(id, username) {
  document.getElementById("edit-user-id").value = id;
  document.getElementById("display-username").innerText = username;
  document.getElementById("reset-password").value = "";

  new bootstrap.Modal(document.getElementById("modalEditUser")).show();
}

async function saveUserChanges() {
  const id = document.getElementById("edit-user-id").value;
  const newPass = document.getElementById("reset-password").value;

  if(!newPass) {
    alert("Bạn chưa nhập mật khẩu mới!");
    return;
  }

  // Gửi yêu cầu đổi pass (Cần viết API bên backend hoặc tái sử dụng API update user)
  // Lưu ý: Nếu dùng API update user cũ, nhớ handle việc mã hóa pass ở backend
  alert("Chức năng đang bảo trì. Vui lòng thực hiện update trực tiếp trong DB hoặc bổ sung API Reset Password.");
}

function logout() {
  if(confirm("Bạn muốn đăng xuất?")) {
    localStorage.clear();
    window.location.href = "admin-login.html";
  }
}

// Khởi chạy: Mặc định vào tab Nhà Hàng
showSection('restaurants');