import React, { useState } from 'react';
import { Card, Input, Typography, Row, Col, Statistic, Table, Tag, Button, Empty, message, Modal } from 'antd';
import { SearchOutlined, UserOutlined, GiftOutlined, HistoryOutlined } from '@ant-design/icons';

const { Title, Text } = Typography;
const { Search } = Input;

// --- DỮ LIỆU TẠM THỜI (Khi nào có API Backend Customer thì xóa đi) ---
const TEMP_MEMBERS = [
  {
    phone: '0909123456',
    name: 'Nguyễn Văn A',
    level: 'GOLD',
    points: 1250,
    history: [
      { id: 1, date: '10/01/2026', action: 'Tích điểm', amount: +100, note: 'Hóa đơn #123' },
    ]
  },
  {
    phone: '0987654321',
    name: 'Trần Thị B',
    level: 'SILVER',
    points: 300,
    history: []
  }
];

const Loyalty = () => {
  const [member, setMember] = useState(null);
  const [loading, setLoading] = useState(false);

  const onSearch = (value) => {
    setLoading(true);
    // Giả lập gọi API
    setTimeout(() => {
      const foundMember = TEMP_MEMBERS.find(m => m.phone === value);
      if (foundMember) {
        setMember(foundMember);
        message.success("Đã tìm thấy khách hàng!");
      } else {
        setMember(null);
        message.error("Không tìm thấy khách hàng này!");
      }
      setLoading(false);
    }, 500);
  };

  const handleRedeem = () => {
    Modal.confirm({
      title: 'Xác nhận đổi điểm',
      content: `Bạn có muốn dùng 500 điểm để đổi Voucher 50k cho khách ${member.name}?`,
      onOk() {
        setMember({
          ...member,
          points: member.points - 500,
          history: [{ id: Date.now(), date: new Date().toLocaleDateString('vi-VN'), action: 'Đổi quà', amount: -500, note: 'Voucher 50k' }, ...member.history]
        });
        message.success('Đổi quà thành công!');
      }
    });
  };

  const columns = [
    { title: 'Ngày', dataIndex: 'date', key: 'date' },
    { title: 'Hoạt động', dataIndex: 'action', key: 'action' },
    { title: 'Điểm', dataIndex: 'amount', key: 'amount', render: (amount) => <Tag color={amount > 0 ? 'green' : 'red'}>{amount > 0 ? `+${amount}` : amount}</Tag> },
    { title: 'Ghi chú', dataIndex: 'note', key: 'note' },
  ];

  return (
      <div style={{ maxWidth: 1000, margin: '0 auto' }}>
        <Title level={2} style={{ textAlign: 'center', marginBottom: 30 }}>🎁 Khách hàng thân thiết</Title>
        <Card style={{ marginBottom: 20, textAlign: 'center' }}>
          <Text strong>Nhập SĐT khách hàng:</Text>
          <Search placeholder="Ví dụ: 0909123456" allowClear enterButton="Tra cứu" size="large" onSearch={onSearch} loading={loading} style={{ width: 400, display: 'block', margin: '10px auto' }} />
        </Card>
        {member ? (
            <Row gutter={20}>
              <Col span={10}>
                <Card title={<><UserOutlined /> Thông tin</>} actions={[<Button type="primary" icon={<GiftOutlined />} onClick={handleRedeem} disabled={member.points < 500}>Đổi Voucher (500đ)</Button>]}>
                  <div style={{ textAlign: 'center', marginBottom: 20 }}><Title level={3}>{member.name}</Title><Tag color="gold">{member.level} MEMBER</Tag></div>
                  <Row gutter={16}><Col span={12}><Statistic title="Điểm" value={member.points} /></Col><Col span={12}><Statistic title="SĐT" value={member.phone} /></Col></Row>
                </Card>
              </Col>
              <Col span={14}><Card title={<><HistoryOutlined /> Lịch sử</>}><Table dataSource={member.history} columns={columns} pagination={false} size="small" rowKey="id" /></Card></Col>
            </Row>
        ) : (!loading && <Empty description="Nhập SĐT để tra cứu" />)}
      </div>
  );
};
export default Loyalty;