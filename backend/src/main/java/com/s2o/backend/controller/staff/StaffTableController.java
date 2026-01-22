package com.s2o.backend.controller.staff;

import com.s2o.backend.entity.DiningTable;
import com.s2o.backend.repository.DiningTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.s2o.backend.entity.User;
import com.s2o.backend.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;

@RestController
@RequestMapping("/api/staff/tables")
@CrossOrigin(origins = "http://localhost:3000")
public class StaffTableController {

    @Autowired
    private DiningTableRepository tableRepository;
    @Autowired private UserRepository userRepository;
    private Long getCurrentRestaurantId() {
        // Lấy username từ Token
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // Tìm User trong DB
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        // Trả về ID nhà hàng của user đó
        return user.getRestaurant().getId();
    }

    @GetMapping
    public List<DiningTable> getMyTables() {
        // KHÔNG DÙNG 1L NỮA -> Dùng hàm vừa viết
        return tableRepository.findByRestaurantId(getCurrentRestaurantId());
    }

    // --- 2. API MỚI: Đổi trạng thái bàn (Dùng cho nút Đặt Bàn / Hủy Đặt) ---
    @PutMapping("/{id}/status")
    public DiningTable updateTableStatus(@PathVariable Long id, @RequestParam String status) {
        DiningTable table = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại"));

        // Cập nhật trạng thái (EMPTY, RESERVED, OCCUPIED)
        table.setStatus(status);

        return tableRepository.save(table);
    }
}