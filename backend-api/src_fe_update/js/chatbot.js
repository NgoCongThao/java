// chatbot.js - Chatbot cho website nhà hàng S2O
// Senior Frontend Developer: Viết bằng Vanilla JS, sử dụng Fuse.js cho fuzzy search
// Tất cả logic, UI, CSS đều nằm trong file này để dễ maintain

// ==================== 1. LOAD THƯ VIỆN & KHỞI TẠO ====================
(function () {
  // Load Fuse.js từ CDN nếu chưa có
  if (!window.Fuse) {
    const script = document.createElement("script");
    script.src = "https://cdn.jsdelivr.net/npm/fuse.js/dist/fuse.min.js";
    script.onload = initChatbot;
    script.onerror = () => console.error("Không tải được Fuse.js");
    document.head.appendChild(script);
  } else {
    initChatbot();
  }

  function initChatbot() {
    // Biến toàn cục
    window.chatbotData = {
      restaurants: [],
      flatMenuItems: [],
      restaurantSearchItems: [],
      itemFuse: null,
      restaurantFuse: null,
      isWelcomed: false,
    };

    // Fetch dữ liệu từ 2 file JSON
    Promise.all([
      fetch("backend-api/src_fe_update/data/restaurants.json")
        .then((r) => (r.ok ? r.json() : []))
        .catch((err) => {
          console.error("Lỗi tải restaurants.json:", err);
          return [];
        }),
      fetch("backend-api/src_fe_update/data/menus.json")
        .then((r) => (r.ok ? r.json() : []))
        .catch((err) => {
          console.error("Lỗi tải menus.json:", err);
          return [];
        }),
    ]).then(([restaurants, menusData]) => {
      chatbotData.restaurants = restaurants;

      // Flatten dữ liệu menus (giả định cấu trúc menus.json là mảng các object {restaurant_id, categories: [{category_name, items: [...] }] })
      chatbotData.flatMenuItems = [];
      chatbotData.restaurantSearchItems = restaurants.map((r) => ({
        ...r,
        searchName: removeAccents(r.name.toLowerCase()),
      }));

      // Flatten menu items
      menusData.forEach((menu) => {
        const restaurant = restaurants.find((r) => r.id === menu.restaurant_id);
        if (restaurant) {
          menu.categories.forEach((category) => {
            category.items.forEach((item) => {
              chatbotData.flatMenuItems.push({
                name: item.name,
                searchName: removeAccents(item.name.toLowerCase()),
                price: item.price || 0,
                category: category.category_name,
                restaurantName: restaurant.name,
                restaurantRating: restaurant.rating || 0,
                isBestSeller: !!item.best_seller,
              });
            });
          });
        }
      });

      // Khởi tạo Fuse.js
      chatbotData.itemFuse = new Fuse(chatbotData.flatMenuItems, {
        keys: ["searchName"],
        threshold: 0.3, // Cho phép sai chính tả nhẹ & không dấu
        includeScore: true,
      });

      chatbotData.restaurantFuse = new Fuse(chatbotData.restaurantSearchItems, {
        keys: ["searchName"],
        threshold: 0.3,
        includeScore: true,
      });

      // Tạo UI sau khi có dữ liệu
      createChatbotUI();
    });
  }
})();

// ==================== 2. HÀM HỖ TRỢ ====================
// Loại bỏ dấu tiếng Việt
function removeAccents(str) {
  if (!str) return "";
  return str
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d")
    .replace(/Đ/g, "D");
}

// Format giá tiền VND
function formatPrice(price) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(price);
}

