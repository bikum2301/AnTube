const express = require('express');
const mysql = require('mysql2');
const nodemailer = require('nodemailer');
const bodyParser = require('body-parser');
const cors = require('cors');
const bcrypt = require('bcrypt'); // Thêm bcrypt để băm mật khẩu
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 2201;

// Middleware
app.use(cors());
app.use(bodyParser.json());

// Kết nối MySQL
const db = mysql.createConnection({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
});

db.connect((err) => {
    if (err) {
        console.error('Lỗi kết nối MySQL:', err);
    } else {
        console.log('Đã kết nối MySQL');
    }
});

// Cấu hình Nodemailer
const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS,
    },
});

// API Đăng ký
app.post('/api/register', async (req, res) => {
    const { email, password } = req.body; // Chỉ nhận email và password từ RegisterRequest

    // Kiểm tra xem các trường bắt buộc có được gửi lên không
    if (!email || !password) {
        return res.status(400).json({ message: 'Vui lòng nhập email và mật khẩu' });
    }

    const verification_token = Math.floor(100000 + Math.random() * 900000).toString(); // Tạo verification_token (OTP) 6 chữ số
    const passwordHash = await bcrypt.hash(password, 10); // Băm mật khẩu

    // Kiểm tra xem email đã tồn tại chưa
    const checkEmailSql = 'SELECT * FROM users WHERE email = ?';
    db.query(checkEmailSql, [email], (err, emailResults) => {
        if (err) {
            return res.status(500).json({ message: 'Đăng ký thất bại', error: err.message });
        }

        if (emailResults.length > 0) {
            return res.status(400).json({ message: 'Email đã tồn tại' });
        }


        // Nếu email chưa tồn tại, tiếp tục đăng ký
        const sql = 'INSERT INTO users (email, password_hash, email_verified, verification_token) VALUES (?, ?, ?, ?)'; // Sử dụng password_hash, email_verified, verification_token
        db.query(sql, [email, passwordHash, 0, verification_token], (err, result) => { // 0 tương đương false cho email_verified
            if (err) {
                return res.status(500).json({ message: 'Đăng ký thất bại', error: err.message });
            }

            // Gửi OTP qua email
            const mailOptions = {
                from: process.env.EMAIL_USER,
                to: email,
                subject: 'Xác thực OTP',
                text: `Mã OTP của bạn là: ${verification_token}`, // Gửi verification_token
            };

            transporter.sendMail(mailOptions, (error, info) => {
                if (error) {
                    return res.status(500).json({ message: 'Gửi OTP thất bại' });
                }
                res.status(200).json({ message: 'OTP đã được gửi qua email' });
            });
        });

    });
});

// API Xác thực OTP
app.post('/api/verify-otp', async (req, res) => {
    const { email, otp } = req.body;

    const sql = 'SELECT * FROM users WHERE email = ? AND verification_token = ?'; // Tìm theo verification_token
    db.query(sql, [email, otp], (err, results) => {
        if (err) {
            return res.status(500).json({ message: 'Xác thực thất bại', error: err.message });
        }

        if (results.length > 0) {
            const updateSql = 'UPDATE users SET email_verified = 1, verification_token = NULL WHERE email = ?'; // Cập nhật email_verified = 1 (true), xóa verification_token
            db.query(updateSql, [email], (err, result) => {
                if (err) {
                    return res.status(500).json({ message: 'Xác thực thất bại', error: err.message });
                }
                const user = results[0];
                res.status(200).json({
                    message: 'Xác thực thành công',
                    user: { // Chỉ trả về email
                        email: user.email,
                    },
                });
            });
        } else {
            res.status(400).json({ message: 'OTP không hợp lệ' });
        }
    });
});

