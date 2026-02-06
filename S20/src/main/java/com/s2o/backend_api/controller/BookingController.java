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
<<<<<<< HEAD
import java.time.LocalDateTime;
=======
import java.time.LocalDateTime; // Import thêm cái này
>>>>>>> edfd887424fb24a79f0b1de399c73811c4707d16
import java.time.LocalTime;
import java.time.format.DateTimeFormatter; // Import thêm cái này để parse giờ mở cửa
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
<<<<<<< HEAD
    // API 1: TẠO BOOKING (ĐÃ FIX LỖI GIỜ MỞ CỬA & TRÙNG BÀN QUA ĐÊM)
=======
    // API 1: TẠO BOOKING (ĐÃ THÊM LOGIC CHECK GIỜ MỞ CỬA & QUÁ KHỨ)
>>>>>>> edfd887424fb24a79f0b1de399c73811c4707d16
    // ========================================================================
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('USER', 'ROLE_USER')")
    @PostMapping("/create")
    public synchronized ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        try {
            System.out.println("--- BẮT ĐẦU ĐẶT BÀN ---");

            // 1. Xử lý thời gian từ Request
            LocalTime bookingTime;
            try {
                bookingTime = LocalTime.parse(request.getTime().toString()); 
            } catch (Exception e) {
                 return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Định dạng giờ không hợp lệ"));
<<<<<<< HEAD
            }

            // 2. Check User & Restaurant
            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tài khoản lỗi."));

            Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId()).orElse(null);
            if (restaurant == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Nhà hàng lỗi."));

            // -----------------------------------------------------------
            // [FIX 1] KIỂM TRA GIỜ MỞ CỬA (HỖ TRỢ QUA ĐÊM 06:30 - 00:30)
            // -----------------------------------------------------------
            String timeRange = restaurant.getTime(); // "HH:mm - HH:mm"
            if (timeRange != null && timeRange.contains("-")) {
                try {
                    String[] parts = timeRange.split("-");
                    LocalTime openTime = LocalTime.parse(parts[0].trim()); 
                    LocalTime closeTime = LocalTime.parse(parts[1].trim());

                    boolean isOpen;
                    if (closeTime.isBefore(openTime)) { 
                        // Trường hợp qua đêm (VD: 06:30 - 00:30)
                        // Hợp lệ nếu: (Giờ đặt >= 06:30) HOẶC (Giờ đặt <= 00:30)
                        isOpen = !bookingTime.isBefore(openTime) || !bookingTime.isAfter(closeTime);
                    } else {
                        // Trường hợp trong ngày (VD: 08:00 - 22:00)
                        isOpen = !bookingTime.isBefore(openTime) && !bookingTime.isAfter(closeTime);
                    }

                    if (!isOpen) {
=======
            }

            // 2. Check User & Restaurant
            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tài khoản lỗi."));

            Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId()).orElse(null);
            if (restaurant == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Nhà hàng lỗi."));

            // -----------------------------------------------------------
            // LOGIC MỚI THÊM: KIỂM TRA THỜI GIAN HỢP LỆ
            // -----------------------------------------------------------

            // A. Kiểm tra Quá khứ (Ngày + Giờ)
            LocalDateTime selectedDateTime = LocalDateTime.of(request.getDate(), bookingTime);
            if (selectedDateTime.isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false, 
                    "message", "Lỗi: Thời gian đặt bàn đã trôi qua. Vui lòng chọn thời gian khác!"
                ));
            }

            // B. Kiểm tra Giờ mở cửa của Nhà hàng (Ví dụ string: "08:00 - 22:00")
            String timeRange = restaurant.getTime(); // Giả sử DB lưu chuỗi "HH:mm - HH:mm"
            if (timeRange != null && timeRange.contains("-")) {
                try {
                    String[] parts = timeRange.split("-");
                    // Xử lý cắt chuỗi và trim khoảng trắng
                    LocalTime openTime = LocalTime.parse(parts[0].trim()); 
                    LocalTime closeTime = LocalTime.parse(parts[1].trim());

                    // Logic: Giờ đặt phải >= Giờ mở VÀ Giờ đặt <= Giờ đóng
                    // (Có thể trừ đi 1 tiếng trước khi đóng cửa để kịp ăn nếu muốn)
                    if (bookingTime.isBefore(openTime) || bookingTime.isAfter(closeTime)) {
>>>>>>> edfd887424fb24a79f0b1de399c73811c4707d16
                        return ResponseEntity.badRequest().body(Map.of(
                            "success", false, 
                            "message", "Nhà hàng chưa mở cửa vào giờ này. Giờ hoạt động: " + timeRange
                        ));
                    }
                } catch (Exception e) {
<<<<<<< HEAD
                    System.out.println("Lỗi parse giờ mở cửa: " + e.getMessage());
                }
            }

            // -----------------------------------------------------------
            // [FIX 2] KIỂM TRA TRÙNG BÀN (LOGIC CHUYỂN ĐỔI SANG PHÚT)
            // -----------------------------------------------------------
=======
                    System.out.println("Lỗi parse giờ mở cửa nhà hàng: " + e.getMessage());
                    // Nếu lỗi format giờ trong DB thì tạm thời cho qua hoặc log lại
                }
            }
            // -----------------------------------------------------------

            // 3. KIỂM TRA TRÙNG BÀN (Logic cũ giữ nguyên)
