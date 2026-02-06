const API_BASE = "http://localhost:8080/api/staff";
const token = localStorage.getItem("token");
let tempCheckInItems = [];
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

// ================= 2. QUẢN LÝ BOOKING (CẬP NHẬT) =================
// ================= 2. QUẢN LÝ BOOKING (ĐÃ SỬA LỖI HIỂN THỊ & THÊM HÀM CHECK-IN) =================
async function loadBookings() {
    try {
        const res = await fetch(`${API_BASE}/bookings`, { headers: { "Authorization": `Bearer ${token}` } });
        if(!res.ok) return;
        const data = await res.json();
        const tbody = document.getElementById("booking-list");
        if(tbody) tbody.innerHTML = "";

        let pendingCount = 0;
        data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

        data.forEach(b => {
            if(b.status === "PENDING") pendingCount++;

            const isConfirmed = b.status === "CONFIRMED";
            // Escape JSON để tránh lỗi khi render nút bấm
            const bookingJson = JSON.stringify(b).replace(/"/g, '&quot;');

            // --- 1. SỬA LỖI HIỂN THỊ "UNDEFINED" ---
            let itemsHtml = "";
            let hasFood = false;

            if (b.items && b.items.length > 0) {
                hasFood = true;
                // LOGIC FIX: Kiểm tra cả 2 trường hợp tên biến (name/itemName và qty/quantity)
                const listItems = b.items.map(i => {
                    const name = i.itemName || i.name || "Món lạ";
                    const qty = i.quantity || i.qty || 0;
                    return `- ${name} <strong class="text-dark">x${qty}</strong>`;
                }).join('<br>');

                itemsHtml = `
                    <div class="mt-2 small text-primary border-top pt-1 bg-light p-1 rounded">
                        <i class="fas fa-utensils"></i> <b>Đặt trước:</b><br>
                        ${listItems}
                    </div>
                `;
            }

            // --- 2. LOGIC NÚT BẤM ---
            let actionBtn = "";

            if (isConfirmed) {
                // Đã xếp bàn -> Hiện nút Check-in
                const btnText = hasFood
                    ? '<i class="fas fa-utensils"></i> Khách đến & Lên món'
                    : '<i class="fas fa-door-open"></i> Check-in (Mở bàn)';

                const btnClass = hasFood ? 'btn-success' : 'btn-info text-white';
                const safeItems = b.items ? JSON.stringify(b.items).replace(/"/g, '&quot;') : '[]';
                actionBtn = `
    <div class="mb-2">
        <span class="badge bg-success" style="font-size: 0.9rem">Bàn ${b.tableNumber}</span>
    </div>
    <button class="btn btn-sm ${btnClass} fw-bold shadow-sm" 
            onclick="openCheckInModal(${b.id}, ${safeItems})">
        ${btnText}
    </button>
`;
            } else {
                // Chưa xếp bàn -> Hiện nút Xếp bàn
                actionBtn = `
                   <button class="btn btn-sm btn-primary fw-bold" onclick="openAssignModal(${bookingJson})">
                        <i class="fas fa-chair"></i> Xếp bàn
                   </button>
                   <button class="btn btn-sm btn-outline-danger ms-1" onclick="cancelBooking(${b.id})" title="Hủy đơn">
                        <i class="fas fa-times"></i>
                   </button>
                `;
            }

            if(tbody) {
                tbody.innerHTML += `
                    <tr>
                        <td>
                            <span class="badge bg-secondary mb-1">#${b.id}</span>
                            <div class="fw-bold text-primary">${b.bookingTime}</div>
                            <div class="small text-muted">${new Date(b.bookingDate).toLocaleDateString('vi-VN')}</div>
                        </td>
                        <td>
                            <div class="fw-bold">${b.customerName}</div>
                            <div class="small"><i class="fas fa-phone-alt text-muted"></i> ${b.phone}</div>
                            ${itemsHtml}
                        </td>
                        <td>${b.peopleCount} khách</td>
                        <td><small class="text-muted fst-italic">${b.note || 'Không có ghi chú'}</small></td>
                        <td>
                            <span class="badge ${isConfirmed ? 'bg-success' : 'bg-warning text-dark'}">
                                ${b.status === 'PENDING' ? 'Chờ xếp bàn' : 'Đã xếp bàn'}
                            </span>
                        </td>
                        <td>${actionBtn}</td>
                    </tr>
                `;
            }
        });

        const badge = document.getElementById("badge-booking-count");
        if(badge) badge.innerText = pendingCount;

    } catch(e) { console.error("Lỗi load booking:", e); }
}

// --- HÀM MỚI QUAN TRỌNG: CHUYỂN ĐỔI BOOKING -> ORDER (SỬA LỖI REFERENCE ERROR) ---
async function convertBookingToOrder(bookingId) {
    if(!confirm("Khách đã đến? Xác nhận check-in và gửi món xuống bếp?")) return;

    try {
        const res = await fetch(`${API_BASE}/bookings/${bookingId}/check-in`, {
            method: "POST",
            headers: { "Authorization": `Bearer ${token}` }
        });

        if(res.ok) {
            const data = await res.json();
            alert(`✅ ${data.message} (Mã đơn: #${data.orderId})`);

            // Reload lại danh sách để cập nhật trạng thái
            loadBookings();
            // Nếu đang ở tab bàn thì reload cả map bàn
            loadTableMap();
        } else {
            const txt = await res.text();
            alert("Lỗi: " + txt);
        }
    } catch(e) {
        console.error("Vui lòng cập nhật hàm loadBookings trước!");
    }
}
function openCheckInModal(bookingId, items) {
    document.getElementById("checkin-booking-id").value = bookingId;

    // Chuẩn hóa dữ liệu items (vì tên trường có thể là name/itemName)
    tempCheckInItems = items.map(i => ({
        name: i.itemName || i.name,
        qty: i.quantity || i.qty,
        price: i.price
    }));

    renderCheckInItems();
    new bootstrap.Modal(document.getElementById("modalCheckInConfirm")).show();
}

// --- HÀM MỚI: RENDER DANH SÁCH TRONG MODAL ---
function renderCheckInItems() {
    const tbody = document.getElementById("checkin-items-list");
    tbody.innerHTML = "";
    let total = 0;

    if (tempCheckInItems.length === 0) {
        tbody.innerHTML = `<tr><td colspan="3" class="text-center text-muted py-3">Không có món ăn</td></tr>`;
    } else {
        tempCheckInItems.forEach((item, index) => {
            total += item.price * item.qty;
            tbody.innerHTML += `
                <tr>
                    <td>
                        <div class="fw-bold small">${item.name}</div>
                        <div class="text-muted small">${item.price.toLocaleString()}đ</div>
                    </td>
                    <td class="text-center">
                        <div class="input-group input-group-sm">
                            <button class="btn btn-outline-secondary" onclick="updateCheckInQty(${index}, -1)">-</button>
                            <input type="text" class="form-control text-center px-0" value="${item.qty}" readonly>
                            <button class="btn btn-outline-secondary" onclick="updateCheckInQty(${index}, 1)">+</button>
                        </div>
                    </td>
                    <td class="text-end">
                        <button class="btn btn-link text-danger p-0" onclick="removeCheckInItem(${index})">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                </tr>
            `;
        });
    }
    document.getElementById("checkin-total").innerText = total.toLocaleString() + "đ";
}

// --- HÀM MỚI: TĂNG GIẢM SỐ LƯỢNG ---
function updateCheckInQty(index, change) {
    const item = tempCheckInItems[index];
    const newQty = item.qty + change;
    if (newQty > 0) {
        item.qty = newQty;
        renderCheckInItems();
    }
}

// --- HÀM MỚI: XÓA MÓN ---
function removeCheckInItem(index) {
    if(confirm("Xóa món này khỏi đơn?")) {
        tempCheckInItems.splice(index, 1);
        renderCheckInItems();
    }
}

// --- HÀM MỚI: GỬI API CHECK-IN ---
async function submitCheckIn() {
    const bookingId = document.getElementById("checkin-booking-id").value;
    const btn = document.querySelector("#modalCheckInConfirm .btn-success");

    // Payload gửi đi
    const payload = {
        items: tempCheckInItems // Gửi danh sách đã sửa
    };

    btn.disabled = true;
    btn.innerHTML = "Đang xử lý...";

    try {
        const res = await fetch(`${API_BASE}/bookings/${bookingId}/check-in`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            const data = await res.json();
            alert(`✅ ${data.message} (Mã đơn: #${data.orderId})`);
            bootstrap.Modal.getInstance(document.getElementById("modalCheckInConfirm")).hide();
            loadBookings();
            loadTableMap(); // Refresh sơ đồ bàn để thấy bàn chuyển màu
        } else {
            const txt = await res.text();
            alert("Lỗi: " + txt);
        }
    } catch (e) {
        alert("Lỗi kết nối");
    } finally {
        btn.disabled = false;
        btn.innerHTML = '<i class="fas fa-check"></i> CHỐT ĐƠN & BẾP NẤU';
    }
}
async function openAssignModal(booking) {
    // 1. Lưu ID và hiển thị thông tin
    document.getElementById("assign-booking-id").value = booking.id;
    document.getElementById("assign-time-display").innerText = `${booking.bookingTime} - ${new Date(booking.bookingDate).toLocaleDateString('vi-VN')}`;
    document.getElementById("assign-people-display").innerText = booking.peopleCount;

    // 2. Mở Modal
    const modal = new bootstrap.Modal(document.getElementById("modalAssignTable"));
    modal.show();

    // 3. Gọi API lấy trạng thái bàn TẠI THỜI ĐIỂM ĐÓ
    const grid = document.getElementById("assign-table-grid");
    grid.innerHTML = '<div class="text-center w-100 py-3"><div class="spinner-border text-primary"></div><div class="mt-2">Đang kiểm tra lịch trùng...</div></div>';

    try {
        // API này Backend cần cung cấp: Check xem vào ngày X giờ Y, bàn nào đang vướng Booking khác hoặc đang có đơn Order chưa thanh toán
        const res = await fetch(`${API_BASE}/tables/status-at-time?date=${booking.bookingDate}&time=${booking.bookingTime}`, {
            headers: { "Authorization": `Bearer ${token}` }
        });

        // Giả sử API trả về mảng: [{number: 1, status: 'FREE'}, {number: 2, status: 'BUSY', reason: 'Booking #123'}]
        let tables = [];
        if (res.ok) {
            tables = await res.json();
        } else {
            // NẾU API LỖI -> BÁO LỖI LUÔN, KHÔNG RANDOM NỮA
            grid.innerHTML = `<div class="text-danger text-center p-3">
                            <i class="fas fa-exclamation-triangle"></i><br>
                            Lỗi: Không tải được trạng thái bàn (HTTP ${res.status})
                          </div>`;
            return; // Dừng lại, không render tiếp
        }

        // 4. Render Grid
        grid.innerHTML = "";
        tables.forEach(t => {
            const isBusy = t.status !== 'FREE';
            const bgClass = isBusy ? 'bg-danger text-white opacity-50' : 'bg-success text-white';
            const cursor = isBusy ? 'not-allowed' : 'pointer';
            const clickAction = isBusy ? '' : `onclick="submitAssignTable(${t.number})"`;
            const icon = isBusy ? '<i class="fas fa-ban"></i>' : '<i class="fas fa-check"></i>';
            const label = isBusy ? (t.reason || 'Bận') : 'Trống';

            grid.innerHTML += `
                <div class="col-3 col-md-2">
                    <div class="p-3 rounded text-center shadow-sm ${bgClass}" 
                         style="cursor: ${cursor}; transition: 0.2s;"
                         ${clickAction}
                         onmouseover="this.style.transform='scale(1.05)'" 
                         onmouseout="this.style.transform='scale(1)'">
                        <div class="fs-4 fw-bold">${t.number}</div>
                        <div class="small">${icon} ${label}</div>
                    </div>
                </div>
            `;
        });

    } catch (e) {
        console.error(e);
        grid.innerHTML = `<div class="text-danger text-center">Lỗi tải dữ liệu bàn: ${e.message}</div>`;
    }
}
async function submitAssignTable(tableNumber) {
    const bookingId = document.getElementById("assign-booking-id").value;

    if(!confirm(`Xác nhận xếp khách vào Bàn ${tableNumber}?`)) return;

    try {
        const res = await fetch(`${API_BASE}/bookings/${bookingId}/confirm`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({ tableNumber: tableNumber })
        });

        if(res.ok) {
            alert("✅ Đã xếp bàn thành công!");
            // Đóng modal
            bootstrap.Modal.getInstance(document.getElementById("modalAssignTable")).hide();
            // Reload lại danh sách
            loadBookings();
            // Nếu ngày đặt là hôm nay, reload luôn sơ đồ bàn chính
            loadTableMap();
        } else {
            const txt = await res.text();
            alert("❌ Lỗi: " + txt);
        }
    } catch (e) {
        alert("Lỗi kết nối server!");
    }
}
function filterBookings() {
    // 1. Lấy từ khóa tìm kiếm, chuyển về chữ thường
    const input = document.getElementById("bookingSearchInput");
    const filter = input.value.toLowerCase();

    // 2. Lấy tất cả các dòng trong bảng
    const table = document.getElementById("booking-list");
    const tr = table.getElementsByTagName("tr");

    // 3. Duyệt qua từng dòng
    for (let i = 0; i < tr.length; i++) {
        // Lấy cột Tên/SĐT (Cột thứ 2 - index 1)
        const tdCustomer = tr[i].getElementsByTagName("td")[1];

        if (tdCustomer) {
            const txtValue = tdCustomer.textContent || tdCustomer.innerText;
            // Nếu tìm thấy từ khóa trong dòng đó -> Hiện, không thì -> Ẩn
            if (txtValue.toLowerCase().indexOf(filter) > -1) {
                tr[i].style.display = "";
            } else {
                tr[i].style.display = "none";
            }
        }
    }
}
// Thêm hàm hủy booking nếu cần
async function cancelBooking(id) {
    if(!confirm("Bạn muốn hủy yêu cầu đặt bàn này?")) return;
    try {
        const res = await fetch(`${API_BASE}/bookings/${id}/cancel`, {
            method: "PUT",
            headers: { "Authorization": `Bearer ${token}` }
        });
        if(res.ok) loadBookings();
    } catch(e) {}
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
