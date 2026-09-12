import { useCallback, useEffect, useState } from 'react';
import { Button, Card, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { ReloadOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import PageHeader from '@/components/common/PageHeader';
import StatusTag from '@/components/common/StatusTag';
import { paymentApi } from '@/api/services';
import { PAYMENT_STATUS_COLOR } from '@/constants/permissions';
import { formatDateTime, formatMoney } from '@/utils/format';
import type { Payment } from '@/types';

const PaymentHistoryPage = () => {
  const navigate = useNavigate();
  const [data, setData] = useState<Payment[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const result = await paymentApi.search({ page, size: 10 });
      setData(result.content);
      setTotal(result.totalElements);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    void load();
  }, [load]);

  const columns: ColumnsType<Payment> = [
    { title: 'Mã giao dịch', dataIndex: 'txnRef', width: 200 },
    {
      title: 'Đơn hàng',
      dataIndex: 'orderCode',
      width: 150,
      render: (value: string, record) => (
        <Button type="link" style={{ padding: 0 }} onClick={() => navigate(`/orders/${record.orderId}`)}>
          {value}
        </Button>
      ),
    },
    { title: 'Cổng thanh toán', dataIndex: 'provider', width: 200, render: (value) => <StatusTag value={value} /> },
    {
      title: 'Số tiền',
      dataIndex: 'amount',
      width: 140,
      align: 'right',
      render: (value: number) => formatMoney(value),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      width: 170,
      render: (value) => <StatusTag value={value} colorMap={PAYMENT_STATUS_COLOR} />,
    },
    { title: 'Mã GD cổng', dataIndex: 'transactionNo', width: 160 },
    { title: 'Ngân hàng', dataIndex: 'bankCode', width: 120 },
    { title: 'Mã phản hồi', dataIndex: 'responseCode', width: 120 },
    {
      title: 'Thanh toán lúc',
      dataIndex: 'paidAt',
      width: 160,
      render: (value: string) => formatDateTime(value),
    },
    { title: 'Khởi tạo lúc', dataIndex: 'createdDate', width: 160, render: (value: string) => formatDateTime(value) },
  ];

  return (
    <div>
      <PageHeader
        title="Lịch sử thanh toán"
        subtitle="Toàn bộ giao dịch qua cổng thanh toán online"
        extra={
          <Button icon={<ReloadOutlined />} onClick={() => void load()}>
            Tải lại
          </Button>
        }
      />

      <Card>
        <Table<Payment>
          rowKey="id"
          columns={columns}
          dataSource={data}
          loading={loading}
          scroll={{ x: 1600 }}
          pagination={{
            current: page + 1,
            pageSize: 10,
            total,
            showTotal: (value) => `Tổng ${value} giao dịch`,
            onChange: (nextPage) => setPage(nextPage - 1),
          }}
          locale={{ emptyText: 'Chưa có giao dịch nào' }}
        />
      </Card>
    </div>
  );
};

export default PaymentHistoryPage;
