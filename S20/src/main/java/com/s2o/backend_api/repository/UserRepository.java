package com.s2o.backend_api.repository;

import com.s2o.backend_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Tìm user theo username
    Optional<User> findByUsername(String username);
    
    // Kiểm tra xem username đã tồn tại chưa (để chặn đăng ký trùng)
    Boolean existsByUsername(String username);
}