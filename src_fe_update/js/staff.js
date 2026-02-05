const API_BASE = "http://localhost:8080/api/staff";
const token = localStorage.getItem("token");

// Auth Check
if (!token || (localStorage.getItem("role") !== "STAFF" && localStorage.getItem("role") !== "MANAGER")) {
    alert("Bạn không có quyền truy cập Staff Portal!");
    window.location.href = "admin-login.html";
}
document.getElementById("staffName").innerText = "NV: " + (localStorage.getItem("username") || "Staff");

// Init
loadTableMap();
loadBookings();
loadOnlineOrders(); // Load luôn đơn online lúc đầu

// Auto Refresh mỗi 5s (Tăng tốc độ cập nhật để bắt sự kiện Gọi thanh toán nhanh hơn)
setInterval(() => {
    // Chỉ refresh nếu đang ở tab Sơ đồ bàn
    if(document.getElementById('tab-tables').style.display === 'block') {
        loadTableMap();
    }
    // Nếu đang ở tab Đơn online thì refresh đơn online
    if(document.getElementById('tab-online-orders').style.display === 'block') {
        loadOnlineOrders();
    }
}, 5000);

function switchTab(tab) {
    ['tables', 'online-orders', 'bookings', 'pos'].forEach(t => {
        const el = document.getElementById('tab-' + t);
        if (el) el.style.display = (t === tab) ? 'block' : 'none';
    });

    document.querySelectorAll('.nav-link').forEach(el => el.classList.remove('active'));
    if(event && event.target) event.target.classList.add('active');

    if(tab === 'tables') loadTableMap();
    if(tab === 'bookings') loadBookings();
    if(tab === 'online-orders') loadOnlineOrders();
}

// ================= 1. SƠ ĐỒ BÀN (CẬP NHẬT TRẠNG THÁI GỌI THANH TOÁN) =================
async function loadTableMap() {
    try {
        const res = await fetch(`${API_BASE}/live-orders`, { headers: { "Authorization": `Bearer ${token}` } });
        if(!res.ok) return;
        const activeOrders = await res.json();

        // Map đơn hàng vào số bàn
        const tableMap = {};
        activeOrders.forEach(o => {
            if(o.tableNumber) tableMap[o.tableNumber] = o;
        });

        const grid = document.getElementById("table-grid");
        if(grid) {
            grid.innerHTML = "";
            for(let i = 1; i <= 20; i++) {
                const order = tableMap[i];
                let statusClass = "table-free"; // Class CSS mặc định (xanh lá hoặc trắng)
                let statusText = "TRỐNG";
                let actionHtml = "";
                let extraStyle = ""; // Style inline để nhấn mạnh

                if(order) {
                    // Logic hiển thị trạng thái
                    if (order.status === "PAYMENT_REQUEST") {
                        // --- CẬP NHẬT: KHÁCH GỌI THANH TOÁN ---
                        statusClass = "table-busy";
                        statusText = "🔔 GỌI THANH TOÁN";
                        extraStyle = "background-color: #ffc107; border: 3px solid #dc3545; animation: blink 1s infinite;"; // Nhấp nháy vàng/đỏ
                    }
                    else if(order.status === "DELIVERING" || order.status === "COOKING" || order.status === "READY") {
                        statusClass = "table-serving";
                        statusText = "ĐANG PHỤC VỤ";
                    }
                    else if (order.status === "PENDING") {
                        statusClass = "table-busy";
                        statusText = "CHỜ BẾP";
                    }

                    // Click vào bàn có khách -> Mở thanh toán
                    actionHtml = `onclick="openPaymentModal(${order.id}, ${i}, ${order.totalPrice})"`;
                } else {
                    // Bàn trống -> Click để tạo đơn mới
                    actionHtml = `onclick="preFillPos(${i})"`;
                }

                // --- CẬP NHẬT: THÊM NÚT QR NHỎ Ở GÓC ---
                // Lưu ý: onclick của nút QR phải có event.stopPropagation() để không kích hoạt click của cả thẻ bàn
                grid.innerHTML += `
                    <div class="col-6 col-md-4 col-lg-3">
                        <div class="table-card ${statusClass}" style="${extraStyle}" ${actionHtml}>
                            
                            <div class="btn-qr-mini" onclick="showQrCode(${i}, event)">
                                <i class="fas fa-qrcode"></i>
                            </div>

                            <div class="table-number">${i}</div>
                            <div class="table-status fw-bold">${statusText}</div>
                            ${order ? `<div class="mt-2 fw-bold text-dark">${formatMoney(order.totalPrice)}</div>` : ''}
                        </div>
                    </div>
                `;
            }
        }
    } catch(e) { console.error(e); }
}
// HÀM HIỂN THỊ QR CODE
// HÀM HIỂN THỊ QR CODE (Cập nhật lấy ID động)
function showQrCode(tableNum, event) {
    // Ngăn click lan ra ngoài
    if(event) event.stopPropagation();

    document.getElementById("qr-table-num").innerText = tableNum;

    // --- 1. LẤY ID NHÀ HÀNG TỪ LOCALSTORAGE ---
    let restaurantId = 1; // Mặc định (fallback)
    const userStr = localStorage.getItem("user");

    if (userStr) {
        try {
            const u = JSON.parse(userStr);
            // Nếu user có restaurantId thì dùng, không thì giữ mặc định
            if (u.restaurantId) {
                restaurantId = u.restaurantId;
            }
        } catch (e) {
            console.error("Lỗi đọc user:", e);
        }
    }
    // ------------------------------------------

    // 2. Tạo Link (Tự động lấy domain hiện tại)
    // Lấy đường dẫn gốc, bỏ phần 'staff.html' đi để thay bằng 'menu.html'
    // Ví dụ: đang ở .../pages/staff.html -> lấy .../pages/
    let baseUrl = window.location.href.substring(0, window.location.href.lastIndexOf("/"));

    // Nếu bạn đang chạy root (vd: localhost:5500/staff.html) thì baseUrl sẽ là localhost:5500
    // Cần đảm bảo link trỏ đúng menu.html
    const link = `${baseUrl}/menu.html?id=${restaurantId}&table=${tableNum}`;

    // 3. Render QR
    const container = document.getElementById("qrcode-container");
    container.innerHTML = "";
    new QRCode(container, {
        text: link,
        width: 150,
        height: 150,
        colorDark : "#000000",
        colorLight : "#ffffff",
        correctLevel : QRCode.CorrectLevel.H
    });

    // 4. Hiển thị Link text
    const linkEl = document.getElementById("qr-link-display");
    if(linkEl) {
        linkEl.href = link;
        linkEl.innerText = link;
    }

    new bootstrap.Modal(document.getElementById("modalQr")).show();
}

