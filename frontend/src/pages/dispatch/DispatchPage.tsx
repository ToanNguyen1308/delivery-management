import { useCallback, useEffect, useMemo, useState } from 'react';
import { Button, Card, Col, Modal, Row, Select, Space, Table, Tag, Typography, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { ReloadOutlined, ThunderboltOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import PageHeader from '@/components/common/PageHeader';
import StatusTag from '@/components/common/StatusTag';
import DeliveryMap, { MapPoint } from '@/components/common/DeliveryMap';
import { dispatchApi, orderApi, shipperApi } from '@/api/services';
import { useStomp } from '@/hooks/useStomp';
import { ORDER_STATUS_COLOR, SHIPPER_STATUS_COLOR } from '@/constants/permissions';
import { formatDateTime, formatMoney } from '@/utils/format';
import type { OrderSummary, Shipper, ShipperLocation } from '@/types';

const DispatchPage = () => {
  const navigate = useNavigate();
  const [pendingOrders, setPendingOrders] = useState<OrderSummary[]>([]);
  const [activeOrders, setActiveOrders] = useState<OrderSummary[]>([]);
  const [shippers, setShippers] = useState<Shipper[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<OrderSummary | null>(null);
  const [selectedShipperId, setSelectedShipperId] = useState<number | undefined>();

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [pending, active, available] = await Promise.all([
        orderApi.search({ unassignedOnly: true, size: 50 }),
        orderApi.search({ statuses: ['ASSIGNED', 'PICKED_UP', 'IN_TRANSIT'], size: 50 }),
        shipperApi.available(),
      ]);
      setPendingOrders(pending.content);
      setActiveOrders(active.content);
      setShippers(available);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const subscriptions = useMemo(
    () => [
      {
        destination: '/topic/shippers/location',
        handler: (payload: unknown) => {
          const location = payload as ShipperLocation;
          setShippers((prev) =>
            prev.map((shipper) =>
              shipper.id === location.shipperId
                ? {
                    ...shipper,
                    currentLatitude: location.latitude,
                    currentLongitude: location.longitude,
                    lastLocationAt: location.recordedAt,
                  }
                : shipper,
            ),
          );
        },
      },
    ],
    [],
  );
  useStomp(subscriptions);

  const handleAutoAssign = async (order: OrderSummary) => {
    const assignment = await dispatchApi.assign(order.id);
    message.success(
      `Đã gán đơn ${order.orderCode} cho ${assignment.shipperCode} - ${assignment.shipperName} (${assignment.assignType.description})`,
    );
    void load();
  };

  const handleManualAssign = async () => {
    if (!selectedOrder || !selectedShipperId) {
      message.warning('Vui lòng chọn shipper');
      return;
    }
    const assignment = await dispatchApi.assign(selectedOrder.id, selectedShipperId);
    message.success(`Đã gán đơn ${selectedOrder.orderCode} cho ${assignment.shipperName}`);
    setSelectedOrder(null);
    setSelectedShipperId(undefined);
    void load();
  };

  const shipperPoints: MapPoint[] = shippers
    .filter((shipper) => shipper.currentLatitude && shipper.currentLongitude)
    .map((shipper) => ({
      latitude: Number(shipper.currentLatitude),
      longitude: Number(shipper.currentLongitude),
      label: `${shipper.shipperCode} - ${shipper.fullName}`,
      description: `${shipper.status.description} · ${shipper.currentLoad}/${shipper.maxConcurrentOrders} đơn`,
      color: shipper.status.code === 'ONLINE' ? '#52c41a' : '#1677ff',
    }));

  const pendingColumns: ColumnsType<OrderSummary> = [
    {
      title: 'Mã vận đơn',
      dataIndex: 'orderCode',
      width: 160,
      render: (value: string, record) => (
        <Button type="link" style={{ padding: 0 }} onClick={() => navigate(`/orders/${record.id}`)}>
          {value}
        </Button>
      ),
    },
    { title: 'Người nhận', dataIndex: 'receiverName', width: 150 },
    { title: 'Địa chỉ giao', dataIndex: 'deliveryAddress', ellipsis: true, width: 220 },
    { title: 'Dịch vụ', dataIndex: 'serviceType', width: 200, render: (value) => <StatusTag value={value} /> },
    {
      title: 'Thu hộ',
      dataIndex: 'codAmount',
      width: 120,
      align: 'right',
      render: (value: number) => formatMoney(value),
    },
    {
      title: 'Hạn giao',
      dataIndex: 'expectedDeliveryAt',
      width: 150,
      render: (value: string) => formatDateTime(value),
    },
    {
      title: 'Phân công',
      width: 220,
      render: (_, record) => (
        <Space>
          <Button
            type="primary"
            size="small"
            icon={<ThunderboltOutlined />}
            onClick={() => void handleAutoAssign(record)}
          >
            Tự động
          </Button>
          <Button size="small" onClick={() => setSelectedOrder(record)}>
            Chọn shipper
          </Button>
        </Space>
      ),
    },
  ];

  const activeColumns: ColumnsType<OrderSummary> = [
    {
      title: 'Mã vận đơn',
      dataIndex: 'orderCode',
      width: 160,
      render: (value: string, record) => (
        <Button type="link" style={{ padding: 0 }} onClick={() => navigate(`/orders/${record.id}`)}>
          {value}
        </Button>
      ),
    },
    { title: 'Shipper', dataIndex: 'shipperName', width: 150 },
    { title: 'Người nhận', dataIndex: 'receiverName', width: 150 },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      width: 170,
      render: (value) => <StatusTag value={value} colorMap={ORDER_STATUS_COLOR} />,
    },
    {
      title: 'Hạn giao',
      dataIndex: 'expectedDeliveryAt',
      width: 150,
      render: (value: string) => formatDateTime(value),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Điều phối vận chuyển"
        subtitle="Gán đơn cho shipper thủ công hoặc để hệ thống tự chọn theo chiến lược đã cấu hình"
        extra={
          <Button icon={<ReloadOutlined />} onClick={() => void load()}>
            Tải lại
          </Button>
        }
      />

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={14}>
          <Card title={`Đơn chờ điều phối (${pendingOrders.length})`} className="mb-4">
            <Table<OrderSummary>
              rowKey="id"
              size="small"
              loading={loading}
              dataSource={pendingOrders}
              columns={pendingColumns}
              pagination={false}
              scroll={{ x: 1220 }}
              locale={{ emptyText: 'Không có đơn nào đang chờ' }}
            />
          </Card>

          <Card title={`Đơn đang giao (${activeOrders.length})`}>
            <Table<OrderSummary>
              rowKey="id"
              size="small"
              loading={loading}
              dataSource={activeOrders}
              columns={activeColumns}
              pagination={false}
              scroll={{ x: 790 }}
              locale={{ emptyText: 'Không có đơn nào đang giao' }}
            />
          </Card>
        </Col>

        <Col xs={24} xl={10}>
          <Card title="Vị trí shipper theo thời gian thực" className="mb-4">
            <DeliveryMap points={shipperPoints} height={340} />
            <Typography.Text type="secondary" className="block mt-2" style={{ fontSize: 12 }}>
              Xanh lá: sẵn sàng nhận đơn · Xanh dương: đang giao hàng
            </Typography.Text>
          </Card>

          <Card title={`Shipper khả dụng (${shippers.length})`}>
            <Table<Shipper>
              rowKey="id"
              size="small"
              pagination={false}
              dataSource={shippers}
              locale={{ emptyText: 'Chưa có shipper nào trực tuyến' }}
              columns={[
                { title: 'Mã', dataIndex: 'shipperCode', width: 100 },
                { title: 'Họ tên', dataIndex: 'fullName' },
                {
                  title: 'Trạng thái',
                  dataIndex: 'status',
                  width: 150,
                  render: (value) => <StatusTag value={value} colorMap={SHIPPER_STATUS_COLOR} />,
                },
                {
                  title: 'Tải',
                  width: 80,
                  align: 'center',
                  render: (_, record) => (
                    <Tag color={record.currentLoad >= record.maxConcurrentOrders ? 'red' : 'green'}>
                      {record.currentLoad}/{record.maxConcurrentOrders}
                    </Tag>
                  ),
                },
              ]}
            />
          </Card>
        </Col>
      </Row>

      <Modal
        open={Boolean(selectedOrder)}
        title={`Chọn shipper cho đơn ${selectedOrder?.orderCode ?? ''}`}
        onCancel={() => {
          setSelectedOrder(null);
          setSelectedShipperId(undefined);
        }}
        onOk={handleManualAssign}
        okText="Phân công"
        cancelText="Đóng"
      >
        <Select
          className="w-full"
          placeholder="Chọn shipper"
          value={selectedShipperId}
          onChange={setSelectedShipperId}
          options={shippers.map((shipper) => ({
            value: shipper.id,
            label: `${shipper.shipperCode} - ${shipper.fullName} (${shipper.currentLoad}/${shipper.maxConcurrentOrders} đơn, ${shipper.status.description})`,
          }))}
        />
      </Modal>
    </div>
  );
};

export default DispatchPage;
