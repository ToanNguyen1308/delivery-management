import { useCallback, useEffect, useState } from 'react';
import {
  Button,
  Card,
  Col,
  DatePicker,
  Form,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Progress,
  Row,
  Select,
  Space,
  Table,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { PlusOutlined, SearchOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import PageHeader from '@/components/common/PageHeader';
import StatusTag from '@/components/common/StatusTag';
import { voucherApi } from '@/api/services';
import { useAuthStore } from '@/store/authStore';
import { PERMISSION, VOUCHER_STATUS_COLOR } from '@/constants/permissions';
import { formatDateTime, formatMoney } from '@/utils/format';
import type { Voucher } from '@/types';

const DISCOUNT_TYPE_OPTIONS = [
  { value: 'PERCENT', label: 'Giảm theo phần trăm' },
  { value: 'FIXED', label: 'Giảm số tiền cố định' },
  { value: 'FREE_SHIP', label: 'Miễn phí vận chuyển' },
];

const VoucherPage = () => {
  const canManage = useAuthStore((state) => state.permissions.includes(PERMISSION.VOUCHER_MANAGE));
  const [searchForm] = Form.useForm();
  const [form] = Form.useForm();
  const [data, setData] = useState<Voucher[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState<Record<string, unknown>>({ page: 0, size: 10 });
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Voucher | null>(null);

  const load = useCallback(async (request: Record<string, unknown>) => {
    setLoading(true);
    try {
      const page = await voucherApi.search(request);
      setData(page.content);
      setTotal(page.totalElements);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load(filters);
  }, [filters, load]);

  const handleSubmit = async (values: Record<string, unknown>) => {
    const range = values.validRange as [dayjs.Dayjs, dayjs.Dayjs];
    const payload = {
      ...values,
      validFrom: range[0].format('YYYY-MM-DDTHH:mm:ss'),
      validTo: range[1].format('YYYY-MM-DDTHH:mm:ss'),
    };
    delete (payload as Record<string, unknown>).validRange;

    if (editing) {
      await voucherApi.update(editing.id, payload);
      message.success('Đã cập nhật voucher');
    } else {
      await voucherApi.create(payload);
      message.success('Đã tạo voucher');
    }
    setModalOpen(false);
    setEditing(null);
    void load(filters);
  };

  const columns: ColumnsType<Voucher> = [
    { title: 'Mã', dataIndex: 'code', width: 130, fixed: 'left' },
    { title: 'Tên chương trình', dataIndex: 'name', width: 230 },
    { title: 'Hình thức', dataIndex: 'discountType', width: 180, render: (value) => <StatusTag value={value} /> },
    {
      title: 'Giá trị giảm',
      width: 130,
      align: 'right',
      render: (_, record) =>
        record.discountType.code === 'PERCENT' ? `${record.discountValue}%` : formatMoney(record.discountValue),
    },
    {
      title: 'Đơn tối thiểu',
      dataIndex: 'minOrderAmount',
      width: 140,
      align: 'right',
      render: (value: number) => formatMoney(value),
    },
    {
      title: 'Giảm tối đa',
      dataIndex: 'maxDiscountAmount',
      width: 140,
      align: 'right',
      render: (value?: number) => (value ? formatMoney(value) : 'Không giới hạn'),
    },
    {
      title: 'Đã dùng',
      width: 160,
      render: (_, record) => (
        <Progress
          percent={Math.round((record.usedCount / record.quantity) * 100)}
          format={() => `${record.usedCount}/${record.quantity}`}
          size="small"
        />
      ),
    },
    { title: 'Giới hạn/người', dataIndex: 'usageLimitPerUser', width: 130, align: 'center' },
    { title: 'Hiệu lực từ', dataIndex: 'validFrom', width: 150, render: (value: string) => formatDateTime(value) },
    { title: 'Đến', dataIndex: 'validTo', width: 150, render: (value: string) => formatDateTime(value) },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      width: 150,
      render: (value) => <StatusTag value={value} colorMap={VOUCHER_STATUS_COLOR} />,
    },
    ...(canManage
      ? [
          {
            title: 'Thao tác',
            width: 160,
            fixed: 'right' as const,
            render: (_: unknown, record: Voucher) => (
              <Space>
                <Button
                  size="small"
                  onClick={() => {
                    setEditing(record);
                    setModalOpen(true);
                    form.setFieldsValue({
                      ...record,
                      discountType: record.discountType.code,
                      status: record.status.code,
                      validRange: [dayjs(record.validFrom), dayjs(record.validTo)],
                    });
                  }}
                >
                  Sửa
                </Button>
                <Popconfirm
                  title="Xóa voucher này?"
                  okText="Xóa"
                  cancelText="Đóng"
                  onConfirm={async () => {
                    await voucherApi.remove(record.id);
                    message.success('Đã xóa voucher');
                    void load(filters);
                  }}
                >
                  <Button size="small" danger>
                    Xóa
                  </Button>
                </Popconfirm>
              </Space>
            ),
          },
        ]
      : []),
  ];

  return (
    <div>
      <PageHeader
        title="Quản lý voucher"
        subtitle="Mã giảm giá áp dụng trên cước vận chuyển khi khách hàng tạo đơn"
        extra={
          canManage && (
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => {
                form.resetFields();
                setEditing(null);
                setModalOpen(true);
              }}
            >
              Tạo voucher
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
                <Input placeholder="Mã hoặc tên chương trình" allowClear />
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
                    { value: 'EXPIRED', label: 'Hết hạn' },
                  ]}
                />
              </Form.Item>
            </Col>
            <Col xs={24} md={6}>
              <Form.Item name="discountType" label="Hình thức">
                <Select allowClear placeholder="Tất cả" options={DISCOUNT_TYPE_OPTIONS} />
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
        <Table<Voucher>
          rowKey="id"
          columns={columns}
          dataSource={data}
          loading={loading}
          scroll={{ x: 1900 }}
          pagination={{
            current: Number(filters.page ?? 0) + 1,
            pageSize: Number(filters.size ?? 10),
            total,
            showTotal: (value) => `Tổng ${value} voucher`,
            onChange: (page, pageSize) => setFilters({ ...filters, page: page - 1, size: pageSize }),
          }}
        />
      </Card>

      <Modal
        open={modalOpen}
        title={editing ? `Cập nhật voucher ${editing.code}` : 'Tạo voucher'}
        onCancel={() => {
          setModalOpen(false);
          setEditing(null);
        }}
        onOk={() => form.submit()}
        okText="Lưu"
        cancelText="Đóng"
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit} initialValues={{ usageLimitPerUser: 1 }}>
          <Row gutter={12}>
            <Col xs={24} md={12}>
              <Form.Item
                name="code"
                label="Mã giảm giá"
                rules={[
                  { required: true, message: 'Bắt buộc' },
                  { pattern: /^[A-Z0-9_]+$/, message: 'Chỉ gồm chữ in hoa, số và gạch dưới' },
                ]}
              >
                <Input placeholder="GIAM30K" />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="discountType" label="Hình thức giảm" rules={[{ required: true, message: 'Bắt buộc' }]}>
                <Select options={DISCOUNT_TYPE_OPTIONS} />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="name" label="Tên chương trình" rules={[{ required: true, message: 'Bắt buộc' }]}>
            <Input />
          </Form.Item>
          <Row gutter={12}>
            <Col xs={24} md={12}>
              <Form.Item name="discountValue" label="Giá trị giảm" rules={[{ required: true, message: 'Bắt buộc' }]}>
                <InputNumber className="w-full" min={0} />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="maxDiscountAmount" label="Giảm tối đa (đ)">
                <InputNumber className="w-full" min={0} step={1000} />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="minOrderAmount" label="Giá trị đơn tối thiểu (đ)">
                <InputNumber className="w-full" min={0} step={1000} />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="quantity" label="Số lượng" rules={[{ required: true, message: 'Bắt buộc' }]}>
                <InputNumber className="w-full" min={1} />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="usageLimitPerUser" label="Giới hạn mỗi người">
                <InputNumber className="w-full" min={1} />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="status" label="Trạng thái" initialValue="ACTIVE">
                <Select
                  options={[
                    { value: 'ACTIVE', label: 'Đang hoạt động' },
                    { value: 'INACTIVE', label: 'Ngừng hoạt động' },
                  ]}
                />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="validRange" label="Thời gian hiệu lực" rules={[{ required: true, message: 'Bắt buộc' }]}>
            <DatePicker.RangePicker showTime format="DD/MM/YYYY HH:mm" className="w-full" />
          </Form.Item>
          <Form.Item name="description" label="Mô tả">
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default VoucherPage;
