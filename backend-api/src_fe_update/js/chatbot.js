/**
 * Chatbot đơn giản cho website S2O
 * Vanilla JS - Chạy hoàn toàn client-side
 */

// ================================
// BIẾN DỮ LIỆU
// ================================

const DB_RESTAURANTS = [
  {
    id: 1,
    name: "Cơm Tấm Cali - Nguyễn Huệ",
    rating: 4.5,
    address: "45 Nguyễn Huệ, P. Bến Nghé, Quận 1, TP.HCM",
    time: "06:30 - 22:30",
  },
  {
    id: 2,
    name: "Phở Hùng - Nguyễn Trãi",
    rating: 4.8,
    address: "243 Nguyễn Trãi, P. Nguyễn Cư Trinh, Quận 1, TP.HCM",
    time: "06:00 - 03:00",
  },
  {
    id: 3,
    name: "KOI Thé - Bitexco Tower",
    image:
      "https://images.unsplash.com/photo-1558350315-8aa00e8e4590?auto=format&fit=crop&w=800&q=80",
    rating: 4.6,
    category: "Trà sữa",
    status: "active",
    isOpen: true,
    latitude: 10.771595,
    longitude: 106.704384,
    description: "Trà sữa Đài Loan cao cấp view đẹp",
    address: "Tầng trệt Bitexco, 2 Hải Triều, Quận 1, TP.HCM",
    time: "09:00 - 22:00",
    totalTables: 15,
  },
  {
    id: 4,
    name: "Haidilao Hotpot - Vincom",
    image:
      "https://images.unsplash.com/photo-1549488344-c7052fb51c5b?auto=format&fit=crop&w=800&q=80",
    rating: 5.0,
    category: "Lẩu",
    status: "active",
    isOpen: true,
    latitude: 10.778153,
    longitude: 106.701724,
    description: "Dịch vụ lẩu 5 sao, múa mì đặc sắc",
    address: "Tầng B3, Vincom Center, 72 Lê Thánh Tôn, Quận 1, TP.HCM",
    time: "10:00 - 02:00",
    totalTables: 15,
  },
  {
    id: 5,
    name: "McDonald's Bưu Điện TP",
    image:
      "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=800&q=80",
    rating: 4.3,
    category: "Gà rán",
    status: "active",
    isOpen: true,
    latitude: 10.779836,
    longitude: 106.699765,
    description: "Burger và Gà rán chuẩn Mỹ ngay trung tâm",
    address: "2 Công Xã Paris, P. Bến Nghé, Quận 1, TP.HCM",
    time: "00:00 - 23:59",
    totalTables: 15,
  },
  {
    id: 6,
    name: "Pizza 4P's - Chợ Bến Thành",
    image:
      "https://images.unsplash.com/photo-1574071318508-1cdbab80d002?auto=format&fit=crop&w=800&q=80",
    rating: 4.9,
    category: "Pizza",
    status: "active",
    isOpen: true,
    latitude: 10.771912,
    longitude: 106.697555,
    description: "Pizza nướng củi phong cách Nhật-Ý",
    address: "8 Thủ Khoa Huân, P. Bến Thành, Quận 1, TP.HCM",
    time: "10:00 - 22:30",
    totalTables: 15,
  },
  {
    id: 7,
    name: "Highlands Coffee - Hồ Con Rùa",
    image:
      "https://images.unsplash.com/photo-1578314675249-a6910f80cc4e?auto=format&fit=crop&w=800&q=80",
    rating: 4.2,
    category: "Đồ uống",
    status: "active",
    isOpen: true,
    latitude: 10.782729,
    longitude: 106.695924,
    description: "Cà phê phin sữa đá đậm chất Việt",
    address: "1 Công Trường Quốc Tế, Phường 6, Quận 3, TP.HCM",
    time: "07:00 - 23:00",
    totalTables: 15,
  },
  {
    id: 8,
    name: "Bánh Mì Huỳnh Hoa",
    image:
      "https://images.unsplash.com/photo-1626509653295-802528373684?auto=format&fit=crop&w=800&q=80",
    rating: 4.7,
    category: "Cơm",
    status: "active",
    isOpen: false,
    latitude: 10.7715,
    longitude: 106.6942,
    description: "Bánh mì đắt nhất Sài Gòn, full topping",
    address: "26 Lê Thị Riêng, P. Phạm Ngũ Lão, Quận 1, TP.HCM",
    time: "14:00 - 23:00",
    totalTables: 15,
  },
  {
    id: 9,
    name: "Kichi Kichi - Cao Thắng",
    image:
      "https://images.unsplash.com/photo-1553621042-f6e147245754?auto=format&fit=crop&w=800&q=80",
    rating: 4.4,
    category: "Lẩu",
    status: "active",
    isOpen: true,
    latitude: 10.7686,
    longitude: 106.6815,
    description: "Lẩu băng chuyền tự chọn không giới hạn",
    address: "84 Cao Thắng, Phường 4, Quận 3, TP.HCM",
    time: "10:00 - 22:00",
    totalTables: 15,
  },
  {
    id: 10,
    name: "Phúc Long - Ngô Đức Kế",
    image:
      "https://images.unsplash.com/photo-1544787219-7f47ccb76574?auto=format&fit=crop&w=800&q=80",
    rating: 4.5,
    category: "Đồ uống",
    status: "active",
    isOpen: true,
    latitude: 10.7725,
    longitude: 106.7038,
    description: "Trà vải và trà đào huyền thoại",
    address: "29 Ngô Đức Kế, P. Bến Nghé, Quận 1, TP.HCM",
    time: "08:00 - 22:30",
    totalTables: 15,
  },
  {
    id: 11,
    name: "Texas Chicken - Nguyễn Thái Học",
    image:
      "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?auto=format&fit=crop&w=800&q=80",
    rating: 4.3,
    category: "Gà rán",
    status: "active",
    isOpen: true,
    latitude: 10.7668,
    longitude: 106.6965,
    description: "Gà rán tươi 100%, biscuit mật ong",
    address: "115 Nguyễn Thái Học, P. Cầu Ông Lãnh, Quận 1, TP.HCM",
    time: "10:00 - 22:00",
    totalTables: 15,
  },
  {
    id: 12,
    name: "Manwah Taiwanese Hotpot",
    image:
      "https://images.unsplash.com/photo-1536304993881-ff00228b4db1?auto=format&fit=crop&w=800&q=80",
    rating: 4.8,
    category: "Lẩu",
    status: "active",
    isOpen: true,
    latitude: 10.7765,
    longitude: 106.7001,
    description: "Lẩu Đài Loan hương vị cung đình",
    address: "Tầng 1, 65 Lê Lợi, P. Bến Nghé, Quận 1, TP.HCM",
    time: "10:00 - 22:00",
    totalTables: 15,
  },
  {
    id: 13,
    name: "Gong Cha - Hồ Tùng Mậu",
    image:
      "https://images.unsplash.com/photo-1558855410-3112e474558d?auto=format&fit=crop&w=800&q=80",
    rating: 4.4,
    category: "Trà sữa",
    status: "inactive",
    isOpen: false,
    latitude: 10.7712,
    longitude: 106.7035,
    description: "Trà sữa trân châu hoàng kim nổi tiếng",
    address: "83 Hồ Tùng Mậu, P. Bến Nghé, Quận 1, TP.HCM",
    time: "09:30 - 21:30",
    totalTables: 15,
  },
  {
    id: 14,
    name: "Cơm Niêu Thiên Lý",
    image:
      "https://images.unsplash.com/photo-1563245372-f21724e3856d?auto=format&fit=crop&w=800&q=80",
    rating: 4.5,
    category: "Cơm",
    status: "active",
    isOpen: true,
    latitude: 10.785,
    longitude: 106.695,
    description: "Cơm niêu cháy giòn, món ăn gia đình",
    address: "16 Nguyễn Đình Chiểu, P. Đa Kao, Quận 1, TP.HCM",
    time: "10:00 - 14:30 | 16:00 - 21:30",
    totalTables: 15,
  },
  {
    id: 15,
    name: "Starbucks Reserve - Hàn Thuyên",
    image:
      "https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=800&q=80",
    rating: 4.6,
    category: "Đồ uống",
    status: "active",
    isOpen: true,
    latitude: 10.7792,
    longitude: 106.6985,
    description: "Cà phê cao cấp ngay cạnh Nhà Thờ Đức Bà",
    address: "11-13 Hàn Thuyên, P. Bến Nghé, Quận 1, TP.HCM",
    time: "07:00 - 22:00",
    totalTables: 15,
  },
];

