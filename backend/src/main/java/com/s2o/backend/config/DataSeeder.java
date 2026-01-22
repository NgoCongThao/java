package com.s2o.backend.config;

import com.s2o.backend.entity.*;
import com.s2o.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Arrays;
import org.springframework.security.crypto.password.PasswordEncoder;
//@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired private RestaurantRepository restaurantRepository;
    @Autowired private DiningTableRepository tableRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Override
    public void run(String... args) throws Exception {
        // Kiểm tra nếu đã có dữ liệu nhà hàng thì không nạp lại
        if (restaurantRepository.count() > 0) return;

        System.out.println("---- KHỞI TẠO DỮ LIỆU SAAS (MULTI-TENANCY) ----");

        // ==========================================
        // 🏠 1. TẠO NHÀ HÀNG 1: S2O RESTAURANT (ID=1)
        // ==========================================
        Restaurant r1 = new Restaurant();
        r1.setName("S2O Restaurant");
        r1.setAddress("Q1, TP.HCM");
        r1.setPhone("0909123456");
        r1.setActive(true);
        restaurantRepository.save(r1);

        // --- Bàn của R1 (Sửa AVAILABLE -> EMPTY để khớp với Frontend) ---
        DiningTable t1 = new DiningTable(); t1.setName("Bàn 01"); t1.setStatus("EMPTY"); t1.setRestaurant(r1);
        DiningTable t2 = new DiningTable(); t2.setName("Bàn 02"); t2.setStatus("OCCUPIED"); t2.setRestaurant(r1);
        DiningTable t3 = new DiningTable(); t3.setName("Bàn VIP"); t3.setStatus("RESERVED"); t3.setRestaurant(r1);
        tableRepository.saveAll(Arrays.asList(t1, t2, t3));

        // --- Menu của R1 ---
        Category c1 = new Category(); c1.setName("Món Chính"); c1.setRestaurant(r1); categoryRepository.save(c1);
        Category c2 = new Category(); c2.setName("Đồ Uống"); c2.setRestaurant(r1); categoryRepository.save(c2);

        // Phở Bò
        Product p1 = new Product();
        p1.setName("Phở Bò"); p1.setPrice(50000.0); p1.setRestaurant(r1); p1.setCategory(c1); p1.setAvailable(true);
        p1.setImage("https://i.pinimg.com/564x/0c/33/08/0c33083e911242940263303d8df589e4.jpg");
        productRepository.save(p1);

        // Cơm Rang
        Product p2 = new Product();
        p2.setName("Cơm Rang Dưa Bò"); p2.setPrice(45000.0); p2.setRestaurant(r1); p2.setCategory(c1); p2.setAvailable(true);
        p2.setImage("https://static.vinwonders.com/production/com-rang-dua-bo-ha-noi-1.jpg");
        productRepository.save(p2);

        // Trà Đá
        Product p3 = new Product();
        p3.setName("Trà Đá"); p3.setPrice(5000.0); p3.setRestaurant(r1); p3.setCategory(c2); p3.setAvailable(true);
        p3.setImage("https://cdn.tgdd.vn/Files/2019/11/26/1222409/tra-da-duong-pho-va-nhung-moi-nguy-hai-tiem-an-cho-suc-khoe-202201051513238692.jpg");
        productRepository.save(p3);

        // Coca Cola
        Product p4 = new Product();
        p4.setName("Coca Cola"); p4.setPrice(15000.0); p4.setRestaurant(r1); p4.setCategory(c2); p4.setAvailable(true);
        p4.setImage("https://images.heb.com/is/image/HEBGrocery/000145353");
        productRepository.save(p4);


        // ==========================================
        // ☕ 2. TẠO NHÀ HÀNG 2: HIGHLANDS COFFEE (ID=2)
        // ==========================================
        Restaurant r2 = new Restaurant();
        r2.setName("Highlands Coffee");
        r2.setAddress("Q3, TP.HCM");
        r2.setPhone("0909888888");
        r2.setActive(true);
        restaurantRepository.save(r2);

        // --- Bàn của R2 (Bàn riêng, không liên quan R1) ---
        DiningTable h1 = new DiningTable(); h1.setName("Bàn H1 (Sofa)"); h1.setStatus("EMPTY"); h1.setRestaurant(r2);
        DiningTable h2 = new DiningTable(); h2.setName("Bàn H2 (Góc)"); h2.setStatus("EMPTY"); h2.setRestaurant(r2);
        DiningTable h3 = new DiningTable(); h3.setName("Bàn H3 (Ngoài trời)"); h3.setStatus("OCCUPIED"); h3.setRestaurant(r2);
        tableRepository.saveAll(Arrays.asList(h1, h2, h3));

        // --- Menu của R2 ---
        Category c3 = new Category(); c3.setName("Cà Phê"); c3.setRestaurant(r2); categoryRepository.save(c3);
        Category c4 = new Category(); c4.setName("Trà"); c4.setRestaurant(r2); categoryRepository.save(c4);

        // Bạc Xỉu (Chỉ R2 mới thấy)
        Product pHigh1 = new Product();
        pHigh1.setName("Bạc Xỉu Đá"); pHigh1.setPrice(29000.0); pHigh1.setRestaurant(r2); pHigh1.setCategory(c3); pHigh1.setAvailable(true);
        pHigh1.setImage("https://www.highlandscoffee.com.vn/vnt_upload/product/04_2018/PHIN-SUA-DA.png");
        productRepository.save(pHigh1);

        // Trà Sen Vàng (Chỉ R2 mới thấy)
        Product pHigh2 = new Product();
        pHigh2.setName("Trà Sen Vàng"); pHigh2.setPrice(45000.0); pHigh2.setRestaurant(r2); pHigh2.setCategory(c4); pHigh2.setAvailable(true);
        pHigh2.setImage("https://www.highlandscoffee.com.vn/vnt_upload/product/03_2018/tra-sen-vang.png");
        productRepository.save(pHigh2);


        // ==========================================
        // 👤 3. TẠO USER (DÙNG ENUM ROLE)
        // ==========================================

        // --- NHÂN SỰ S2O (R1) ---
        User u1 = new User();
        u1.setUsername("staff1");
        u1.setPassword(passwordEncoder.encode("123"));// Password không mã hóa để test nhanh
        u1.setRole(Role.STAFF);
        u1.setFullName("Nhân viên S2O");
        u1.setRestaurant(r1); // Gắn vào R1
        userRepository.save(u1);

        User owner1 = new User();
        owner1.setUsername("manage1");
        owner1.setPassword(passwordEncoder.encode("123"));
        owner1.setRole(Role.OWNER);
        owner1.setFullName("Chủ quán S2O");
        owner1.setRestaurant(r1); // Gắn vào R1
        userRepository.save(owner1);

        // --- NHÂN SỰ HIGHLANDS (R2) ---
        User u2 = new User();
        u2.setUsername("staff2");
        u2.setPassword(passwordEncoder.encode("123"));
        u2.setRole(Role.STAFF);
        u2.setFullName("Nhân viên Highlands");
        u2.setRestaurant(r2); // Gắn vào R2
        userRepository.save(u2);

        System.out.println("✅ NẠP DỮ LIỆU THÀNH CÔNG CHO 2 NHÀ HÀNG!");
        System.out.println("👉 Staff 1 (S2O): staff1 / 123");
        System.out.println("👉 Staff 2 (Highlands): staff2 / 123");
    }
}