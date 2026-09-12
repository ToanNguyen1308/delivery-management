import { useCallback, useEffect, useRef, useState } from 'react';
import { Button, Card, Col, Input, Modal, Row, Space, Statistic, Switch, Table, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { CheckOutlined, CloseOutlined, EnvironmentOutlined, ReloadOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import PageHeader from '@/components/common/PageHeader';
import StatusTag from '@/components/common/StatusTag';
import { dispatchApi, shipperApi, trackingApi } from '@/api/services';
import { ASSIGNMENT_STATUS_COLOR, ORDER_STATUS_COLOR } from '@/constants/permissions';
import { formatDateTime, formatMoney } from '@/utils/format';
import type { Assignment, Shipper, ShipperPerformance } from '@/types';

const LOCATION_PUSH_INTERVAL_MS = 15000;

const MyTasksPage = () => {
  const navigate = useNavigate();
  const [assignments, setAssignments] = useState<Assignment[]>([]);
  const [profile, setProfile] = useState<Shipper | null>(null);
  const [performance, setPerformance] = useState<ShipperPerformance | null>(null);
  const [loading, setLoading] = useState(false);
  const [sharingLocation, setSharingLocation] = useState(false);
  const intervalRef = useRef<number | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [page, myProfile, myPerformance] = await Promise.all([
        dispatchApi.myAssignments({ size: 50 }),
        shipperApi.myProfile(),
        shipperApi.myPerformance(),
      ]);
      setAssignments(page.content);
      setProfile(myProfile);
      setPerformance(myPerformance);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  /** Dung chia se vi tri khi roi khoi trang de tranh ro ri interval. */
  useEffect(
    () => () => {
      if (intervalRef.current) {
        window.clearInterval(intervalRef.current);
      }
    },
    [],
  );

  const pushCurrentLocation = useCallback(
    (orderId?: number) => {
      if (!navigator.geolocation) {
        message.warning('Thiết bị không hỗ trợ định vị');
        return;
      }
      navigator.geolocation.getCurrentPosition(
        async (position) => {
          await trackingApi.pushLocation({
            latitude: Number(position.coords.latitude.toFixed(7)),
            longitude: Number(position.coords.longitude.toFixed(7)),
            orderId,
            speedKmh: position.coords.speed ? Number((position.coords.speed * 3.6).toFixed(2)) : undefined,
          });
        },
        () => message.warning('Không lấy được vị trí, vui lòng cho phép quyền truy cập định vị'),
        { enableHighAccuracy: true, timeout: 10000 },
      );
    },
    [],
  );

  const activeOrderId = assignments.find((item) =>
    ['ASSIGNED', 'PICKED_UP', 'IN_TRANSIT'].includes(item.orderStatus.code),
  )?.orderId;

  const toggleLocationSharing = (enabled: boolean) => {
    setSharingLocation(enabled);
    if (intervalRef.current) {
      window.clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
    if (enabled) {
      pushCurrentLocation(activeOrderId);
      intervalRef.current = window.setInterval(
        () => pushCurrentLocation(activeOrderId),
        LOCATION_PUSH_INTERVAL_MS,
      );
      message.success('Đã bật chia sẻ vị trí, hệ thống cập nhật mỗi 15 giây');
    }
  };

  const handleStatusToggle = async (checked: boolean) => {
    const updated = await shipperApi.updateMyStatus(checked ? 'ONLINE' : 'OFFLINE');
    setProfile(updated);
    message.success(`Đã chuyển sang trạng thái: ${updated.status.description}`);
  };

  const handleAccept = async (assignment: Assignment) => {
    await dispatchApi.respond(assignment.id, true);
    message.success(`Đã nhận đơn ${assignment.orderCode}`);
    void load();
  };

  const handleReject = (assignment: Assignment) => {
    let reason = '';
    Modal.confirm({
      title: `Từ chối đơn ${assignment.orderCode}`,
      content: (
        <Input.TextArea
          rows={3}
          placeholder="Lý do từ chối"
          onChange={(event) => {
            reason = event.target.value;
          }}
        />
      ),
      okText: 'Từ chối',
      okButtonProps: { danger: true },
      cancelText: 'Đóng',
      onOk: async () => {
        await dispatchApi.respond(assignment.id, false, reason);
        message.success('Đã từ chối, đơn quay lại hàng chờ điều phối');
        void load();
      },
    });
  };

  const columns: ColumnsType<Assignment> = [
    {
      title: 'Mã vận đơn',
      dataIndex: 'orderCode',
      width: 150,
      render: (value: string, record) => (
        <Button type="link" style={{ padding: 0 }} onClick={() => navigate(`/orders/${record.orderId}`)}>
          {value}
        </Button>
      ),
    },
    { title: 'Người nhận', dataIndex: 'receiverName', width: 150 },
    { title: 'Điện thoại', dataIndex: 'receiverPhone', width: 120 },
    { title: 'Lấy tại', dataIndex: 'pickupAddress', ellipsis: true },
    { title: 'Giao tới', dataIndex: 'deliveryAddress', ellipsis: true },
    {
      title: 'Thu hộ',
      dataIndex: 'codAmount',
      width: 130,
      align: 'right',
      render: (value: number) => formatMoney(value),
    },
    {
      title: 'Đơn hàng',
      dataIndex: 'orderStatus',
      width: 170,
      render: (value) => <StatusTag value={value} colorMap={ORDER_STATUS_COLOR} />,
    },
    {
      title: 'Phân công',
      dataIndex: 'status',
      width: 170,
      render: (value) => <StatusTag value={value} colorMap={ASSIGNMENT_STATUS_COLOR} />,
    },
    {
      title: 'Thời điểm gán',
      dataIndex: 'assignedAt',
      width: 150,
      render: (value: string) => formatDateTime(value),
    },
    {
      title: 'Thao tác',
      width: 180,
      fixed: 'right',
      render: (_, record) =>
        record.status.code === 'PENDING' ? (
          <Space>
            <Button type="primary" size="small" icon={<CheckOutlined />} onClick={() => void handleAccept(record)}>
              Nhận
            </Button>
            <Button danger size="small" icon={<CloseOutlined />} onClick={() => handleReject(record)}>
              Từ chối
            </Button>
          </Space>
        ) : (
          <Button size="small" onClick={() => navigate(`/orders/${record.orderId}`)}>
            Cập nhật
          </Button>
        ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Nhiệm vụ giao hàng của tôi"
        subtitle={profile ? `${profile.shipperCode} · ${profile.licensePlate ?? 'chưa có biển số'}` : undefined}
        extra={
          <Space wrap>
            <span>
              Trạng thái:{' '}
              <Switch
                checked={profile?.status.code === 'ONLINE' || profile?.status.code === 'BUSY'}
                onChange={handleStatusToggle}
                checkedChildren="Trực tuyến"
                unCheckedChildren="Ngoại tuyến"
              />
            </span>
            <span>
              Chia sẻ vị trí:{' '}
              <Switch
                checked={sharingLocation}
                onChange={toggleLocationSharing}
                checkedChildren="Bật"
                unCheckedChildren="Tắt"
              />
            </span>
            <Button icon={<EnvironmentOutlined />} onClick={() => pushCurrentLocation(activeOrderId)}>
              Gửi vị trí ngay
            </Button>
            <Button icon={<ReloadOutlined />} onClick={() => void load()}>
              Tải lại
            </Button>
          </Space>
        }
      />

      <Row gutter={[16, 16]} className="mb-4">
        <Col xs={12} md={6}>
          <Card className="stat-card">
            <Statistic title="Đang giao" value={performance?.inProgress ?? 0} />
          </Card>
        </Col>
        <Col xs={12} md={6}>
          <Card className="stat-card">
            <Statistic title="Đã giao thành công" value={performance?.totalDelivered ?? 0} valueStyle={{ color: '#52c41a' }} />
          </Card>
        </Col>
        <Col xs={12} md={6}>
          <Card className="stat-card">
            <Statistic title="Giao thất bại" value={performance?.totalFailed ?? 0} valueStyle={{ color: '#d32f2f' }} />
          </Card>
        </Col>
        <Col xs={12} md={6}>
          <Card className="stat-card">
            <Statistic
              title="Tỉ lệ thành công"
              value={performance?.successRate ?? 0}
              precision={2}
              suffix="%"
            />
            <Tag color="gold" className="mt-2">
              Đánh giá {performance?.rating ?? 0}/5
            </Tag>
          </Card>
        </Col>
      </Row>

      <Card>
        <Table<Assignment>
          rowKey="id"
          columns={columns}
          dataSource={assignments}
          loading={loading}
          scroll={{ x: 1700 }}
          pagination={{ pageSize: 10 }}
          locale={{ emptyText: 'Chưa có nhiệm vụ nào' }}
        />
      </Card>
    </div>
  );
};

export default MyTasksPage;
