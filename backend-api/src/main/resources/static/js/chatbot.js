// src_fe_update/js/chatbot.js

// 1. Định nghĩa giao diện HTML
// URL ảnh Robot 3D (Bạn có thể thay link khác nếu muốn)
const ROBOT_IMG_URL = "https://cdn-icons-png.flaticon.com/512/4712/4712109.png";

const chatWidgetHTML = `
<div id="chat-widget-container">
    <div class="chat-launcher-wrapper">
        <div class="chat-tooltip">Chat với chúng tôi</div>
        <button id="chat-btn" class="chat-launcher" onclick="toggleChat()" aria-label="Mở chat hỗ trợ">
            <img src="${ROBOT_IMG_URL}" alt="Robot AI" class="launcher-robot-img">
            <span class="pulse-ring"></span>
            <span class="pulse-ring pulse-ring-delay"></span>
        </button>
    </div>

    <div id="chat-box" class="chat-window">
        <div class="chat-header">
            <div class="chat-header-info">
                <div class="chat-avatar">
                    <img src="${ROBOT_IMG_URL}" alt="Avatar">
                </div>
                <div class="chat-title">
                    <h3 class="m-0">Trợ lý S2O</h3>
                    <div class="chat-status">
                        <span class="status-indicator online"></span>
                        <span class="status-text">Đang trực tuyến</span>
                    </div>
                </div>
            </div>
            <button class="chat-close-btn" onclick="toggleChat()">
                <i class="fas fa-times"></i>
            </button>
        </div>
        
        <div id="chat-messages" class="chat-body">
            <div class="message-bubble bot-message">
                <div class="message-content">
                    <div class="message-text">
                        Xin chào! 👋 Em là AI hỗ trợ của S2O.<br>
                        <b>Hotline:</b> <a href="tel:0384001761" style="color: inherit; text-decoration: underline;">038.400.1761</a><br>
                        Anh/chị cần em giúp gì không ạ?
                    </div>
                    <div class="message-time">Vừa xong</div>
                </div>
            </div>
            
            <div class="quick-replies-container">
                <p class="quick-replies-title">Gợi ý cho bạn:</p>
                <div class="quick-replies">
                    <button class="quick-reply-chip" onclick="selectQuickReply('Giờ mở cửa là mấy giờ?')">🕒 Giờ mở cửa?</button>
                    <button class="quick-reply-chip" onclick="selectQuickReply('Gọi hotline hỗ trợ')">📞 Gọi Hotline</button>
                    <button class="quick-reply-chip" onclick="selectQuickReply('Menu hôm nay có gì?')">🍽 Menu hôm nay</button>
                    <button class="quick-reply-chip" onclick="selectQuickReply('Địa chỉ quán ở đâu?')">📍 Địa chỉ quán</button>
                </div>
            </div>
        </div>
        
        <div id="bot-typing" class="typing-indicator">
            <div class="typing-dots">
                <div class="dot"></div><div class="dot"></div><div class="dot"></div>
            </div>
            <span class="typing-text">AI đang trả lời...</span>
        </div>

        <div class="chat-footer">
            <div class="chat-input-container">
                <input type="text" id="chat-input" class="chat-input" 
                       placeholder="Nhập tin nhắn..." 
                       onkeypress="handleEnter(event)">
            </div>
            <button class="send-btn" onclick="sendMessage()">
                <i class="fas fa-paper-plane"></i>
            </button>
        </div>
    </div>
</div>
`;

// 2. Inject vào DOM
document.addEventListener("DOMContentLoaded", function () {
  document.body.insertAdjacentHTML("beforeend", chatWidgetHTML);
});

// 3. Biến toàn cục
let isChatOpen = false;

// 4. Toggle Chat
function toggleChat() {
  const chatBox = document.getElementById("chat-box");
  const chatBtn = document.getElementById("chat-btn");

  isChatOpen = !isChatOpen;

  if (isChatOpen) {
    chatBox.classList.add("chat-open");
    chatBox.classList.remove("chat-closed");
    chatBtn.classList.add("active");
    document.getElementById("chat-input").focus();
    document.querySelector(".chat-tooltip").style.opacity = "0";
  } else {
    chatBox.classList.remove("chat-open");
    chatBox.classList.add("chat-closed");
    chatBtn.classList.remove("active");
    setTimeout(() => {
      if (!isChatOpen)
        document.querySelector(".chat-tooltip").style.opacity = "1";
    }, 1000);
  }
}

// 5. Xử lý Enter
function handleEnter(e) {
  if (e.key === "Enter") sendMessage();
}

