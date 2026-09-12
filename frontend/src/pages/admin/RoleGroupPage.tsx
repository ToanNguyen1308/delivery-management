import { useCallback, useEffect, useMemo, useState } from 'react';
import { Button, Card, Checkbox, Col, Collapse, Form, Input, Modal, Row, Space, Table, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { PlusOutlined } from '@ant-design/icons';
import PageHeader from '@/components/common/PageHeader';
import { roleApi } from '@/api/services';
import { useAuthStore } from '@/store/authStore';
import { PERMISSION } from '@/constants/permissions';
import type { FunctionItem, RoleGroup } from '@/types';

const MODULE_LABELS: Record<string, string> = {
  USER: 'Người dùng',
  ROLE: 'Nhóm quyền',
  SHIPPER: 'Shipper',
  ORDER: 'Đơn hàng',
  DISPATCH: 'Điều phối',
  TRACKING: 'Tracking',
  PRICING: 'Bảng phí',
  VOUCHER: 'Voucher',
  PAYMENT: 'Thanh toán',
  DASHBOARD: 'Dashboard',
};

const RoleGroupPage = () => {
  const canManage = useAuthStore((state) => state.permissions.includes(PERMISSION.ROLE_MANAGE));
  const [form] = Form.useForm();
  const [roles, setRoles] = useState<RoleGroup[]>([]);
  const [functions, setFunctions] = useState<FunctionItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<RoleGroup | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [roleList, functionList] = await Promise.all([roleApi.list(), roleApi.functions()]);
      setRoles(roleList);
      setFunctions(functionList);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  /** Nhom chuc nang theo module de form chon quyen de doc hon. */
  const functionsByModule = useMemo(() => {
    return functions.reduce<Record<string, FunctionItem[]>>((acc, item) => {
      const key = item.module ?? 'KHAC';
      acc[key] = [...(acc[key] ?? []), item];
      return acc;
    }, {});
  }, [functions]);

  const handleSubmit = async (values: Record<string, unknown>) => {
    if (editing) {
      await roleApi.update(editing.id, values);
      message.success('Đã cập nhật nhóm quyền');
    } else {
      await roleApi.create(values);
      message.success('Đã tạo nhóm quyền');
    }
    setModalOpen(false);
    setEditing(null);
    void load();
  };

  const columns: ColumnsType<RoleGroup> = [
    { title: 'Mã nhóm quyền', dataIndex: 'roleGroupCode', width: 160 },
    { title: 'Tên nhóm quyền', dataIndex: 'roleGroupName', width: 200 },
    { title: 'Mô tả', dataIndex: 'description' },
    {
      title: 'Số chức năng',
      width: 130,
      align: 'center',
      render: (_, record) => <Tag color="red">{record.functions.length}</Tag>,
    },
    ...(canManage
      ? [
          {
            title: 'Thao tác',
            width: 110,
            render: (_: unknown, record: RoleGroup) => (
              <Button
                size="small"
                onClick={() => {
                  setEditing(record);
                  setModalOpen(true);
                  form.setFieldsValue({
                    roleGroupCode: record.roleGroupCode,
                    roleGroupName: record.roleGroupName,
                    description: record.description,
                    functionIds: record.functions.map((item) => item.id),
                  });
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
      <PageHeader
        title="Nhóm quyền và chức năng"
        subtitle="Phân quyền theo function_code, menu và API đều kiểm tra theo danh sách này"
        extra={
          canManage && (
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => {
                form.resetFields();
                setEditing(null);
                setModalOpen(true);
              }}
            >
              Tạo nhóm quyền
            </Button>
          )
        }
      />

      <Card className="mb-4">
        <Table<RoleGroup>
          rowKey="id"
          columns={columns}
          dataSource={roles}
          loading={loading}
          pagination={false}
          expandable={{
            expandedRowRender: (record) => (
              <Space wrap>
                {record.functions.map((item) => (
                  <Tag key={item.id}>{item.functionName}</Tag>
                ))}
              </Space>
            ),
          }}
        />
      </Card>

      <Card title="Toàn bộ chức năng hệ thống">
        <Collapse
          items={Object.entries(functionsByModule).map(([module, items]) => ({
            key: module,
            label: `${MODULE_LABELS[module] ?? module} (${items.length})`,
            children: (
              <Row gutter={[8, 8]}>
                {items.map((item) => (
                  <Col xs={24} md={12} lg={8} key={item.id}>
                    <Tag className="w-full" style={{ whiteSpace: 'normal' }}>
                      <strong>{item.functionCode}</strong>
                      <br />
                      {item.functionName}
                    </Tag>
                  </Col>
                ))}
              </Row>
            ),
          }))}
        />
      </Card>

      <Modal
        open={modalOpen}
        width={760}
        title={editing ? `Cập nhật ${editing.roleGroupCode}` : 'Tạo nhóm quyền'}
        onCancel={() => {
          setModalOpen(false);
          setEditing(null);
        }}
        onOk={() => form.submit()}
        okText="Lưu"
        cancelText="Đóng"
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Row gutter={12}>
            <Col xs={24} md={12}>
              <Form.Item
                name="roleGroupCode"
                label="Mã nhóm quyền"
                rules={[
                  { required: true, message: 'Bắt buộc' },
                  { pattern: /^[A-Z0-9_]+$/, message: 'Chỉ gồm chữ in hoa, số và gạch dưới' },
                ]}
              >
                <Input placeholder="ACCOUNTANT" />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="roleGroupName" label="Tên nhóm quyền" rules={[{ required: true, message: 'Bắt buộc' }]}>
                <Input placeholder="Kế toán" />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="description" label="Mô tả">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item
            name="functionIds"
            label="Chức năng được phép"
            rules={[{ required: true, message: 'Chọn ít nhất một chức năng' }]}
          >
            <Checkbox.Group className="w-full">
              <Collapse
                className="w-full"
                items={Object.entries(functionsByModule).map(([module, items]) => ({
                  key: module,
                  label: MODULE_LABELS[module] ?? module,
                  children: (
                    <Row gutter={[8, 8]}>
                      {items.map((item) => (
                        <Col xs={24} md={12} key={item.id}>
                          <Checkbox value={item.id}>{item.functionName}</Checkbox>
                        </Col>
                      ))}
                    </Row>
                  ),
                }))}
              />
            </Checkbox.Group>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default RoleGroupPage;
