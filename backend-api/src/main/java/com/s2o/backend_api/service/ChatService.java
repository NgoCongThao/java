package com.s2o.backend_api.service;

import com.s2o.backend_api.entity.MenuItem;
import com.s2o.backend_api.entity.Restaurant;
import com.s2o.backend_api.repository.MenuItemRepository;
import com.s2o.backend_api.repository.RestaurantRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private MenuItemRepository menuItemRepository;
    
    @Autowired
    private RestaurantRepository restaurantRepository;

    // Key Groq của bạn
    private final String API_KEY = "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"; 
    private final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    public String chatWithAI(String userMessage, Long restaurantId) {
        String contextData = "";
        String systemRole = "";

        if (restaurantId != null) {
            // === TRƯỜNG HỢP 1: KHÁCH ĐANG Ở TRONG 1 QUÁN CỤ THỂ ===
            try {
                Restaurant res = restaurantRepository.findById(restaurantId).orElse(null);
                List<MenuItem> menu = menuItemRepository.findByRestaurantId(restaurantId);
                
                if (res != null) {
                    // Tạo danh sách món ăn chi tiết hơn
                    String menuText = menu.stream()
                        .map(m -> String.format("- %s: %,.0f đ %s", 
                                m.getName(), 
                                m.getPrice(),
                                (m.getIsAvailable() != null && !m.getIsAvailable() ? "(Hết món)" : "") // Báo hết món nếu có
                        ))
                        .collect(Collectors.joining("\n"));
                    
                    systemRole = "Bạn là nhân viên phục vụ chuyên nghiệp của nhà hàng '" + res.getName() + "'.";
                    
                    // --- NẠP FULL DỮ LIỆU VÀO ĐÂY ---
                    contextData = "Thông tin chi tiết về quán:\n" +
                                  "1. Tên quán: " + res.getName() + "\n" +
                                  "2. Mô tả: " + (res.getDescription() != null ? res.getDescription() : "Không có mô tả") + "\n" +
                                  "3. Địa chỉ: " + (res.getAddress() != null ? res.getAddress() : "Đang cập nhật") + "\n" +
                                  "4. Giờ mở cửa: " + (res.getTime() != null ? res.getTime() : "Chưa cập nhật") + "\n" +
                                  "5. Số điện thoại: " + (res.getPhone() != null ? res.getPhone() : "Không có") + "\n" +
                                  "6. Đánh giá: " + (res.getRating() != null ? res.getRating() : "Chưa có") + "/5 sao\n" +
                                  "7. Trạng thái hiện tại: " + (Boolean.TRUE.equals(res.getIsOpen()) ? "Đang mở cửa" : "Đang đóng cửa") + "\n" +
                                  "\n--- MENU CỦA QUÁN ---\n" + menuText;
                }
            } catch (Exception ex) {
                System.out.println("Lỗi DB: " + ex.getMessage());
            }
        } else {
            // === TRƯỜNG HỢP 2: KHÁCH ĐANG Ở TRANG CHỦ (TÌM QUÁN) ===
            try {
                List<Restaurant> allRestaurants = restaurantRepository.findAll();
                
                // Nạp nhiều thông tin hơn cho danh sách tổng
                String allResText = allRestaurants.stream()
                    .map(r -> String.format("- %s (Loại: %s)\n  + Đánh giá: %s/5 sao\n  + Địa chỉ: %s\n  + Giờ mở: %s", 
                            r.getName(), 
                            r.getCategory(), 
                            (r.getRating() != null ? r.getRating() : "N/A"),
                            r.getAddress(),
                            (r.getTime() != null ? r.getTime() : "N/A")))
                    .collect(Collectors.joining("\n\n"));

                systemRole = "Bạn là Trợ lý ảo thông minh của hệ thống đặt bàn S2O.";
                contextData = "Dưới đây là danh sách tất cả nhà hàng trong hệ thống:\n" + allResText + 
                              "\n\nHãy giúp khách hàng tìm quán phù hợp (VD: tìm quán ăn ngon, tìm quán mở khuya, tìm quán cafe...).";
            } catch (Exception ex) {
                System.out.println("Lỗi DB All: " + ex.getMessage());
            }
        }

        // Tạo Prompt gửi cho AI
        String systemContent = systemRole + " Hãy trả lời ngắn gọn, thân thiện, dùng tiếng Việt và Emoji.\n" +
                               "Tuyệt đối chỉ tư vấn dựa trên thông tin dữ liệu dưới đây:\n" + contextData;

        return callGroqAPI(systemContent, userMessage);
    }

    private String callGroqAPI(String systemPrompt, String userMessage) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + API_KEY);

            JSONObject messageSystem = new JSONObject();
            messageSystem.put("role", "system");
            messageSystem.put("content", systemPrompt);

            JSONObject messageUser = new JSONObject();
            messageUser.put("role", "user");
            // Xử lý null an toàn
            messageUser.put("content", userMessage != null ? userMessage : "");

            JSONArray messages = new JSONArray();
            messages.put(messageSystem);
            messages.put(messageUser);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "llama-3.3-70b-versatile"); 
            requestBody.put("max_tokens", 1024);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);

            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            
            String response = restTemplate.postForObject(GROQ_URL, request, String.class);

            JSONObject jsonResponse = new JSONObject(response);
            return jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

        } catch (HttpClientErrorException e) {
            System.err.println("🔴 LỖI GROQ: " + e.getResponseBodyAsString());
            return "Lỗi kết nối AI: " + e.getStatusCode();
        } catch (Exception e) {
            e.printStackTrace();
            return "Xin lỗi, hệ thống đang bận.";
        }
    }
}