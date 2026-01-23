-- Tạo database và user mặc định cho Restaurant Admin
CREATE DATABASE IF NOT EXISTS restaurant_db;
USE restaurant_db;

-- Tạo bảng users
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('manager', 'chef', 'waiter') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng menu
CREATE TABLE IF NOT EXISTS menu (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(50),
    available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng bookings
CREATE TABLE IF NOT EXISTS bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    booking_date DATE NOT NULL,
    booking_time TIME NOT NULL,
    num_guests INT NOT NULL,
    special_requests TEXT,
    status ENUM('pending', 'confirmed', 'cancelled') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng orders (cho báo cáo doanh thu)
CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT,
    total_amount DECIMAL(10,2) NOT NULL,
    order_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

-- Thêm user admin mặc định
-- Username: admin
-- Password: admin123
INSERT IGNORE INTO users (username, password, full_name, role) VALUES
('admin', 'admin123', 'Administrator', 'manager');

-- Thêm một số menu mẫu
INSERT IGNORE INTO menu (name, description, price, category) VALUES
('Phở Bò', 'Phở bò truyền thống với thịt bò tươi', 45000, 'Món chính'),
('Cơm Tấm', 'Cơm tấm sườn bì chả', 35000, 'Món chính'),
('Gỏi Cuốn', 'Gỏi cuốn tôm thịt', 25000, 'Khai vị'),
('Trà Đá', 'Trà đá pha sẵn', 15000, 'Đồ uống'),
('Cà Phê Sữa Đá', 'Cà phê sữa đá Việt Nam', 20000, 'Đồ uống');