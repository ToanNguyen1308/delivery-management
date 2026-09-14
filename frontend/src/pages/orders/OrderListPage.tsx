import { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Button,
  Card,
  Col,
  DatePicker,
  Form,
  Input,
  Modal,
  Row,
  Select,
  Space,
  Table,
  Upload,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  DownloadOutlined,
  FileExcelOutlined,
  ReloadOutlined,
  SearchOutlined,
  UploadOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import dayjs, { Dayjs } from 'dayjs';
import PageHeader from '@/components/common/PageHeader';
import StatusTag from '@/components/common/StatusTag';
import { orderApi } from '@/api/services';
import { useAuthStore } from '@/store/authStore';
import { ORDER_STATUS_COLOR, PAYMENT_STATUS_COLOR, PERMISSION } from '@/constants/permissions';
import { downloadBlob, formatDateTime, formatMoney } from '@/utils/format';
import type { ExcelImportResult, OrderSearchRequest, OrderSummary } from '@/types';

const ORDER_STATUS_OPTIONS = [
  { value: 'CREATED', label: 'Chờ xác nhận' },
  { value: 'CONFIRMED', label: 'Đã xác nhận' },
  { value: 'ASSIGNED', label: 'Đã phân công shipper' },
  { value: 'PICKED_UP', label: 'Đã lấy hàng' },
  { value: 'IN_TRANSIT', label: 'Đang vận chuyển' },
  { value: 'DELIVERED', label: 'Giao thành công' },
  { value: 'FAILED', label: 'Giao thất bại' },
  { value: 'RETURNED', label: 'Đã hoàn trả' },
  { value: 'CANCELLED', label: 'Đã hủy' },
];

const SERVICE_TYPE_OPTIONS = [
  { value: 'STANDARD', label: 'Giao tiêu chuẩn' },
  { value: 'EXPRESS', label: 'Giao nhanh' },
  { value: 'SAME_DAY', label: 'Giao trong ngày' },
];

const OrderListPage = () => {
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const { hasPermission } = useAuthStore();
  const [data, setData] = useState<OrderSummary[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState<OrderSearchRequest>({ page: 0, size: 10 });
  const [importResult, setImportResult] = useState<ExcelImportResult | null>(null);

  const load = useCallback(async (request: OrderSearchRequest) => {
    setLoading(true);
    try {
      const page = await orderApi.search(request);
      setData(page.content);
      setTotal(page.totalElements);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load(filters);
  }, [filters, load]);

  const handleSearch = (values: Record<string, unknown>) => {
    const dateRange = values.dateRange as [Dayjs, Dayjs] | undefined;
    setFilters({
      ...filters,
      page: 0,
      keyword: values.keyword as string,
      statuses: values.statuses as string[],
      serviceType: values.serviceType as string,
      paymentStatus: values.paymentStatus as string,
      fromDate: dateRange?.[0]?.format('YYYY-MM-DD'),
      toDate: dateRange?.[1]?.format('YYYY-MM-DD'),
    });
  };

  const handleExport = async () => {
    const blob = await orderApi.exportExcel({ ...filters, page: 0, size: 200 });
    downloadBlob(blob, `danh-sach-don-hang-${dayjs().format('YYYYMMDD-HHmm')}.xlsx`);
    message.success('Đã xuất file Excel');
  };

  const handleDownloadTemplate = async () => {
    const blob = await orderApi.downloadTemplate();
    downloadBlob(blob, 'mau-import-don-hang.xlsx');
  };

  const handleImport = async (file: File) => {
    try {
      const result = await orderApi.importExcel(file);
      setImportResult(result);
      void load(filters);
    } catch {
      // interceptor đã hiện toast
    }
    return false;
  };

  const columns: ColumnsType<OrderSummary> = [
    {
      title: 'Mã vận đơn',
      dataIndex: 'orderCode',
      width: 150,
      fixed: 'left',
      render: (value: string, record) => (
        <Button type="link" style={{ padding: 0 }} onClick={() => navigate(`/orders/${record.id}`)}>
          {value}
        </Button>
      ),
    },
    { title: 'Người nhận', dataIndex: 'receiverName', width: 160 },
    { title: 'Điện thoại', dataIndex: 'receiverPhone', width: 120 },
    { title: 'Địa chỉ giao', dataIndex: 'deliveryAddress', ellipsis: true, width: 240 },
    {
      title: 'Dịch vụ',
      dataIndex: 'serviceType',
      width: 200,
      render: (value) => <StatusTag value={value} />,
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      width: 170,
      render: (value) => <StatusTag value={value} colorMap={ORDER_STATUS_COLOR} />,
    },
    {
      title: 'Thanh toán',
      dataIndex: 'paymentStatus',
      width: 170,
      render: (value) => <StatusTag value={value} colorMap={PAYMENT_STATUS_COLOR} />,
    },
    {
      title: 'Cước',
      dataIndex: 'shippingFee',
      width: 120,
      align: 'right',
      render: (value: number) => formatMoney(value),
    },
    {
      title: 'Thu hộ',
      dataIndex: 'codAmount',
      width: 130,
      align: 'right',
      render: (value: number) => formatMoney(value),
    },
    { title: 'Shipper', dataIndex: 'shipperName', width: 150, render: (value?: string) => value ?? '-' },
    {
      title: 'Ngày tạo',
      dataIndex: 'createdDate',
      width: 150,
      render: (value: string) => formatDateTime(value),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Quản lý đơn hàng"
        subtitle="Danh sách tự động giới hạn theo quyền của tài khoản đang đăng nhập"
        extra={
          <Space wrap>
            {hasPermission(PERMISSION.ORDER_IMPORT) && (
              <>
                <Button icon={<DownloadOutlined />} onClick={handleDownloadTemplate}>
                  Tải file mẫu
                </Button>
                <Upload beforeUpload={handleImport} showUploadList={false} accept=".xlsx">
                  <Button icon={<UploadOutlined />}>Import Excel</Button>
                </Upload>
              </>
            )}
            {hasPermission(PERMISSION.ORDER_EXPORT) && (
              <Button icon={<FileExcelOutlined />} onClick={handleExport}>
                Xuất Excel
              </Button>
            )}
            {hasPermission(PERMISSION.ORDER_CREATE) && (
              <Button type="primary" onClick={() => navigate('/orders/create')}>
                Tạo đơn hàng
              </Button>
            )}
          </Space>
        }
      />

      <Card className="mb-4">
        <Form form={form} layout="vertical" onFinish={handleSearch}>
          <Row gutter={12}>
            <Col xs={24} md={6}>
              <Form.Item name="keyword" label="Từ khóa">
                <Input placeholder="Mã vận đơn, tên hoặc SĐT người nhận" allowClear />
              </Form.Item>
            </Col>
            <Col xs={24} md={6}>
              <Form.Item name="statuses" label="Trạng thái">
                <Select mode="multiple" allowClear options={ORDER_STATUS_OPTIONS} placeholder="Tất cả" />
              </Form.Item>
            </Col>
            <Col xs={24} md={4}>
              <Form.Item name="serviceType" label="Dịch vụ">
                <Select allowClear options={SERVICE_TYPE_OPTIONS} placeholder="Tất cả" />
              </Form.Item>
            </Col>
            <Col xs={24} md={4}>
              <Form.Item name="paymentStatus" label="Thanh toán">
                <Select
                  allowClear
                  placeholder="Tất cả"
                  options={[
                    { value: 'UNPAID', label: 'Chưa thanh toán' },
                    { value: 'PENDING', label: 'Đang chờ' },
                    { value: 'PAID', label: 'Đã thanh toán' },
                    { value: 'FAILED', label: 'Thất bại' },
                  ]}
                />
              </Form.Item>
            </Col>
            <Col xs={24} md={4}>
              <Form.Item name="dateRange" label="Khoảng ngày tạo">
                <DatePicker.RangePicker format="DD/MM/YYYY" className="w-full" />
              </Form.Item>
            </Col>
          </Row>
          <Space>
            <Button type="primary" icon={<SearchOutlined />} htmlType="submit">
              Tìm kiếm
            </Button>
            <Button
              icon={<ReloadOutlined />}
              onClick={() => {
                form.resetFields();
                setFilters({ page: 0, size: 10 });
              }}
            >
              Đặt lại
            </Button>
          </Space>
        </Form>
      </Card>

      <Card>
        <Table<OrderSummary>
          rowKey="id"
          columns={columns}
          dataSource={data}
          loading={loading}
          scroll={{ x: 1780 }}
          pagination={{
            current: (filters.page ?? 0) + 1,
            pageSize: filters.size ?? 10,
            total,
            showSizeChanger: true,
            showTotal: (value) => `Tổng ${value} đơn hàng`,
            onChange: (page, pageSize) => setFilters({ ...filters, page: page - 1, size: pageSize }),
          }}
        />
      </Card>

      <Modal
        open={Boolean(importResult)}
        title="Kết quả import đơn hàng"
        onCancel={() => setImportResult(null)}
        onOk={() => setImportResult(null)}
        footer={null}
      >
        {importResult && (
          <>
            <Alert
              type={importResult.failedCount === 0 ? 'success' : 'warning'}
              message={`Tổng ${importResult.totalRows} dòng: thành công ${importResult.successCount}, lỗi ${importResult.failedCount}`}
              className="mb-3"
            />
            {importResult.createdOrderCodes.length > 0 && (
              <p>
                <strong>Mã vận đơn đã tạo:</strong> {importResult.createdOrderCodes.join(', ')}
              </p>
            )}
            {importResult.errors.length > 0 && (
              <Table
                size="small"
                rowKey="rowNumber"
                pagination={false}
                dataSource={importResult.errors}
                columns={[
                  { title: 'Dòng', dataIndex: 'rowNumber', width: 70 },
                  { title: 'Lỗi', dataIndex: 'message' },
                ]}
              />
            )}
          </>
        )}
      </Modal>
    </div>
  );
};

export default OrderListPage;
