import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Row, Col, Card, Button, Tabs, List, Typography, message, InputNumber, Divider, Spin } from 'antd';
import { ShoppingCartOutlined, SendOutlined, ArrowLeftOutlined } from '@ant-design/icons';
// Import Axios Client để gọi API
import axiosClient from '../../api/axiosClient';

const { Meta } = Card;
const { Title } = Typography;

const CreateOrder = () => {
  const { id } = useParams(); // Lấy ID bàn từ URL
  const navigate = useNavigate();

  // --- STATE DỮ LIỆU THẬT ---
  const [menuItems, setMenuItems] = useState([]); // Danh sách món từ DB
  const [categories, setCategories] = useState(['Tất cả']); // Danh sách danh mục tự động
  const [loading, setLoading] = useState(false);

  const [cart, setCart] = useState([]); // Giỏ hàng tạm
  const [activeCategory, setActiveCategory] = useState('Tất cả');

  // --- GỌI API LẤY MENU TỪ DATABASE ---
  useEffect(() => {
    const fetchMenu = async () => {
      setLoading(true);
      try {
        // Gọi API Backend: GET /api/staff/products
        const res = await axiosClient.get('/staff/products');
        setMenuItems(res);

        // Logic tự động lấy danh mục:
        // 1. Duyệt qua tất cả món ăn
        // 2. Lấy tên category (item.category.name)
        // 3. Dùng Set để lọc trùng lặp -> Tạo ra danh sách Tabs
        const uniqueCategories = [
          'Tất cả',
          ...new Set(res.map(item => item.category ? item.category.name : 'Khác'))
        ];
        setCategories(uniqueCategories);

      } catch (error) {
        console.error(error);
        message.error("Lỗi tải thực đơn từ hệ thống!");
      } finally {
        setLoading(false);
      }
    };

    fetchMenu();
  }, []);

  // --- LOGIC LỌC MÓN ---
  // Lưu ý: Dữ liệu thật có cấu trúc item.category là OBJECT, không phải string
  const filteredMenu = activeCategory === 'Tất cả'
      ? menuItems
      : menuItems.filter(item => item.category?.name === activeCategory);

  // Thêm món vào giỏ
  const addToCart = (item) => {
    // Kiểm tra nếu món không có sẵn (Available = false)
    if (item.available === false) { // Chú ý: JSON thường trả về lowercase 'available'
      message.warning("Món này hiện đang hết hàng!");
      return;
    }

    const existingItem = cart.find(x => x.id === item.id);
    if (existingItem) {
      setCart(cart.map(x => x.id === item.id ? { ...x, quantity: x.quantity + 1 } : x));
    } else {
      setCart([...cart, { ...item, quantity: 1 }]);
    }
    message.success(`Đã thêm ${item.name}`);
  };

  // Tăng giảm số lượng
  const updateQuantity = (itemId, value) => {
    if (value === 0) {
      setCart(cart.filter(x => x.id !== itemId));
    } else {
      setCart(cart.map(x => x.id === itemId ? { ...x, quantity: value } : x));
    }
  };

  const totalAmount = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);

  const handleSubmitOrder = async () => {
    if (cart.length === 0) {
      message.error("Chưa chọn món nào!");
      return;
    }

    try {
      message.loading({ content: "Đang gửi đơn xuống bếp...", key: 'order_msg' });

      // CHUẨN BỊ DỮ LIỆU ĐỂ GỬI (Khớp với DTO OrderRequest bên Java)
      const payload = {
        tableId: id, // ID bàn lấy từ URL
        items: cart.map(item => ({
          productId: item.id,
          quantity: item.quantity
        }))
      };

      // GỌI API
      await axiosClient.post('/staff/orders', payload);

      message.success({ content: "Gửi đơn thành công!", key: 'order_msg' });

      // Quay về trang sơ đồ bàn
      navigate('/staff/tables');

    } catch (error) {
      console.error(error);
      message.error({ content: "Lỗi gửi đơn: " + error.message, key: 'order_msg' });
    }
  };

  return (
      <div style={{ height: '85vh' }}>
        <Row gutter={16} style={{ height: '100%' }}>

          {/* CỘT TRÁI: MENU */}
          <Col span={16} style={{ height: '100%', overflowY: 'auto' }}>
            <div style={{ display: 'flex', alignItems: 'center', marginBottom: 15 }}>
              <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/staff/tables')} style={{ marginRight: 10 }} />
              <Title level={4} style={{ margin: 0 }}>Gọi món - Bàn {id}</Title>
            </div>

            {/* Hiển thị Loading khi đang tải */}
            {loading ? (
                <div style={{ textAlign: 'center', marginTop: 50 }}><Spin size="large" tip="Đang tải thực đơn..." /></div>
            ) : (
                <>
                  {/* THANH TAB DANH MỤC */}
                  <Tabs
                      defaultActiveKey="Tất cả"
                      items={categories.map(c => ({ label: c, key: c }))}
                      onChange={setActiveCategory}
                      type="card"
                      style={{ marginBottom: 15 }}
                  />

                  {/* LƯỚI MÓN ĂN */}
                  <Row gutter={[16, 16]}>
                    {filteredMenu.map(item => (
                        <Col xs={12} sm={8} md={8} lg={6} key={item.id}>
                          <Card
                              hoverable
                              // Xử lý ảnh: Nếu null thì dùng ảnh placeholder
                              cover={
                                <div style={{ position: 'relative' }}>
                                  <img
                                      alt={item.name}
                                      src={item.image || "https://placehold.co/300x200?text=No+Image"}
                                      style={{ height: 150, width: '100%', objectFit: 'cover', filter: item.available === false ? 'grayscale(100%)' : 'none' }}
                                  />
                                  {/* Nhãn hết hàng nếu available = false */}
                                  {item.available === false && (
                                      <div style={{position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', color: 'white', display: 'flex', justifyContent: 'center', alignItems: 'center', fontWeight: 'bold'}}>HẾT HÀNG</div>
                                  )}
                                </div>
                              }
                              onClick={() => addToCart(item)}
                              style={{ opacity: item.available === false ? 0.6 : 1 }}
                          >
                            <Meta
                                title={<div style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{item.name}</div>}
                                description={<b style={{ color: '#d4380d' }}>{item.price?.toLocaleString()}đ</b>}
                            />
                          </Card>
                        </Col>
                    ))}
                  </Row>
                </>
            )}
          </Col>

          {/* CỘT PHẢI: GIỎ HÀNG */}
          <Col span={8} style={{ height: '100%' }}>
            <Card
                title={<span><ShoppingCartOutlined /> Giỏ hàng (Bàn {id})</span>}
                style={{ height: '100%', display: 'flex', flexDirection: 'column' }}
                bodyStyle={{ flex: 1, overflowY: 'auto' }}
            >
              <List
                  itemLayout="horizontal"
                  dataSource={cart}
                  renderItem={(item) => (
                      <List.Item>
                        <List.Item.Meta
                            title={item.name}
                            description={`${item.price?.toLocaleString()}đ`}
                        />
                        <InputNumber
                            min={0}
                            value={item.quantity}
                            onChange={(val) => updateQuantity(item.id, val)}
                        />
                      </List.Item>
                  )}
              />

              <Divider />

              <div style={{ marginTop: 'auto' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 15, fontSize: 18, fontWeight: 'bold' }}>
                  <span>Tổng cộng:</span>
                  <span style={{ color: '#d4380d' }}>{totalAmount.toLocaleString()}đ</span>
                </div>
                <Button
                    type="primary"
                    size="large"
                    block
                    icon={<SendOutlined />}
                    onClick={handleSubmitOrder}
                    style={{ height: 50, fontSize: 18 }}
                >
                  GỬI BẾP
                </Button>
              </div>
            </Card>
          </Col>
        </Row>
      </div>
  );
};

export default CreateOrder;