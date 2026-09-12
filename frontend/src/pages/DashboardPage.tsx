import { useEffect, useState } from 'react';
import { Card, Col, DatePicker, Empty, Row, Spin, Statistic, Table, Tag } from 'antd';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import dayjs, { Dayjs } from 'dayjs';
import PageHeader from '@/components/common/PageHeader';
import { dashboardApi } from '@/api/services';
import { useAuthStore } from '@/store/authStore';
import { PERMISSION } from '@/constants/permissions';
import { formatDate, formatMoney, formatNumber } from '@/utils/format';
import type {
  DashboardOverview,
  OrderCountByDate,
  OrderCountByStatus,
  RevenueByProvince,
  TopShipper,
} from '@/types';

const CHART_COLORS = ['#d32f2f', '#1677ff', '#52c41a', '#faad14', '#722ed1', '#13c2c2', '#eb2f96', '#8c8c8c'];

const DashboardPage = () => {
  const isAdminView = useAuthStore((state) => state.permissions.includes(PERMISSION.DASHBOARD_ADMIN));
  const [range, setRange] = useState<[Dayjs, Dayjs]>([dayjs().subtract(30, 'day'), dayjs()]);
  const [loading, setLoading] = useState(true);
  const [overview, setOverview] = useState<DashboardOverview | null>(null);
  const [byStatus, setByStatus] = useState<OrderCountByStatus[]>([]);
  const [byDate, setByDate] = useState<OrderCountByDate[]>([]);
  const [byProvince, setByProvince] = useState<RevenueByProvince[]>([]);
  const [topShippers, setTopShippers] = useState<TopShipper[]>([]);

  useEffect(() => {
    const payload = {
      fromDate: range[0].format('YYYY-MM-DD'),
      toDate: range[1].format('YYYY-MM-DD'),
      topSize: 5,
    };

    const load = async () => {
      setLoading(true);
      try {
        setOverview(await dashboardApi.overview(payload));
        if (isAdminView) {
          const [statusData, dateData, provinceData, shipperData] = await Promise.all([
            dashboardApi.ordersByStatus(payload),
            dashboardApi.ordersByDate(payload),
            dashboardApi.revenueByProvince(payload),
            dashboardApi.topShippers(payload),
          ]);
          setByStatus(statusData);
          setByDate(dateData);
          setByProvince(provinceData);
          setTopShippers(shipperData);
        }
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, [range, isAdminView]);

  return (
    <div>
      <PageHeader
        title="Tổng quan"
        subtitle="Chỉ số vận hành theo khoảng thời gian được chọn"
        extra={
          <DatePicker.RangePicker
            value={range}
            format="DD/MM/YYYY"
            allowClear={false}
            onChange={(values) => values && setRange(values as [Dayjs, Dayjs])}
          />
        }
      />

      <Spin spinning={loading}>
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={12} lg={6}>
            <Card className="stat-card">
              <Statistic title="Tổng đơn hàng" value={overview?.totalOrders ?? 0} />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card className="stat-card">
              <Statistic title="Giao thành công" value={overview?.deliveredOrders ?? 0} valueStyle={{ color: '#52c41a' }} />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card className="stat-card">
              <Statistic title="Đang xử lý" value={overview?.inProgressOrders ?? 0} valueStyle={{ color: '#1677ff' }} />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card className="stat-card">
              <Statistic
                title="Tỉ lệ thành công"
                value={overview?.successRate ?? 0}
                precision={2}
                suffix="%"
                valueStyle={{ color: '#d32f2f' }}
              />
            </Card>
          </Col>

          <Col xs={24} sm={12} lg={6}>
            <Card className="stat-card">
              <Statistic title="Doanh thu cước" value={formatMoney(overview?.totalRevenue)} />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card className="stat-card">
              <Statistic title="Doanh thu hôm nay" value={formatMoney(overview?.revenueToday)} />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card className="stat-card">
              <Statistic title="COD đã thu hộ" value={formatMoney(overview?.totalCodCollected)} />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card className="stat-card">
              {isAdminView ? (
                <Statistic
                  title="Shipper trực tuyến"
                  value={`${overview?.onlineShippers ?? 0}/${overview?.totalShippers ?? 0}`}
                />
              ) : (
                <Statistic title="Đơn hôm nay" value={overview?.ordersToday ?? 0} />
              )}
            </Card>
          </Col>

          {isAdminView && (
            <>
              <Col xs={24} lg={16}>
                <Card title="Số đơn và doanh thu theo ngày">
                  {byDate.length === 0 ? (
                    <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Chưa có dữ liệu" />
                  ) : (
                    <ResponsiveContainer width="100%" height={300}>
                      <LineChart data={byDate.map((item) => ({ ...item, label: formatDate(item.date) }))}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="label" />
                        <YAxis yAxisId="left" allowDecimals={false} />
                        <YAxis yAxisId="right" orientation="right" tickFormatter={(v) => formatNumber(v as number)} />
                        <Tooltip
                          formatter={(value, name) =>
                            name === 'Doanh thu' ? formatMoney(value as number) : formatNumber(value as number)
                          }
                        />
                        <Legend />
                        <Line yAxisId="left" type="monotone" dataKey="quantity" name="Số đơn" stroke="#1677ff" strokeWidth={2} />
                        <Line yAxisId="right" type="monotone" dataKey="revenue" name="Doanh thu" stroke="#d32f2f" strokeWidth={2} />
                      </LineChart>
                    </ResponsiveContainer>
                  )}
                </Card>
              </Col>

              <Col xs={24} lg={8}>
                <Card title="Tỉ lệ đơn theo trạng thái">
                  {byStatus.length === 0 ? (
                    <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Chưa có dữ liệu" />
                  ) : (
                    <ResponsiveContainer width="100%" height={300}>
                      <PieChart>
                        <Pie
                          data={byStatus.map((item) => ({ name: item.status.description, value: item.quantity }))}
                          dataKey="value"
                          nameKey="name"
                          outerRadius={95}
                          label
                        >
                          {byStatus.map((_, index) => (
                            <Cell key={index} fill={CHART_COLORS[index % CHART_COLORS.length]} />
                          ))}
                        </Pie>
                        <Tooltip />
                        <Legend />
                      </PieChart>
                    </ResponsiveContainer>
                  )}
                </Card>
              </Col>

              <Col xs={24} lg={12}>
                <Card title="Doanh thu theo tỉnh/thành">
                  {byProvince.length === 0 ? (
                    <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Chưa có dữ liệu" />
                  ) : (
                    <ResponsiveContainer width="100%" height={300}>
                      <BarChart data={byProvince}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="province" />
                        <YAxis tickFormatter={(v) => formatNumber(v as number)} />
                        <Tooltip formatter={(value) => formatMoney(value as number)} />
                        <Bar dataKey="revenue" name="Doanh thu" fill="#d32f2f" radius={[4, 4, 0, 0]} />
                      </BarChart>
                    </ResponsiveContainer>
                  )}
                </Card>
              </Col>

              <Col xs={24} lg={12}>
                <Card title="Top shipper giao nhiều đơn nhất">
                  <Table<TopShipper>
                    rowKey="shipperId"
                    size="small"
                    pagination={false}
                    dataSource={topShippers}
                    locale={{ emptyText: 'Chưa có dữ liệu' }}
                    columns={[
                      {
                        title: '#',
                        width: 50,
                        render: (_, __, index) => <Tag color={CHART_COLORS[index]}>{index + 1}</Tag>,
                      },
                      { title: 'Mã shipper', dataIndex: 'shipperCode', width: 120 },
                      { title: 'Họ tên', dataIndex: 'fullName' },
                      { title: 'Đơn thành công', dataIndex: 'deliveredCount', align: 'right', width: 130 },
                      {
                        title: 'Đánh giá',
                        dataIndex: 'rating',
                        align: 'right',
                        width: 100,
                        render: (value: number) => `${value} / 5`,
                      },
                    ]}
                  />
                </Card>
              </Col>

              <Col span={24}>
                <Card>
                  <Statistic
                    title="Thời gian giao hàng trung bình"
                    value={overview?.averageDeliveryHours ?? 0}
                    precision={2}
                    suffix="giờ"
                  />
                </Card>
              </Col>
            </>
          )}
        </Row>
      </Spin>
    </div>
  );
};

export default DashboardPage;
