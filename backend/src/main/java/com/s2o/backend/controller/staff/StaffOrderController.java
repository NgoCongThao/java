package com.s2o.backend.controller.staff;

import com.s2o.backend.dto.OrderRequest;
import com.s2o.backend.entity.*;
import com.s2o.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.s2o.backend.repository.UserRepository;
import com.s2o.backend.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.Optional; // Nhớ import Optional

@RestController
@RequestMapping("/api/staff/orders")
@CrossOrigin(origins = "http://localhost:3000")
public class StaffOrderController {

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private DiningTableRepository tableRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    // API: Gửi đơn xuống bếp (Logic GỘP ĐƠN MỚI)
    @PostMapping
    @Transactional
    public Order createOrder(@RequestBody OrderRequest request) {

        // 1. Kiểm tra xem bàn này đang có đơn nào chưa thanh toán không?
        // (Logic: Tìm đơn của bàn này mà trạng thái KHÔNG PHẢI là PAID hoặc CANCELLED)
        Optional<Order> existingOrder = orderRepository.findByTableIdAndStatusNotAndStatusNot(
                request.getTableId(),
                OrderStatus.PAID,
                OrderStatus.CANCELLED
        );

        Order order;

        if (existingOrder.isPresent()) {
            // === TRƯỜNG HỢP 1: BÀN ĐANG CÓ KHÁCH -> DÙNG LẠI ĐƠN CŨ ===
            order = existingOrder.get();

            // Nếu đơn cũ đang ở trạng thái đã xong (READY) hoặc đã lên món (DELIVERED)
            // mà khách gọi thêm món mới -> Đổi về COOKING để bếp chú ý
            if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.READY) {
                order.setStatus(OrderStatus.COOKING);
            }
        } else {
            // === TRƯỜNG HỢP 2: BÀN TRỐNG -> TẠO ĐƠN MỚI TINH ===
            DiningTable table = tableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new RuntimeException("Bàn không tồn tại!"));

            order = new Order();
            order.setTable(table);
            order.setRestaurant(table.getRestaurant());
            order.setStatus(OrderStatus.PENDING);
            order.setTotalAmount(0.0); // Khởi tạo bằng 0

            // Đổi trạng thái bàn thành "Có khách"
            table.setStatus("OCCUPIED");
            tableRepository.save(table);

            // Lưu đơn mới để có ID
            order = orderRepository.save(order);
        }

        // 2. Xử lý danh sách món mới gọi (Thêm vào đơn)
        double additionalAmount = 0;

        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Món không tồn tại ID: " + itemReq.getProductId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order); // Gắn vào đơn (cũ hoặc mới đều được)
            orderItem.setProduct(product);
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setPrice(product.getPrice());

            // --- QUAN TRỌNG: Món mới thêm vào luôn là PENDING (Chờ) ---
            // Để phân biệt với các món cũ đã ra rồi
            orderItem.setStatus(OrderStatus.PENDING);

            // Cộng dồn tiền phát sinh
            additionalAmount += (product.getPrice() * itemReq.getQuantity());

            // Lưu món vào DB
            orderItemRepository.save(orderItem);
        }

        // 3. Cập nhật lại tổng tiền (Tiền cũ + Tiền mới)
        double currentTotal = order.getTotalAmount() == null ? 0 : order.getTotalAmount();
        order.setTotalAmount(currentTotal + additionalAmount);

        return orderRepository.save(order);
    }
    // --- API MỚI 1: Cập nhật trạng thái từng món (Click để đổi màu) ---
    @PutMapping("/items/{itemId}/status")
    public OrderItem updateItemStatus(@PathVariable Long itemId, @RequestParam OrderStatus newStatus) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Món không tồn tại"));
        item.setStatus(newStatus);
        return orderItemRepository.save(item);
    }

    // --- API MỚI 2: Hủy món (Xóa món khỏi bill & Trừ lại tiền) ---
    @DeleteMapping("/items/{itemId}")
    @Transactional
    public Order deleteItem(@PathVariable Long itemId) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Món không tồn tại"));

        Order order = item.getOrder();

        // 1. Trừ tiền món này ra khỏi tổng Bill
        double itemAmount = item.getPrice() * item.getQuantity();
        double newTotal = order.getTotalAmount() - itemAmount;
        if (newTotal < 0) newTotal = 0; // Đề phòng âm tiền
        order.setTotalAmount(newTotal);

        // 2. Xóa món khỏi Database
        orderItemRepository.delete(item);

        // 3. Lưu lại Order với giá tiền mới
        return orderRepository.save(order);
    }
    private Long getCurrentRestaurantId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        return user.getRestaurant().getId();
    }

    // API: Lấy danh sách đơn hàng
    @GetMapping
    public List<Order> getOrders(@RequestParam(required = false) String status) {
        // Fix cứng ID = 1L  <-- XÓA DÒNG CŨ
        // Thay bằng dòng mới:
        return orderRepository.findByRestaurantId(getCurrentRestaurantId());
    }
    // API: Cập nhật trạng thái đơn
    @PutMapping("/{id}/status")
    @Transactional
    public Order updateStatus(@PathVariable Long id, @RequestParam OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn không tồn tại"));

        order.setStatus(newStatus);
        // 2. LOGIC MỚI: Nếu Thanh Toán Xong (PAID) hoặc Khách Hủy (CANCELLED) -> Trả bàn về Trống
        if (newStatus == OrderStatus.PAID || newStatus == OrderStatus.CANCELLED) {
            DiningTable table = order.getTable();
            if (table != null) {
                table.setStatus("EMPTY"); // Trả về màu Xanh
                tableRepository.save(table);
            }
        }
        return orderRepository.save(order);

    }
}