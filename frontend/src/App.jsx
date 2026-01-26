import React, { useState } from 'react';
import ChatBox from './components/ChatBox';
import InputArea from './components/InputArea';
import { sendQuestionToAI } from './services/api';
import './index.css';

function App() {
  const [messages, setMessages] = useState([
    { sender: 'bot', text: 'Xin chào! Tôi là trợ lý AI. Bạn cần tìm thông tin gì trong hệ thống?' }
  ]);
  const [loading, setLoading] = useState(false);

  const handleSendQuestion = async (questionText) => {
    // 1. Thêm tin nhắn của User vào list
    const userMsg = { sender: 'user', text: questionText };
    setMessages(prev => [...prev, userMsg]);
    setLoading(true);

    // 2. Gọi API Backend
    const result = await sendQuestionToAI(questionText);

    // 3. Thêm câu trả lời của Bot vào list
    const botMsg = { 
      sender: 'bot', 
      text: result.answer,
      sources: result.sources 
    };
    setMessages(prev => [...prev, botMsg]);
    setLoading(false);
  };

  return (
    <div className="app-container">
      <header className="app-header">
        <h1>🤖 Chatbot Dự Án</h1>
      </header>
      
      <main className="chat-container">
        <ChatBox messages={messages} loading={loading} />
        <InputArea onSend={handleSendQuestion} loading={loading} />
      </main>
    </div>
  );
}

export default App;