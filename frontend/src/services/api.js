import axios from 'axios';

// Lấy đường dẫn từ file .env.local
const API_URL = import.meta.env.VITE_API_URL || 'http://127.0.0.1:8000/api';

export const sendQuestionToAI = async (question) => {
  try {
    const response = await axios.post(`${API_URL}/chat`, { question });
    return response.data;
  } catch (error) {
    console.error("Lỗi gọi API:", error);
    return { 
      answer: "Xin lỗi, không thể kết nối tới Server Chatbot.", 
      sources: [] 
    };
  }
};