// ================================
// TIỆN ÍCH TIẾNG VIỆT
// ================================

function normalizeVietnamese(str) {
  if (!str) return "";
  return str
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d")
    .replace(/Đ/g, "d");
}

// ================================
// LOGIC CHATBOT
// ================================

function getSmartReply(userMessage) {
  const normalizedMsg = normalizeVietnamese(userMessage);

  // 1. Chào hỏi
  if (
    normalizedMsg.includes("xin chao") ||
    normalizedMsg.includes("hello") ||
    normalizedMsg.includes("hi") ||
    normalizedMsg.includes("chao")
  ) {
    return {
      type: "text",
      content: "Xin chào! 👋 Tôi là trợ lý ảo S2O. Tôi có thể giúp gì cho bạn?",
    };
  }

  // 2. Đặt bàn
  if (
    normalizedMsg.includes("dat ban") ||
    normalizedMsg.includes("book") ||
    normalizedMsg.includes("dat cho")
  ) {
    return {
      type: "text",
      content:
        'Để đặt bàn, vui lòng:\n1. Chọn nhà hàng\n2. Click nút "ĐẶT BÀN"\n3. Điền thông tin\n4. Xác nhận\nHoặc gọi 1900 1234',
    };
  }

  // 3. Hỗ trợ
  if (
    normalizedMsg.includes("ho tro") ||
    normalizedMsg.includes("hotline") ||
    normalizedMsg.includes("so dien thoai") ||
    normalizedMsg.includes("sdt")
  ) {
    return {
      type: "text",
      content:
        "📞 Hotline hỗ trợ: 1900 1234\nEmail: support@s2o.vn\nGiờ làm việc: 8:00 - 22:00",
    };
  }

  // 4. Quán ngon
  if (
    normalizedMsg.includes("quan nao ngon") ||
    normalizedMsg.includes("review") ||
    normalizedMsg.includes("danh gia")
  ) {
    const topRestaurants = DB_RESTAURANTS.filter((r) => r.rating >= 4.5);
    let reply = "🍽️ **Nhà hàng đánh giá cao:**\n\n";
    topRestaurants.forEach((r, i) => {
      reply += `${i + 1}. ${r.name} ⭐ ${r.rating}\n📍 ${r.address}\n\n`;
    });
    return { type: "text", content: reply };
  }

  // 5. Tìm món ăn
  if (
    normalizedMsg.includes("com") ||
    normalizedMsg.includes("pho") ||
    normalizedMsg.includes("banh mi") ||
    normalizedMsg.includes("tra sua")
  ) {
    return {
      type: "text",
      content:
        'Tôi có thể giúp bạn tìm món ăn. Hãy hỏi cụ thể hơn như:\n"Giá cơm tấm bao nhiêu?"\nhoặc\n"Có món phở nào ngon?"',
    };
  }

  // 6. Mặc định
  return {
    type: "text",
    content:
      "Tôi có thể giúp bạn:\n• Tìm nhà hàng\n• Đặt bàn\n• Tư vấn món ăn\n• Hỗ trợ đặt hàng\nHãy hỏi tôi nhé!",
  };
}

