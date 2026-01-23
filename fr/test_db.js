const mysql = require('mysql2');

// Test database connection
const db = mysql.createConnection({
    host: 'localhost',
    user: 'root',
    password: '', // Mặc định của XAMPP
    database: 'restaurant_db'
});

db.connect((err) => {
    if (err) {
        console.error('❌ Lỗi kết nối database:', err.message);
        return;
    }
    console.log('✅ Kết nối database thành công');

    // Kiểm tra user admin
    db.query('SELECT * FROM users WHERE username = "admin"', (err, results) => {
        if (err) {
            console.error('❌ Lỗi query:', err.message);
            return;
        }

        if (results.length > 0) {
            console.log('✅ User admin tồn tại:', results[0]);
        } else {
            console.log('❌ User admin không tồn tại');
            console.log('🔧 Tạo user admin...');

            // Tạo user admin
            db.query('INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)',
                ['admin', 'admin123', 'Administrator', 'manager'], (err, result) => {
                if (err) {
                    console.error('❌ Lỗi tạo user:', err.message);
                } else {
                    console.log('✅ Đã tạo user admin thành công');
                }
                db.end();
            });
        }
    });
});