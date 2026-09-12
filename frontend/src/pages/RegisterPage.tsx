import { useState } from 'react';
import { Button, Card, Form, Input, Typography, message } from 'antd';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '@/api/services';

const RegisterPage = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (values: Record<string, unknown>) => {
    setLoading(true);
    try {
      await authApi.register(values);
      message.success('Đăng ký thành công, vui lòng đăng nhập');
      navigate('/login');
    } catch {
      // Interceptor da hien thi loi
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-gradient-to-br from-red-50 to-slate-100">
      <Card style={{ width: 460 }} className="shadow-lg">
        <Typography.Title level={4} className="text-center">
          Đăng ký tài khoản khách hàng
        </Typography.Title>

        <Form layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            name="username"
            label="Tên đăng nhập"
            rules={[
              { required: true, message: 'Vui lòng nhập tên đăng nhập' },
              { min: 4, message: 'Tên đăng nhập tối thiểu 4 ký tự' },
              { pattern: /^[a-zA-Z0-9._-]+$/, message: 'Chỉ gồm chữ, số và các ký tự . _ -' },
            ]}
          >
            <Input placeholder="nguyenvana" />
          </Form.Item>
          <Form.Item
            name="password"
            label="Mật khẩu"
            rules={[
              { required: true, message: 'Vui lòng nhập mật khẩu' },
              { min: 6, message: 'Mật khẩu tối thiểu 6 ký tự' },
            ]}
          >
            <Input.Password />
          </Form.Item>
          <Form.Item name="fullName" label="Họ và tên" rules={[{ required: true, message: 'Vui lòng nhập họ tên' }]}>
            <Input placeholder="Nguyễn Văn A" />
          </Form.Item>
          <Form.Item
            name="phoneNumber"
            label="Số điện thoại"
            rules={[
              { required: true, message: 'Vui lòng nhập số điện thoại' },
              { pattern: /^0\d{9,10}$/, message: 'Số điện thoại không đúng định dạng' },
            ]}
          >
            <Input placeholder="0912345678" />
          </Form.Item>
          <Form.Item name="email" label="Email" rules={[{ type: 'email', message: 'Email không đúng định dạng' }]}>
            <Input placeholder="email@example.com" />
          </Form.Item>
          <Form.Item name="address" label="Địa chỉ">
            <Input placeholder="Số nhà, đường, quận/huyện, tỉnh/thành" />
          </Form.Item>

          <Button type="primary" htmlType="submit" block loading={loading}>
            Đăng ký
          </Button>
        </Form>

        <div className="text-center mt-4 text-sm">
          Đã có tài khoản? <Link to="/login">Đăng nhập</Link>
        </div>
      </Card>
    </div>
  );
};

export default RegisterPage;