// ================================
// GIAO DIỆN ĐƠN GIẢN
// ================================

class SimpleChatbot {
  constructor() {
    this.isOpen = false;
    this.messages = [];
    this.init();
  }

  init() {
    this.createHTML();
    this.bindEvents();
    this.addMessage(
      "Xin chào! Tôi là trợ lý S2O. Tôi có thể giúp gì cho bạn?",
      "bot",
    );
  }

  createHTML() {
    // Tạo container
    const container = document.createElement("div");
    container.id = "s2o-chatbot";
    container.innerHTML = `
      <style>
        /* Chatbot Styles */
        #s2o-chatbot {
          position: fixed;
          bottom: 20px;
          right: 20px;
          z-index: 10000;
          font-family: Arial, sans-serif;
        }
        
        .chatbot-toggle {
          width: 60px;
          height: 60px;
          border-radius: 50%;
          background: #ff4757;
          border: none;
          color: white;
          font-size: 24px;
          cursor: pointer;
          box-shadow: 0 4px 12px rgba(255, 71, 87, 0.4);
          display: flex;
          align-items: center;
          justify-content: center;
          transition: all 0.3s;
        }
        
        .chatbot-toggle:hover {
          transform: scale(1.1);
          background: #ff3838;
        }
        
        .chatbot-window {
          position: absolute;
          bottom: 70px;
          right: 0;
          width: 320px;
          height: 400px;
          background: white;
          border-radius: 12px;
          box-shadow: 0 8px 30px rgba(0,0,0,0.2);
          display: none;
          flex-direction: column;
          overflow: hidden;
        }
        
        .chatbot-window.open {
          display: flex;
        }
        
        .chatbot-header {
          background: #ff4757;
          color: white;
          padding: 15px;
          display: flex;
          align-items: center;
          justify-content: space-between;
        }
        
        .chatbot-title {
          font-weight: bold;
          font-size: 16px;
        }
        
        .chatbot-close {
          background: none;
          border: none;
          color: white;
          font-size: 20px;
          cursor: pointer;
          padding: 0;
        }
        
        .chatbot-messages {
          flex: 1;
          padding: 15px;
          overflow-y: auto;
          background: #f8f9fa;
        }
        
        .message {
          margin-bottom: 10px;
          padding: 10px 15px;
          border-radius: 18px;
          max-width: 85%;
          word-wrap: break-word;
          font-size: 14px;
          line-height: 1.4;
        }
        
        .message-bot {
          background: white;
          align-self: flex-start;
          border-bottom-left-radius: 5px;
        }
        
        .message-user {
          background: #3498db;
          color: white;
          margin-left: auto;
          border-bottom-right-radius: 5px;
        }
        
        .chatbot-input-area {
          display: flex;
          padding: 10px;
          border-top: 1px solid #eee;
          background: white;
        }
        
        .chatbot-input {
          flex: 1;
          padding: 10px 15px;
          border: 1px solid #ddd;
          border-radius: 20px;
          font-size: 14px;
          outline: none;
        }
        
        .chatbot-input:focus {
          border-color: #3498db;
        }
        
        .chatbot-send {
          margin-left: 10px;
          padding: 10px 15px;
          background: #ff4757;
          color: white;
          border: none;
          border-radius: 20px;
          cursor: pointer;
        }
      </style>
      
      <button class="chatbot-toggle">💬</button>
      
      <div class="chatbot-window">
        <div class="chatbot-header">
          <div class="chatbot-title">Trợ lý S2O</div>
          <button class="chatbot-close">×</button>
        </div>
        
        <div class="chatbot-messages" id="chatbot-messages">
          <!-- Messages appear here -->
        </div>
        
        <div class="chatbot-input-area">
          <input type="text" class="chatbot-input" placeholder="Nhập câu hỏi..." id="chatbot-input">
          <button class="chatbot-send" id="chatbot-send">Gửi</button>
        </div>
      </div>
    `;

    document.body.appendChild(container);
  }

