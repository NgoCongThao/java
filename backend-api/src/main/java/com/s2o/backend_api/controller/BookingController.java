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
import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime;

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
                System.out.println("Lỗi: User không tồn tại (Do ID cũ?)");
                return ResponseEntity.badRequest().body("Tài khoản không hợp lệ. Vui lòng đăng xuất và đăng nhập lại.");
            }

            // 2. Kiểm tra Nhà hàng
            Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                    .orElse(null);
            if (restaurant == null) {
                System.out.println("Lỗi: Nhà hàng không tồn tại");
                return ResponseEntity.badRequest().body("Nhà hàng không tồn tại.");
            }

            // 3. Logic kiểm tra bàn trống
            LocalTime startCheck = request.getTime().minusHours(2);
            LocalTime endCheck = request.getTime().plusHours(2);

            // FIX LỖI TIME: Nếu qua đêm (Ví dụ 23h + 2h = 1h sáng), logic BETWEEN sẽ lỗi
            // Tạm thời để đơn giản: Nếu start > end (qua đêm), ta bỏ qua check hoặc check kiểu khác.
            // Ở đây ta chỉ check nếu trong cùng 1 ngày
            long currentBookings = 0;
            if (startCheck.isBefore(endCheck)) {
                currentBookings = bookingRepository.countBookedTables(
                        restaurant.getId(),
                        request.getDate(),
                        startCheck,
                        endCheck
                );
            } else {
                // Trường hợp qua đêm (ít gặp ở quán ăn thường), tạm tính là 0 để không lỗi SQL
                System.out.println("Cảnh báo: Đặt bàn qua đêm, tạm bỏ qua check trùng.");
            }

            int maxCapacity = restaurant.getTotalTables() != null ? restaurant.getTotalTables() : 10;

            System.out.println("Đã đặt: " + currentBookings + " / " + maxCapacity);

            if (currentBookings >= maxCapacity) {
                return ResponseEntity.badRequest()
                        .body("Nhà hàng đã hết bàn vào khung giờ " + request.getTime() + ". Vui lòng chọn giờ khác!");
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
            // Nếu khách quét QR tại bàn và đặt, lưu luôn số bàn
            if (request.getTableNumber() != null && request.getTableNumber() > 0) {
                booking.setTableNumber(request.getTableNumber());
                // Nếu đã có bàn cụ thể, có thể set trạng thái là CONFIRMED luôn (tùy logic quán)
                // booking.setStatus("CONFIRMED"); 
            }
            booking.setStatus("PENDING");

            // --- LOGIC LƯU MÓN ĂN KÈM THEO (MỚI) ---
            if (request.getItems() != null && !request.getItems().isEmpty()) {
                List<BookingItem> bookingItems = new ArrayList<>();
                for (BookingRequest.BookingItemRequest itemReq : request.getItems()) {
                    BookingItem item = new BookingItem();
                    item.setItemName(itemReq.getName());
                    item.setQuantity(itemReq.getQty());
                    item.setPrice(itemReq.getPrice());
                    item.setBooking(booking); // Gán item này thuộc về booking đang tạo
                    bookingItems.add(item);
                }
                booking.setItems(bookingItems); // Hibernate sẽ tự động lưu item nhờ CascadeType.ALL
            }
            // ---------------------------------------

            bookingRepository.save(booking);
            System.out.println("--- ĐẶT BÀN KÈM MÓN THÀNH CÔNG ---");

            return ResponseEntity.ok("Đặt bàn thành công! Mã đơn: " + booking.getId());

        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra màn hình đen
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
    // API: Nhân viên xếp bàn cho khách (Check-in)
    @PutMapping("/{id}/assign-table")
    public ResponseEntity<?> assignTable(@PathVariable Long id, @RequestParam Integer tableNumber) {
        return bookingRepository.findById(id)
                .map(booking -> {
                    booking.setTableNumber(tableNumber);
                    booking.setStatus("CONFIRMED"); // Đổi trạng thái thành đã đến/xác nhận
                    bookingRepository.save(booking);
                    return ResponseEntity.ok("Đã xếp bàn số " + tableNumber + " cho khách " + booking.getCustomerName());
                })
                .orElse(ResponseEntity.badRequest().body("Booking không tồn tại"));
    }
    // API: Lấy lịch sử đặt bàn của khách hàng
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserBookings(@PathVariable Long userId) {
        // Cần đảm bảo BookingRepository đã có hàm này:
        // List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
        return ResponseEntity.ok(bookingRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }
}