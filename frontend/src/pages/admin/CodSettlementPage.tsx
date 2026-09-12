import { useCallback, useEffect, useState } from 'react';
import { Button, Card, Input, Modal, Select, Space, Table, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { CheckCircleOutlined, ReloadOutlined } from '@ant-design/icons';
import PageHeader from '@/components/common/PageHeader';
import StatusTag from '@/components/common/StatusTag';
import { codApi } from '@/api/services';
import { COD_STATUS_COLOR } from '@/constants/permissions';
import { formatDateTime, formatMoney } from '@/utils/format';
import type { CodSettlement } from '@/types';

const CodSettlementPage = () => {
  const [data, setData] = useState<CodSettlement[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [statuses, setStatuses] = useState<string[]>(['SUBMITTED']);
  const [page, setPage] = useState(0);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const result = await codApi.search({ statuses, page, size: 10 });
      setData(result.content);
      setTotal(result.totalElements);
    } finally {
      setLoading(false);
    }
  }, [statuses, page]);

  useEffect(() => {
    void load();
  }, [load]);

  const handleConfirm = (record: CodSettlement) => {
    let note = '';
    Modal.confirm({
      title: `Xác nhận đã nhận ${formatMoney(record.amount)} từ ${record.shipperName}?`,
      content: (
        <Input.TextArea
          rows={2}
          placeholder="Ghi chú đối soát"
          onChange={(event) => {
            note = event.target.value;
          }}
        />
      ),
      okText: 'Xác nhận',
      cancelText: 'Đóng',
      onOk: async () => {
        await codApi.confirm(record.id, note);
        message.success('Đã xác nhận đối soát');
        void load();
      },
    });
  };

  const columns: ColumnsType<CodSettlement> = [
    { title: 'Mã vận đơn', dataIndex: 'orderCode', width: 150 },
    { title: 'Người nhận', dataIndex: 'receiverName', width: 160 },
    { title: 'Shipper', width: 200, render: (_, record) => `${record.shipperCode} - ${record.shipperName}` },
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
      title: 'Thao tác',
      width: 140,
      fixed: 'right',
      render: (_, record) =>
        record.status.code === 'SUBMITTED' ? (
          <Button size="small" type="primary" icon={<CheckCircleOutlined />} onClick={() => handleConfirm(record)}>
            Xác nhận
          </Button>
        ) : (
          '-'
        ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Đối soát tiền COD"
        subtitle="Xác nhận đã nhận đủ tiền thu hộ do shipper nộp lại"
        extra={
          <Space>
            <Select
              mode="multiple"
              className="min-w-56"
              value={statuses}
              onChange={setStatuses}
              placeholder="Lọc theo trạng thái"
              options={[
                { value: 'HOLDING', label: 'Shipper đang giữ tiền' },
                { value: 'SUBMITTED', label: 'Đã nộp, chờ xác nhận' },
                { value: 'CONFIRMED', label: 'Kế toán đã xác nhận' },
              ]}
            />
            <Button icon={<ReloadOutlined />} onClick={() => void load()}>
              Tải lại
            </Button>
          </Space>
        }
      />

      <Card>
        <Table<CodSettlement>
          rowKey="id"
          columns={columns}
          dataSource={data}
          loading={loading}
          scroll={{ x: 1350 }}
          pagination={{
            current: page + 1,
            pageSize: 10,
            total,
            showTotal: (value) => `Tổng ${value} khoản`,
            onChange: (nextPage) => setPage(nextPage - 1),
          }}
          locale={{ emptyText: 'Không có khoản COD nào' }}
        />
      </Card>
    </div>
  );
};

export default CodSettlementPage;