// API Đăng nhập
app.post('/api/login', async (req, res) => {
    const { email, password } = req.body;

    const sql = 'SELECT * FROM users WHERE email = ?'; // Tìm user theo email

    db.query(sql, [email], async (err, results) => { // Thêm async để dùng await
        if (err) {
            return res.status(500).json({ message: 'Đăng nhập thất bại', error: err.message });
        }

        if (results.length > 0) {
            const user = results[0];
            const passwordMatch = await bcrypt.compare(password, user.password_hash); // So sánh mật khẩu đã băm
            if (user.email_verified && passwordMatch) { // Kiểm tra email_verified và passwordMatch
                res.status(200).json({
                    message: 'Đăng nhập thành công',
                    user: { // Chỉ trả về email
                        email: user.email,
                    },
                });
            } else {
                if (!user.email_verified) {
                    return res.status(400).json({ message: 'Tài khoản chưa được xác thực email' });
                } else {
                    return res.status(400).json({ message: 'Email hoặc mật khẩu không đúng' }); // Password không khớp
                }
            }
        } else {
            res.status(400).json({ message: 'Email hoặc mật khẩu không đúng' }); // Email không tồn tại
        }
    });
});

// API Quên mật khẩu
app.post('/api/forgot-password', async (req, res) => {
    const { email } = req.body;
    const verification_token = Math.floor(100000 + Math.random() * 900000).toString(); // Tạo verification_token (OTP) 6 chữ số

    const sql = 'UPDATE users SET verification_token = ? WHERE email = ?'; // Cập nhật verification_token
    db.query(sql, [verification_token, email], (err, result) => {
        if (err) {
            return res.status(500).json({ message: 'Quên mật khẩu thất bại', error: err.message });
        }

        if (result.affectedRows > 0) {
            // Gửi OTP qua email
            const mailOptions = {
                from: process.env.EMAIL_USER,
                to: email,
                subject: 'Đặt lại mật khẩu',
                text: `Mã OTP của bạn là: ${verification_token}`, // Gửi verification_token
            };

            transporter.sendMail(mailOptions, (error, info) => {
                if (error) {
                    return res.status(500).json({ message: 'Gửi OTP thất bại' });
                }
                res.status(200).json({ message: 'OTP đã được gửi qua email' });
            });
        } else {
            res.status(404).json({ message: 'Email không tồn tại' });
        }
    });
});

// API Đặt lại mật khẩu
app.post('/api/reset-password', async (req, res) => {
    const { email, otp, newPassword } = req.body;

    const sql = 'SELECT * FROM users WHERE email = ? AND verification_token = ?'; // Tìm user theo verification_token
    db.query(sql, [email, otp], async (err, results) => { // Thêm async để dùng await
        if (err) {
            return res.status(500).json({ message: 'Đặt lại mật khẩu thất bại', error: err.message });
        }

        if (results.length > 0) {
            const passwordHash = await bcrypt.hash(newPassword, 10); // Băm mật khẩu mới
            const updateSql = 'UPDATE users SET password_hash = ?, verification_token = NULL WHERE email = ?'; // Cập nhật password_hash, xóa verification_token
            db.query(updateSql, [passwordHash, email], (err, result) => {
                if (err) {
                    return res.status(500).json({ message: 'Đặt lại mật khẩu thất bại', error: err.message });
                }
                res.status(200).json({ message: 'Đặt lại mật khẩu thành công' });
            });
        } else {
            res.status(400).json({ message: 'OTP không hợp lệ' });
        }
    });
});

// API Lấy thông tin profile (ví dụ - cần chỉnh sửa cho phù hợp)
app.get('/api/profile', async (req, res) => {
    // TODO: Implement logic to get user profile (cần xác thực, lấy user ID từ session/token)
    res.status(200).json({ message: 'Tính năng profile chưa được triển khai trong backend đơn giản này.' });
});

// API Lấy danh sách videos (ví dụ - cần chỉnh sửa cho phù hợp)
app.get('/api/videos', async (req, res) => {
    // TODO: Implement logic to get video list (lấy từ database hoặc nguồn dữ liệu video của bạn)
    res.status(200).json({ message: 'Tính năng video list chưa được triển khai trong backend đơn giản này.' });
});

// API Lấy thông tin chi tiết video (ví dụ - cần chỉnh sửa cho phù hợp)
app.get('/api/videos/:id', async (req, res) => {
    const videoId = req.params.id;
    // TODO: Implement logic to get video details by ID
    res.status(200).json({ message: `Tính năng video detail cho ID ${videoId} chưa được triển khai trong backend đơn giản này.` });
});


// Khởi động server
app.listen(PORT, () => {
    console.log(`Server đang chạy trên cổng ${PORT}`);
});