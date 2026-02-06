const API_BASE = "http://localhost:8080/api";
const token = localStorage.getItem("token");

// 1. Kiểm tra đăng nhập & Quyền
if (!token) {
    alert("Vui lòng đăng nhập!");
    window.location.href = "authcus.html";
}

// Giải mã token đơn giản để lấy tên (Optional) hoặc lấy từ localStorage nếu lúc login bạn có lưu
const userRole = localStorage.getItem("role");
if (userRole !== "STAFF" &&  userRole !== "MANAGER") {
    alert("Bạn không có quyền truy cập trang này!");
    window.location.href = "staff-auth.html";
}

// Hiển thị tên
document.getElementById("staff-name").innerText = `Staff: ${localStorage.getItem("username") || "Nhân viên"}`;

// 2. Hàm tải danh sách món "READY"
async function loadReadyOrders() {
    try {
        const res = await fetch(`${API_BASE}/staff/ready-orders`, {
            headers: { "Authorization": `Bearer ${token}` }
        });

        if (res.status === 403) {
            alert("Hết phiên đăng nhập!");
            logout();
            return;
        }

        const orders = await res.json();
        renderOrders(orders);

        // Cập nhật thời gian
        const now = new Date();
        document.getElementById("last-update").innerText = `Cập nhật: ${now.getHours()}:${now.getMinutes()}:${now.getSeconds()}`;

    } catch (e) {
        console.error("Lỗi tải đơn:", e);
    }
}

// 3. Render ra HTML
function renderOrders(orders) {
    const container = document.getElementById("order-list");
    container.innerHTML = "";

    // Lọc ra các item con trong order để hiển thị từng món lẻ (nếu muốn)
    // Hoặc hiển thị theo Order lớn. Ở đây mình hiển thị theo Order lớn cho đơn giản,
    // nhưng liệt kê chi tiết các món bên trong.

    if (orders.length === 0) {
        container.innerHTML = `
            <div class="col-12 text-center text-muted mt-5">
                <i class="fas fa-check-circle fa-3x text-success mb-3"></i>
                <h4>Tuyệt vời! Không còn món nào chờ phục vụ.</h4>
            </div>`;
        return;
    }

    orders.forEach(order => {
        // Tạo danh sách món ăn trong đơn này (HTML)
        let itemsHtml = "";
        order.items.forEach(item => {
            itemsHtml += `<li class="list-group-item d-flex justify-content-between align-items-center">
                <span><i class="fas fa-caret-right text-primary"></i> ${item.itemName}</span>
                <span class="badge bg-secondary rounded-pill">x${item.quantity}</span>
            </li>`;
        });

        // HTML cho 1 thẻ Order
        const cardHtml = `
        <div class="col-md-4 col-sm-6 mb-4 animate-new">
            <div class="card card-order h-100">
                <div class="card-header bg-white d-flex justify-content-between align-items-center py-3">
                    <span class="badge-table">Bàn ${order.tableNumber}</span>
                    <small class="text-muted">#${order.id}</small>
                </div>
                <div class="card-body">
                    <h6 class="card-subtitle mb-2 text-muted"><i class="far fa-clock"></i> Chờ từ: ${new Date(order.createdAt).toLocaleTimeString()}</h6>
                    <ul class="list-group list-group-flush mt-3">
                        ${itemsHtml}
                    </ul>
                    ${order.note ? `<div class="alert alert-warning mt-2 p-1 small"><i class="fas fa-sticky-note"></i> ${order.note}</div>` : ''}
                </div>
                <button onclick="serveOrder(${order.id})" class="btn btn-success btn-serve py-3">
                    <i class="fas fa-check"></i> ĐÃ PHỤC VỤ XONG
                </button>
            </div>
        </div>
        `;
        container.innerHTML += cardHtml;
    });
}

// 4. Hàm xác nhận phục vụ
async function serveOrder(orderId) {
    if (!confirm("Xác nhận phục vụ?")) return;

    try {
        const res = await fetch(`${API_BASE}/staff/serve/${orderId}`, {
            method: "PUT",
            headers: { "Authorization": `Bearer ${token}` }
        });

        if (res.ok) {
            // Reload lại danh sách ngay lập tức để cảm giác mượt mà
            loadReadyOrders();
        } else {
            alert("Lỗi: Không thể cập nhật trạng thái!");
        }
    } catch (e) {
        alert("Lỗi kết nối server!");
    }
}

function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("username");
    window.location.href = "authcus.html";
}

// 5. Tự động tải lại mỗi 5 giây (Polling)
loadReadyOrders(); // Tải ngay khi mở trang
setInterval(loadReadyOrders, 5000); // Sau đó cứ 5s tải lại 1 lần