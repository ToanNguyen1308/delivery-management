import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Button,
  Card,
  Col,
  Descriptions,
  Form,
  Image,
  Input,
  Modal,
  Row,
  Space,
  Spin,
  Table,
  Timeline,
  Typography,
  Upload,
  message,
} from 'antd';
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  CreditCardOutlined,
  ReloadOutlined,
  UploadOutlined,
} from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import PageHeader from '@/components/common/PageHeader';
import StatusTag from '@/components/common/StatusTag';
import DeliveryMap, { MapPoint } from '@/components/common/DeliveryMap';
import { fileApi, orderApi, paymentApi, trackingApi } from '@/api/services';
import { useStomp } from '@/hooks/useStomp';
import { useAuthStore } from '@/store/authStore';
import { ORDER_STATUS_COLOR, PAYMENT_STATUS_COLOR, PERMISSION, ROLE_GROUP } from '@/constants/permissions';
import { DETAIL_DESCRIPTIONS_COLUMN, DETAIL_DESCRIPTIONS_STYLES } from '@/constants/layout';
import { formatDateTime, formatMoney, formatTime } from '@/utils/format';
import type { EnumValue, Order, OrderStatusHistory, OrderTracking, Payment } from '@/types';

const OrderDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const orderId = Number(id);
  const navigate = useNavigate();
  const { hasPermission, hasRole } = useAuthStore();

  const [order, setOrder] = useState<Order | null>(null);
  const [history, setHistory] = useState<OrderStatusHistory[]>([]);
  const [tracking, setTracking] = useState<OrderTracking | null>(null);
  const [payments, setPayments] = useState<Payment[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusTarget, setStatusTarget] = useState<EnumValue | null>(null);
  const [proofUrl, setProofUrl] = useState<string>('');
  const [statusForm] = Form.useForm();

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [orderData, historyData, trackingData] = await Promise.all([
        orderApi.getById(orderId),
        orderApi.history(orderId),
        trackingApi.byOrder(orderId),
      ]);
      setOrder(orderData);
      setHistory(historyData);
      setTracking(trackingData);
      if (hasPermission(PERMISSION.PAYMENT_VIEW)) {
        setPayments(await paymentApi.byOrder(orderId));
      }
    } finally {
      setLoading(false);
    }
  }, [orderId, hasPermission]);

  useEffect(() => {
    void load();
  }, [load]);

  // Nhan cap nhat vi tri shipper va su kien moi theo thoi gian thuc
  const subscriptions = useMemo(
    () =>
      order
        ? [
            {
              destination: `/topic/orders/${order.orderCode}`,
              handler: () => {
                void trackingApi.byOrder(orderId).then(setTracking);
              },
            },
          ]
        : [],
    [order, orderId],
  );
  useStomp(subscriptions, Boolean(order));

  const handleConfirm = async () => {
    await orderApi.confirm(orderId);
    message.success('Đã xác nhận đơn hàng');
    void load();
  };

  const handleCancel = () => {
    let reason = '';
    Modal.confirm({
      title: 'Hủy đơn hàng',
      content: (
        <Input.TextArea
          rows={3}
          placeholder="Lý do hủy đơn"
          onChange={(event) => {
            reason = event.target.value;
          }}
        />
      ),
      okText: 'Xác nhận hủy',
      okButtonProps: { danger: true },
      cancelText: 'Đóng',
      onOk: async () => {
        await orderApi.cancel(orderId, reason);
        message.success('Đã hủy đơn hàng');
        void load();
      },
    });
  };

  const handleStatusSubmit = async (values: Record<string, unknown>) => {
    if (!statusTarget) {
      return;
    }
    await orderApi.updateStatus(orderId, {
      status: statusTarget.code,
      note: values.note,
      failureReason: values.failureReason,
      proofImageUrl: proofUrl || undefined,
      latitude: tracking?.currentLatitude,
      longitude: tracking?.currentLongitude,
    });
    message.success(`Đã chuyển sang trạng thái: ${statusTarget.description}`);
    setStatusTarget(null);
    setProofUrl('');
    statusForm.resetFields();
    void load();
  };

  const handleUploadProof = async (file: File) => {
    const stored = await fileApi.upload(file, 'delivery-proof', orderId);
    setProofUrl(stored.url);
    message.success('Đã tải ảnh xác nhận giao hàng');
    return false;
  };

  const handlePay = async () => {
    const init = await paymentApi.create(orderId);
    if (init.mockMode) {
      Modal.confirm({
        title: 'Chế độ thanh toán giả lập',
        content: `Hệ thống chưa cấu hình VNPay sandbox. Xác nhận thanh toán ${formatMoney(init.amount)} cho đơn ${init.orderCode}?`,
        okText: 'Thanh toán thành công',
        cancelText: 'Thanh toán thất bại',
        onOk: async () => {
          await paymentApi.completeMock(init.txnRef, true);
          message.success('Thanh toán thành công');
          void load();
        },
        onCancel: async () => {
          await paymentApi.completeMock(init.txnRef, false);
          message.warning('Giao dịch thất bại');
          void load();
        },
      });
      return;
    }
    window.location.href = init.payUrl;
  };

  const mapPoints: MapPoint[] = useMemo(() => {
    if (!tracking) {
      return [];
    }
    const points: MapPoint[] = [];
    if (tracking.currentLatitude && tracking.currentLongitude) {
      points.push({
        latitude: tracking.currentLatitude,
        longitude: tracking.currentLongitude,
        label: `Shipper: ${tracking.shipperName ?? ''}`,
        description: `Cập nhật ${formatDateTime(tracking.lastLocationAt)}`,
        color: '#1677ff',
      });
    }
    if (tracking.pickupLatitude && tracking.pickupLongitude) {
      points.push({
        latitude: tracking.pickupLatitude,
        longitude: tracking.pickupLongitude,
        label: 'Điểm lấy hàng',
        color: '#faad14',
      });
    }
    if (tracking.deliveryLatitude && tracking.deliveryLongitude) {
      points.push({
        latitude: tracking.deliveryLatitude,
        longitude: tracking.deliveryLongitude,
        label: 'Điểm giao hàng',
        description: tracking.deliveryAddress,
        color: '#52c41a',
      });
    }
    return points;
  }, [tracking]);

  if (loading && !order) {
    return (
      <div className="flex justify-center py-20">
        <Spin size="large" />
      </div>
    );
  }

  if (!order) {
    return <Alert type="error" message="Không tìm thấy đơn hàng" />;
  }

  const canConfirm = hasPermission(PERMISSION.ORDER_CONFIRM) && order.status.code === 'CREATED';
  const canCancel =
    hasPermission(PERMISSION.ORDER_CANCEL) && ['CREATED', 'CONFIRMED', 'ASSIGNED'].includes(order.status.code);
  const canPay =
    hasPermission(PERMISSION.PAYMENT_CREATE) &&
    order.paymentMethod.code === 'VNPAY' &&
    order.paymentStatus.code !== 'PAID';
  const canOperateDelivery = hasRole(ROLE_GROUP.SHIPPER);
  const shipperStatuses = new Set(['PICKED_UP', 'IN_TRANSIT', 'DELIVERED', 'FAILED', 'RETURNED']);

  return (
    <div>
      <PageHeader
        title={`Đơn hàng ${order.orderCode}`}
        subtitle={`Tạo lúc ${formatDateTime(order.createdDate)} bởi ${order.createdBy ?? '-'}`}
        extra={
          <Space wrap>
            <Button icon={<ReloadOutlined />} onClick={() => void load()}>
              Tải lại
            </Button>
            {canPay && (
              <Button type="primary" icon={<CreditCardOutlined />} onClick={handlePay}>
                Thanh toán {formatMoney(order.totalAmount)}
              </Button>
            )}
            {canConfirm && (
              <Button type="primary" icon={<CheckCircleOutlined />} onClick={handleConfirm}>
                Xác nhận đơn
              </Button>
            )}
            {order.nextStatuses
              .filter((status) => !['CANCELLED', 'CONFIRMED'].includes(status.code))
              .filter((status) => canOperateDelivery || !shipperStatuses.has(status.code))
              .map((status) => (
                <Button key={status.code} onClick={() => setStatusTarget(status)}>
                  {status.description}
                </Button>
              ))}
            {canCancel && (
              <Button danger icon={<CloseCircleOutlined />} onClick={handleCancel}>
                Hủy đơn
              </Button>
            )}
            <Button onClick={() => navigate('/orders')}>Về danh sách</Button>
          </Space>
        }
      />

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={14}>
          <Card title="Thông tin đơn hàng" className="mb-4">
            <Descriptions
              column={DETAIL_DESCRIPTIONS_COLUMN}
              styles={DETAIL_DESCRIPTIONS_STYLES}
              size="small"
              bordered
            >
              <Descriptions.Item label="Trạng thái">
                <StatusTag value={order.status} colorMap={ORDER_STATUS_COLOR} />
              </Descriptions.Item>
              <Descriptions.Item label="Dịch vụ">
                <StatusTag value={order.serviceType} />
              </Descriptions.Item>
              <Descriptions.Item label="Khách hàng">{order.customerName}</Descriptions.Item>
              <Descriptions.Item label="SĐT khách hàng">{order.customerPhone ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="Người gửi">{order.senderName}</Descriptions.Item>
              <Descriptions.Item label="SĐT người gửi">{order.senderPhone}</Descriptions.Item>
              <Descriptions.Item label="Điểm lấy hàng" span="filled">
                {order.pickupAddress}
              </Descriptions.Item>
              <Descriptions.Item label="Người nhận">{order.receiverName}</Descriptions.Item>
              <Descriptions.Item label="SĐT người nhận">{order.receiverPhone}</Descriptions.Item>
              <Descriptions.Item label="Điểm giao hàng" span="filled">
                {order.deliveryAddress}
              </Descriptions.Item>
              <Descriptions.Item label="Khối lượng">{order.weightKg} kg</Descriptions.Item>
              <Descriptions.Item label="Quãng đường">{order.distanceKm ?? 0} km</Descriptions.Item>
              <Descriptions.Item label="Hàng hóa" span="filled">
                {order.packageDescription ?? '-'}
              </Descriptions.Item>
              <Descriptions.Item label="Hạn giao dự kiến">{formatDateTime(order.expectedDeliveryAt)}</Descriptions.Item>
              <Descriptions.Item label="Thời điểm giao">{formatDateTime(order.deliveredAt)}</Descriptions.Item>
              <Descriptions.Item label="Shipper">
                {order.shipperName ? `${order.shipperCode} - ${order.shipperName}` : 'Chưa phân công'}
              </Descriptions.Item>
              <Descriptions.Item label="SĐT shipper">{order.shipperPhone ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="Ghi chú" span="filled">
                {order.note ?? '-'}
              </Descriptions.Item>
              {order.cancelReason && (
                <Descriptions.Item label="Lý do hủy" span="filled">
                  {order.cancelReason}
                </Descriptions.Item>
              )}
              {order.failureReason && (
                <Descriptions.Item label="Lý do giao thất bại" span="filled">
                  {order.failureReason}
                </Descriptions.Item>
              )}
            </Descriptions>
          </Card>

          <Card title="Cước phí và thanh toán" className="mb-4">
            <Descriptions
              column={DETAIL_DESCRIPTIONS_COLUMN}
              styles={DETAIL_DESCRIPTIONS_STYLES}
              size="small"
              bordered
            >
              <Descriptions.Item label="Cước vận chuyển">{formatMoney(order.shippingFee)}</Descriptions.Item>
              <Descriptions.Item label="Phụ phí">{formatMoney(order.surcharge)}</Descriptions.Item>
              <Descriptions.Item label="Giảm giá">
                -{formatMoney(order.discountAmount)} {order.voucherCode ? `(${order.voucherCode})` : ''}
              </Descriptions.Item>
              <Descriptions.Item label="Tiền thu hộ">{formatMoney(order.codAmount)}</Descriptions.Item>
              <Descriptions.Item label="Khách phải trả">
                <strong className="text-red-600">{formatMoney(order.totalAmount)}</strong>
              </Descriptions.Item>
              <Descriptions.Item label="Phương thức">
                <StatusTag value={order.paymentMethod} />
              </Descriptions.Item>
              <Descriptions.Item label="Trạng thái thanh toán">
                <StatusTag value={order.paymentStatus} colorMap={PAYMENT_STATUS_COLOR} />
              </Descriptions.Item>
              <Descriptions.Item label="Đối soát COD">
                <StatusTag value={order.codSettlementStatus} />
              </Descriptions.Item>
            </Descriptions>

            {payments.length > 0 && (
              <Table<Payment>
                className="mt-4"
                rowKey="id"
                size="small"
                pagination={false}
                dataSource={payments}
                columns={[
                  { title: 'Mã giao dịch', dataIndex: 'txnRef' },
                  { title: 'Cổng', dataIndex: 'provider', render: (value) => <StatusTag value={value} /> },
                  {
                    title: 'Số tiền',
                    dataIndex: 'amount',
                    align: 'right',
                    render: (value: number) => formatMoney(value),
                  },
                  {
                    title: 'Trạng thái',
                    dataIndex: 'status',
                    render: (value) => <StatusTag value={value} colorMap={PAYMENT_STATUS_COLOR} />,
                  },
                  { title: 'Thời điểm', dataIndex: 'paidAt', render: (value: string) => formatDateTime(value) },
                ]}
              />
            )}
          </Card>

          {order.items.length > 0 && (
            <Card title="Danh sách mặt hàng">
              <Table
                rowKey={(record) => String(record.id)}
                size="small"
                pagination={false}
                dataSource={order.items}
                columns={[
                  { title: 'Tên mặt hàng', dataIndex: 'itemName' },
                  { title: 'Số lượng', dataIndex: 'quantity', align: 'right', width: 100 },
                  {
                    title: 'Đơn giá',
                    dataIndex: 'unitPrice',
                    align: 'right',
                    width: 140,
                    render: (value: number) => formatMoney(value),
                  },
                  { title: 'Khối lượng', dataIndex: 'weightKg', align: 'right', width: 120 },
                ]}
              />
            </Card>
          )}
        </Col>

        <Col xs={24} xl={10}>
          <Card title="Vị trí giao hàng theo thời gian thực" className="mb-4">
            <DeliveryMap points={mapPoints} route={tracking?.route ?? []} height={320} followFirstPoint />
            <Typography.Text type="secondary" className="block mt-2" style={{ fontSize: 12 }}>
              Điểm xanh dương là shipper, vàng là điểm lấy hàng, xanh lá là điểm giao hàng.
            </Typography.Text>
          </Card>

          {order.proofImageUrl && (
            <Card title="Ảnh xác nhận giao hàng" className="mb-4">
              <Image src={order.proofImageUrl} alt="Ảnh xác nhận giao hàng" />
            </Card>
          )}

          <Card title="Hành trình đơn hàng" className="mb-4">
            <Timeline
              className="tracking-timeline"
              items={(tracking?.events ?? []).map((event) => ({
                color: event.eventType.code === 'DELIVERED' ? 'green' : 'blue',
                children: (
                  <>
                    <strong>{event.eventType.description}</strong>
                    <div>{event.description}</div>
                    <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                      {formatDateTime(event.occurredAt)}
                    </Typography.Text>
                  </>
                ),
              }))}
            />
          </Card>

          <Card title="Lịch sử chuyển trạng thái">
            <Timeline
              className="tracking-timeline"
              items={history.map((item) => ({
                children: (
                  <>
                    <strong>
                      {item.fromStatus ? `${item.fromStatus.description} → ` : ''}
                      {item.toStatus.description}
                    </strong>
                    <div>{item.note}</div>
                    <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                      {formatTime(item.changedAt)} · {item.changedByName}
                    </Typography.Text>
                  </>
                ),
              }))}
            />
          </Card>
        </Col>
      </Row>

      <Modal
        open={Boolean(statusTarget)}
        title={`Chuyển trạng thái: ${statusTarget?.description ?? ''}`}
        onCancel={() => {
          setStatusTarget(null);
          setProofUrl('');
        }}
        onOk={() => statusForm.submit()}
        okText="Xác nhận"
        cancelText="Đóng"
      >
        <Form form={statusForm} layout="vertical" onFinish={handleStatusSubmit}>
          <Form.Item name="note" label="Ghi chú">
            <Input.TextArea rows={3} placeholder="Thông tin bổ sung cho bước này" />
          </Form.Item>
          {statusTarget?.code === 'FAILED' && (
            <Form.Item name="failureReason" label="Lý do giao thất bại" rules={[{ required: true }]}>
              <Input.TextArea rows={2} />
            </Form.Item>
          )}
          {statusTarget?.code === 'DELIVERED' && (
            <Form.Item label="Ảnh xác nhận giao hàng" required>
              <Upload beforeUpload={handleUploadProof} showUploadList={false} accept="image/*">
                <Button icon={<UploadOutlined />}>Tải ảnh lên MinIO</Button>
              </Upload>
              {proofUrl ? (
                <Alert type="success" showIcon className="mt-2" message="Đã có ảnh xác nhận" />
              ) : (
                <Alert
                  type="warning"
                  showIcon
                  className="mt-2"
                  message="Bắt buộc phải có ảnh xác nhận trước khi hoàn tất đơn"
                />
              )}
            </Form.Item>
          )}
        </Form>
      </Modal>
    </div>
  );
};

export default OrderDetailPage;
