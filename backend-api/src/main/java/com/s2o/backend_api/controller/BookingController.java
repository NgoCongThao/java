package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.BookingRequest;
import com.s2o.backend_api.entity.Booking;
import com.s2o.backend_api.entity.Restaurant;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.BookingRepository;
import com.s2o.backend_api.repository.RestaurantRepository;
import com.s2o.backend_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.s2o.backend_api.entity.BookingItem;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RestaurantRepository restaurantRepository;

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
            // 1. Kiểm tra nhà hàng
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new RuntimeException("Nhà hàng không tồn tại"));
            
            // 2. Lấy tổng số bàn
            Integer totalTables = restaurant.getTotalTables();
            if (totalTables == null || totalTables <= 0) {
                totalTables = 10; // Mặc định nếu chưa cấu hình
            }
            
            // 3. Parse thời gian
            LocalTime bookingTime = LocalTime.parse(time);
            LocalTime startCheck = bookingTime.minusHours(2);
            LocalTime endCheck = bookingTime.plusHours(2);
            
            // 4. Lấy danh sách bàn đã đặt
            List<Integer> bookedTableNumbers;
            if (startCheck.isBefore(endCheck)) {
                bookedTableNumbers = bookingRepository.findBookedTableNumbers(
                        restaurantId,
                        LocalDate.parse(date),
                        startCheck,
                        endCheck
                );
            } else {
                // Trường hợp qua đêm, không lấy danh sách bàn đã đặt
                bookedTableNumbers = new ArrayList<>();
            }
            
            // 5. Tạo Set để kiểm tra nhanh
            Set<Integer> bookedSet = new HashSet<>(bookedTableNumbers);
            
            // 6. Tạo danh sách trạng thái tất cả các bàn
            List<Map<String, Object>> tableStatusList = new ArrayList<>();
            for (int i = 1; i <= totalTables; i++) {
                Map<String, Object> tableStatus = new HashMap<>();
                tableStatus.put("number", i);
                tableStatus.put("status", bookedSet.contains(i) ? "booked" : "available");
                tableStatusList.add(tableStatus);
            }
            
            return ResponseEntity.ok(tableStatusList);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "success", false,
                        "message", "Lỗi khi lấy trạng thái bàn: " + e.getMessage()
                    ));
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
}