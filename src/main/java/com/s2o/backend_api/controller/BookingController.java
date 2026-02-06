package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.BookingRequest;
import com.s2o.backend_api.entity.*;
import com.s2o.backend_api.repository.BookingRepository;
import com.s2o.backend_api.repository.OrderRepository;
import com.s2o.backend_api.repository.RestaurantRepository;
import com.s2o.backend_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping({"/api/bookings", "/api/staff/bookings"})
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private OrderRepository orderRepo;
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    @PostMapping("/{id}/check-in")
    // Thêm @RequestBody để nhận danh sách món đã chỉnh sửa
    public ResponseEntity<?> checkInBooking(@PathVariable Long id, @RequestBody(required = false) CheckInRequest req) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if ("COMPLETED".equals(booking.getStatus())) {
            return ResponseEntity.badRequest().body("Booking này đã check-in rồi!");
        }

        // 1. Tạo Order cơ bản
        Order newOrder = new Order();
        newOrder.setCustomerName(booking.getCustomerName());
        newOrder.setCustomerPhone(booking.getPhone());
        newOrder.setTableNumber(booking.getTableNumber());
        newOrder.setOrderType("DINE_IN");
        newOrder.setStatus("PENDING");
        newOrder.setCreatedAt(LocalDateTime.now());
        if (booking.getUser() != null) newOrder.setUserId(booking.getUser().getId());
        if (booking.getRestaurant() != null) {
            newOrder.setRestaurantId(booking.getRestaurant().getId());
            // 🔥 FIX: Copy thêm tên nhà hàng
            newOrder.setRestaurantName(booking.getRestaurant().getName());
        }
        newOrder.setNote("Booking #" + booking.getId() + ". " + booking.getNote());

        // 2. XỬ LÝ DANH SÁCH MÓN (LOGIC MỚI)
        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0;

        // Nếu Frontend có gửi danh sách món đã sửa -> Dùng danh sách đó
        if (req != null && req.items != null) {
            for (var reqItem : req.items) {
                OrderItem oItem = new OrderItem();
                oItem.setItemName(reqItem.getName());
                oItem.setPrice(reqItem.getPrice());
                oItem.setQuantity(reqItem.getQty());
                oItem.setStatus("PENDING");
                oItem.setOrder(newOrder);
                orderItems.add(oItem);
                total += (reqItem.getPrice() * reqItem.getQty());
            }
        }
        // Nếu không gửi gì -> Dùng danh sách gốc trong Booking (Fallback)
        else if (booking.getItems() != null) {
            for (BookingItem bItem : booking.getItems()) {
                OrderItem oItem = new OrderItem();
                oItem.setItemName(bItem.getItemName());
                oItem.setPrice(bItem.getPrice());
                oItem.setQuantity(bItem.getQuantity());
                oItem.setStatus("PENDING");
                oItem.setOrder(newOrder);
                orderItems.add(oItem);
                total += (bItem.getPrice() * bItem.getQuantity());
            }
        }

        newOrder.setItems(orderItems);
        newOrder.setTotalPrice(total);
        newOrder.setFinalPrice(total);

        orderRepo.save(newOrder);

        booking.setStatus("COMPLETED");
        bookingRepository.save(booking);

        return ResponseEntity.ok(Map.of("message", "Check-in thành công!", "orderId", newOrder.getId()));
    }
    // API tạo booking
    @PostMapping("/create")
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        try {
            System.out.println("--- BẮT ĐẦU ĐẶT BÀN ---");
            System.out.println("User ID: " + request.getUserId());
            System.out.println("Nhà hàng ID: " + request.getRestaurantId());
            System.out.println("Giờ đặt: " + request.getTime());

            // 1. Kiểm tra User
            User user = userRepository.findById(request.getUserId())
                    .orElse(null);
            if (user == null) {
                System.out.println("Lỗi: User không tồn tại");
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Tài khoản không hợp lệ. Vui lòng đăng xuất và đăng nhập lại."
                ));
            }

            // 2. Kiểm tra Nhà hàng
            Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                    .orElse(null);
            if (restaurant == null) {
                System.out.println("Lỗi: Nhà hàng không tồn tại");
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Nhà hàng không tồn tại."
                ));
            }

            // 3. Logic kiểm tra bàn trống
            LocalTime startCheck = request.getTime().minusHours(2);
            LocalTime endCheck = request.getTime().plusHours(2);

            long currentBookings = 0;
            if (startCheck.isBefore(endCheck)) {
                currentBookings = bookingRepository.countBookedTables(
                        restaurant.getId(),
                        request.getDate(),
                        startCheck,
                        endCheck
                );
            } else {
                System.out.println("Cảnh báo: Đặt bàn qua đêm, tạm bỏ qua check trùng.");
            }

            int maxCapacity = restaurant.getTotalTables() != null ? restaurant.getTotalTables() : 10;

            System.out.println("Đã đặt: " + currentBookings + " / " + maxCapacity);

            if (currentBookings >= maxCapacity) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                            "success", false,
                            "message", "Nhà hàng đã hết bàn vào khung giờ " + request.getTime() + ". Vui lòng chọn giờ khác!"
                        ));
            }

            // 4. Lưu Booking
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setRestaurant(restaurant);
            booking.setCustomerName(request.getCustomerName());
            booking.setPhone(request.getPhone());
            booking.setBookingDate(request.getDate());
            booking.setBookingTime(request.getTime());
            booking.setGuestCount(request.getGuests());
            booking.setNote(request.getNote());
            
            if (request.getTableNumber() != null && request.getTableNumber() > 0) {
                booking.setTableNumber(request.getTableNumber());
            }
            booking.setStatus("PENDING");

            // --- LOGIC LƯU MÓN ĂN KÈM THEO ---
            if (request.getItems() != null && !request.getItems().isEmpty()) {
                List<BookingItem> bookingItems = new ArrayList<>();
                for (BookingRequest.BookingItemRequest itemReq : request.getItems()) {
                    BookingItem item = new BookingItem();
                    item.setItemName(itemReq.getName());
                    item.setQuantity(itemReq.getQty());
                    item.setPrice(itemReq.getPrice());
                    item.setBooking(booking);
                    bookingItems.add(item);
                }
                booking.setItems(bookingItems);
            }

            bookingRepository.save(booking);
            System.out.println("--- ĐẶT BÀN KÈM MÓN THÀNH CÔNG ---");

            // TRẢ VỀ JSON OBJECT THAY VÌ CHUỖI
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đặt bàn thành công",
                "id", booking.getId(),
                "booking", booking
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Lỗi hệ thống: " + e.getMessage()
            ));
        }
    }

    // API: Nhân viên xếp bàn cho khách (Check-in)
    @PutMapping("/{id}/assign-table")
    public ResponseEntity<?> assignTable(@PathVariable Long id, @RequestParam Integer tableNumber) {
        return bookingRepository.findById(id)
                .map(booking -> {
                    booking.setTableNumber(tableNumber);
                    booking.setStatus("CONFIRMED");
                    bookingRepository.save(booking);
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Đã xếp bàn số " + tableNumber + " cho khách " + booking.getCustomerName()
                    ));
                })
                .orElse(ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Booking không tồn tại"
                )));
    }

    // API: Lấy lịch sử đặt bàn của khách hàng
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserBookings(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    // THÊM API MỚI: Lấy trạng thái bàn theo ngày giờ

    @GetMapping("/table-status")
    public ResponseEntity<?> getTableStatus(
            @RequestParam Long restaurantId,
            @RequestParam String date,
            @RequestParam String time) {

        try {
            // 1. Chuẩn bị dữ liệu
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new RuntimeException("Nhà hàng không tồn tại"));
            int totalTables = (restaurant.getTotalTables() != null && restaurant.getTotalTables() > 0)
                    ? restaurant.getTotalTables() : 10;

            LocalDate checkDate = LocalDate.parse(date);
            LocalTime checkTime = LocalTime.parse(time);

            // Set chứa các bàn BẬN (Dùng Set để tự động loại bỏ trùng lặp)
            Set<Integer> busyTables = new HashSet<>();

            // =================================================================
            // 🛑 PHẦN 1: CHECK BOOKING (Áp dụng cho CẢ hôm nay và tương lai)
            // =================================================================
            // Logic: Tìm các bàn bị Booking giữ chỗ trong khung giờ +/- 2 tiếng
            // (Lưu ý: Repo đã loại bỏ các đơn COMPLETED nên rất an toàn)
            LocalTime startCheck = checkTime.minusHours(2);
            LocalTime endCheck = checkTime.plusHours(2);

            if (startCheck.isBefore(endCheck)) {
                List<Integer> bookedTables = bookingRepository.findBookedTableNumbers(
                        restaurantId, checkDate, startCheck, endCheck
                );
                busyTables.addAll(bookedTables);
            }

            // =================================================================
            // 🛑 PHẦN 2: CHECK ORDER (CHỈ Áp dụng cho HÔM NAY)
            // =================================================================
            // Logic: Nếu đang check ngày hôm nay, phải xem có ai đang ngồi ăn thật không
            // (Bao gồm cả khách Booking đã đến check-in và khách vãng lai)
            if (checkDate.equals(LocalDate.now())) {
                List<Integer> diningTables = orderRepo.findBusyTableNumbers(restaurantId);
                busyTables.addAll(diningTables);
            }

            // =================================================================
            // 3. TỔNG HỢP KẾT QUẢ
            // =================================================================
            List<Map<String, Object>> result = new ArrayList<>();
            for (int i = 1; i <= totalTables; i++) {
                Map<String, Object> map = new HashMap<>();
                map.put("number", i);

                if (busyTables.contains(i)) {
                    map.put("status", "booked"); // Hoặc "busy"
                } else {
                    map.put("status", "available");
                }
                result.add(map);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }
    // --- API MỚI CHO BẾP: Cập nhật trạng thái Booking ---
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        if (newStatus == null || newStatus.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Trạng thái không hợp lệ"));
        }

        return bookingRepository.findById(id)
                .map(booking -> {
                    booking.setStatus(newStatus);
                    bookingRepository.save(booking);
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Cập nhật trạng thái thành công: " + newStatus
                    ));
                })
                .orElse(ResponseEntity.badRequest().body(Map.of("success", false, "message", "Booking không tồn tại")));
    }
    public static class CheckInRequest {
        public List<BookingRequest.BookingItemRequest> items; // Tận dụng DTO món ăn cũ
    }
}