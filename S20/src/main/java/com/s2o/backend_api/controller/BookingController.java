package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.BookingRequest;
import com.s2o.backend_api.entity.Booking;
import com.s2o.backend_api.entity.BookingItem;
import com.s2o.backend_api.entity.Restaurant;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.BookingRepository;
import com.s2o.backend_api.repository.RestaurantRepository;
import com.s2o.backend_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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

    // ========================================================================
    // API 1: TẠO BOOKING (SỬ DỤNG JAVA LOGIC ĐỂ CHẶN TRÙNG)
    // ========================================================================
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('USER', 'ROLE_USER')")
    @PostMapping("/create")
    public synchronized ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        try {
            System.out.println("--- BẮT ĐẦU ĐẶT BÀN ---");

            // 1. Xử lý thời gian
            LocalTime bookingTime;
            try {
                bookingTime = LocalTime.parse(request.getTime().toString()); 
            } catch (Exception e) {
                 return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Định dạng giờ không hợp lệ"));
            }

            // 2. Check User & Restaurant
            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tài khoản lỗi."));

            Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId()).orElse(null);
            if (restaurant == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Nhà hàng lỗi."));

            // 3. KIỂM TRA TRÙNG BÀN (Logic Java chính xác 100%)
            if (request.getTableNumber() != null && request.getTableNumber() > 0) {
                // Lấy danh sách đơn của bàn này
                List<Booking> bookings = bookingRepository.findBookingsForConflictCheck(
                        restaurant.getId(), 
                        request.getTableNumber(), 
                        request.getDate()
                );

                if (isTimeOverlapping(bookings, bookingTime)) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Rất tiếc! Bàn số " + request.getTableNumber() + " đã bị trùng giờ với khách khác. Vui lòng chọn bàn khác!"
                    ));
                }
            }

            // 4. Lưu Booking
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setRestaurant(restaurant);
            booking.setCustomerName(request.getCustomerName());
            booking.setPhone(request.getPhone());
            booking.setBookingDate(request.getDate());
            booking.setBookingTime(bookingTime);
            booking.setGuestCount(request.getGuests());
            booking.setNote(request.getNote());
            if (request.getTableNumber() != null) booking.setTableNumber(request.getTableNumber());
            booking.setStatus("PENDING");

            if (request.getItems() != null) {
                List<BookingItem> items = new ArrayList<>();
                for (BookingRequest.BookingItemRequest i : request.getItems()) {
                    BookingItem item = new BookingItem();
                    item.setItemName(i.getName());
                    item.setQuantity(i.getQty());
                    item.setPrice(i.getPrice());
                    item.setBooking(booking);
                    items.add(item);
                }
                booking.setItems(items);
            }

            bookingRepository.save(booking);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đặt bàn thành công", "id", booking.getId()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    // ========================================================================
    // API 2: LẤY TRẠNG THÁI BÀN (SỬA LẠI ĐỂ ĐỒNG BỘ VỚI LOGIC ĐẶT BÀN)
    // ========================================================================
    @GetMapping("/table-status")
    public ResponseEntity<?> getTableStatus(@RequestParam Long restaurantId, @RequestParam String date, @RequestParam String time) {
        try {
            Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(() -> new RuntimeException("Nhà hàng không tồn tại"));
            Integer totalTables = restaurant.getTotalTables() != null ? restaurant.getTotalTables() : 10;
            
            // Thời gian khách đang xem trên giao diện
            LocalTime viewTime = LocalTime.parse(time);

            // Lấy TOÀN BỘ đơn đặt trong ngày của nhà hàng (truyền null vào tableNumber)
            List<Booking> allBookingsToday = bookingRepository.findBookingsForConflictCheck(
                restaurantId, 
                null, 
                LocalDate.parse(date)
            );

            List<Map<String, Object>> tableStatusList = new ArrayList<>();

            // Duyệt từng bàn từ 1 đến Max
            for (int i = 1; i <= totalTables; i++) {
                int currentTableNum = i;
                
                // Lọc ra các đơn thuộc về bàn số i
                List<Booking> bookingsForThisTable = allBookingsToday.stream()
                        .filter(b -> b.getTableNumber() != null && b.getTableNumber() == currentTableNum)
                        .collect(Collectors.toList());

                // Kiểm tra xem giờ khách đang xem có trùng với đơn nào không
                boolean isBooked = isTimeOverlapping(bookingsForThisTable, viewTime);

                Map<String, Object> tableStatus = new HashMap<>();
                tableStatus.put("number", i);
                // QUAN TRỌNG: Nếu trùng -> trả về "booked". Frontend sẽ tự tô màu đỏ.
                tableStatus.put("status", isBooked ? "booked" : "available"); 
                tableStatusList.add(tableStatus);
            }
            
            return ResponseEntity.ok(tableStatusList);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ========================================================================
    // HÀM PHỤ TRỢ: LOGIC KIỂM TRA TRÙNG GIỜ (DÙNG CHUNG CHO CẢ 2 API)
    // ========================================================================
    private boolean isTimeOverlapping(List<Booking> bookings, LocalTime checkTime) {
        LocalTime newStart = checkTime;
        LocalTime newEnd = checkTime.plusHours(2); // Quy ước mỗi slot ăn 2 tiếng

        for (Booking b : bookings) {
            LocalTime existingStart = b.getBookingTime();
            if (existingStart == null) continue;
            LocalTime existingEnd = existingStart.plusHours(2);

            // Logic kiểm tra giao nhau:
            // Không trùng khi: (Mới kết thúc <= Cũ bắt đầu) HOẶC (Mới bắt đầu >= Cũ kết thúc)
            // Ngược lại là trùng.
            boolean noOverlap = newEnd.isBefore(existingStart) || newEnd.equals(existingStart) || 
                                newStart.isAfter(existingEnd) || newStart.equals(existingEnd);
            
            if (!noOverlap) {
                return true; // Có trùng -> Bàn đã bị đặt
            }
        }
        return false; // Không trùng -> Bàn trống
    }

    // ========================================================================
    // CÁC API KHÁC (GIỮ NGUYÊN)
    // ========================================================================
    @PutMapping("/{id}/assign-table")
    public ResponseEntity<?> assignTable(@PathVariable Long id, @RequestParam Integer tableNumber) {
        return bookingRepository.findById(id).map(b -> {
            b.setTableNumber(tableNumber);
            b.setStatus("CONFIRMED");
            bookingRepository.save(b);
            return ResponseEntity.ok(Map.of("success", true));
        }).orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserBookings(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String s = body.get("status");
        return bookingRepository.findById(id).map(b -> {
            b.setStatus(s);
            bookingRepository.save(b);
            return ResponseEntity.ok(Map.of("success", true));
        }).orElse(ResponseEntity.badRequest().build());
    }
}