import React, { useState, useEffect } from 'react';
import { Table, Tag, Button, Space, Typography, Card, Modal, Divider, message, Tabs, Row, Col, Spin, Popconfirm, Radio, Image } from 'antd';
import { CheckCircleOutlined, ClockCircleOutlined, SyncOutlined, DollarOutlined, FileTextOutlined, HistoryOutlined, DeleteOutlined, WalletOutlined, QrcodeOutlined } from '@ant-design/icons';
import axiosClient from '../../api/axiosClient';

const { Title, Text } = Typography;

const OrderList = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);

  // Modal & Thanh toán
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [currentOrder, setCurrentOrder] = useState(null);
  const [paymentMethod, setPaymentMethod] = useState('CASH');

  // --- 1. GỌI API LẤY DANH SÁCH ĐƠN ---
  const fetchOrders = async () => {
    setLoading(true);
    try {
      const res = await axiosClient.get('/staff/orders');

      const mappedOrders = res.map(order => ({
        key: order.id,
        id: order.id,
        table: order.table ? order.table.name : 'Mang về',
        items: order.items.map(i => ({
          id: i.id,
          name: i.product ? i.product.name : 'Món đã xóa',
          quantity: i.quantity,
          price: i.price,
          status: i.status
        })),
        total: order.totalAmount,
        status: order.status,
        createdAt: order.createdAt
      }));

      setOrders(mappedOrders.reverse());
    } catch (error) {
      console.log(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
    const interval = setInterval(fetchOrders, 5000); // 5s refresh 1 lần cho nhanh
    return () => clearInterval(interval);
  }, []);

  // --- 2. CÁC HÀM XỬ LÝ TRẠNG THÁI MÓN & ĐƠN ---

  // Đổi trạng thái từng món (Chờ -> Nấu -> Lên)
  const handleItemClick = async (itemId, currentStatus) => {
    let nextStatus = '';
    if (currentStatus === 'PENDING') nextStatus = 'COOKING';
    else if (currentStatus === 'COOKING') nextStatus = 'DELIVERED';
    else return;

    try {
      await axiosClient.put(`/staff/orders/items/${itemId}/status?newStatus=${nextStatus}`);
      message.success("Đã cập nhật món!");
      fetchOrders();
    } catch (error) {
      message.error("Lỗi cập nhật món!");
    }
  };

  // Hủy món (Trừ tiền)
  const handleDeleteItem = async (itemId) => {
    try {
      await axiosClient.delete(`/staff/orders/items/${itemId}`);
      message.success("Đã hủy món thành công!");
      fetchOrders();
    } catch (error) {
      message.error("Không thể hủy món này!");
    }
  };

  // Cập nhật trạng thái đơn (Thanh toán xong -> PAID)
  const updateStatus = async (orderId, newStatus) => {
    try {
      await axiosClient.put(`/staff/orders/${orderId}/status?newStatus=${newStatus}`);
      message.success("Đã cập nhật đơn hàng!");
      fetchOrders();
    } catch (error) {
      message.error("Lỗi cập nhật đơn!");
    }
  };

  // --- 3. LOGIC MODAL THANH TOÁN ---
  const showBill = (order) => {
    setCurrentOrder(order);
    setPaymentMethod('CASH'); // Mặc định tiền mặt
    setIsModalOpen(true);
  };

  const handlePaymentSuccess = async () => {
    await updateStatus(currentOrder.id, 'PAID');
    setIsModalOpen(false);
    message.success(`Thanh toán thành công! Thực thu: ${currentOrder.total.toLocaleString()}đ`);
  };

  // --- CẤU HÌNH CỘT BẢNG ---
  const columns = [
    {
      title: 'Bàn', dataIndex: 'table', key: 'table',
      render: (text) => <b style={{ fontSize: 16, color: '#1890ff' }}>{text}</b>,
    },
    {
      title: 'Chi tiết món',
      dataIndex: 'items',
      key: 'items',
      width: 400,
      render: (items) => (
          <ul style={{ paddingLeft: 0, listStyle: 'none' }}>
            {items.map((item, index) => {
              let color = 'default';
              let statusText = 'Chờ';
              let cursor = 'pointer';
              let isCancelled = false;

              if (item.status === 'COOKING') { color = 'orange'; statusText = 'Đang nấu'; }
              if (item.status === 'DELIVERED') { color = 'green'; statusText = 'Đã lên'; cursor = 'default'; }
              if (item.status === 'PENDING') { color = 'red'; statusText = 'Chờ'; }
              if (item.status === 'CANCELLED') { color = '#d9d9d9'; statusText = 'Đã hủy'; cursor = 'default'; isCancelled = true; }

              return (
                  <li key={index} style={{ marginBottom: 8, display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px dashed #eee', paddingBottom: 5, textDecoration: isCancelled ? 'line-through' : 'none', color: isCancelled ? '#999' : 'inherit' }}>
                    <Space>
                      <b>{item.quantity}x</b>
                      <span>{item.name}</span>
                    </Space>

                    <Space>
                      {/* Tag trạng thái */}
                      <Tag color={color} style={{ cursor: cursor, userSelect: 'none' }} onClick={() => !isCancelled && handleItemClick(item.id, item.status)}>
                        {statusText}
                      </Tag>

                      {/* Nút Hủy món */}
                      {!isCancelled && item.status !== 'DELIVERED' && (
                          <Popconfirm title="Hủy món này?" description="Tiền sẽ được trừ lại." onConfirm={() => handleDeleteItem(item.id)} okText="Hủy món" cancelText="Đóng">
                            <Button type="text" danger size="small" icon={<DeleteOutlined />} />
                          </Popconfirm>
                      )}
                    </Space>
                  </li>
              );
            })}
          </ul>
      ),
    },
    {
      title: 'Tổng tiền', dataIndex: 'total', key: 'total',
      render: (price) => <span style={{ fontWeight: 'bold', color: '#d4380d' }}>{price?.toLocaleString()}đ</span>,
    },
    {
      title: 'Trạng thái Đơn', key: 'status',
      render: (_, record) => {
        let color = 'default'; let icon = <ClockCircleOutlined />; let text = 'Chờ xác nhận';
        if (record.status === 'COOKING') { color = 'processing'; icon = <SyncOutlined spin />; text = 'Đang phục vụ'; }
        if (record.status === 'DELIVERED') { color = 'success'; icon = <CheckCircleOutlined />; text = 'Đủ món'; }
        if (record.status === 'PAID') { color = '#87d068'; icon = <DollarOutlined />; text = 'Đã thanh toán'; }
        return <Tag icon={icon} color={color}>{text}</Tag>;
      },
    },
    {
      title: 'Hành động', key: 'action',
      render: (_, record) => {
        if (record.status === 'PAID') return <Button size="small" onClick={() => showBill(record)}>Xem Bill</Button>;
        return (
            <Space size="small">
              <Button type="primary" danger icon={<DollarOutlined />} onClick={() => showBill(record)}>Thanh toán</Button>
            </Space>
        );
      },
    },
  ];

  const activeOrders = orders.filter(o => o.status !== 'PAID');
  const historyOrders = orders.filter(o => o.status === 'PAID');

  return (
      <Card>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 10 }}>
          <Title level={3} style={{ margin: 0 }}>📋 Quản lý Đơn Hàng (Staff)</Title>
          <Button onClick={fetchOrders} icon={<SyncOutlined />}>Làm mới</Button>
        </div>

        {loading && orders.length === 0 ? <div style={{textAlign: 'center'}}><Spin /></div> : (
            <Tabs defaultActiveKey="1" items={[
              { key: '1', label: <span><FileTextOutlined /> Đang phục vụ ({activeOrders.length})</span>, children: <Table columns={columns} dataSource={activeOrders} pagination={false} bordered rowKey="id" /> },
              { key: '2', label: <span><HistoryOutlined /> Lịch sử ({historyOrders.length})</span>, children: <Table columns={columns} dataSource={historyOrders} pagination={false} bordered rowKey="id" /> },
            ]} />
        )}

        {/* MODAL THANH TOÁN (GỌN NHẸ) */}
        <Modal
            title={<div style={{ textAlign: 'center', fontSize: 20 }}>THANH TOÁN HÓA ĐƠN</div>}
            open={isModalOpen}
            onCancel={() => setIsModalOpen(false)}
            width={600}
            footer={[
              <Button key="close" onClick={() => setIsModalOpen(false)}>Đóng</Button>,
              currentOrder?.status !== 'PAID' && (
                  <Button key="submit" type="primary" size="large" onClick={handlePaymentSuccess}>
                    Xác nhận thu {currentOrder?.total?.toLocaleString()}đ
                  </Button>
              )
            ]}
        >
          {currentOrder && (
              <div>
                <div style={{ textAlign: 'center', marginBottom: 10 }}>
                  <Title level={4}>{currentOrder.table}</Title>
                  <Text type="secondary">Mã đơn: #{currentOrder.id}</Text>
                </div>

                {/* BILL CHI TIẾT */}
                <Table
                    dataSource={currentOrder.items}
                    pagination={false}
                    size="small"
                    rowKey="id"
                    columns={[
                      { title: 'Món', dataIndex: 'name', render: (text, r) => <span style={r.status==='CANCELLED'?{textDecoration:'line-through', color:'#999'}:{}}>{text}</span> },
                      { title: 'SL', dataIndex: 'quantity' },
                      { title: 'Tiền', render: (_, r) => r.status==='CANCELLED' ? <Text delete>0đ</Text> : (r.price * r.quantity).toLocaleString() }
                    ]}
                />

                {currentOrder.status !== 'PAID' && (
                    <>
                      <Divider style={{margin: '15px 0'}} />

                      {/* CHỌN PHƯƠNG THỨC THANH TOÁN */}
                      <div style={{ marginBottom: 15 }}>
                        <Text strong>Phương thức thanh toán:</Text> <br/>
                        <Radio.Group
                            onChange={(e) => setPaymentMethod(e.target.value)}
                            value={paymentMethod}
                            style={{ marginTop: 10, width: '100%' }}
                        >
                          <Row>
                            <Col span={12}><Radio value="CASH"><WalletOutlined /> Tiền mặt</Radio></Col>
                            <Col span={12}><Radio value="BANK"><QrcodeOutlined /> Chuyển khoản / QR</Radio></Col>
                          </Row>
                        </Radio.Group>

                        {paymentMethod === 'BANK' && (
                            <div style={{ textAlign: 'center', marginTop: 15, padding: 10, background: '#f5f5f5', borderRadius: 8 }}>
                              <Text type="secondary">Quét mã để thanh toán:</Text> <br/>
                              <Image width={150} src="https://img.vietqr.io/image/MB-0000000000-compact.png" preview={false} style={{marginTop: 5}}/>
                            </div>
                        )}
                      </div>

                      <Divider style={{margin: '15px 0'}} />

                      {/* TỔNG TIỀN */}
                      <Row justify="space-between" align="middle">
                        <Title level={5} style={{margin: 0}}>KHÁCH CẦN TRẢ:</Title>
                        <Title level={3} style={{ color: '#d4380d', margin: 0 }}>{currentOrder.total?.toLocaleString()}đ</Title>
                      </Row>
                    </>
                )}

                {currentOrder.status === 'PAID' && (
                    <div style={{textAlign: 'center', marginTop: 20, color: 'green', fontWeight: 'bold'}}>
                      ĐƠN HÀNG ĐÃ THANH TOÁN THÀNH CÔNG
                    </div>
                )}
              </div>
          )}
        </Modal>
      </Card>
  );
};

export default OrderList;