// ==================== 3. TẠO UI & CSS ====================
function createChatbotUI() {
  // Inject CSS
  const style = document.createElement("style");
  style.textContent = `
    /* Chatbot Styles - Giống Facebook Messenger */
    #s2o-chatbot-container { font-family: Arial, sans-serif; }
    #s2o-floating-button {
      position: fixed;
      bottom: 20px;
      right: 20px;
      width: 60px;
      height: 60px;
      background: #007bff;
      border-radius: 50%;
      box-shadow: 0 4px 12px rgba(0,0,0,0.3);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
      transition: transform 0.2s;
    }
    #s2o-floating-button:hover { transform: scale(1.1); }
    #s2o-floating-button img { width: 32px; height: 32px; }

    #s2o-chat-window {
      position: fixed;
      bottom: 90px;
      right: 20px;
      width: 380px;
      max-width: 90vw;
      height: 560px;
      background: white;
      border-radius: 12px;
      box-shadow: 0 8px 32px rgba(0,0,0,0.3);
      display: none;
      flex-direction: column;
      z-index: 1000;
      overflow: hidden;
    }
    #s2o-chat-header {
      background: #007bff;
      color: white;
      padding: 15px;
      font-weight: bold;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    #s2o-chat-header .close-btn { cursor: pointer; font-size: 24px; }

    #s2o-messages {
      flex: 1;
      padding: 15px;
      overflow-y: auto;
      background: #f8f9fa;
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    .message { display: flex; align-items: flex-start; max-width: 80%; }
    .message.bot { align-self: flex-start; }
    .message.user { align-self: flex-end; flex-direction: row-reverse; }
    .message .avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      margin: 0 8px;
    }
    .bubble {
      padding: 10px 15px;
      border-radius: 18px;
      line-height: 1.4;
      word-wrap: break-word;
    }
    .bot-bubble {
      background: #e9ecef;
      border-bottom-left-radius: 4px;
    }
    .user-bubble {
      background: #007bff;
      color: white;
      border-bottom-right-radius: 4px;
    }
    .typing .bubble {
      background: #e9ecef;
      display: flex;
      align-items: center;
      gap: 4px;
    }
    .typing span {
      display: inline-block;
      width: 8px;
      height: 8px;
      background: #999;
      border-radius: 50%;
      animation: bounce 1.2s infinite;
    }
    .typing span:nth-child(2) { animation-delay: 0.2s; }
    .typing span:nth-child(3) { animation-delay: 0.4s; }
    @keyframes bounce {
      0%, 80%, 100% { transform: translateY(0); }
      40% { transform: translateY(-8px); }
    }

    #s2o-input-area {
      padding: 12px;
      background: white;
      border-top: 1px solid #ddd;
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
    #s2o-input-wrapper {
      display: flex;
      gap: 8px;
    }
    #s2o-chat-input {
      flex: 1;
      padding: 12px;
      border: 1px solid #ddd;
      border-radius: 24px;
      outline: none;
    }
    #s2o-send-btn {
      background: #007bff;
      color: white;
      border: none;
      width: 40px;
      height: 40px;
      border-radius: 50%;
      cursor: pointer;
    }

    /* Quick Chips - Gợi ý nhanh */
    #s2o-quick-chips {
      display: flex;
      gap: 10px;
      overflow-x: auto;
      padding: 8px 0;
      scrollbar-width: none;
    }
    #s2o-quick-chips::-webkit-scrollbar { display: none; }
    .chip {
      background: #e3f2fd;
      color: #1976d2;
      padding: 10px 16px;
      border-radius: 24px;
      white-space: nowrap;
      cursor: pointer;
      font-size: 14px;
      flex-shrink: 0;
      transition: background 0.2s;
    }
    .chip:hover {
      background: #bbdefb;
    }
  `;
  document.head.appendChild(style);

  // Tạo floating button
  const floatingBtn = document.createElement("div");
  floatingBtn.id = "s2o-floating-button";
  floatingBtn.innerHTML = `<img src="https://img.icons8.com/fluency/48/chat.png" alt="Chat">`;
  floatingBtn.onclick = openChat;
  document.body.appendChild(floatingBtn);

  // Tạo chat window
  const chatWindow = document.createElement("div");
  chatWindow.id = "s2o-chat-window";
  chatWindow.innerHTML = `
    <div id="s2o-chat-header">
      <span>Chat với S2O</span>
      <span class="close-btn">&times;</span>
    </div>
    <div id="s2o-messages"></div>
    <div id="s2o-input-area">
      <div id="s2o-quick-chips"></div>
      <div id="s2o-input-wrapper">
        <input type="text" id="s2o-chat-input" placeholder="Nhập tin nhắn..." autocomplete="off">
        <button id="s2o-send-btn">➤</button>
      </div>
    </div>
  `;
  document.body.appendChild(chatWindow);

  // Cache elements
  const messagesContainer = chatWindow.querySelector("#s2o-messages");
  const input = chatWindow.querySelector("#s2o-chat-input");
  const sendBtn = chatWindow.querySelector("#s2o-send-btn");
  const quickChipsDiv = chatWindow.querySelector("#s2o-quick-chips");
  const closeBtn = chatWindow.querySelector(".close-btn");

  // Quick suggestions
  const quickSuggestions = [
    "🔥 Món Best Seller",
    "📍 Địa chỉ các quán",
    "💰 Giá buffet",
    "📝 Cách đặt bàn",
  ];

  // Event listeners
  closeBtn.onclick = closeChat;
  sendBtn.onclick = () => sendUserMessage(input.value.trim());
  input.addEventListener("keypress", (e) => {
    if (e.key === "Enter") sendUserMessage(input.value.trim());
  });

  // Hàm mở/đóng chat
  function openChat() {
    chatWindow.style.display = "flex";
    floatingBtn.style.display = "none";
    input.focus();

    // Welcome lần đầu
    if (!chatbotData.isWelcomed) {
      chatbotData.isWelcomed = true;
      setTimeout(() => {
        showTyping();
        setTimeout(() => {
          hideTyping();
          addBotMessage(
            "Chào bạn! 👋 Mình là trợ lý ảo của S2O. Bạn cần hỗ trợ gì hôm nay ạ?",
          );
          showQuickChips();
          scrollToBottom();
        }, 800);
      }, 300);
    }
  }

  function closeChat() {
    chatWindow.style.display = "none";
    floatingBtn.style.display = "flex";
  }

  // Scroll xuống cuối
  function scrollToBottom() {
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
  }

  // Hiển thị typing indicator
  function showTyping() {
    const typingDiv = document.createElement("div");
    typingDiv.className = "message bot typing";
    typingDiv.id = "typing-indicator";
    typingDiv.innerHTML = `
      <img src="https://via.placeholder.com/40?text=B" class="avatar">
      <div class="bubble">Bot đang soạn tin...<span></span><span></span><span></span></div>
    `;
    messagesContainer.appendChild(typingDiv);
    scrollToBottom();
  }

  function hideTyping() {
    const typing = document.getElementById("typing-indicator");
    if (typing) typing.remove();
  }

  // Thêm tin nhắn
  function addBotMessage(text) {
    const msg = document.createElement("div");
    msg.className = "message bot";
    msg.innerHTML = `
      <img src="https://via.placeholder.com/40?text=B" class="avatar">
      <div class="bubble bot-bubble">${text.replace(/\n/g, "<br>")}</div>
    `;
    messagesContainer.appendChild(msg);
  }

  function addUserMessage(text) {
    const msg = document.createElement("div");
    msg.className = "message user";
    msg.innerHTML = `
      <img src="https://via.placeholder.com/40?text=U" class="avatar">
      <div class="bubble user-bubble">${text.replace(/\n/g, "<br>")}</div>
    `;
    messagesContainer.appendChild(msg);
    input.value = "";
  }

  // Quick chips
  function showQuickChips() {
    quickChipsDiv.innerHTML = "";
    quickSuggestions.forEach((text) => {
      const chip = document.createElement("div");
      chip.className = "chip";
      chip.textContent = text;
      chip.onclick = () => {
        sendUserMessage(text);
        quickChipsDiv.style.display = "none"; // Ẩn sau khi dùng
      };
      quickChipsDiv.appendChild(chip);
    });
    quickChipsDiv.style.display = "flex";
  }

  // Gửi tin nhắn người dùng & xử lý phản hồi
  function sendUserMessage(text) {
    if (!text) return;
    addUserMessage(text);
    quickChipsDiv.style.display = "none"; // Ẩn quick chips sau tin nhắn đầu
    scrollToBottom();
    showTyping();

    setTimeout(
      () => {
        const reply = getBotReply(text);
        hideTyping();
        addBotMessage(reply);
        scrollToBottom();
      },
      600 + Math.random() * 600,
    );
  }

  // ==================== 4. LOGIC TRẢ LỜI BOT ====================
  function getBotReply(message) {
    const normalized = removeAccents(message.toLowerCase().trim());

    // Các câu hỏi quick chips - xử lý chính xác
    if (message === "🔥 Món Best Seller") {
      const bestSellers = chatbotData.flatMenuItems.filter(
        (i) => i.isBestSeller,
      );
      if (bestSellers.length === 0)
        return "Hiện chưa có thông tin món best seller nổi bật ạ.";
      let reply = "🔥 Các món Best Seller:\n\n";
      bestSellers.forEach((item) => {
        reply += `• ${item.name} tại ${item.restaurantName}\n  Giá: ${formatPrice(item.price)}\n\n`;
      });
      return reply.trim();
    }

    if (message === "📍 Địa chỉ các quán") {
      if (chatbotData.restaurants.length === 0)
        return "Chưa tải được danh sách quán.";
      let reply = "📍 Danh sách các quán S2O:\n\n";
      chatbotData.restaurants.forEach((r) => {
        reply += `• ${r.name}\n  Địa chỉ: ${r.address}\n  Giờ mở: ${r.opening_hours}\n\n`;
      });
      return reply.trim();
    }

    if (message === "💰 Giá buffet") {
      return "💰 Giá buffet hiện tại:\n• Người lớn: 399.000 VND\n• Trẻ em (1m-1.4m): 199.000 VND\n• Bao gồm lẩu + đồ ăn kèm đa dạng";
    }

    if (message === "📝 Cách đặt bàn") {
      return "📝 Cách đặt bàn:\n• Đặt trực tuyến qua website S2O\n• Gọi hotline: 1800-XXXXXXX\n• Qua ứng dụng di động\nChúng tôi khuyến khích đặt trước để giữ chỗ!";
    }

    // Chào hỏi
    if (/chào|hi|hello|xin chào|hế lô/.test(normalized)) {
      const greetings = [
        "Chào bạn! 😊 Rất vui được hỗ trợ!",
        "Xin chào! 👋 Hôm nay bạn muốn tìm món gì ngon?",
        "Hi bạn! Có thể giúp gì cho bạn không ạ?",
      ];
      return greetings[Math.floor(Math.random() * greetings.length)];
    }

    // Gợi ý quán ngon
    if (
      normalized.includes("ngon") ||
      normalized.includes("tốt") ||
      normalized.includes("review") ||
      normalized.includes("đánh giá")
    ) {
      const goodOnes = chatbotData.restaurants.filter((r) => r.rating >= 4.5);
      if (goodOnes.length === 0)
        return "Tất cả các quán S2O đều được khách yêu thích lắm ạ! 😄";
      goodOnes.sort((a, b) => b.rating - a.rating);
      let reply = "🌟 Các quán được đánh giá cao:\n\n";
      goodOnes.forEach((r) => {
        reply += `• ${r.name} - ${r.rating}⭐\n  ${r.address}\n\n`;
      });
      return reply.trim();
    }

    // Tìm tên quán (ưu tiên)
    const restaurantResults = chatbotData.restaurantFuse.search(normalized);
    if (restaurantResults.length > 0 && restaurantResults[0].score < 0.4) {
      const r = restaurantResults[0].item;
      return `🏠 ${r.name}\n📍 Địa chỉ: ${r.address}\n🕒 Giờ mở cửa: ${r.opening_hours}\n⭐ Đánh giá: ${r.rating}`;
    }

    // Tìm món ăn
    const itemResults = chatbotData.itemFuse.search(normalized);
    if (itemResults.length > 0) {
      // Lấy top 10 match tốt nhất, sau đó ưu tiên quán rating cao
      let candidates = itemResults.slice(0, 10);
      candidates.sort((a, b) => {
        if (b.item.restaurantRating !== a.item.restaurantRating) {
          return b.item.restaurantRating - a.item.restaurantRating;
        }
        return a.score - b.score;
      });

      const top3 = candidates.slice(0, 3);
      let reply = `🍜 Tìm thấy một số món liên quan đến "${message}":\n\n`;
      top3.forEach((res) => {
        const i = res.item;
        reply += `• ${i.name} (${i.category})\n  Tại: ${i.restaurantName}\n  Giá: ${formatPrice(i.price)}\n\n`;
      });
      reply += "Bạn muốn biết thêm về món nào không ạ? 😊";
      return reply.trim();
    }

    // Không hiểu
    return "Xin lỗi bạn, mình chưa hiểu rõ câu hỏi 😅\nBạn có thể hỏi về món ăn, địa chỉ quán, giá buffet hoặc cách đặt bàn nhé!";
  }

  // Expose để có thể mở từ ngoài nếu cần
  window.openS2OChat = openChat;
}
