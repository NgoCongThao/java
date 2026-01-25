const express = require('express');
const mysql = require('mysql2');
const cors = require('cors');
const jwt = require('jsonwebtoken');

const app = express();
app.use(cors());
app.use(express.json());

// Kết nối MySQL
const db = mysql.createPool({
    host: 'localhost',
    user: 'root',
    password: '18072005', // Mặc định của XAMPP là trống
    database: 'restaurant_db'
});

// Middleware xác thực token
const authenticateToken = (req, res, next) => {
    const token = req.headers['authorization']?.split(' ')[1];
    if (!token) return res.status(401).json({ message: 'Access denied' });

    jwt.verify(token, 'SECRET_KEY', (err, user) => {
        if (err) return res.status(403).json({ message: 'Invalid token' });
        req.user = user;
        next();
    });
};

// API Đăng nhập
app.post('/api/login', (req, res) => {
    const { username, password } = req.body;
    const query = "SELECT * FROM users WHERE username = ? AND password = ?";
    
    db.query(query, [username, password], (err, results) => {
        if (err) return res.status(500).json({ message: 'Database error' });
        if (results.length > 0) {
            const user = results[0];
            const token = jwt.sign({ id: user.id, role: user.role }, 'SECRET_KEY');
            res.json({ success: true, token, role: user.role, name: user.full_name });
        } else {
            res.status(401).json({ message: "Sai tài khoản hoặc mật khẩu" });
        }
    });
});

// API Menu
app.get('/api/menu', authenticateToken, (req, res) => {
    db.query('SELECT * FROM menu', (err, results) => {
        if (err) return res.status(500).json({ message: 'Database error' });
        res.json(results);
    });
});

app.post('/api/menu', authenticateToken, (req, res) => {
    const { name, price, description, image } = req.body;
    db.query('INSERT INTO menu (name, price, description, image) VALUES (?, ?, ?, ?)', [name, price, description, image], (err, result) => {
        if (err) return res.status(500).json({ message: 'Database error' });
        res.json({ id: result.insertId, name, price, description, image });
    });
});

app.put('/api/menu/:id', authenticateToken, (req, res) => {
    const { id } = req.params;
    const { name, price, description, image } = req.body;
    db.query('UPDATE menu SET name = ?, price = ?, description = ?, image = ? WHERE id = ?', [name, price, description, image, id], (err) => {
        if (err) return res.status(500).json({ message: 'Database error' });
        res.json({ message: 'Updated' });
    });
});

app.delete('/api/menu/:id', authenticateToken, (req, res) => {
    const { id } = req.params;
    db.query('DELETE FROM menu WHERE id = ?', [id], (err) => {
        if (err) return res.status(500).json({ message: 'Database error' });
        res.json({ message: 'Deleted' });
    });
});

// API Staff
app.get('/api/staff', authenticateToken, (req, res) => {
    db.query('SELECT id, username, full_name, role FROM users WHERE role != "manager"', (err, results) => {
        if (err) return res.status(500).json({ message: 'Database error' });
        res.json(results);
    });
});

app.post('/api/staff', authenticateToken, (req, res) => {
    const { username, password, full_name, role } = req.body;
    db.query('INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)', [username, password, full_name, role], (err, result) => {
        if (err) return res.status(500).json({ message: 'Database error' });
        res.json({ id: result.insertId, username, full_name, role });
    });
});

app.put('/api/staff/:id', authenticateToken, (req, res) => {
    const { id } = req.params;
    const { username, full_name, role } = req.body;
    db.query('UPDATE users SET username = ?, full_name = ?, role = ? WHERE id = ?', [username, full_name, role, id], (err) => {
        if (err) return res.status(500).json({ message: 'Database error' });
        res.json({ message: 'Updated' });
    });
});

app.delete('/api/staff/:id', authenticateToken, (req, res) => {
    const { id } = req.params;
    db.query('DELETE FROM users WHERE id = ?', [id], (err) => {
        if (err) return res.status(500).json({ message: 'Database error' });
        res.json({ message: 'Deleted' });
    });
});

// API Bookings
app.get('/api/bookings', authenticateToken, (req, res) => {
    db.query('SELECT * FROM bookings', (err, results) => {
        if (err) return res.status(500).json({ message: 'Database error' });
        res.json(results);
    });
});

app.post('/api/bookings', authenticateToken, (req, res) => {
    const { customer_name, phone, date, time, num_guests } = req.body;
    db.query(
        'INSERT INTO bookings (customer_name, phone, booking_date, booking_time, num_guests) VALUES (?, ?, ?, ?, ?)',
        [customer_name, phone, date, time, num_guests],
        (err, result) => {
            if (err) return res.status(500).json({ message: 'Database error' });
            res.json({ id: result.insertId, customer_name, phone, date, time, num_guests });
        }
    );
});

app.put('/api/bookings/:id', authenticateToken, (req, res) => {
    const { id } = req.params;
    const { customer_name, phone, date, time, num_guests } = req.body;
    db.query('UPDATE bookings SET customer_name = ?, phone = ?, date = ?, time = ?, num_guests = ? WHERE id = ?', [customer_name, phone, date, time, num_guests, id], (err) => {
        if (err) return res.status(500).json({ message: 'Database error' });
        res.json({ message: 'Updated' });
    });
});

app.delete('/api/bookings/:id', authenticateToken, (req, res) => {
    const { id } = req.params;
    db.query('DELETE FROM bookings WHERE id = ?', [id], (err) => {
        if (err) return res.status(500).json({ message: 'Database error' });
        res.json({ message: 'Deleted' });
    });
});

// API Revenue
app.get('/api/revenue', authenticateToken, (req, res) => {
    const { start_date, end_date } = req.query;

    const query = `
        SELECT SUM(total_amount) AS total
        FROM orders
        WHERE order_date BETWEEN ? AND ?
    `;

    db.query(query, [start_date, end_date], (err, results) => {
        if (err) {
            console.error('❌ Revenue SQL error:', err);
            return res.status(500).json(err);
        }
        res.json({ total: results[0]?.total || 0 });
    });
});

app.listen(5000, () => console.log("Server chạy tại port 5000"));