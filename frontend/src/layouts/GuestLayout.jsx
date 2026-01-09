import React from 'react';
import { Layout, Menu } from 'antd';
import { Outlet, useNavigate } from 'react-router-dom';

const { Header, Content, Footer } = Layout;

const GuestLayout = () => {
  const navigate = useNavigate();

  return (
    <Layout>
      <Header style={{ display: 'flex', alignItems: 'center' }}>
        <div style={{ color: 'white', fontSize: '20px', fontWeight: 'bold', marginRight: '30px' }}>S2O RESTAURANT</div>
        <Menu
          theme="dark"
          mode="horizontal"
          defaultSelectedKeys={['1']}
          onClick={({ key }) => navigate(key)}
          items={[
            { key: '/', label: 'Trang chủ' },
            { key: '/menu', label: 'Thực đơn' },
            { key: '/about', label: 'Giới thiệu' },
          ]}
        />
      </Header>
      <Content style={{ padding: '0 50px', marginTop: '20px', minHeight: '80vh' }}>
        <div style={{ background: '#fff', padding: 24, minHeight: 380 }}>
          <Outlet />
        </div>
      </Content>
      <Footer style={{ textAlign: 'center' }}>
        S2O Restaurant ©2026 Created by Team Java
      </Footer>
    </Layout>
  );
};

export default GuestLayout;