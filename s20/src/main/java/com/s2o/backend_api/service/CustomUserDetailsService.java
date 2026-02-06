package com.s2o.backend_api.service;

import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.UserRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Tìm user trong DB
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // 2. Xử lý Role: Nếu null thì gán USER, KHÔNG ĐƯỢC THÊM "ROLE_" VÀO TRƯỚC
        String role = (user.getRole() == null || user.getRole().trim().isEmpty()) ? "USER" : user.getRole();
        GrantedAuthority authority = new SimpleGrantedAuthority(role); 

        // 3. Trả về CustomUserDetails (để chứa thêm restaurantId)
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRestaurantId(),
                Collections.singletonList(authority)
        );
    }

    // Class nội bộ để lưu thêm ID và RestaurantId
    @Getter
    public static class CustomUserDetails extends org.springframework.security.core.userdetails.User {
        private final Long id;
        private final Long restaurantId;

        public CustomUserDetails(Long id, String username, String password, Long restaurantId, java.util.Collection<? extends GrantedAuthority> authorities) {
            super(username, password, authorities);
            this.id = id;
            this.restaurantId = restaurantId;
        }
    }
}