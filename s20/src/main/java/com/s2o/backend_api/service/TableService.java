package com.s2o.backend_api.service;

import com.s2o.backend_api.dto.TableStatusDTO;
import com.s2o.backend_api.entity.Booking;
import com.s2o.backend_api.entity.Order;
import com.s2o.backend_api.repository.BookingRepository;
import com.s2o.backend_api.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
@Service
public class TableService {
    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private OrderRepository orderRepo;

    // Giả định tổng số bàn của quán là 20
    private final int TOTAL_TABLES = 20;
    // Giả định mỗi lượt khách ăn khoảng 2 tiếng
    private final int EATING_DURATION_HOURS = 2;

    public List<TableStatusDTO> getTableStatusAt(LocalDate date, LocalTime time) {
        List<TableStatusDTO> tableStatuses = new ArrayList<>();

        // 1. Khởi tạo tất cả bàn là TRỐNG (FREE)
        for (int i = 1; i <= TOTAL_TABLES; i++) {
            tableStatuses.add(new TableStatusDTO(i, "FREE", null));
        }

        // 2. CHECK BOOKING (Kiểm tra trùng lịch đặt bàn)
        List<Booking> bookings = bookingRepo.findConfirmedBookingsByDate(date);

        // Khung giờ khách muốn đặt: [reqStart, reqEnd]
        LocalTime reqStart = time;
        LocalTime reqEnd = time.plusHours(EATING_DURATION_HOURS);

        for (Booking b : bookings) {
            // Khung giờ của booking đã có: [bookStart, bookEnd]
            LocalTime bookStart = b.getBookingTime(); // Giả sử lưu dạng LocalTime
            LocalTime bookEnd = bookStart.plusHours(EATING_DURATION_HOURS);

            // Kiểm tra giao nhau (Overlap Logic): (StartA < EndB) && (StartB < EndA)
            if (reqStart.isBefore(bookEnd) && bookStart.isBefore(reqEnd)) {
                int tableIndex = b.getTableNumber() - 1;
                if (tableIndex >= 0 && tableIndex < TOTAL_TABLES) {
                    TableStatusDTO dto = tableStatuses.get(tableIndex);
                    dto.setStatus("BUSY");
                    dto.setReason("Khách đặt lúc " + bookStart);
                }
            }
        }

        // 3. CHECK LIVE ORDERS (Chỉ kiểm tra nếu ngày chọn là HÔM NAY)
        if (date.equals(LocalDate.now())) {
            List<Order> activeOrders = orderRepo.findActiveOrders();
            for (Order o : activeOrders) {
                int tableIndex = o.getTableNumber() - 1;
                if (tableIndex >= 0 && tableIndex < TOTAL_TABLES) {
                    TableStatusDTO dto = tableStatuses.get(tableIndex);
                    // Nếu bàn đang có khách ăn, chắc chắn là BẬN
                    dto.setStatus("BUSY");
                    dto.setReason("Đang phục vụ khách");
                }
            }
        }

        return tableStatuses;
    }
}
