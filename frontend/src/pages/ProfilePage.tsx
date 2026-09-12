import { useState } from 'react';
import { Button, Card, Col, Descriptions, Form, Input, Row, Space, Tag, message } from 'antd';
import PageHeader from '@/components/common/PageHeader';
import { authApi } from '@/api/services';
import { useAuthStore } from '@/store/authStore';
import { DETAIL_DESCRIPTIONS_COLUMN, DETAIL_DESCRIPTIONS_STYLES } from '@/constants/layout';
import { formatDate, formatDateTime } from '@/utils/format';

const ProfilePage = () => {
  const { user, permissions, roleGroups } = useAuthStore();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  const handleChangePassword = async (values: { oldPassword: string; newPassword: string }) => {
    setLoading(true);
    try {
      await authApi.changePassword(values.oldPassword, values.newPassword);
      message.success('Đã đổi mật khẩu, lần đăng nhập sau vui lòng dùng mật khẩu mới');
      form.resetFields();
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <PageHeader title="Thông tin tài khoản" subtitle="Hồ sơ và quyền hạn của tài khoản đang đăng nhập" />

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={14}>
          <Card title="Hồ sơ" className="mb-4">
            <Descriptions
              column={DETAIL_DESCRIPTIONS_COLUMN}
              styles={DETAIL_DESCRIPTIONS_STYLES}
              size="small"
              bordered
            >
              <Descriptions.Item label="Tên đăng nhập">{user?.username}</Descriptions.Item>
              <Descriptions.Item label="Họ và tên">{user?.fullName}</Descriptions.Item>
              <Descriptions.Item label="Email">{user?.email ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="Số điện thoại">{user?.phoneNumber ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="Số CCCD">{user?.identityNumber ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="Ngày sinh">{formatDate(user?.birthday)}</Descriptions.Item>
              <Descriptions.Item label="Địa chỉ" span="filled">
                {user?.address ?? '-'}
              </Descriptions.Item>
              <Descriptions.Item label="Nhóm quyền" span="filled">
                <Space wrap>
                  {roleGroups.map((role) => (
                    <Tag color="red" key={role}>
                      {role}
                    </Tag>
                  ))}
                </Space>
              </Descriptions.Item>
              <Descriptions.Item label="Đăng nhập lần cuối" span="filled">
                {formatDateTime(user?.lastLoginAt)}
              </Descriptions.Item>
            </Descriptions>
          </Card>

          <Card title={`Chức năng được phép (${permissions.length})`}>
            <Space wrap size={[4, 8]}>
              {permissions.map((permission) => (
                <Tag key={permission}>{permission}</Tag>
              ))}
            </Space>
          </Card>
        </Col>

        <Col xs={24} xl={10}>
          <Card title="Đổi mật khẩu">
            <Form form={form} layout="vertical" onFinish={handleChangePassword}>
              <Form.Item
                name="oldPassword"
                label="Mật khẩu hiện tại"
                rules={[{ required: true, message: 'Bắt buộc' }]}
              >
                <Input.Password />
              </Form.Item>
              <Form.Item
                name="newPassword"
                label="Mật khẩu mới"
                rules={[{ required: true, message: 'Bắt buộc' }, { min: 6, message: 'Tối thiểu 6 ký tự' }]}
              >
                <Input.Password />
              </Form.Item>
              <Form.Item
                name="confirmPassword"
                label="Nhập lại mật khẩu mới"
                dependencies={['newPassword']}
                rules={[
                  { required: true, message: 'Bắt buộc' },
                  ({ getFieldValue }) => ({
                    validator: (_, value) =>
                      !value || getFieldValue('newPassword') === value
                        ? Promise.resolve()
                        : Promise.reject(new Error('Mật khẩu nhập lại không khớp')),
                  }),
                ]}
              >
                <Input.Password />
              </Form.Item>
              <Button type="primary" htmlType="submit" block loading={loading}>
                Đổi mật khẩu
              </Button>
            </Form>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default ProfilePage;
