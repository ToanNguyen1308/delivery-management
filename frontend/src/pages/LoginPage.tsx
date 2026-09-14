import { useState } from 'react';
import { Button, Card, Divider, Form, Input, Space, Typography, message } from 'antd';
import { CarOutlined, LockOutlined, UserOutlined } from '@ant-design/icons';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';

interface DemoAccount {
  username: string;
  password: string;
  label: string;
}

const DEMO_ACCOUNTS: DemoAccount[] = [
  { username: 'admin', password: 'Admin@123', label: 'Quản trị' },
  { username: 'dispatcher', password: 'Dispatch@123', label: 'Điều phối' },
  { username: 'shipper01', password: 'Shipper@123', label: 'Shipper' },
  { username: 'customer01', password: 'Customer@123', label: 'Khách hàng' },
];

const LoginPage = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const login = useAuthStore((state) => state.login);

  const handleSubmit = async (values: { username: string; password: string }) => {
    setLoading(true);
    try {
      await login(values.username, values.password);
      message.success('Đăng nhập thành công');
      navigate('/dashboard');
    } catch {
      // interceptor đã hiện toast
    } finally {
      setLoading(false);
    }
  };

  const fillAccount = (account: DemoAccount) => {
    form.setFieldsValue({ username: account.username, password: account.password });
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-gradient-to-br from-red-50 to-slate-100">
      <Card style={{ width: 420 }} className="shadow-lg">
        <div className="text-center mb-6">
          <CarOutlined style={{ fontSize: 40, color: '#d32f2f' }} />
          <Typography.Title level={3} style={{ marginTop: 12, marginBottom: 0 }}>
            Hệ thống quản lý giao hàng
          </Typography.Title>
          <Typography.Text type="secondary">Đăng nhập để tiếp tục</Typography.Text>
        </div>

        <Form form={form} layout="vertical" onFinish={handleSubmit} size="large">
          <Form.Item
            name="username"
            label="Tên đăng nhập"
            rules={[{ required: true, message: 'Vui lòng nhập tên đăng nhập' }]}
          >
            <Input prefix={<UserOutlined />} placeholder="admin" autoComplete="username" />
          </Form.Item>
          <Form.Item
            name="password"
            label="Mật khẩu"
            rules={[{ required: true, message: 'Vui lòng nhập mật khẩu' }]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="••••••••" autoComplete="current-password" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block loading={loading}>
            Đăng nhập
          </Button>
        </Form>

        <Divider plain style={{ fontSize: 12 }}>
          Tài khoản demo (bấm để điền nhanh)
        </Divider>
        <Space wrap className="justify-center w-full">
          {DEMO_ACCOUNTS.map((account) => (
            <Button key={account.username} size="small" onClick={() => fillAccount(account)}>
              {account.label}
            </Button>
          ))}
        </Space>

        <div className="text-center mt-4 text-sm">
          <Link to="/register">Đăng ký tài khoản khách hàng</Link>
          <span className="mx-2 text-slate-300">|</span>
          <Link to="/tracking">Tra cứu vận đơn</Link>
        </div>
      </Card>
    </div>
  );
};

export default LoginPage;
