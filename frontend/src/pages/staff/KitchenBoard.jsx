import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Tag, Button, Typography, message, Spin, Empty } from 'antd';
import { FireOutlined, CheckCircleOutlined, ClockCircleOutlined, SyncOutlined } from '@ant-design/icons';
import axiosClient from '../../api/axiosClient';

const { Title, Text } = Typography;

const KitchenBoard = () => {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(false);

    // 1. Lấy danh sách đơn hàng (Lọc phía Frontend cho nhanh)
    const fetchOrders = async () => {
        setLoading(true);
        try {
            const res = await axiosClient.get('/staff/orders');

            // LOGIC LỌC BẾP:
            // Chỉ lấy những đơn hàng CÓ món đang chờ (PENDING) hoặc đang nấu (COOKING)
            // Những đơn đã xong hết (DELIVERED) hoặc đã trả tiền (PAID) thì ẩn đi cho gọn bếp
            const activeOrders = res.filter(order => {
                // Kiểm tra xem trong đơn có món nào cần làm không
                const hasPendingItems = order.items.some(item =>
                    item.status === 'PENDING' || item.status === 'COOKING'
                );
                return hasPendingItems && order.status !== 'PAID';
            });

            // Map lại dữ liệu cần thiết
            const formattedOrders = activeOrders.map(order => ({
                id: order.id,
                tableName: order.table ? order.table.name : 'Mang về',
                createdAt: order.createdAt, // Có thể dùng để tính thời gian chờ
                // Chỉ hiển thị những món Bếp cần quan tâm
                items: order.items.filter(i => i.status === 'PENDING' || i.status === 'COOKING')
            }));

            setOrders(formattedOrders);
        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchOrders();
        // Tự động refresh 5s/lần để nhận món mới (Realtime kiểu đơn giản)
        const interval = setInterval(fetchOrders, 5000);
        return () => clearInterval(interval);
    }, []);

    // 2. Xử lý chuyển trạng thái: PENDING -> COOKING -> DELIVERED
    const handleItemAction = async (itemId, currentStatus) => {
        let nextStatus = '';
        if (currentStatus === 'PENDING') nextStatus = 'COOKING'; // Bấm phát chuyển sang Đang nấu
        else if (currentStatus === 'COOKING') nextStatus = 'DELIVERED'; // Bấm phát là Xong

        try {
            await axiosClient.put(`/staff/orders/items/${itemId}/status?newStatus=${nextStatus}`);
            message.success("Đã cập nhật!");
            fetchOrders(); // Load lại ngay
        } catch (error) {
            message.error("Lỗi cập nhật!");
        }
    };

    return (
        <div style={{ padding: 20, backgroundColor: '#f0f2f5', minHeight: '100vh' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 20 }}>
                <Title level={2} style={{ margin: 0, color: '#d4380d' }}>🔥 BẾP TRUNG TÂM (KDS)</Title>
                <Button type="primary" size="large" icon={<SyncOutlined />} onClick={fetchOrders}>Làm mới</Button>
            </div>

            {loading && orders.length === 0 ? (
                <div style={{textAlign: 'center', marginTop: 100}}><Spin size="large" /></div>
            ) : orders.length === 0 ? (
                <Empty description="Bếp đang rảnh rỗi!" style={{marginTop: 100}} />
            ) : (
                <Row gutter={[16, 16]}>
                    {orders.map(order => (
                        <Col xs={24} sm={12} md={8} lg={6} key={order.id}>
                            <Card
                                title={<span style={{fontSize: 18, fontWeight: 'bold'}}>{order.tableName}</span>}
                                extra={<Text type="secondary">#{order.id}</Text>}
                                headStyle={{ backgroundColor: '#fff7e6', borderBottom: '2px solid #ffa940' }}
                                bodyStyle={{ padding: 10 }}
                                hoverable
                            >
                                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                                    {order.items.map(item => {
                                        const isCooking = item.status === 'COOKING';
                                        return (
                                            <div
                                                key={item.id}
                                                style={{
                                                    display: 'flex',
                                                    justifyContent: 'space-between',
                                                    alignItems: 'center',
                                                    backgroundColor: isCooking ? '#e6f7ff' : '#fff',
                                                    padding: 10,
                                                    border: '1px solid #eee',
                                                    borderRadius: 8
                                                }}
                                            >
                                                <div>
                                                    <div style={{ fontSize: 16, fontWeight: 'bold' }}>
                                                        <span style={{color: '#d4380d', marginRight: 5}}>{item.quantity}x</span>
                                                        {item.product ? item.product.name : 'Món xóa'}
                                                    </div>
                                                    <Tag color={isCooking ? 'blue' : 'red'}>
                                                        {isCooking ? 'Đang nấu...' : 'Chờ nấu'}
                                                    </Tag>
                                                </div>

                                                <Button
                                                    type={isCooking ? "primary" : "default"}
                                                    danger={!isCooking}
                                                    shape="circle"
                                                    size="large"
                                                    style={{ marginLeft: 10 }}
                                                    icon={isCooking ? <CheckCircleOutlined /> : <FireOutlined />}
                                                    onClick={() => handleItemAction(item.id, item.status)}
                                                />
                                            </div>
                                        );
                                    })}
                                </div>
                            </Card>
                        </Col>
                    ))}
                </Row>
            )}
        </div>
    );
};

export default KitchenBoard;