import React, { useState } from 'react';
import { Card, Form, Input, Button, Typography, message } from 'antd';
import { UserOutlined, LockOutlined, LoginOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import axiosClient from '../../api/axiosClient'; // Đảm bảo đường dẫn đúng tới file axiosClient của bạn

const { Title, Text } = Typography;

const Login = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const onFinish = async (values) => {
    setLoading(true);
    try {
      // 1. GỌI API ĐĂNG NHẬP THẬT TỪ BACKEND
      // Payload gửi đi: { username: "staff1", password: "123" }
      const response = await axiosClient.post('/auth/login', values);

      // 2. NẾU THÀNH CÔNG (Backend trả về 200 OK)
      message.success('Đăng nhập thành công!');

      // Lưu Token để dùng cho các request sau (Quan trọng nhất)
      localStorage.setItem('ACCESS_TOKEN', response.token);

      // Lưu thông tin user để hiển thị (VD: Xin chào, Nguyễn Văn A)
      localStorage.setItem('user', JSON.stringify(response));

      // 3. ĐIỀU HƯỚNG DỰA TRÊN ROLE
      if (response.role === 'ADMIN' || response.role === 'OWNER') {
        navigate('/admin/dashboard');
      } else {
        // Mặc định Staff vào thẳng sơ đồ bàn
        navigate('/staff/tables');
      }

    } catch (error) {
      // 4. XỬ LÝ LỖI (Backend trả về 401)
      console.error("Lỗi đăng nhập:", error);
      // Lấy câu thông báo lỗi từ Backend (nếu có), không thì báo chung chung
      const errorMsg = error.response?.data || 'Sai tài khoản hoặc mật khẩu!';
      message.error(typeof errorMsg === 'string' ? errorMsg : 'Đăng nhập thất bại!');
    } finally {
      setLoading(false);
    }
  };

  return (
      <div style={{
        height: '100vh',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        background: 'linear-gradient(135deg, #1890ff 0%, #001529 100%)'
      }}>
        <Card style={{ width: 400, borderRadius: 10, boxShadow: '0 4px 12px rgba(0,0,0,0.15)' }}>
          <div style={{ textAlign: 'center', marginBottom: 30 }}>
            <div style={{ fontSize: 40, color: '#1890ff' }}><LoginOutlined /></div>
            <Title level={2} style={{ margin: '10px 0' }}>S2O STAFF</Title>
            <Text type="secondary">Hệ thống quản lý nhà hàng (SaaS)</Text>
          </div>

          <Form
              name="login"
              initialValues={{ remember: true }}
              onFinish={onFinish}
              size="large"
          >
            <Form.Item
                name="username"
                rules={[{ required: true, message: 'Vui lòng nhập tài khoản!' }]}
            >
              <Input prefix={<UserOutlined />} placeholder="Tài khoản (VD: staff1)" />
            </Form.Item>

            <Form.Item
                name="password"
                rules={[{ required: true, message: 'Vui lòng nhập mật khẩu!' }]}
            >
              <Input.Password prefix={<LockOutlined />} placeholder="Mật khẩu" />
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" block loading={loading} style={{ height: 45, fontSize: 16 }}>
                ĐĂNG NHẬP
              </Button>
            </Form.Item>
          </Form>

          <div style={{ textAlign: 'center', color: '#888', fontSize: 12 }}>
            <p>Test S2O: staff1 / 123</p>
            <p>Test Highlands: staff2 / 123</p>
          </div>
        </Card>
      </div>
  );
};

export default Login;