  bindEvents() {
    // Toggle button
    document
      .querySelector("#s2o-chatbot .chatbot-toggle")
      .addEventListener("click", () => {
        this.toggleChat();
      });

    // Close button
    document
      .querySelector("#s2o-chatbot .chatbot-close")
      .addEventListener("click", () => {
        this.closeChat();
      });

    // Send button
    document
      .querySelector("#s2o-chatbot .chatbot-send")
      .addEventListener("click", () => {
        this.sendMessage();
      });

    // Enter key
    document
      .querySelector("#s2o-chatbot .chatbot-input")
      .addEventListener("keypress", (e) => {
        if (e.key === "Enter") {
          this.sendMessage();
        }
      });
  }

  toggleChat() {
    const window = document.querySelector("#s2o-chatbot .chatbot-window");
    this.isOpen = !this.isOpen;

    if (this.isOpen) {
      window.classList.add("open");
      document.querySelector("#s2o-chatbot .chatbot-input").focus();
    } else {
      window.classList.remove("open");
    }
  }

  closeChat() {
    this.isOpen = false;
    document
      .querySelector("#s2o-chatbot .chatbot-window")
      .classList.remove("open");
  }

  sendMessage() {
    const input = document.querySelector("#s2o-chatbot .chatbot-input");
    const message = input.value.trim();

    if (!message) return;

    // Thêm tin nhắn user
    this.addMessage(message, "user");
    input.value = "";

    // Xử lý và trả lời
    setTimeout(() => {
      const reply = getSmartReply(message);
      this.addMessage(reply.content, "bot");
    }, 500);
  }

  addMessage(content, sender) {
    const messagesContainer = document.querySelector(
      "#s2o-chatbot .chatbot-messages",
    );

    const messageDiv = document.createElement("div");
    messageDiv.className = `message message-${sender}`;
    messageDiv.textContent = content;

    messagesContainer.appendChild(messageDiv);

    // Scroll xuống cuối
    messagesContainer.scrollTop = messagesContainer.scrollHeight;

    // Lưu vào history
    this.messages.push({ content, sender, time: new Date() });
  }
}

// ================================
// KHỞI TẠO
// ================================

// Chờ trang load xong
if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initChatbot);
} else {
  initChatbot();
}

function initChatbot() {
  console.log("Initializing S2O Chatbot...");

  // Tạo chatbot
  window.s2oChatbot = new SimpleChatbot();

  // Thông báo console
  console.log("✅ S2O Chatbot đã sẵn sàng!");
  console.log("👉 Click vào nút 💬 ở góc dưới bên phải để mở chatbot");

  // Hiển thị thông báo sau 3 giây
  setTimeout(() => {
    console.log(
      '💡 Gợi ý: Hãy hỏi "Xin chào", "Quán nào ngon?", "Cách đặt bàn?"',
    );
  }, 3000);
}