>>>>>>> edfd887424fb24a79f0b1de399c73811c4707d16
            if (request.getTableNumber() != null && request.getTableNumber() > 0) {
                List<Booking> bookings = bookingRepository.findBookingsForConflictCheck(
                        restaurant.getId(), 
                        request.getTableNumber(), 
                        request.getDate()
                );

                if (isTimeOverlapping(bookings, bookingTime)) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Rất tiếc! Bàn số " + request.getTableNumber() + " đã bị trùng giờ với khách khác."
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

<<<<<<< HEAD
            // Lưu món và tính tiền
            if (request.getItems() != null) {
                List<BookingItem> items = new ArrayList<>();
                double grandTotal = 0; 
                for (BookingRequest.BookingItemRequest i : request.getItems()) {
                    BookingItem item = new BookingItem();
                    item.setItemName(i.getName());
                    item.setQuantity(i.getQty());
                    item.setPrice(i.getPrice());
                    item.setBooking(booking);
                    items.add(item);
                    grandTotal += (i.getPrice() != null ? i.getPrice() : 0) * i.getQty();
                }
                booking.setItems(items);
                booking.setTotalPrice(grandTotal); 
            }
=======
            // Huy đã sửa đoạn này để tính tổng tiền
            if (request.getItems() != null) {
    List<BookingItem> items = new ArrayList<>();
    double grandTotal = 0; // Biến tạm để tính tổng

    for (BookingRequest.BookingItemRequest i : request.getItems()) {
        BookingItem item = new BookingItem();
        item.setItemName(i.getName());
        item.setQuantity(i.getQty());
        item.setPrice(i.getPrice());
        item.setBooking(booking);
        items.add(item);
        
        // Cộng dồn: Giá * Số lượng
        grandTotal += (i.getPrice() != null ? i.getPrice() : 0) * i.getQty();
    }
    booking.setItems(items);
    booking.setTotalPrice(grandTotal); // Lưu tổng tiền vào bảng cha
}
>>>>>>> edfd887424fb24a79f0b1de399c73811c4707d16

            bookingRepository.save(booking);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đặt bàn thành công", "id", booking.getId()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

<<<<<<< HEAD
    // ========================================================================
    // API 2: LẤY TRẠNG THÁI BÀN (DÙNG CHUNG LOGIC VỚI CREATE ĐỂ ĐỒNG BỘ)
=======
    // ... (Giữ nguyên phần còn lại của file: API Table Status, Helper methods...)
    
    // ========================================================================
    // API 2: LẤY TRẠNG THÁI BÀN (SỬA LẠI ĐỂ ĐỒNG BỘ VỚI LOGIC ĐẶT BÀN)
>>>>>>> edfd887424fb24a79f0b1de399c73811c4707d16
    // ========================================================================
    @GetMapping("/table-status")
    public ResponseEntity<?> getTableStatus(@RequestParam Long restaurantId, @RequestParam String date, @RequestParam String time) {
        try {
            Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(() -> new RuntimeException("Nhà hàng không tồn tại"));
            Integer totalTables = restaurant.getTotalTables() != null ? restaurant.getTotalTables() : 10;
            
<<<<<<< HEAD
            LocalTime viewTime = LocalTime.parse(time);

            // Lấy TOÀN BỘ đơn đặt trong ngày (tableNumber = null)
=======
            // Thời gian khách đang xem trên giao diện
            LocalTime viewTime = LocalTime.parse(time);

            // Lấy TOÀN BỘ đơn đặt trong ngày của nhà hàng (truyền null vào tableNumber)
>>>>>>> edfd887424fb24a79f0b1de399c73811c4707d16
            List<Booking> allBookingsToday = bookingRepository.findBookingsForConflictCheck(
                restaurantId, 
                null, 
                LocalDate.parse(date)
            );

            List<Map<String, Object>> tableStatusList = new ArrayList<>();

<<<<<<< HEAD
=======
            // Duyệt từng bàn từ 1 đến Max
>>>>>>> edfd887424fb24a79f0b1de399c73811c4707d16
            for (int i = 1; i <= totalTables; i++) {
                int currentTableNum = i;
                
                // Lọc ra các đơn thuộc về bàn số i
                List<Booking> bookingsForThisTable = allBookingsToday.stream()
                        .filter(b -> b.getTableNumber() != null && b.getTableNumber() == currentTableNum)
                        .collect(Collectors.toList());

<<<<<<< HEAD
                // Kiểm tra trùng bằng logic chuẩn
=======
                // Kiểm tra xem giờ khách đang xem có trùng với đơn nào không
>>>>>>> edfd887424fb24a79f0b1de399c73811c4707d16
                boolean isBooked = isTimeOverlapping(bookingsForThisTable, viewTime);

                Map<String, Object> tableStatus = new HashMap<>();
                tableStatus.put("number", i);
<<<<<<< HEAD
                tableStatus.put("status", isBooked ? "booked" : "available"); // Trả về booked để hiện đỏ
=======
                // QUAN TRỌNG: Nếu trùng -> trả về "booked". Frontend sẽ tự tô màu đỏ.
                tableStatus.put("status", isBooked ? "booked" : "available"); 
>>>>>>> edfd887424fb24a79f0b1de399c73811c4707d16
                tableStatusList.add(tableStatus);
            }
            
            return ResponseEntity.ok(tableStatusList);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ========================================================================
<<<<<<< HEAD
    // [LOGIC CỐT LÕI] HÀM KIỂM TRA TRÙNG GIỜ (ĐÃ FIX LỖI QUA ĐÊM)
    // ========================================================================
    private boolean isTimeOverlapping(List<Booking> bookings, LocalTime checkTime) {
        // Quy đổi thời gian ra PHÚT (từ 00:00) để so sánh tuyến tính
        // Ví dụ: 01:00 sáng hôm sau sẽ là 25:00 (1500 phút)
        int checkStartMinutes = checkTime.getHour() * 60 + checkTime.getMinute();
        int checkEndMinutes = checkStartMinutes + 120; // Ăn 2 tiếng (120 phút)

        for (Booking b : bookings) {
            LocalTime bTime = b.getBookingTime();
            if (bTime == null) continue;

            int bStartMinutes = bTime.getHour() * 60 + bTime.getMinute();
            int bEndMinutes = bStartMinutes + 120; // Ăn 2 tiếng

            // XỬ LÝ QUA ĐÊM (Nếu giờ kết thúc nhỏ hơn giờ bắt đầu -> Cộng thêm 24h)
            // Mẹo: Vì chúng ta đang xét trong cùng 1 ngày (theo bookingDate), 
            // nên nếu checkTime là 23:00, end sẽ là 01:00 hôm sau (tức là phút thứ 1500)
            // Logic so sánh số nguyên: [Start1, End1] có giao nhau với [Start2, End2] không?
            
            // Nếu đơn đã có là 23:00 -> 01:00 (Hôm sau)
            // bStart = 23*60 = 1380. bEnd = 1380 + 120 = 1500.
            
            // Nếu khách đặt 00:30 (Hôm nay - thực tế là sáng sớm)
            // checkStart = 30. checkEnd = 150.
            // -> Không trùng (30-150 không chạm 1380-1500). -> ĐÚNG.
            
            // Nếu khách đặt 23:30 (Hôm nay)
            // checkStart = 1410. checkEnd = 1530.
            // -> 1410 nằm giữa 1380 và 1500 -> TRÙNG. -> ĐÚNG.

            // Công thức kiểm tra giao nhau giữa 2 đoạn [A, B] và [C, D]:
            // Giao nhau khi: Max(A, C) < Min(B, D)
            
            int maxStart = Math.max(checkStartMinutes, bStartMinutes);
            int minEnd = Math.min(checkEndMinutes, bEndMinutes);

            if (maxStart < minEnd) {
                return true; // Có trùng
            }
        }
        return false;
    }

    // ... (Giữ nguyên các API assignTable, getUserBookings, updateBookingStatus bên dưới) ...
=======
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
>>>>>>> edfd887424fb24a79f0b1de399c73811c4707d16
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