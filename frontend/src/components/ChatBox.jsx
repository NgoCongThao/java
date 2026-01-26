import React, { useRef, useEffect } from 'react';
import Message from './Message';

const ChatBox = ({ messages, loading }) => {
  const messagesEndRef = useRef(null);

  // Tự động cuộn xuống cuối khi có tin nhắn mới
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  return (
    <div className="chat-box">
      {messages.map((msg, index) => (
        <Message key={index} msg={msg} />
      ))}
      
      {loading && (
        <div className="message-wrapper bot-wrapper">
          <div className="message-bubble bot-bubble loading-bubble">
            ...
          </div>
        </div>
      )}
      <div ref={messagesEndRef} />
    </div>
  );
};

export default ChatBox;