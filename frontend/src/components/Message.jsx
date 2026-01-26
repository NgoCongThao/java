import React from 'react';

const Message = ({ msg }) => {
  const isBot = msg.sender === 'bot';
  
  return (
    <div className={`message-wrapper ${isBot ? 'bot-wrapper' : 'user-wrapper'}`}>
      <div className={`message-bubble ${isBot ? 'bot-bubble' : 'user-bubble'}`}>
        <div className="message-text">{msg.text}</div>
        
        {/* Nếu là bot và có nguồn trích dẫn thì hiển thị */}
        {isBot && msg.sources && msg.sources.length > 0 && (
          <div className="message-sources">
            <strong>Nguồn tham khảo:</strong>
            <ul>
              {msg.sources.map((src, index) => (
                <li key={index}>{src.split('/').pop()}</li> 
              ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  );
};

export default Message;