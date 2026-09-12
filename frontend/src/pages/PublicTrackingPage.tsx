import { useCallback, useEffect, useMemo, useState } from 'react';
import { Card, Col, Descriptions, Empty, Input, Row, Timeline, Typography, message } from 'antd';
import { CarOutlined, SearchOutlined } from '@ant-design/icons';
import { Link, useSearchParams } from 'react-router-dom';
import StatusTag from '@/components/common/StatusTag';
import DeliveryMap, { MapPoint } from '@/components/common/DeliveryMap';
import { trackingApi } from '@/api/services';
import { useStomp } from '@/hooks/useStomp';
import { ORDER_STATUS_COLOR } from '@/constants/permissions';
import { formatDateTime } from '@/utils/format';
import type { OrderTracking } from '@/types';

/** Trang tra cuu van don khong yeu cau dang nhap. */
const PublicTrackingPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [orderCode, setOrderCode] = useState(searchParams.get('code') ?? '');
  const [tracking, setTracking] = useState<OrderTracking | null>(null);
  const [loading, setLoading] = useState(false);

  const search = useCallback(async (code: string) => {
    if (!code.trim()) {
      message.warning('Vui lòng nhập mã vận đơn');
      return;
    }
    setLoading(true);
    try {
      setTracking(await trackingApi.publicTracking(code.trim().toUpperCase()));
      setSearchParams({ code: code.trim().toUpperCase() });
    } catch {
      setTracking(null);
    } finally {
      setLoading(false);
    }
  }, [setSearchParams]);

  useEffect(() => {
    const initialCode = searchParams.get('code');
    if (initialCode) {
      void search(initialCode);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Tu dong cap nhat khi don co su kien moi
  const subscriptions = useMemo(
    () =>
      tracking
        ? [
            {
              destination: `/topic/orders/${tracking.orderCode}`,
              handler: () => {
                void trackingApi.publicTracking(tracking.orderCode).then(setTracking);
              },
            },
          ]
        : [],
    [tracking],
  );
  useStomp(subscriptions, Boolean(tracking));

  const mapPoints: MapPoint[] = useMemo(() => {
    if (!tracking) {
      return [];
    }
    const points: MapPoint[] = [];
    if (tracking.currentLatitude && tracking.currentLongitude) {
      points.push({
        latitude: tracking.currentLatitude,
        longitude: tracking.currentLongitude,
        label: 'Vị trí shipper',
        description: `Cập nhật ${formatDateTime(tracking.lastLocationAt)}`,
        color: '#1677ff',
      });
    }
    if (tracking.deliveryLatitude && tracking.deliveryLongitude) {
      points.push({
        latitude: tracking.deliveryLatitude,
        longitude: tracking.deliveryLongitude,
        label: 'Điểm giao hàng',
        color: '#52c41a',
      });
    }
    return points;
  }, [tracking]);

  return (
    <div className="min-h-screen bg-slate-100">
      <div className="max-w-5xl mx-auto p-4">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <CarOutlined style={{ fontSize: 24, color: '#d32f2f' }} />
            <Typography.Title level={4} style={{ margin: 0 }}>
              Tra cứu vận đơn
            </Typography.Title>
          </div>
          <Link to="/login">Đăng nhập hệ thống</Link>
        </div>

        <Card className="mb-4">
          <Input.Search
            size="large"
            placeholder="Nhập mã vận đơn, ví dụ DH260911ABCDEF"
            enterButton={
              <>
                <SearchOutlined /> Tra cứu
              </>
            }
            value={orderCode}
            loading={loading}
            onChange={(event) => setOrderCode(event.target.value)}
            onSearch={(value) => void search(value)}
          />
          <Typography.Text type="secondary" className="block mt-2" style={{ fontSize: 12 }}>
            Thông tin người nhận và địa chỉ được che một phần để bảo vệ dữ liệu cá nhân.
          </Typography.Text>
        </Card>

        {!tracking ? (
          <Card>
            <Empty description="Nhập mã vận đơn để xem hành trình đơn hàng" />
          </Card>
        ) : (
          <Row gutter={[16, 16]}>
            <Col xs={24} md={12}>
              <Card title={`Đơn hàng ${tracking.orderCode}`} className="mb-4">
                <Descriptions column={1} size="small" bordered>
                  <Descriptions.Item label="Trạng thái">
                    <StatusTag value={tracking.status} colorMap={ORDER_STATUS_COLOR} />
                  </Descriptions.Item>
                  <Descriptions.Item label="Dịch vụ">
                    <StatusTag value={tracking.serviceType} />
                  </Descriptions.Item>
                  <Descriptions.Item label="Người nhận">{tracking.receiverName}</Descriptions.Item>
                  <Descriptions.Item label="Khu vực giao">{tracking.deliveryAddress}</Descriptions.Item>
                  <Descriptions.Item label="Shipper phụ trách">{tracking.shipperName ?? 'Chưa phân công'}</Descriptions.Item>
                  <Descriptions.Item label="Hạn giao dự kiến">
                    {formatDateTime(tracking.expectedDeliveryAt)}
                  </Descriptions.Item>
                  <Descriptions.Item label="Thời điểm giao">{formatDateTime(tracking.deliveredAt)}</Descriptions.Item>
                </Descriptions>
              </Card>

              <Card title="Vị trí giao hàng">
                <DeliveryMap points={mapPoints} height={280} followFirstPoint />
              </Card>
            </Col>

            <Col xs={24} md={12}>
              <Card title="Hành trình đơn hàng">
                <Timeline
                  className="tracking-timeline"
                  items={tracking.events.map((event) => ({
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
            </Col>
          </Row>
        )}
      </div>
    </div>
  );
};

export default PublicTrackingPage;
