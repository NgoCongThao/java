import React, { useState } from 'react';

const InputArea = ({ onSend, loading }) => {
  const [text, setText] = useState('');

  const handleSend = () => {
    if (text.trim() && !loading) {
      onSend(text);
      setText('');
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      handleSend();
    }
  };

  return (
    <div className="input-area">
      <input
        type="text"
        placeholder="Nhập câu hỏi về dữ liệu..."
        value={text}
        onChange={(e) => setText(e.target.value)}
        onKeyDown={handleKeyDown}
        disabled={loading}
      />
      <button onClick={handleSend} disabled={loading || !text.trim()}>
        {loading ? 'Đang gửi...' : 'Gửi'}
      </button>
    </div>
  );
};

export default InputArea;