import { useCallback, useEffect, useState } from 'react';
import {
  Button,
  Card,
  Col,
  Form,
  Input,
  InputNumber,
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
import { PlusOutlined, SearchOutlined } from '@ant-design/icons';
import PageHeader from '@/components/common/PageHeader';
import StatusTag from '@/components/common/StatusTag';
import { shipperApi, userApi } from '@/api/services';
import { useAuthStore } from '@/store/authStore';
import { PERMISSION, SHIPPER_STATUS_COLOR } from '@/constants/permissions';
import { formatDateTime } from '@/utils/format';
import type { Shipper, User } from '@/types';

const VEHICLE_OPTIONS = [
  { value: 'MOTORBIKE', label: 'Xe máy (30kg)' },
  { value: 'CAR', label: 'Ô tô (200kg)' },
  { value: 'VAN', label: 'Xe tải van (800kg)' },
  { value: 'TRUCK', label: 'Xe tải (3000kg)' },
];

const STATUS_OPTIONS = [
  { value: 'ONLINE', label: 'Sẵn sàng nhận đơn' },
  { value: 'BUSY', label: 'Đang giao hàng' },
  { value: 'OFFLINE', label: 'Ngoại tuyến' },
  { value: 'SUSPENDED', label: 'Tạm khóa' },
];

const ShipperPage = () => {
  const { hasPermission } = useAuthStore();
  const [searchForm] = Form.useForm();
  const [modalForm] = Form.useForm();
  const [data, setData] = useState<Shipper[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState<Record<string, unknown>>({ page: 0, size: 10 });
  const [editing, setEditing] = useState<Shipper | null>(null);
  const [creating, setCreating] = useState(false);
  const [shipperCandidates, setShipperCandidates] = useState<User[]>([]);

  const load = useCallback(async (request: Record<string, unknown>) => {
    setLoading(true);
    try {
      const page = await shipperApi.search(request);
      setData(page.content);
      setTotal(page.totalElements);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load(filters);
  }, [filters, load]);

  const openCreate = async () => {
    modalForm.resetFields();
    setCreating(true);
    if (hasPermission(PERMISSION.USER_VIEW)) {
      const page = await userApi.search({ roleGroupCode: 'SHIPPER', size: 100 });
      setShipperCandidates(page.content);
    }
  };

  const handleCreate = async (values: Record<string, unknown>) => {
    await shipperApi.create(values);
    message.success('Đã tạo hồ sơ shipper');
    setCreating(false);
    void load(filters);
  };

  const handleUpdate = async (values: Record<string, unknown>) => {
    if (!editing) {
      return;
    }
    await shipperApi.update(editing.id, values);
    message.success('Đã cập nhật hồ sơ shipper');
    setEditing(null);
    void load(filters);
  };

  const columns: ColumnsType<Shipper> = [
    { title: 'Mã shipper', dataIndex: 'shipperCode', width: 120, fixed: 'left' },
    { title: 'Họ tên', dataIndex: 'fullName', width: 170 },
    { title: 'Điện thoại', dataIndex: 'phoneNumber', width: 120 },
    { title: 'Phương tiện', dataIndex: 'vehicleType', width: 130, render: (value) => <StatusTag value={value} /> },
    { title: 'Biển số', dataIndex: 'licensePlate', width: 120 },
    { title: 'Khu vực', dataIndex: 'zone', width: 130 },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      width: 160,
      render: (value) => <StatusTag value={value} colorMap={SHIPPER_STATUS_COLOR} />,
    },
    {
      title: 'Tải hiện tại',
      width: 110,
      align: 'center',
      render: (_, record) => (
        <Tag color={record.currentLoad >= record.maxConcurrentOrders ? 'red' : 'green'}>
          {record.currentLoad}/{record.maxConcurrentOrders}
        </Tag>
      ),
    },
    { title: 'Đã giao', dataIndex: 'totalDelivered', width: 90, align: 'right' },
    { title: 'Thất bại', dataIndex: 'totalFailed', width: 90, align: 'right' },
    {
      title: 'Đánh giá',
      dataIndex: 'rating',
      width: 100,
      align: 'right',
      render: (value: number) => `${value ?? 0}/5`,
    },
    {
      title: 'Vị trí cuối',
      dataIndex: 'lastLocationAt',
      width: 150,
      render: (value: string) => formatDateTime(value),
    },
    {
      title: 'Thao tác',
      width: 170,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          {hasPermission(PERMISSION.SHIPPER_UPDATE) && (
            <Button
              size="small"
              onClick={() => {
                setEditing(record);
                modalForm.setFieldsValue({
                  vehicleType: record.vehicleType.code,
                  licensePlate: record.licensePlate,
                  maxLoadKg: record.maxLoadKg,
                  zone: record.zone,
                  status: record.status.code,
                  maxConcurrentOrders: record.maxConcurrentOrders,
                  note: record.note,
                });
              }}
            >
              Sửa
            </Button>
          )}
          {hasPermission(PERMISSION.SHIPPER_DELETE) && (
            <Popconfirm
              title="Xóa hồ sơ shipper này?"
              okText="Xóa"
              cancelText="Đóng"
              onConfirm={async () => {
                await shipperApi.remove(record.id);
                message.success('Đã xóa hồ sơ shipper');
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

  return (
    <div>
      <PageHeader
        title="Quản lý shipper"
        subtitle="Hồ sơ, phương tiện, khu vực hoạt động và hiệu suất giao hàng"
        extra={
          hasPermission(PERMISSION.SHIPPER_CREATE) && (
            <Button type="primary" icon={<PlusOutlined />} onClick={() => void openCreate()}>
              Tạo hồ sơ shipper
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
                <Input placeholder="Mã shipper, họ tên, SĐT, biển số" allowClear />
              </Form.Item>
            </Col>
            <Col xs={24} md={5}>
              <Form.Item name="status" label="Trạng thái">
                <Select allowClear options={STATUS_OPTIONS} placeholder="Tất cả" />
              </Form.Item>
            </Col>
            <Col xs={24} md={5}>
              <Form.Item name="vehicleType" label="Phương tiện">
                <Select allowClear options={VEHICLE_OPTIONS} placeholder="Tất cả" />
              </Form.Item>
            </Col>
            <Col xs={24} md={6}>
              <Form.Item name="zone" label="Khu vực">
                <Input placeholder="Ví dụ: Cầu Giấy" allowClear />
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
        <Table<Shipper>
          rowKey="id"
          columns={columns}
          dataSource={data}
          loading={loading}
          scroll={{ x: 1800 }}
          pagination={{
            current: Number(filters.page ?? 0) + 1,
            pageSize: Number(filters.size ?? 10),
            total,
            showTotal: (value) => `Tổng ${value} shipper`,
            onChange: (page, pageSize) => setFilters({ ...filters, page: page - 1, size: pageSize }),
          }}
        />
      </Card>

      <Modal
        open={creating}
        title="Tạo hồ sơ shipper"
        onCancel={() => setCreating(false)}
        onOk={() => modalForm.submit()}
        okText="Tạo mới"
        cancelText="Đóng"
      >
        <Form form={modalForm} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="userId" label="Tài khoản người dùng" rules={[{ required: true, message: 'Bắt buộc' }]}>
            <Select
              placeholder="Chọn tài khoản thuộc nhóm quyền SHIPPER"
              options={shipperCandidates.map((user) => ({
                value: user.id,
                label: `${user.username} - ${user.fullName}`,
              }))}
            />
          </Form.Item>
          <Form.Item name="vehicleType" label="Phương tiện" rules={[{ required: true, message: 'Bắt buộc' }]}>
            <Select options={VEHICLE_OPTIONS} />
          </Form.Item>
          <Form.Item name="licensePlate" label="Biển số">
            <Input placeholder="29H1-12345" />
          </Form.Item>
          <Form.Item name="maxLoadKg" label="Tải trọng tối đa (kg)">
            <InputNumber className="w-full" min={0.1} />
          </Form.Item>
          <Form.Item name="zone" label="Khu vực hoạt động">
            <Input placeholder="Cầu Giấy" />
          </Form.Item>
          <Form.Item name="maxConcurrentOrders" label="Số đơn nhận cùng lúc" initialValue={5}>
            <InputNumber className="w-full" min={1} max={20} />
          </Form.Item>
          <Form.Item name="note" label="Ghi chú">
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        open={Boolean(editing)}
        title={`Cập nhật hồ sơ ${editing?.shipperCode ?? ''}`}
        onCancel={() => setEditing(null)}
        onOk={() => modalForm.submit()}
        okText="Lưu"
        cancelText="Đóng"
      >
        <Form form={modalForm} layout="vertical" onFinish={handleUpdate}>
          <Form.Item name="vehicleType" label="Phương tiện">
            <Select options={VEHICLE_OPTIONS} />
          </Form.Item>
          <Form.Item name="licensePlate" label="Biển số">
            <Input />
          </Form.Item>
          <Form.Item name="maxLoadKg" label="Tải trọng tối đa (kg)">
            <InputNumber className="w-full" min={0.1} />
          </Form.Item>
          <Form.Item name="zone" label="Khu vực hoạt động">
            <Input />
          </Form.Item>
          <Form.Item name="status" label="Trạng thái">
            <Select options={STATUS_OPTIONS} />
          </Form.Item>
          <Form.Item name="maxConcurrentOrders" label="Số đơn nhận cùng lúc">
            <InputNumber className="w-full" min={1} max={20} />
          </Form.Item>
          <Form.Item name="note" label="Ghi chú">
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default ShipperPage;
