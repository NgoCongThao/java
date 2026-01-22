import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Badge, Tag, Typography, message, Spin, Dropdown, Button } from 'antd';
import { useNavigate } from 'react-router-dom';
import { EllipsisOutlined, CalendarOutlined, CloseCircleOutlined, UserAddOutlined, ClearOutlined, SyncOutlined } from '@ant-design/icons';
import axiosClient from '../../api/axiosClient';

const { Title } = Typography;

// --- HÀM TIỆN ÍCH (Đã sửa AVAILABLE -> EMPTY cho khớp Backend) ---
const getStatusColor = (status) => {
    switch (status) {
        case 'EMPTY': return '#52c41a';    // Xanh lá (Trống)
        case 'OCCUPIED': return '#ff4d4f'; // Đỏ (Có khách)
        case 'RESERVED': return '#faad14'; // Vàng (Đã đặt)
        default: return '#d9d9d9';
    }
};

const getStatusText = (status) => {
    switch (status) {
        case 'EMPTY': return 'Trống';
        case 'OCCUPIED': return 'Có khách';
        case 'RESERVED': return 'Đã đặt trước';
        default: return 'Khác';
    }
};

const TableMap = () => {
    const navigate = useNavigate();
    const [listTables, setListTables] = useState([]);
    const [loading, setLoading] = useState(false);

    // 1. Hàm gọi API lấy danh sách bàn
    const fetchTables = async () => {
        setLoading(true);
        try {
            const response = await axiosClient.get('/staff/tables');
            setListTables(response);
        } catch (error) {
            console.error("Lỗi tải bàn:", error);
            // message.error("Lỗi kết nối!"); // Tắt bớt cho đỡ phiền
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchTables();
        // Tự động refresh sau mỗi 10s để cập nhật trạng thái từ các máy khác
        const interval = setInterval(fetchTables, 10000);
        return () => clearInterval(interval);
    }, []);

    // 2. Hàm chuyển hướng sang trang gọi món
    const handleTableClick = (tableId) => {
        navigate(`/staff/order/create/${tableId}`);
    };

    // 3. HÀM XỬ LÝ ĐỔI TRẠNG THÁI (GỌI API PUT)
    const handleUpdateStatus = async (tableId, newStatus) => {
        try {
            // Gọi API: PUT /api/staff/tables/{id}/status?status=...
            await axiosClient.put(`/staff/tables/${tableId}/status?status=${newStatus}`);
            message.success(`Đã cập nhật trạng thái bàn!`);
            fetchTables(); // Tải lại ngay lập tức
        } catch (error) {
            message.error("Không thể cập nhật trạng thái!");
        }
    };

    // 4. CẤU HÌNH MENU CHO TỪNG LOẠI TRẠNG THÁI
    const getMenuProps = (table) => {
        let items = [];

        // Logic menu dựa trên trạng thái hiện tại
        if (table.status === 'EMPTY') {
            items = [
                {
                    key: 'reserve',
                    label: 'Đặt bàn này',
                    icon: <CalendarOutlined />,
                    onClick: () => handleUpdateStatus(table.id, 'RESERVED')
                },
                {
                    key: 'order',
                    label: 'Vào gọi món ngay',
                    icon: <UserAddOutlined />,
                    onClick: () => handleTableClick(table.id)
                }
            ];
        } else if (table.status === 'RESERVED') {
            items = [
                {
                    key: 'cancel',
                    label: 'Hủy đặt bàn',
                    icon: <CloseCircleOutlined />,
                    danger: true,
                    onClick: () => handleUpdateStatus(table.id, 'EMPTY')
                },
                {
                    key: 'checkin',
                    label: 'Khách đã đến (Nhận bàn)',
                    icon: <UserAddOutlined />,
                    onClick: () => handleTableClick(table.id)
                }
            ];
        } else if (table.status === 'OCCUPIED') {
            items = [
                {
                    key: 'order',
                    label: 'Gọi thêm món',
                    icon: <UserAddOutlined />,
                    onClick: () => handleTableClick(table.id)
                },
                {
                    key: 'clean',
                    label: 'Dọn bàn (Force Clear)',
                    icon: <ClearOutlined />,
                    danger: true,
                    onClick: () => handleUpdateStatus(table.id, 'EMPTY')
                    // Nút này dùng khi lỡ thanh toán rồi mà bàn chưa chuyển xanh, hoặc khách bỏ về
                }
            ];
        }

        return { items };
    };

    return (
        <div>
            {/* Header + Chú thích */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
                <Title level={3} style={{ margin: 0 }}>Sơ đồ bàn ăn (Online)</Title>
                <div style={{display: 'flex', alignItems: 'center', gap: 10}}>
                    <Button icon={<SyncOutlined />} onClick={fetchTables}>Làm mới</Button>
                    <div>
                        <Tag color="#52c41a">Trống</Tag>
                        <Tag color="#ff4d4f">Có khách</Tag>
                        <Tag color="#faad14">Đã đặt</Tag>
                    </div>
                </div>
            </div>

            {loading && listTables.length === 0 ? (
                <div style={{ textAlign: 'center', marginTop: 50 }}>
                    <Spin size="large" tip="Đang tải dữ liệu..." />
                </div>
            ) : (
                <Row gutter={[16, 16]}>
                    {listTables.map((table) => (
                        <Col xs={12} sm={8} md={6} lg={4} key={table.id}>
                            <Card
                                hoverable
                                // Logic: Bấm vào thẻ thì vào gọi món
                                onClick={() => handleTableClick(table.id)}
                                style={{
                                    textAlign: 'center',
                                    borderTop: `4px solid ${getStatusColor(table.status)}`,
                                    backgroundColor: table.status === 'OCCUPIED' ? '#fff1f0' : (table.status === 'RESERVED' ? '#fffbe6' : '#fff'),
                                    position: 'relative'
                                }}
                                // Thêm nút "..." ở góc trên bên phải thẻ
                                extra={
                                    <Dropdown menu={getMenuProps(table)} trigger={['click']}>
                                        <Button
                                            type="text"
                                            icon={<EllipsisOutlined style={{ fontSize: 20, fontWeight: 'bold' }} />}
                                            // Quan trọng: Ngăn không cho sự kiện click lan ra thẻ cha (không bị nhảy trang gọi món)
                                            onClick={(e) => e.stopPropagation()}
                                        />
                                    </Dropdown>
                                }
                            >
                                <div style={{ fontSize: '18px', fontWeight: 'bold', marginBottom: 5 }}>
                                    {table.name}
                                </div>
                                <Badge
                                    color={getStatusColor(table.status)}
                                    text={getStatusText(table.status)}
                                />
                                <div style={{ marginTop: 10, fontSize: '12px', color: '#888' }}>
                                    ID: {table.id}
                                </div>
                            </Card>
                        </Col>
                    ))}
                </Row>
            )}
        </div>
    );
};

export default TableMap;