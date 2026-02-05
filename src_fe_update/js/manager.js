const API_BASE = "http://localhost:8080/api/manager";
const token = localStorage.getItem("token");

// Auth Check
if (!token || localStorage.getItem("role") !== "MANAGER") {
    alert("Bạn không có quyền truy cập!");
    window.location.href = "admin-login.html";
}

// Init
document.getElementById("managerName").innerText = "Manager: " + (localStorage.getItem("username") || "User");
loadStats();

// --- TAB SWITCHING ---
function switchTab(tabName) {
    ['dashboard', 'menu', 'staff', 'settings'].forEach(t => {
        document.getElementById('tab-' + t).style.display = (t === tabName) ? 'block' : 'none';
    });
    // Load data on switch
    if(tabName === 'menu') loadMenu();
    if(tabName === 'staff') loadStaff();
    if(tabName === 'settings') loadSettings();
}

// --- 1. DASHBOARD ---
async function loadStats() {
    const res = await fetch(`${API_BASE}/stats`, { headers: { "Authorization": `Bearer ${token}` } });
    const data = await res.json();

    document.getElementById("stat-revenue").innerText = data.revenue.toLocaleString();
    document.getElementById("stat-orders").innerText = data.totalOrders;

    const list = document.getElementById("list-top-items");
    list.innerHTML = "";
    data.topItems.forEach(item => {
        // item = [name, quantity]
        list.innerHTML += `<li><strong>${item[0]}</strong>: ${item[1]} lượt bán</li>`;
    });
}

// --- 2. MENU ---
async function loadMenu() {
    const res = await fetch(`${API_BASE}/menu`, { headers: { "Authorization": `Bearer ${token}` } });
    const data = await res.json();
    const tbody = document.getElementById("table-menu");
    tbody.innerHTML = "";
    data.forEach(d => {
        tbody.innerHTML += `<tr>
            <td><img src="${d.imageUrl}" style="width:40px;height:40px;object-fit:cover"></td>
            <td>${d.name}</td>
            <td>${d.price.toLocaleString()}</td>
            <td>${d.category}</td>
            <td><button class="btn btn-sm btn-danger" onclick="deleteDish(${d.id})"><i class="fas fa-trash"></i></button></td>
        </tr>`;
    });
}
function openAddDishModal() { new bootstrap.Modal(document.getElementById("modalDish")).show(); }
async function submitDish() {
    const body = {
        name: document.getElementById("new-dish-name").value,
        price: document.getElementById("new-dish-price").value,
        category: document.getElementById("new-dish-cat").value,
        imageUrl: document.getElementById("new-dish-img").value
    };
    await fetch(`${API_BASE}/menu`, {
        method: "POST",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
        body: JSON.stringify(body)
    });
    alert("Đã thêm món!");
    location.reload();
}
async function deleteDish(id) {
    if(!confirm("Xóa món này?")) return;
    await fetch(`${API_BASE}/menu/${id}`, { method: "DELETE", headers: { "Authorization": `Bearer ${token}` } });
    loadMenu();
}

// --- 3. STAFF ---
async function loadStaff() {
    const res = await fetch(`${API_BASE}/staff`, { headers: { "Authorization": `Bearer ${token}` } });
    const data = await res.json();
    const div = document.getElementById("list-staff");
    div.innerHTML = "";
    data.forEach(u => {
        div.innerHTML += `<div class="col-md-4 mb-3"><div class="card p-3 shadow-sm border-0">
            <h5>${u.fullName || u.username}</h5>
            <span class="badge ${u.role==='KITCHEN'?'bg-warning':'bg-info'} mb-2">${u.role}</span>
            <button class="btn btn-sm btn-outline-danger" onclick="fireStaff(${u.id})">Sa thải</button>
        </div></div>`;
    });
}
function openAddStaffModal() { new bootstrap.Modal(document.getElementById("modalStaff")).show(); }
async function submitStaff() {
    const body = {
        username: document.getElementById("new-staff-user").value,
        password: document.getElementById("new-staff-pass").value,
        fullName: document.getElementById("new-staff-name").value,
        role: document.getElementById("new-staff-role").value
    };
    const res = await fetch(`${API_BASE}/staff`, {
        method: "POST",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
        body: JSON.stringify(body)
    });
    if(res.ok) { alert("Đã tạo nhân viên!"); location.reload(); }
    else alert("Lỗi: Username có thể đã tồn tại");
}
async function fireStaff(id) {
    if(!confirm("Sa thải nhân viên này?")) return;
    await fetch(`${API_BASE}/staff/${id}`, { method: "DELETE", headers: { "Authorization": `Bearer ${token}` } });
    loadStaff();
}

// --- 4. SETTINGS ---
async function loadSettings() {
    const res = await fetch(`${API_BASE}/restaurant`, { headers: { "Authorization": `Bearer ${token}` } });
    const r = await res.json();
    document.getElementById("set-name").value = r.name;
    document.getElementById("set-address").value = r.address;
    document.getElementById("set-phone").value = r.phone;
    document.getElementById("set-desc").value = r.description;
    document.getElementById("set-img").value = r.image;
    document.getElementById("set-isOpen").checked = r.isOpen;
}
async function saveRestaurantInfo() {
    const body = {
        name: document.getElementById("set-name").value,
        address: document.getElementById("set-address").value,
        phone: document.getElementById("set-phone").value,
        description: document.getElementById("set-desc").value,
        image: document.getElementById("set-img").value,
        isOpen: document.getElementById("set-isOpen").checked
    };
    await fetch(`${API_BASE}/restaurant`, {
        method: "PUT",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
        body: JSON.stringify(body)
    });
    alert("Cập nhật thông tin quán thành công!");
}

function logout() { localStorage.clear(); window.location.href = "admin-login.html"; }