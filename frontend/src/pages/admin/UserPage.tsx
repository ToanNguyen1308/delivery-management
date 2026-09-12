import { useCallback, useEffect, useState } from 'react';
import {
  Button,
  Card,
  Col,
  Form,
  Input,
  Modal,
  Popconfirm,
  Row,
  Select,
  Space,
  Table,
  Tag,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { KeyOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons';
import PageHeader from '@/components/common/PageHeader';
import StatusTag from '@/components/common/StatusTag';
import { roleApi, userApi } from '@/api/services';
import { useAuthStore } from '@/store/authStore';
import { PERMISSION } from '@/constants/permissions';
import { formatDateTime } from '@/utils/format';
import type { RoleGroup, User } from '@/types';

const USER_STATUS_COLOR: Record<string, string> = {
  ACTIVE: 'success',
  INACTIVE: 'default',
  LOCKED: 'error',
};

const UserPage = () => {
  const { hasPermission } = useAuthStore();
  const [searchForm] = Form.useForm();
  const [modalForm] = Form.useForm();
  const [data, setData] = useState<User[]>([]);
  const [roleGroups, setRoleGroups] = useState<RoleGroup[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState<Record<string, unknown>>({ page: 0, size: 10 });
  const [modalMode, setModalMode] = useState<'create' | 'edit' | null>(null);
  const [editingUser, setEditingUser] = useState<User | null>(null);

  const load = useCallback(async (request: Record<string, unknown>) => {
    setLoading(true);
    try {
      const page = await userApi.search(request);
      setData(page.content);
      setTotal(page.totalElements);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load(filters);
  }, [filters, load]);

  useEffect(() => {
    if (hasPermission(PERMISSION.ROLE_VIEW)) {
      void roleApi.list().then(setRoleGroups);
    }
  }, [hasPermission]);

  const handleSubmit = async (values: Record<string, unknown>) => {
    if (modalMode === 'create') {
      await userApi.create(values);
      message.success('Đã tạo người dùng');
    } else if (editingUser) {
      await userApi.update(editingUser.id, values);
      message.success('Đã cập nhật người dùng');
    }
    setModalMode(null);
    setEditingUser(null);
    void load(filters);
  };

  const handleResetPassword = (user: User) => {
    let newPassword = '';
    Modal.confirm({
      title: `Đặt lại mật khẩu cho ${user.username}`,
      content: (
        <Input.Password
          placeholder="Mật khẩu mới (tối thiểu 6 ký tự)"
          onChange={(event) => {
            newPassword = event.target.value;
          }}
        />
      ),
      okText: 'Đặt lại',
      cancelText: 'Đóng',
      onOk: async () => {
        if (newPassword.length < 6) {
          message.error('Mật khẩu tối thiểu 6 ký tự');
          return Promise.reject(new Error('invalid password'));
        }
        await userApi.resetPassword(user.id, newPassword);
        message.success('Đã đặt lại mật khẩu');
        return undefined;
      },
    });
  };

  const columns: ColumnsType<User> = [
    { title: 'Tên đăng nhập', dataIndex: 'username', width: 140, fixed: 'left' },
    { title: 'Họ tên', dataIndex: 'fullName', width: 180 },
    { title: 'Email', dataIndex: 'email', width: 200 },
    { title: 'Điện thoại', dataIndex: 'phoneNumber', width: 130 },
    {
      title: 'Nhóm quyền',
      dataIndex: 'roleGroups',
      width: 200,
      render: (values: User['roleGroups']) => (
        <Space wrap size={4}>
          {(values ?? []).map((role) => (
            <Tag key={role.id} color="red">
              {role.roleGroupName}
            </Tag>
          ))}
        </Space>
      ),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      width: 140,
      render: (value) => <StatusTag value={value} colorMap={USER_STATUS_COLOR} />,
    },
    {
      title: 'Đăng nhập lần cuối',
      dataIndex: 'lastLoginAt',
      width: 160,
      render: (value: string) => formatDateTime(value),
    },
    {
      title: 'Thao tác',
      width: 230,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          {hasPermission(PERMISSION.USER_UPDATE) && (
            <>
              <Button
                size="small"
                onClick={() => {
                  setEditingUser(record);
                  setModalMode('edit');
                  modalForm.setFieldsValue({
                    fullName: record.fullName,
                    email: record.email,
                    phoneNumber: record.phoneNumber,
                    identityNumber: record.identityNumber,
                    address: record.address,
                    status: record.status.code,
                    roleGroupIds: (record.roleGroups ?? []).map((role) => role.id),
                  });
                }}
              >
                Sửa
              </Button>
              <Button size="small" icon={<KeyOutlined />} onClick={() => handleResetPassword(record)} />
            </>
          )}
          {hasPermission(PERMISSION.USER_DELETE) && (
            <Popconfirm
              title="Xóa người dùng này?"
              okText="Xóa"
              cancelText="Đóng"
              onConfirm={async () => {
                await userApi.remove(record.id);
                message.success('Đã xóa người dùng');
                void load(filters);
              }}
            >
              <Button size="small" danger>
                Xóa
              </Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  const roleOptions = roleGroups.map((role) => ({ value: role.id, label: role.roleGroupName }));

  return (
    <div>
      <PageHeader
        title="Quản lý người dùng"
        subtitle="Tạo tài khoản và gán nhóm quyền cho từng vai trò trong hệ thống"
        extra={
          hasPermission(PERMISSION.USER_CREATE) && (
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => {
                modalForm.resetFields();
                setModalMode('create');
              }}
            >
              Tạo người dùng
            </Button>
          )
        }
      />

      <Card className="mb-4">
        <Form
          form={searchForm}
          layout="vertical"
          onFinish={(values) => setFilters({ ...filters, ...values, page: 0 })}
        >
          <Row gutter={12}>
            <Col xs={24} md={8}>
              <Form.Item name="keyword" label="Từ khóa">
                <Input placeholder="Tên đăng nhập, họ tên, email, SĐT" allowClear />
              </Form.Item>
            </Col>
            <Col xs={24} md={6}>
              <Form.Item name="roleGroupCode" label="Nhóm quyền">
                <Select
                  allowClear
                  placeholder="Tất cả"
                  options={roleGroups.map((role) => ({ value: role.roleGroupCode, label: role.roleGroupName }))}
                />
              </Form.Item>
            </Col>
            <Col xs={24} md={6}>
              <Form.Item name="status" label="Trạng thái">
                <Select
                  allowClear
                  placeholder="Tất cả"
                  options={[
                    { value: 'ACTIVE', label: 'Đang hoạt động' },
                    { value: 'INACTIVE', label: 'Ngừng hoạt động' },
                    { value: 'LOCKED', label: 'Bị khóa' },
                  ]}
                />
              </Form.Item>
            </Col>
          </Row>
          <Space>
            <Button type="primary" icon={<SearchOutlined />} htmlType="submit">
              Tìm kiếm
            </Button>
            <Button
              onClick={() => {
                searchForm.resetFields();
                setFilters({ page: 0, size: 10 });
              }}
            >
              Đặt lại
            </Button>
          </Space>
        </Form>
      </Card>

      <Card>
        <Table<User>
          rowKey="id"
          columns={columns}
          dataSource={data}
          loading={loading}
          scroll={{ x: 1500 }}
          pagination={{
            current: Number(filters.page ?? 0) + 1,
            pageSize: Number(filters.size ?? 10),
            total,
            showTotal: (value) => `Tổng ${value} người dùng`,
            onChange: (page, pageSize) => setFilters({ ...filters, page: page - 1, size: pageSize }),
          }}
        />
      </Card>

      <Modal
        open={Boolean(modalMode)}
        title={modalMode === 'create' ? 'Tạo người dùng' : `Cập nhật ${editingUser?.username ?? ''}`}
        onCancel={() => {
          setModalMode(null);
          setEditingUser(null);
        }}
        onOk={() => modalForm.submit()}
        okText="Lưu"
        cancelText="Đóng"
      >
        <Form form={modalForm} layout="vertical" onFinish={handleSubmit}>
          {modalMode === 'create' && (
            <>
              <Form.Item
                name="username"
                label="Tên đăng nhập"
                rules={[{ required: true, message: 'Bắt buộc' }, { min: 4, message: 'Tối thiểu 4 ký tự' }]}
              >
                <Input />
              </Form.Item>
              <Form.Item
                name="password"
                label="Mật khẩu"
                rules={[{ required: true, message: 'Bắt buộc' }, { min: 6, message: 'Tối thiểu 6 ký tự' }]}
              >
                <Input.Password />
              </Form.Item>
            </>
          )}
          <Form.Item name="fullName" label="Họ và tên" rules={[{ required: true, message: 'Bắt buộc' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="email" label="Email" rules={[{ type: 'email', message: 'Email không đúng định dạng' }]}>
            <Input />
          </Form.Item>
          <Form.Item
            name="phoneNumber"
            label="Số điện thoại"
            rules={[{ pattern: /^0\d{9,10}$/, message: 'Số điện thoại không đúng định dạng' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item name="identityNumber" label="Số CCCD">
            <Input />
          </Form.Item>
          <Form.Item name="address" label="Địa chỉ">
            <Input />
          </Form.Item>
          {modalMode === 'edit' && (
            <Form.Item name="status" label="Trạng thái">
              <Select
                options={[
                  { value: 'ACTIVE', label: 'Đang hoạt động' },
                  { value: 'INACTIVE', label: 'Ngừng hoạt động' },
                  { value: 'LOCKED', label: 'Bị khóa' },
                ]}
              />
            </Form.Item>
          )}
          <Form.Item
            name="roleGroupIds"
            label="Nhóm quyền"
            rules={modalMode === 'create' ? [{ required: true, message: 'Chọn ít nhất một nhóm quyền' }] : []}
          >
            <Select mode="multiple" options={roleOptions} placeholder="Chọn nhóm quyền" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default UserPage;
