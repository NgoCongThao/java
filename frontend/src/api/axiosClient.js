import axios from 'axios';

const axiosClient = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

// 👇 1. THÊM REQUEST INTERCEPTOR (QUAN TRỌNG)
// Tác dụng: Trước khi gửi request đi, tự động lấy Token từ kho và dán vào Header
axiosClient.interceptors.request.use(async (config) => {
    // Lấy token đã lưu lúc đăng nhập
    const token = localStorage.getItem('ACCESS_TOKEN');

    if (token) {
        // Dán vào Header theo chuẩn: "Bearer <token>"
        // Backend (SimpleAuthenticationFilter) sẽ đọc chuỗi này để biết bạn là ai
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

// 2. RESPONSE INTERCEPTOR (Giữ nguyên như cũ)
axiosClient.interceptors.response.use(
    (response) => {
        if (response && response.data) {
            return response.data;
        }
        return response;
    },
    (error) => {
        console.error("Lỗi API:", error);
        throw error;
    }
);

export default axiosClient;