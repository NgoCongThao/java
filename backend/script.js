// Biến lưu menu
let menuData = [];

// 1. Tải dữ liệu từ file JSON khi web vừa mở
async function loadData() {
    try {
        const response = await fetch('data.json');
        menuData = await response.json();
        console.log("Đã tải menu thành công:", menuData);
    } catch (error) {
        console.error("Lỗi tải dữ liệu:", error);
        addMessage("bot", "Hệ thống đang bảo trì, vui lòng quay lại sau!");
    }
}

// Gọi hàm tải dữ liệu ngay lập tức
loadData();

// 2. Xử lý sự kiện gửi tin nhắn
const userInput = document.getElementById("userInput");
const chatBox = document.getElementById("chatBox");

// Cho phép nhấn Enter để gửi
userInput.addEventListener("keypress", function(event) {
    if (event.key === "Enter") {
        sendMessage();
    }
});

function sendMessage() {
    const text = userInput.value.trim();
    if (text === "") return;

    // Hiển thị tin nhắn người dùng
    addMessage("user", text);
    userInput.value = "";

    // Bot suy nghĩ và trả lời (delay 0.5s cho giống thật)
    setTimeout(() => {
        handleBotResponse(text);
    }, 500);
}

// 3. Logic trả lời của Bot
function handleBotResponse(userText) {
    const lowerText = userText.toLowerCase();
    
    // Tìm món ăn khớp với từ khóa
    // Logic: Duyệt qua từng món, xem các keywords của món đó có nằm trong câu user nói không
    let foundItem = null;

    for (let item of menuData) {
        // Kiểm tra từng từ khóa của món (ví dụ: "cơm sườn", "sườn bì")
        for (let keyword of item.keywords) {
            if (lowerText.includes(keyword)) {
                foundItem = item;
                break; // Tìm thấy thì dừng
            }
        }
        if (foundItem) break;
    }

    if (!foundItem) {
        addMessage("bot", "Dạ em chưa hiểu rõ món bạn cần. Bạn thử hỏi tên món cụ thể xem sao nhé (ví dụ: Cơm sườn, Cơm gà...)");
        return;
    }

    // Xử lý logic Còn/Hết
    if (foundItem.status === "available") {
        const msg = `Dạ món <b>${foundItem.name}</b> bên em vẫn còn nóng hổi ạ! <br> Giá: ${formatMoney(foundItem.price)} <br> <a href="${foundItem.link}" target="_blank" class="product-link">👉 Bấm vào đây để đặt ngay</a>`;
        addMessage("bot", msg);
    } else {
        // Món HẾT -> Tìm gợi ý cùng giá
        const suggestions = menuData.filter(item => 
            item.price === foundItem.price && 
            item.status === "available" && 
            item.id !== foundItem.id
        );

        let reply = `Dạ tiếc quá, món <b>${foundItem.name}</b> hôm nay bên em vừa hết ạ. 😭`;
        
        if (suggestions.length > 0) {
            const listNames = suggestions.map(s => `<b>${s.name}</b>`).join(", ");
            reply += `<br><br>Hay là bạn thử sang món: ${listNames} nhé? <br>Đồng giá <b>${formatMoney(foundItem.price)}</b> đó ạ!`;
        } else {
            reply += "<br>Bạn vui lòng xem menu chọn món khác giúp em nha.";
        }
        
        addMessage("bot", reply);
    }
}

// Hàm phụ trợ: Hiển thị tin nhắn lên màn hình
function addMessage(sender, htmlContent) {
    const div = document.createElement("div");
    div.classList.add("message");
    div.classList.add(sender === "user" ? "user-msg" : "bot-msg");
    div.innerHTML = htmlContent;
    chatBox.appendChild(div);
    
    // Tự động cuộn xuống cuối
    chatBox.scrollTop = chatBox.scrollHeight;
}

// Hàm phụ trợ: Định dạng tiền tệ (35000 -> 35.000đ)
function formatMoney(amount) {
    return amount.toLocaleString('vi-VN') + 'đ';
}