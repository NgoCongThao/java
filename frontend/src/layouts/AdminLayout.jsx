import React, { useEffect, useState } from 'react';
import { Layout, Menu, theme, Modal, message } from 'antd';
import {
    UserOutlined,
    ShopOutlined,
    FileTextOutlined,
    AppstoreOutlined,
    FireOutlined,
    LogoutOutlined
} from '@ant-design/icons';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';

const { Header, Sider, Content } = Layout;

const AdminLayout = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const {
        token: { colorBgContainer },
    } = theme.useToken();

    // 1. Lấy thông tin User và Role từ LocalStorage
    const [user, setUser] = useState({});

    useEffect(() => {
        const userStr = localStorage.getItem('user');
        if (userStr) {
            setUser(JSON.parse(userStr));
        } else {
            // Chưa đăng nhập mà đòi vào -> đá ra login
            navigate('/login');
        }
    }, [navigate]);

    const handleLogout = () => {
        Modal.confirm({
            title: 'Xác nhận đăng xuất',
            content: 'Bạn có chắc chắn muốn thoát khỏi hệ thống?',
            okText: 'Đăng xuất',
            cancelText: 'Hủy',
            okType: 'danger',
            onOk: () => {
                localStorage.removeItem('ACCESS_TOKEN');
                localStorage.removeItem('user');
                navigate('/login');
            }
        });
    };

    // 2. CẤU HÌNH MENU DỰA TRÊN ROLE
    const getMenuItems = () => {
        const role = user.role; // Lấy role: 'ADMIN', 'OWNER', 'STAFF'

        // Danh sách menu chung (Ai cũng thấy)
        const items = [
            { key: '/staff/tables', icon: <AppstoreOutlined />, label: 'Sơ đồ bàn' },
            { key: '/staff/orders', icon: <FileTextOutlined />, label: 'Quản lý Đơn' },
            { key: '/staff/kitchen', icon: <FireOutlined />, label: 'Bếp / Bar (KDS)' },
        ];

        // Nếu là SẾP (Admin/Owner) thì chèn thêm mục quản lý vào đầu và cuối
        if (role === 'ADMIN' || role === 'OWNER') {
            // Chèn Dashboard vào đầu
            items.unshift({ key: '/admin/dashboard', icon: <ShopOutlined />, label: 'Tổng quan (Boss)' });

            // Chèn Quản lý nhân sự vào sau cùng (trước nút logout)
            items.push({ key: '/admin/users', icon: <UserOutlined />, label: 'Nhân sự' });
        }

        // Cuối cùng luôn là nút Đăng xuất
        items.push({ type: 'divider' });
        items.push({ key: 'logout', icon: <LogoutOutlined />, label: 'Đăng xuất', danger: true });

        return items;
    };

    return (
        <Layout style={{ minHeight: '100vh' }}>
            <Sider collapsible>
                <div style={{ height: 32, margin: 16, background: 'rgba(255, 255, 255, 0.2)', color: 'white', textAlign: 'center', lineHeight: '32px', fontWeight: 'bold' }}>
                    S2O {user.role === 'STAFF' ? 'STAFF' : 'ADMIN'}
                </div>

                <Menu
                    theme="dark"
                    mode="inline"
                    selectedKeys={[location.pathname]} // Tự động highlight mục đang chọn
                    onClick={({ key }) => key === 'logout' ? handleLogout() : navigate(key)}
                    items={getMenuItems()} // <--- GỌI HÀM LẤY MENU ĐỘNG
                />
            </Sider>

            <Layout>
                <Header style={{ padding: 0, background: colorBgContainer, textAlign: 'right', paddingRight: '20px' }}>
          <span style={{ marginRight: 15 }}>
            Xin chào, <b>{user.fullName || user.username}</b> ({user.role})
          </span>
                </Header>
                <Content style={{ margin: '16px' }}>
                    <div style={{ padding: 24, minHeight: 360, background: colorBgContainer }}>
                        <Outlet />
                    </div>
                </Content>
            </Layout>
        </Layout>
    );
};

export default AdminLayout;