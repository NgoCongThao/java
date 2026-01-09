import React from 'react';
import { Layout, Menu, theme } from 'antd';
import { UserOutlined, ShopOutlined, FileTextOutlined } from '@ant-design/icons';
import { Outlet, useNavigate } from 'react-router-dom'; // Outlet là nơi hiển thị nội dung con

const { Header, Sider, Content } = Layout;

const AdminLayout = () => {
  const navigate = useNavigate();
  const {
    token: { colorBgContainer },
  } = theme.useToken();

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible>
        <div style={{ height: 32, margin: 16, background: 'rgba(255, 255, 255, 0.2)', color: 'white', textAlign: 'center', lineHeight: '32px' }}>
          S2O ADMIN
        </div>
        <Menu
          theme="dark"
          mode="inline"
          defaultSelectedKeys={['1']}
          onClick={({ key }) => navigate(key)}
          items={[
            { key: '/admin/dashboard', icon: <ShopOutlined />, label: 'Tổng quan' },
            { key: '/staff/orders', icon: <FileTextOutlined />, label: 'Đơn hàng (Staff)' },
            { key: '/admin/users', icon: <UserOutlined />, label: 'Nhân sự' },
          ]}
        />
      </Sider>
      <Layout>
        <Header style={{ padding: 0, background: colorBgContainer, textAlign: 'right', paddingRight: '20px' }}>
          Xin chào, Admin!
        </Header>
        <Content style={{ margin: '16px' }}>
          <div style={{ padding: 24, minHeight: 360, background: colorBgContainer }}>
             {/* Đây là nơi các trang con (Dashboard, OrderList) sẽ hiện ra */}
            <Outlet /> 
          </div>
        </Content>
      </Layout>
    </Layout>
  );
};

export default AdminLayout;