import axios from 'axios';

const axiosClient = axios.create({
  baseURL: 'http://localhost:8080/api', // Đường dẫn tới Backend của bạn
  headers: {
    'Content-Type': 'application/json',
  },
});

// Xử lý dữ liệu trả về (Response Interceptor)
axiosClient.interceptors.response.use(
  (response) => {
    // Nếu Backend trả về dữ liệu, chỉ lấy phần data thôi
    if (response && response.data) {
      return response.data;
    }
    return response;
  },
  (error) => {
    // Nếu có lỗi thì in ra console để debug
    console.error("Lỗi API:", error);
    throw error;
  }
);

export default axiosClient;