// 6. Gửi tin nhắn
async function sendMessage() {
  const input = document.getElementById("chat-input");
  const msg = input.value.trim();
  if (!msg) return;

  // Xử lý logic Hotline ở Frontend (để phản hồi nhanh)
  if (
    msg.toLowerCase().includes("hotline") ||
    msg.toLowerCase().includes("sdt") ||
    msg.includes("số điện thoại")
  ) {
    addMessage(msg, "user");
    input.value = "";
    setTimeout(() => {
      addMessage(
        "📞 Hotline quản lý: <b>038.400.1761</b>. Anh/chị có thể gọi trực tiếp để được hỗ trợ nhanh nhất ạ!",
        "bot",
      );
    }, 500);
    return;
  }

  addMessage(msg, "user");
  input.value = "";

  // Hiện loading
  document.getElementById("bot-typing").style.display = "flex";
  const chatBody = document.getElementById("chat-messages");
  chatBody.scrollTop = chatBody.scrollHeight;

  try {
    const urlParams = new URLSearchParams(window.location.search);
    const resId = urlParams.get("id");

    const res = await fetch("/api/chat/ask", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ message: msg, restaurantId: resId }),
    });

    const data = await res.json();
    document.getElementById("bot-typing").style.display = "none";
    addMessage(data.reply, "bot");

    // Hiện gợi ý tiếp theo
    showDynamicQuickReplies(msg);
  } catch (e) {
    document.getElementById("bot-typing").style.display = "none";
    addMessage("Xin lỗi, Server đang bận. Thử lại sau nhé!", "bot");
    console.error(e);
  }
}

// 7. Thêm tin nhắn vào khung chat
function addMessage(text, sender) {
  const chatBody = document.getElementById("chat-messages");
  const formattedText = text.replace(/\n/g, "<br>");
  const messageDiv = document.createElement("div");
  messageDiv.className = `message-bubble ${sender}-message`;

  const now = new Date();
  const timeString = now.toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
  });

  messageDiv.innerHTML = `
        <div class="message-content">
            <div class="message-text">${formattedText}</div>
            <div class="message-time">${timeString}</div>
        </div>
    `;

  chatBody.appendChild(messageDiv);
  chatBody.scrollTop = chatBody.scrollHeight;

  // Ẩn gợi ý cũ nếu user tự chat
  if (sender === "user") {
    const defaultReplies = document.querySelector(".quick-replies-container");
    if (defaultReplies) defaultReplies.style.display = "none";
    const dynamicReplies = document.querySelector(".dynamic-quick-replies");
    if (dynamicReplies) dynamicReplies.remove();
  }
}

// 8. Chọn Quick Reply
function selectQuickReply(text) {
  const input = document.getElementById("chat-input");
  input.value = text;
  sendMessage();
}

// 9. Gợi ý động thông minh
function showDynamicQuickReplies(userMessage) {
  const chatBody = document.getElementById("chat-messages");
  const oldReplies = document.querySelector(".dynamic-quick-replies");
  if (oldReplies) oldReplies.remove();

  let replies = [];
  const lowerMsg = userMessage.toLowerCase();

  // Logic gợi ý dựa trên câu hỏi trước
  if (lowerMsg.includes("menu") || lowerMsg.includes("ăn")) {
    replies = [
      "Món nào bán chạy nhất?",
      "Có món chay không?",
      "Giá khoảng bao nhiêu?",
    ];
  } else if (lowerMsg.includes("địa chỉ") || lowerMsg.includes("ở đâu")) {
    replies = ["Giờ mở cửa?", "Có chỗ đậu xe không?", "Chỉ đường"];
  } else {
    replies = ["Menu chi tiết", "Liên hệ Hotline", "Đặt bàn"];
  }

  const repliesContainer = document.createElement("div");
  repliesContainer.className = "dynamic-quick-replies quick-replies-container";
  repliesContainer.innerHTML = `
        <div class="quick-replies" style="justify-content: flex-end;">
            ${replies.map((r) => `<button class="quick-reply-chip" onclick="selectQuickReply('${r}')">${r}</button>`).join("")}
        </div>
    `;
  chatBody.appendChild(repliesContainer);
  chatBody.scrollTop = chatBody.scrollHeight;
}

// Tự động bật tooltip sau 3s
setTimeout(() => {
  if (!isChatOpen) {
    const tooltip = document.querySelector(".chat-tooltip");
    if (tooltip) tooltip.style.opacity = "1";
  }
}, 3000);
