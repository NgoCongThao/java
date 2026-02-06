package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.OrderRequest;
import com.s2o.backend_api.entity.*;
import com.s2o.backend_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "*")
public class StaffController {

    @Autowired private OrderRepository orderRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private MenuItemRepository menuItemRepository;
    @Autowired private RestaurantRepository restaurantRepository;

    // Helper: Lấy ID quán an toàn hơn
    private Long getCurrentRestaurantId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên: " + username));

        if (user.getRestaurantId() == null) {
            throw new RuntimeException("Tài khoản " + username + " chưa được gán vào Nhà hàng nào!");
        }
        return user.getRestaurantId();
    }

    // ================= 1. QUẢN LÝ ĐẶT BÀN (BOOKING) - ĐÃ SỬA LỖI NGÀY GIỜ =================
    @GetMapping("/bookings")
    public List<Booking> getBookings() {
        return bookingRepository.findByRestaurantId(getCurrentRestaurantId());
    }

    @PutMapping("/bookings/{id}/confirm")
    public ResponseEntity<?> confirmBooking(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new RuntimeException("Đơn đặt bàn không tồn tại"));
        Long resId = getCurrentRestaurantId();

        // 1. Lấy số bàn Staff chọn
        int tableNumber = 0;
        if(body.containsKey("tableNumber")) {
            tableNumber = Integer.parseInt(body.get("tableNumber").toString());
        }

        // 2. KIỂM TRA TRÙNG LỊCH (Logic 3 tiếng)
        // Ghép Ngày + Giờ lại thành LocalDateTime để tính toán
        // Giả sử Booking có trường bookingDate (LocalDate) và bookingTime (LocalTime)
        LocalDateTime startNew = LocalDateTime.of(booking.getBookingDate(), booking.getBookingTime());
        LocalDateTime endNew = startNew.plusHours(3); // Ăn trong 3 tiếng

        // Lấy tất cả đơn đặt bàn ĐÃ CONFIRM của quán
        List<Booking> existingBookings = bookingRepository.findByRestaurantId(resId);

        for (Booking b : existingBookings) {
            // Chỉ check những đơn ĐÃ DUYỆT, CÙNG SỐ BÀN, KHÁC ID ĐƠN HIỆN TẠI
            if ("CONFIRMED".equals(b.getStatus()) &&
                    b.getTableNumber() == tableNumber &&
                    !b.getId().equals(booking.getId())) {

                // Ghép ngày giờ của đơn cũ trong DB
                LocalDateTime startOld = LocalDateTime.of(b.getBookingDate(), b.getBookingTime());
                LocalDateTime endOld = startOld.plusHours(3);

                // Công thức check giao nhau (Overlap): StartA < EndB && EndA > StartB
                if (startNew.isBefore(endOld) && endNew.isAfter(startOld)) {
                    return ResponseEntity.badRequest().body("❌ Bàn " + tableNumber + " đã bị kẹt lịch từ " + b.getBookingTime() + " đến " + endOld.toLocalTime());
                }
            }
        }

        // 3. Nếu không trùng -> Lưu
        booking.setTableNumber(tableNumber);
        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);

        return ResponseEntity.ok(Map.of("message", "✅ Đã xếp bàn " + tableNumber + " thành công!"));
    }

    // ================= 2. QUẢN LÝ ĐƠN HÀNG & SƠ ĐỒ BÀN =================

    @GetMapping("/live-orders")
    public List<Order> getLiveOrders() {
        return orderRepository.findByRestaurantIdAndStatusNotOrderByCreatedAtAsc(getCurrentRestaurantId(), "COMPLETED");
    }

    @PostMapping("/orders/create")
    public ResponseEntity<?> createOrderForGuest(@RequestBody OrderRequest req) {
        Long resId = getCurrentRestaurantId();
        Restaurant res = restaurantRepository.findById(resId).orElseThrow();

        Order order = new Order();
        order.setRestaurantId(resId);
        order.setRestaurantName(res.getName());
        order.setTableNumber(req.getTableNumber());
        order.setNote(req.getNote());
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        if (req.getOrderType() != null) {
            order.setOrderType(req.getOrderType());
        } else {
            order.setOrderType("DINE_IN");
        }

        if (req.getPhone() != null && !req.getPhone().isEmpty()) {
            Optional<User> customer = userRepository.findByUsername(req.getPhone());
            customer.ifPresent(c -> {
                order.setUserId(c.getId());
                order.setCustomerName(c.getFullName());
            });
        }

        order.setTotalPrice(0.0);
        Order savedOrder = orderRepository.save(order);
        return ResponseEntity.ok(savedOrder);
    }

    // ================= 3. THANH TOÁN & TÍCH ĐIỂM =================
    // 2. API THANH TOÁN NÂNG CẤP (Đã chỉnh tỷ lệ & Lưu cột finalPrice)
    @PutMapping("/orders/{id}/pay")
    public ResponseEntity<?> payOrder(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Order order = orderRepository.findById(id).orElseThrow();

        if ("COMPLETED".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body("Đơn này đã thanh toán rồi!");
        }

        // Lấy thông tin từ Body
        String phone = (String) body.get("phone");
        // Fix lỗi parse Integer từ JSON
        int pointsToUse = body.containsKey("pointsToUse") ? Integer.parseInt(body.get("pointsToUse").toString()) : 0;

        User customer = null;

        // --- A. TÌM KHÁCH HÀNG ---
        if (phone != null && !phone.isEmpty()) {
            Optional<User> userOpt = userRepository.findByUsername(phone);
            if (userOpt.isPresent()) {
                customer = userOpt.get();
                order.setUserId(customer.getId());
                order.setCustomerName(customer.getFullName());
            }
        } else if (order.getUserId() != null) {
            customer = userRepository.findById(order.getUserId()).orElse(null);
        }

        // --- B. TÍNH TOÁN TIỀN & ĐIỂM (TỶ LỆ MỚI) ---
        double originalTotal = order.getTotalPrice();
        double discountAmount = 0;

        if (customer != null) {
            int currentPoints = customer.getPoints() == null ? 0 : customer.getPoints();

            // 1. TRỪ ĐIỂM (Quy đổi: 1 điểm = 1.000 VND)
            if (pointsToUse > 0) {
                if (pointsToUse > currentPoints) {
                    return ResponseEntity.badRequest().body("Khách không đủ điểm để trừ!");
                }

                discountAmount = pointsToUse * 1000; // 1 điểm = 1.000đ

                // Trừ điểm trong tài khoản
                currentPoints = currentPoints - pointsToUse;
            }

            // 2. TÍCH ĐIỂM MỚI (Dựa trên GIÁ GỐC)
            // Tỷ lệ: 20.000đ = 1 điểm (Thay vì 100k)
            int pointsEarned = (int) (originalTotal / 20000);
            currentPoints = currentPoints + pointsEarned;

            // Lưu điểm mới vào DB User
            customer.setPoints(currentPoints);
            userRepository.save(customer);
        }

        // --- C. HOÀN TẤT ĐƠN & LƯU TIỀN ---
        order.setStatus("COMPLETED");

        // Tính tiền thực thu
        double finalPrice = originalTotal - discountAmount;
        if (finalPrice < 0) finalPrice = 0; // Không để âm tiền

        // Lưu vào cột mới (finalPrice)
        order.setFinalPrice(finalPrice);

        // Ghi chú thêm để Staff dễ đối soát (Optional)
        if (discountAmount > 0) {
            String note = order.getNote() == null ? "" : order.getNote();
            order.setNote(note + " [Trừ " + pointsToUse + " điểm (-" + (long)discountAmount + "đ)]");
        }

        orderRepository.save(order);

        return ResponseEntity.ok(Map.of(
                "message", "Thanh toán thành công!",
                "pointsEarned", customer != null ? (int)(originalTotal / 20000) : 0,
                "pointsUsed", pointsToUse,
                "originalTotal", originalTotal,
                "finalTotal", finalPrice
        ));
    }

    // ================= 4. ĐƠN ONLINE (GIAO HÀNG) =================
    @GetMapping("/orders/online-pending")
    public List<Order> getOnlinePendingOrders() {
        Long resId = getCurrentRestaurantId();
        return orderRepository.findByRestaurantIdAndStatus(resId, "WAITING_CONFIRM");
    }

    @GetMapping("/customers/lookup")
    public ResponseEntity<?> lookupCustomer(@RequestParam String phone) {
        Optional<User> userOpt = userRepository.findByUsername(phone);
        if (userOpt.isPresent()) {
            User u = userOpt.get();
            return ResponseEntity.ok(Map.of(
                    "found", true,
                    "name", u.getFullName() != null ? u.getFullName() : "Khách hàng",
                    "points", u.getPoints() != null ? u.getPoints() : 0,
                    "userId", u.getId()
            ));
        } else {
            return ResponseEntity.ok(Map.of("found", false));
        }
    }
    @PutMapping("/orders/{id}/approve")
    public ResponseEntity<?> approveOnlineOrder(@PathVariable Long id) {
        Order order = orderRepository.findById(id).orElseThrow();

        if (!"WAITING_CONFIRM".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body("Đơn này không ở trạng thái chờ duyệt!");
        }

        order.setStatus("PENDING");
        orderRepository.save(order);

        return ResponseEntity.ok("Đã duyệt đơn! Bếp bắt đầu nấu.");
    }

    @PutMapping("/orders/{id}/reject")
    public ResponseEntity<?> rejectOnlineOrder(@PathVariable Long id) {
        Order order = orderRepository.findById(id).orElseThrow();

        if ("CANCELLED".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body("Đơn đã hủy rồi!");
        }

        order.setStatus("CANCELLED");

        // --- LOGIC HOÀN ĐIỂM ---
        if (order.getPointsUsed() != null && order.getPointsUsed() > 0 && order.getUserId() != null) {
            User user = userRepository.findById(order.getUserId()).orElse(null);
            if (user != null) {
                int currentPoints = user.getPoints() == null ? 0 : user.getPoints();
                user.setPoints(currentPoints + order.getPointsUsed()); // Cộng lại điểm đã trừ
                userRepository.save(user);
            }
        }
        // ------------------------

        orderRepository.save(order);
        return ResponseEntity.ok("Đã từ chối đơn hàng và hoàn điểm cho khách.");
    }
}