import { useCallback, useEffect, useState } from 'react';
import { Alert, Button, Card, Form, InputNumber, Modal, Switch, Table, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import PageHeader from '@/components/common/PageHeader';
import StatusTag from '@/components/common/StatusTag';
import { pricingApi } from '@/api/services';
import { useAuthStore } from '@/store/authStore';
import { PERMISSION } from '@/constants/permissions';
import { formatMoney } from '@/utils/format';
import type { PricingRule } from '@/types';

const PricingPage = () => {
  const canManage = useAuthStore((state) => state.permissions.includes(PERMISSION.PRICING_MANAGE));
  const [form] = Form.useForm();
  const [rules, setRules] = useState<PricingRule[]>([]);
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState<PricingRule | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setRules(await pricingApi.rules());
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const handleSubmit = async (values: Record<string, unknown>) => {
    if (!editing) {
      return;
    }
    await pricingApi.updateRule(editing.id, values);
    message.success('Đã cập nhật bảng phí');
    setEditing(null);
    void load();
  };

  const columns: ColumnsType<PricingRule> = [
    { title: 'Loại dịch vụ', dataIndex: 'serviceType', width: 200, render: (value) => <StatusTag value={value} /> },
    {
      title: 'Phí cơ bản',
      dataIndex: 'baseFee',
      align: 'right',
      render: (value: number) => formatMoney(value),
    },
    { title: 'Quãng đường gồm trong phí', dataIndex: 'baseDistanceKm', align: 'right', render: (v: number) => `${v} km` },
    {
      title: 'Phí mỗi km vượt',
      dataIndex: 'perKmFee',
      align: 'right',
      render: (value: number) => formatMoney(value),
    },
    { title: 'Khối lượng gồm trong phí', dataIndex: 'baseWeightKg', align: 'right', render: (v: number) => `${v} kg` },
    {
      title: 'Phí mỗi kg vượt',
      dataIndex: 'perKgFee',
      align: 'right',
      render: (value: number) => formatMoney(value),
    },
    {
      title: 'Phụ phí vùng xa',
      dataIndex: 'remoteAreaSurcharge',
      align: 'right',
      render: (value: number) => formatMoney(value),
    },
    { title: 'Phí COD (%)', dataIndex: 'codFeePercent', align: 'right', render: (v: number) => `${v}%` },
    {
      title: 'Phí tối thiểu',
      dataIndex: 'minFee',
      align: 'right',
      render: (value: number) => formatMoney(value),
    },
    {
      title: 'Áp dụng',
      dataIndex: 'active',
      align: 'center',
      width: 90,
      render: (value: boolean) => <Switch checked={value} disabled size="small" />,
    },
    ...(canManage
      ? [
          {
            title: 'Thao tác',
            width: 100,
            render: (_: unknown, record: PricingRule) => (
              <Button
                size="small"
                onClick={() => {
                  setEditing(record);
                  form.setFieldsValue(record);
                }}
              >
                Sửa
              </Button>
            ),
          },
        ]
      : []),
  ];

  return (
    <div>
      <PageHeader title="Cấu hình bảng phí" subtitle="Công thức tính cước áp dụng cho từng loại dịch vụ" />

      <Alert
        className="mb-4"
        type="info"
        showIcon
        message="Công thức tính cước"
        description="Cước = Phí cơ bản + (quãng đường vượt × phí mỗi km) + (khối lượng vượt × phí mỗi kg) + phụ phí + phí thu hộ COD. Nếu nhỏ hơn phí tối thiểu thì lấy phí tối thiểu. Giao nhanh cộng thêm 15% phần phí quãng đường, giao trong ngày đặt sau 15h cộng thêm 20% phụ phí giờ cao điểm."
      />

      <Card>
        <Table<PricingRule>
          rowKey="id"
          columns={columns}
          dataSource={rules}
          loading={loading}
          pagination={false}
          scroll={{ x: 1500 }}
        />
      </Card>

      <Modal
        open={Boolean(editing)}
        title={`Cập nhật bảng phí: ${editing?.serviceType.description ?? ''}`}
        onCancel={() => setEditing(null)}
        onOk={() => form.submit()}
        okText="Lưu"
        cancelText="Đóng"
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="baseFee" label="Phí cơ bản (đ)" rules={[{ required: true, message: 'Bắt buộc' }]}>
            <InputNumber className="w-full" min={0} step={1000} />
          </Form.Item>
          <Form.Item
            name="baseDistanceKm"
            label="Quãng đường đã gồm trong phí cơ bản (km)"
            rules={[{ required: true, message: 'Bắt buộc' }]}
          >
            <InputNumber className="w-full" min={0} step={0.5} />
          </Form.Item>
          <Form.Item name="perKmFee" label="Phí mỗi km vượt (đ)" rules={[{ required: true, message: 'Bắt buộc' }]}>
            <InputNumber className="w-full" min={0} step={500} />
          </Form.Item>
          <Form.Item
            name="baseWeightKg"
            label="Khối lượng đã gồm trong phí cơ bản (kg)"
            rules={[{ required: true, message: 'Bắt buộc' }]}
          >
            <InputNumber className="w-full" min={0} step={0.5} />
          </Form.Item>
          <Form.Item name="perKgFee" label="Phí mỗi kg vượt (đ)" rules={[{ required: true, message: 'Bắt buộc' }]}>
            <InputNumber className="w-full" min={0} step={500} />
          </Form.Item>
          <Form.Item name="remoteAreaSurcharge" label="Phụ phí vùng xa (đ)">
            <InputNumber className="w-full" min={0} step={1000} />
          </Form.Item>
          <Form.Item name="codFeePercent" label="Phí thu hộ COD (%)">
            <InputNumber className="w-full" min={0} max={100} step={0.5} />
          </Form.Item>
          <Form.Item name="minFee" label="Phí tối thiểu (đ)">
            <InputNumber className="w-full" min={0} step={1000} />
          </Form.Item>
          <Form.Item name="active" label="Đang áp dụng" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default PricingPage;