// Hàm in (Optional - in vùng modal thôi hơi phức tạp, đây là lệnh in đơn giản)
function printQr() {
    window.print();
}
function preFillPos(tableNum) {
    const posBtn = document.querySelector("button[onclick=\"switchTab('pos')\"]");
    if(posBtn) posBtn.click();
    else switchTab('pos');
    document.getElementById("pos-table").value = tableNum;
}

// ================= 2. QUẢN LÝ BOOKING =================
async function loadBookings() {
    try {
        const res = await fetch(`${API_BASE}/bookings`, { headers: { "Authorization": `Bearer ${token}` } });
        if(!res.ok) return;
        const data = await res.json();
        const tbody = document.getElementById("booking-list");
        if(tbody) tbody.innerHTML = "";

        let pendingCount = 0;
        data.forEach(b => {
            if(b.status === "PENDING") pendingCount++;

            const isConfirmed = b.status === "CONFIRMED";
            const actionBtn = isConfirmed
                ? `<span class="badge bg-success">Đã xếp bàn ${b.tableNumber}</span>`
                : `<button class="btn btn-sm btn-primary" onclick="confirmBooking(${b.id})">Xếp bàn</button>`;

            if(tbody) {
                tbody.innerHTML += `
                    <tr>
                        <td>${new Date(b.bookingDate).toLocaleDateString()} ${b.bookingTime}</td>
                        <td>
                            <div class="fw-bold">${b.customerName || b.user?.fullName || 'Khách'}</div>
                            <small>${b.phone || b.user?.username || ''}</small>
                        </td>
                        <td>${b.peopleCount} người</td>
                        <td><small>${b.note || ''}</small></td>
                        <td><span class="badge ${isConfirmed?'bg-success':'bg-warning'}">${b.status}</span></td>
                        <td>${actionBtn}</td>
                    </tr>
                `;
            }
        });
        const badge = document.getElementById("badge-booking-count");
        if(badge) badge.innerText = pendingCount;
    } catch(e) { console.error(e); }
}

async function confirmBooking(id) {
    const tableNum = prompt("Nhập số bàn muốn xếp cho khách này:");
    if(!tableNum) return;

    try {
        const res = await fetch(`${API_BASE}/bookings/${id}/confirm`, {
            method: "PUT",
            headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
            body: JSON.stringify({ tableNumber: parseInt(tableNum) })
        });

        if(res.ok) {
            alert("Đã xếp bàn thành công!");
            loadBookings();
        } else {
            const txt = await res.text();
            alert("Lỗi: " + txt);
        }
    } catch (e) { alert("Lỗi kết nối"); }
}

