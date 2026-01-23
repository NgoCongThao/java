let orders = []; 
let menuData = []; 
let currentSelection = {}; 

document.addEventListener('DOMContentLoaded', () => {
    // === SỬA ĐƯỜNG DẪN Ở ĐÂY ===
    // '../data.json' nghĩa là tìm file ở thư mục cha bên ngoài
    // Nếu file đang ở chung thư mục, hãy sửa thành 'data.json'
    fetch('data.json') 
        .then(response => {
            if (!response.ok) {
                throw new Error("Không tìm thấy file data.json! Kiểm tra lại vị trí file.");
            }
            return response.json();
        })
        .then(data => {
            menuData = data;
            console.log("Đã tải menu thành công:", menuData.length, "món");
        })
        .catch(err => {
            console.error(err);
            alert("Lỗi: " + err.message + "\n\nGợi ý: Hãy thử di chuyển file data.json vào trong cùng thư mục với file index.html");
        });

    startSystemClock();
    setInterval(updateTimers, 1000);
});

// --- CÁC HÀM XỬ LÝ MODAL ---

function openOrderModal() {
    if (menuData.length === 0) {
        alert("Chưa tải được dữ liệu món ăn! Vui lòng kiểm tra file data.json");
        return;
    }
    currentSelection = {}; 
    renderMenuModal();     
    document.getElementById('order-modal').classList.remove('hidden');
    updateTotalCount();
}

function closeOrderModal() {
    document.getElementById('order-modal').classList.add('hidden');
}

function renderMenuModal() {
    const container = document.getElementById('menu-list');
    container.innerHTML = '';

    menuData.forEach(item => {
        const isOutOfStock = item.status === 'out_of_stock';
        const qty = currentSelection[item.id] || 0;
        const selectedClass = qty > 0 ? 'selected' : '';
        const disabledClass = isOutOfStock ? 'disabled' : '';

        const div = document.createElement('div');
        div.className = `menu-item ${selectedClass} ${disabledClass}`;
        div.onclick = () => selectItem(item.id, isOutOfStock);
        
        div.innerHTML = `
            <div class="qty-badge">${qty}</div>
            <span class="menu-name">${item.name}</span>
            <span class="menu-price">${item.price.toLocaleString()}đ</span>
            ${isOutOfStock ? '<br><small style="color:#f44336">(Hết)</small>' : ''}
        `;
        container.appendChild(div);
    });
}

function selectItem(id, isOutOfStock) {
    if (isOutOfStock) return; 

    if (!currentSelection[id]) currentSelection[id] = 0;
    currentSelection[id]++;
    
    renderMenuModal();
    updateTotalCount();
}

function updateTotalCount() {
    let total = 0;
    Object.values(currentSelection).forEach(qty => total += qty);
    document.getElementById('total-count').innerText = total;
}

function confirmOrder() {
    const selectedIds = Object.keys(currentSelection);
    if (selectedIds.length === 0) {
        alert("Bạn chưa chọn món nào!");
        return;
    }

    const orderItems = selectedIds.map(id => {
        const itemInfo = menuData.find(m => m.id === id);
        return {
            name: itemInfo.name,
            qty: currentSelection[id],
            note: "" 
        };
    });

    const newOrder = {
        id: Date.now(),
        table: "Bàn " + (Math.floor(Math.random() * 20) + 1),
        startTime: new Date().getTime(),
        items: orderItems
    };

    orders.push(newOrder);
    renderOrders();     
    closeOrderModal();  
}

// --- CÁC HÀM CŨ ---

function renderOrders() {
    const container = document.getElementById('order-container');
    container.innerHTML = ''; 

    orders.forEach(order => {
        const card = document.createElement('div');
        card.className = 'order-card';
        
        let itemsHtml = '';
        order.items.forEach(item => {
            itemsHtml += `
                <div class="order-item">
                    <div>
                        <span class="item-qty">${item.qty}x</span>
                        <span class="item-name">${item.name}</span>
                        ${item.note ? `<span class="item-note">(${item.note})</span>` : ''}
                    </div>
                </div>
            `;
        });

        card.innerHTML = `
            <div class="card-header">
                <span class="table-num">${order.table}</span>
                <span class="timer safe" data-start="${order.startTime}">00:00</span>
            </div>
            <div class="item-list">
                ${itemsHtml}
            </div>
            <div class="card-actions">
                <button class="btn-done" onclick="completeOrder(${order.id})">HOÀN THÀNH</button>
            </div>
        `;
        container.appendChild(card);
    });
}

function updateTimers() {
    const timers = document.querySelectorAll('.timer');
    const now = new Date().getTime();

    timers.forEach(timer => {
        const startTime = parseInt(timer.getAttribute('data-start'));
        const elapsed = Math.floor((now - startTime) / 1000); 
        
        const minutes = Math.floor(elapsed / 60).toString().padStart(2, '0');
        const seconds = (elapsed % 60).toString().padStart(2, '0');
        timer.textContent = `${minutes}:${seconds}`;

        timer.className = 'timer'; 
        if (elapsed < 300) timer.classList.add('safe');
        else if (elapsed < 600) timer.classList.add('warn');
        else timer.classList.add('late');
    });
}

function completeOrder(id) {
    if(confirm("Xong đơn này rồi chứ?")) {
        orders = orders.filter(o => o.id !== id);
        renderOrders();
    }
}

function startSystemClock() {
    setInterval(() => {
        const now = new Date();
        document.getElementById('system-clock').innerText = now.toLocaleTimeString('vi-VN');
    }, 1000);
}
// Hàm mở Modal (Sửa lại để reset ô nhập bàn)
function openOrderModal() {
    if (menuData.length === 0) {
        alert("Chưa tải được dữ liệu món ăn! Vui lòng kiểm tra file data.json");
        return;
    }
    
    // Reset dữ liệu cũ
    currentSelection = {}; 
    document.getElementById('table-input').value = ""; // <--- Xóa tên bàn cũ
    document.getElementById('table-input').focus();    // <--- Tự động đặt chuột vào ô nhập cho tiện

    renderMenuModal();     
    document.getElementById('order-modal').classList.remove('hidden');
    updateTotalCount();
}

// Hàm xác nhận đơn (Sửa lại để lấy tên bàn từ ô nhập)
function confirmOrder() {
    // 1. Lấy tên bàn từ ô input
    const tableName = document.getElementById('table-input').value.trim();

    // 2. Validate: Bắt buộc phải nhập tên bàn mới cho tạo
    if (!tableName) {
        alert("Vui lòng nhập tên Bàn hoặc Nguồn đơn (Ví dụ: Bàn 1, Grab...)");
        document.getElementById('table-input').focus();
        return;
    }

    // 3. Kiểm tra xem có chọn món nào chưa
    const selectedIds = Object.keys(currentSelection);
    if (selectedIds.length === 0) {
        alert("Bạn chưa chọn món nào!");
        return;
    }

    const orderItems = selectedIds.map(id => {
        const itemInfo = menuData.find(m => m.id === id);
        return {
            name: itemInfo.name,
            qty: currentSelection[id],
            note: "" 
        };
    });

    const newOrder = {
        id: Date.now(),
        table: tableName, // <--- Dùng tên bàn vừa nhập (Không random nữa)
        startTime: new Date().getTime(),
        items: orderItems
    };

    orders.push(newOrder);
    renderOrders();     
    closeOrderModal();  
}