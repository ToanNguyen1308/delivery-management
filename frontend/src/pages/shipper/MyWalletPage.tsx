import { useCallback, useEffect, useState } from 'react';
import { Alert, Button, Card, Col, Modal, Row, Statistic, Table, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { DollarOutlined, ReloadOutlined } from '@ant-design/icons';
import PageHeader from '@/components/common/PageHeader';
import StatusTag from '@/components/common/StatusTag';
import { codApi } from '@/api/services';
import { COD_STATUS_COLOR } from '@/constants/permissions';
import { formatDateTime, formatMoney } from '@/utils/format';
import type { CodSettlement, CodWallet } from '@/types';

const MyWalletPage = () => {
  const [wallet, setWallet] = useState<CodWallet | null>(null);
  const [settlements, setSettlements] = useState<CodSettlement[]>([]);
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [walletData, page] = await Promise.all([
        codApi.myWallet(),
        codApi.mySettlements({ size: 50 }),
      ]);
      setWallet(walletData);
      setSettlements(page.content);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const handleSubmitAll = () => {
    Modal.confirm({
      title: 'Nộp tiền COD',
      content: `Xác nhận nộp ${formatMoney(wallet?.holdingAmount)} của ${wallet?.holdingCount ?? 0} đơn cho công ty?`,
      okText: 'Nộp tiền',
      cancelText: 'Đóng',
      onOk: async () => {
        const count = await codApi.submitAll();
        message.success(`Đã nộp ${count} khoản COD, chờ kế toán xác nhận`);
        void load();
      },
    });
  };

  const columns: ColumnsType<CodSettlement> = [
    { title: 'Mã vận đơn', dataIndex: 'orderCode', width: 150 },
    { title: 'Người nhận', dataIndex: 'receiverName' },
    {
      title: 'Số tiền',
      dataIndex: 'amount',
      align: 'right',
      width: 140,
      render: (value: number) => formatMoney(value),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      width: 250,
      render: (value) => <StatusTag value={value} colorMap={COD_STATUS_COLOR} />,
    },
    { title: 'Thu lúc', dataIndex: 'collectedAt', width: 150, render: (value: string) => formatDateTime(value) },
    { title: 'Nộp lúc', dataIndex: 'submittedAt', width: 150, render: (value: string) => formatDateTime(value) },
    {
      title: 'Xác nhận lúc',
      dataIndex: 'confirmedAt',
      width: 150,
      render: (value: string) => formatDateTime(value),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Ví COD của tôi"
        subtitle="Tiền thu hộ đã nhận từ người nhận và cần nộp lại cho công ty"
        extra={
          <>
            <Button icon={<ReloadOutlined />} onClick={() => void load()}>
              Tải lại
            </Button>
            <Button
              type="primary"
              icon={<DollarOutlined />}
              disabled={!wallet?.holdingCount}
              onClick={handleSubmitAll}
            >
              Nộp toàn bộ
            </Button>
          </>
        }
      />

      <Row gutter={[16, 16]} className="mb-4">
        <Col xs={24} md={8}>
          <Card className="stat-card">
            <Statistic
              title={`Đang giữ (${wallet?.holdingCount ?? 0} đơn)`}
              value={formatMoney(wallet?.holdingAmount)}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card className="stat-card">
            <Statistic
              title={`Đã nộp, chờ xác nhận (${wallet?.submittedCount ?? 0} đơn)`}
              value={formatMoney(wallet?.submittedAmount)}
              valueStyle={{ color: '#1677ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card className="stat-card">
            <Statistic
              title="Đã đối soát xong"
              value={formatMoney(wallet?.confirmedAmount)}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
      </Row>

      {Boolean(wallet?.holdingCount) && (
        <Alert
          className="mb-4"
          type="warning"
          showIcon
          message={`Bạn đang giữ ${formatMoney(wallet?.holdingAmount)} tiền thu hộ, vui lòng nộp lại cho công ty.`}
        />
      )}

      <Card>
        <Table<CodSettlement>
          rowKey="id"
          columns={columns}
          dataSource={settlements}
          loading={loading}
          scroll={{ x: 1150 }}
          pagination={{ pageSize: 10 }}
          locale={{ emptyText: 'Chưa có khoản COD nào' }}
        />
      </Card>
    </div>
  );
};

export default MyWalletPage;