/// ... (Các phần code cũ giữ nguyên) ...

// ================= 3. THANH TOÁN NÂNG CẤP =================

let currentCustomerPoints = 0; // Biến lưu điểm hiện tại của khách

function openPaymentModal(orderId, tableNum, total) {
    // Reset Form
    document.getElementById("pay-order-id").value = orderId;
    document.getElementById("pay-table-num").innerText = tableNum;

    // Lưu giá gốc (dạng số) để tính toán
    document.getElementById("pay-original-total-value").value = total;
    document.getElementById("pay-total").innerText = formatMoney(total);

    // Reset các trường nhập liệu
    document.getElementById("pay-phone").value = "";
    document.getElementById("customer-info").style.display = "none";
    document.getElementById("customer-info").innerHTML = "";
    document.getElementById("point-redeem-section").style.display = "none";
    document.getElementById("pay-points-use").value = 0;
    const points = Math.floor(total / 20000); // Sửa thành chia 20.000
    document.getElementById("pay-points-earn").innerText = `+${points}`;
    // Tính lại giá lần đầu
    calculateFinalTotal();

    new bootstrap.Modal(document.getElementById("modalPayment")).show();
}

// Hàm kiểm tra SĐT
async function checkCustomerPoint() {
    const phone = document.getElementById("pay-phone").value.trim();
    if (!phone) {
        alert("Vui lòng nhập số điện thoại!");
        return;
    }

    try {
        // Gọi API check khách
        const res = await fetch(`${API_BASE}/customers/lookup?phone=${phone}`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        const data = await res.json();

        const infoDiv = document.getElementById("customer-info");
        const redeemDiv = document.getElementById("point-redeem-section");

        if (data.found) {
            currentCustomerPoints = data.points;
            infoDiv.style.display = "block";
            infoDiv.innerHTML = `<i class="fas fa-user-check"></i> ${data.name} - Hiện có: ${data.points} điểm`;

            // Hiện ô nhập điểm trừ
            redeemDiv.style.display = "block";
            document.getElementById("pay-points-use").max = data.points; // Không cho nhập quá số điểm đang có
        } else {
            currentCustomerPoints = 0;
            infoDiv.style.display = "block";
            infoDiv.innerHTML = `<i class="fas fa-user-times"></i> Khách hàng mới (Sẽ tạo tích điểm sau khi thanh toán)`;
            redeemDiv.style.display = "none";
        }
    } catch (e) {
        console.error(e);
        alert("Lỗi kiểm tra khách hàng");
    }
}

// Hàm tính toán tiền real-time khi nhập điểm
function calculateFinalTotal() {
    const originalTotal = parseFloat(document.getElementById("pay-original-total-value").value);
    let pointsToUse = parseInt(document.getElementById("pay-points-use").value) || 0;

    // Validate: Không được nhập quá điểm hiện có
    if (pointsToUse > currentCustomerPoints) {
        pointsToUse = currentCustomerPoints;
        document.getElementById("pay-points-use").value = pointsToUse;
    }

    // Quy đổi: 1 điểm = 10 VND
    const discount = pointsToUse * 1000;

    // Validate: Không được giảm quá tổng tiền
    if (discount > originalTotal) {
        alert("Số điểm trừ vượt quá giá trị đơn hàng!");
        document.getElementById("pay-points-use").value = 0;
        calculateFinalTotal();
        return;
    }

    const finalTotal = originalTotal - discount;

    // Hiển thị
    document.getElementById("discount-display").innerText = `-${formatMoney(discount)}`;
    document.getElementById("pay-final").innerText = formatMoney(finalTotal);

    // Tính điểm sẽ tích được (dựa trên giá gốc)
    const pointsEarn = Math.floor(originalTotal / 20000);
    document.getElementById("pay-points-earn").innerText = `+${pointsEarn}`;
}

async function confirmPayment() {
    const orderId = document.getElementById("pay-order-id").value;
    const phone = document.getElementById("pay-phone").value.trim();
    const pointsToUse = document.getElementById("pay-points-use").value;

    const payload = {
        phone: phone,
        pointsToUse: pointsToUse
    };

    if(!confirm("Xác nhận thanh toán?")) return;

    try {
        const res = await fetch(`${API_BASE}/orders/${orderId}/pay`, {
            method: "PUT",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });

        if(res.ok) {
            const data = await res.json();
            alert(`✅ ${data.message}\n💰 Khách trả: ${formatMoney(data.finalTotal)}\n⭐ Điểm tích thêm: ${data.pointsEarned}`);

            // Đóng modal & Reload
            bootstrap.Modal.getInstance(document.getElementById("modalPayment")).hide();
            loadTableMap();
        } else {
            const txt = await res.text();
            alert("❌ Lỗi: " + txt);
        }
    } catch (e) {
        console.error(e);
        alert("Lỗi kết nối");
    }
}
// ================= 4. TẠO ĐƠN MỚI (POS) =================
async function createQuickOrder() {
    const body = {
        tableNumber: parseInt(document.getElementById("pos-table").value),
        // Không bắt buộc SĐT ở bước này, có thể nhập khi thanh toán
        phone: document.getElementById("pos-phone").value,
        note: document.getElementById("pos-note").value,
        items: []
    };

    if(!body.tableNumber) { alert("Chưa nhập số bàn!"); return; }

    const res = await fetch(`${API_BASE}/orders/create`, {
        method: "POST",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
        body: JSON.stringify(body)
    });

    if(res.ok) {
        alert("Đã mở bàn thành công! Vui lòng thêm món cho bàn này.");
        const tablesBtn = document.querySelector("button[onclick=\"switchTab('tables')\"]");
        if(tablesBtn) tablesBtn.click();
        else switchTab('tables');
    } else alert("Lỗi tạo đơn!");
}

// ================= 5. QUẢN LÝ ĐƠN ONLINE =================
async function loadOnlineOrders() {
    try {
        const res = await fetch(`${API_BASE}/orders/online-pending`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if(!res.ok) return;

        const data = await res.json();
        const tbody = document.getElementById("online-order-list");
        if(tbody) {
            tbody.innerHTML = "";
            const badge = document.getElementById("badge-online");
            if(badge) badge.innerText = data.length;

            data.forEach(o => {
                let timeNote = "";
                if(o.desiredTime) {
                    const time = new Date(o.desiredTime).toLocaleTimeString();
                    timeNote = `<br><span class="badge bg-info">Hẹn giao: ${time}</span>`;
                }

                let typeBadge = `<span class="badge bg-secondary">${o.orderType}</span>`;
                if(o.orderType === 'DELIVERY') typeBadge = `<span class="badge bg-primary"><i class="fas fa-truck"></i> Giao đi</span>`;
                if(o.orderType === 'TAKEAWAY') typeBadge = `<span class="badge bg-warning text-dark"><i class="fas fa-shopping-bag"></i> Mang về</span>`;

                tbody.innerHTML += `
                    <tr>
                        <td>${new Date(o.createdAt).toLocaleTimeString()}</td>
                        <td>
                            <strong>${o.customerName || 'Khách lẻ'}</strong><br>
                            <small>${o.customerPhone || ''}</small><br>
                            <small class="text-muted">📍 ${o.address || 'Tại quán'}</small>
                        </td>
                        <td>${typeBadge}</td>
                        <td class="text-danger fw-bold">${formatMoney(o.totalPrice)}</td>
                        <td>${o.note || ''} ${timeNote}</td>
                        <td>
                            <button class="btn btn-sm btn-success" onclick="approveOrder(${o.id})">
                                <i class="fas fa-check"></i> Duyệt
                            </button>
                            <button class="btn btn-sm btn-outline-danger" onclick="rejectOrder(${o.id})">
                                <i class="fas fa-times"></i> Hủy
                            </button>
                        </td>
                    </tr>
                `;
            });
        }
    } catch(e) { console.error("Lỗi load đơn online:", e); }
}

async function approveOrder(id) {
    if(!confirm("Chuyển đơn này xuống bếp nấu ngay?")) return;

    const res = await fetch(`${API_BASE}/orders/${id}/approve`, {
        method: "PUT",
        headers: { "Authorization": `Bearer ${token}` }
    });

    if(res.ok) {
        alert("Đã duyệt đơn! Bếp đã nhận được thông báo.");
        loadOnlineOrders();
    } else {
        alert("Lỗi khi duyệt đơn!");
    }
}

async function rejectOrder(id) {
    if(!confirm("Bạn có chắc muốn HỦY đơn hàng này?")) return;

    const res = await fetch(`${API_BASE}/orders/${id}/reject`, {
        method: "PUT",
        headers: { "Authorization": `Bearer ${token}` }
    });

    if(res.ok) {
        alert("Đã hủy đơn hàng.");
        loadOnlineOrders();
    } else {
        alert("Lỗi khi hủy đơn!");
    }
}

function formatMoney(n) {
    return n ? n.toLocaleString("vi-VN", { style: "currency", currency: "VND" }) : "0 ₫";
}

function logout() {
    localStorage.clear();
    window.location.href = "admin-login.html";
}
