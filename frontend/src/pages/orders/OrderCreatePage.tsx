import { useState } from 'react';
import {
  Alert,
  Button,
  Card,
  Checkbox,
  Col,
  Descriptions,
  Divider,
  Form,
  Input,
  InputNumber,
  Radio,
  Row,
  Select,
  Space,
  message,
} from 'antd';
import { CalculatorOutlined, PlusOutlined, MinusCircleOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import PageHeader from '@/components/common/PageHeader';
import { orderApi, pricingApi } from '@/api/services';
import { formatMoney } from '@/utils/format';
import type { FeePreview } from '@/types';

/**
 * Toa do mau cua mot so quan tai Ha Noi, giup dien nhanh khi demo
 * vi he thong chua tich hop dich vu geocoding.
 */
const DISTRICT_PRESETS = [
  { label: 'Cầu Giấy, Hà Nội', district: 'Cầu Giấy', province: 'Hà Nội', latitude: 21.0313, longitude: 105.7967 },
  { label: 'Hoàn Kiếm, Hà Nội', district: 'Hoàn Kiếm', province: 'Hà Nội', latitude: 21.0285, longitude: 105.8542 },
  { label: 'Đống Đa, Hà Nội', district: 'Đống Đa', province: 'Hà Nội', latitude: 21.0136, longitude: 105.829 },
  { label: 'Hai Bà Trưng, Hà Nội', district: 'Hai Bà Trưng', province: 'Hà Nội', latitude: 21.0075, longitude: 105.85 },
  { label: 'Thanh Xuân, Hà Nội', district: 'Thanh Xuân', province: 'Hà Nội', latitude: 20.995, longitude: 105.806 },
  { label: 'Long Biên, Hà Nội', district: 'Long Biên', province: 'Hà Nội', latitude: 21.045, longitude: 105.89 },
];

const SERVICE_TYPE_OPTIONS = [
  { value: 'STANDARD', label: 'Giao tiêu chuẩn (2-3 ngày)' },
  { value: 'EXPRESS', label: 'Giao nhanh (24h)' },
  { value: 'SAME_DAY', label: 'Giao trong ngày (6h)' },
];

const OrderCreatePage = () => {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const [preview, setPreview] = useState<FeePreview | null>(null);
  const [previewing, setPreviewing] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const applyPreset = (fieldPrefix: 'pickup' | 'delivery', presetLabel: string) => {
    const preset = DISTRICT_PRESETS.find((item) => item.label === presetLabel);
    if (!preset) {
      return;
    }
    form.setFieldsValue({
      [`${fieldPrefix}District`]: preset.district,
      [`${fieldPrefix}Province`]: preset.province,
      [`${fieldPrefix}Latitude`]: preset.latitude,
      [`${fieldPrefix}Longitude`]: preset.longitude,
    });
  };

  const handlePreview = async () => {
    try {
      const values = await form.validateFields([
        'serviceType',
        'weightKg',
        'pickupLatitude',
        'pickupLongitude',
        'deliveryLatitude',
        'deliveryLongitude',
      ]);
      setPreviewing(true);
      const result = await pricingApi.preview({
        serviceType: values.serviceType,
        weightKg: values.weightKg,
        pickupLatitude: values.pickupLatitude,
        pickupLongitude: values.pickupLongitude,
        deliveryLatitude: values.deliveryLatitude,
        deliveryLongitude: values.deliveryLongitude,
        codAmount: form.getFieldValue('codAmount') ?? 0,
        fragile: form.getFieldValue('fragile') ?? false,
        remoteArea: form.getFieldValue('remoteArea') ?? false,
        voucherCode: form.getFieldValue('voucherCode'),
      });
      setPreview(result);
    } catch {
      message.warning('Vui lòng nhập đủ thông tin dịch vụ, khối lượng và toạ độ hai điểm');
    } finally {
      setPreviewing(false);
    }
  };

  const handleSubmit = async (values: Record<string, unknown>) => {
    setSubmitting(true);
    try {
      const order = await orderApi.create(values);
      message.success(`Đã tạo đơn hàng ${order.orderCode}`);
      navigate(`/orders/${order.id}`);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <PageHeader title="Tạo đơn hàng" subtitle="Hệ thống tự tính cước và áp dụng voucher nếu hợp lệ" />

      <Form
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
        initialValues={{ serviceType: 'STANDARD', paymentMethod: 'COD', weightKg: 1, codAmount: 0 }}
      >
        <Row gutter={16}>
          <Col xs={24} lg={16}>
            <Card title="Thông tin người gửi và điểm lấy hàng" className="mb-4">
              <Row gutter={12}>
                <Col xs={24} md={12}>
                  <Form.Item name="senderName" label="Tên người gửi" rules={[{ required: true, message: 'Bắt buộc' }]}>
                    <Input />
                  </Form.Item>
                </Col>
                <Col xs={24} md={12}>
                  <Form.Item
                    name="senderPhone"
                    label="Số điện thoại người gửi"
                    rules={[
                      { required: true, message: 'Bắt buộc' },
                      { pattern: /^0\d{9,10}$/, message: 'Số điện thoại không đúng định dạng' },
                    ]}
                  >
                    <Input />
                  </Form.Item>
                </Col>
                <Col span={24}>
                  <Form.Item name="pickupAddress" label="Địa chỉ lấy hàng" rules={[{ required: true, message: 'Bắt buộc' }]}>
                    <Input placeholder="Số nhà, đường..." />
                  </Form.Item>
                </Col>
                <Col span={24}>
                  <Form.Item label="Chọn nhanh quận/huyện lấy hàng">
                    <Select
                      placeholder="Chọn để tự điền quận/huyện và toạ độ"
                      options={DISTRICT_PRESETS.map((item) => ({ value: item.label, label: item.label }))}
                      onChange={(value) => applyPreset('pickup', value)}
                    />
                  </Form.Item>
                </Col>
                <Col xs={24} md={6}>
                  <Form.Item name="pickupDistrict" label="Quận/Huyện">
                    <Input />
                  </Form.Item>
                </Col>
                <Col xs={24} md={6}>
                  <Form.Item name="pickupProvince" label="Tỉnh/Thành">
                    <Input />
                  </Form.Item>
                </Col>
                <Col xs={24} md={6}>
                  <Form.Item name="pickupLatitude" label="Vĩ độ" rules={[{ required: true, message: 'Bắt buộc' }]}>
                    <InputNumber className="w-full" step={0.0001} />
                  </Form.Item>
                </Col>
                <Col xs={24} md={6}>
                  <Form.Item name="pickupLongitude" label="Kinh độ" rules={[{ required: true, message: 'Bắt buộc' }]}>
                    <InputNumber className="w-full" step={0.0001} />
                  </Form.Item>
                </Col>
              </Row>
            </Card>

            <Card title="Thông tin người nhận và điểm giao hàng" className="mb-4">
              <Row gutter={12}>
                <Col xs={24} md={12}>
                  <Form.Item name="receiverName" label="Tên người nhận" rules={[{ required: true, message: 'Bắt buộc' }]}>
                    <Input />
                  </Form.Item>
                </Col>
                <Col xs={24} md={12}>
                  <Form.Item
                    name="receiverPhone"
                    label="Số điện thoại người nhận"
                    rules={[
                      { required: true, message: 'Bắt buộc' },
                      { pattern: /^0\d{9,10}$/, message: 'Số điện thoại không đúng định dạng' },
                    ]}
                  >
                    <Input />
                  </Form.Item>
                </Col>
                <Col span={24}>
                  <Form.Item
                    name="deliveryAddress"
                    label="Địa chỉ giao hàng"
                    rules={[{ required: true, message: 'Bắt buộc' }]}
                  >
                    <Input placeholder="Số nhà, đường..." />
                  </Form.Item>
                </Col>
                <Col span={24}>
                  <Form.Item label="Chọn nhanh quận/huyện giao hàng">
                    <Select
                      placeholder="Chọn để tự điền quận/huyện và toạ độ"
                      options={DISTRICT_PRESETS.map((item) => ({ value: item.label, label: item.label }))}
                      onChange={(value) => applyPreset('delivery', value)}
                    />
                  </Form.Item>
                </Col>
                <Col xs={24} md={6}>
                  <Form.Item name="deliveryDistrict" label="Quận/Huyện">
                    <Input />
                  </Form.Item>
                </Col>
                <Col xs={24} md={6}>
                  <Form.Item name="deliveryProvince" label="Tỉnh/Thành">
                    <Input />
                  </Form.Item>
                </Col>
                <Col xs={24} md={6}>
                  <Form.Item name="deliveryLatitude" label="Vĩ độ" rules={[{ required: true, message: 'Bắt buộc' }]}>
                    <InputNumber className="w-full" step={0.0001} />
                  </Form.Item>
                </Col>
                <Col xs={24} md={6}>
                  <Form.Item name="deliveryLongitude" label="Kinh độ" rules={[{ required: true, message: 'Bắt buộc' }]}>
                    <InputNumber className="w-full" step={0.0001} />
                  </Form.Item>
                </Col>
              </Row>
            </Card>

            <Card title="Kiện hàng">
              <Row gutter={12}>
                <Col xs={24} md={8}>
                  <Form.Item
                    name="weightKg"
                    label="Khối lượng (kg)"
                    rules={[{ required: true, message: 'Bắt buộc' }]}
                  >
                    <InputNumber className="w-full" min={0.1} step={0.1} />
                  </Form.Item>
                </Col>
                <Col xs={24} md={16}>
                  <Form.Item name="packageDescription" label="Mô tả hàng hóa">
                    <Input placeholder="Ví dụ: quần áo, đồ điện tử..." />
                  </Form.Item>
                </Col>
                <Col xs={8} md={5}>
                  <Form.Item name="lengthCm" label="Dài (cm)">
                    <InputNumber className="w-full" min={0} />
                  </Form.Item>
                </Col>
                <Col xs={8} md={5}>
                  <Form.Item name="widthCm" label="Rộng (cm)">
                    <InputNumber className="w-full" min={0} />
                  </Form.Item>
                </Col>
                <Col xs={8} md={5}>
                  <Form.Item name="heightCm" label="Cao (cm)">
                    <InputNumber className="w-full" min={0} />
                  </Form.Item>
                </Col>
                <Col xs={24} md={9}>
                  <Form.Item label=" ">
                    <Space>
                      <Form.Item name="fragile" valuePropName="checked" noStyle>
                        <Checkbox>Hàng dễ vỡ</Checkbox>
                      </Form.Item>
                      <Form.Item name="remoteArea" valuePropName="checked" noStyle>
                        <Checkbox>Vùng xa</Checkbox>
                      </Form.Item>
                    </Space>
                  </Form.Item>
                </Col>
              </Row>

              <Divider orientation="left" plain>
                Danh sách mặt hàng (không bắt buộc)
              </Divider>
              <Form.List name="items">
                {(fields, { add, remove }) => (
                  <>
                    {fields.map((field) => (
                      <Row gutter={8} key={field.key} align="middle">
                        <Col xs={24} md={9}>
                          <Form.Item
                            name={[field.name, 'itemName']}
                            rules={[{ required: true, message: 'Nhập tên mặt hàng' }]}
                          >
                            <Input placeholder="Tên mặt hàng" />
                          </Form.Item>
                        </Col>
                        <Col xs={8} md={4}>
                          <Form.Item name={[field.name, 'quantity']} initialValue={1}>
                            <InputNumber className="w-full" min={1} placeholder="SL" />
                          </Form.Item>
                        </Col>
                        <Col xs={8} md={5}>
                          <Form.Item name={[field.name, 'unitPrice']}>
                            <InputNumber className="w-full" min={0} placeholder="Đơn giá" />
                          </Form.Item>
                        </Col>
                        <Col xs={8} md={4}>
                          <Form.Item name={[field.name, 'weightKg']}>
                            <InputNumber className="w-full" min={0} step={0.1} placeholder="KL (kg)" />
                          </Form.Item>
                        </Col>
                        <Col md={2}>
                          <MinusCircleOutlined onClick={() => remove(field.name)} className="text-red-500" />
                        </Col>
                      </Row>
                    ))}
                    <Button type="dashed" onClick={() => add()} icon={<PlusOutlined />} block>
                      Thêm mặt hàng
                    </Button>
                  </>
                )}
              </Form.List>
            </Card>
          </Col>

          <Col xs={24} lg={8}>
            <Card title="Dịch vụ và thanh toán" className="mb-4">
              <Form.Item name="serviceType" label="Loại dịch vụ" rules={[{ required: true }]}>
                <Select options={SERVICE_TYPE_OPTIONS} />
              </Form.Item>
              <Form.Item name="paymentMethod" label="Phương thức thanh toán" rules={[{ required: true }]}>
                <Radio.Group>
                  <Radio.Button value="COD">Thu tiền khi nhận</Radio.Button>
                  <Radio.Button value="VNPAY">Thanh toán VNPay</Radio.Button>
                </Radio.Group>
              </Form.Item>
              <Form.Item name="codAmount" label="Tiền thu hộ (COD)">
                <InputNumber className="w-full" min={0} step={10000} />
              </Form.Item>
              <Form.Item name="voucherCode" label="Mã giảm giá">
                <Input placeholder="FREESHIP, GIAM20, GIAM10K" />
              </Form.Item>
              <Form.Item name="note" label="Ghi chú">
                <Input.TextArea rows={2} />
              </Form.Item>
              <Button icon={<CalculatorOutlined />} block loading={previewing} onClick={handlePreview}>
                Tính cước tạm tính
              </Button>
            </Card>

            {preview && (
              <Card title="Cước tạm tính" className="mb-4">
                <Descriptions column={1} size="small" bordered>
                  <Descriptions.Item label="Quãng đường">{preview.distanceKm} km</Descriptions.Item>
                  <Descriptions.Item label="Phí cơ bản">{formatMoney(preview.baseFee)}</Descriptions.Item>
                  <Descriptions.Item label="Phí quãng đường">{formatMoney(preview.distanceFee)}</Descriptions.Item>
                  <Descriptions.Item label="Phí khối lượng">{formatMoney(preview.weightFee)}</Descriptions.Item>
                  <Descriptions.Item label="Phụ phí">{formatMoney(preview.surcharge)}</Descriptions.Item>
                  <Descriptions.Item label="Phí thu hộ">{formatMoney(preview.codFee)}</Descriptions.Item>
                  <Descriptions.Item label="Tổng cước">{formatMoney(preview.shippingFee)}</Descriptions.Item>
                  <Descriptions.Item label="Giảm giá">-{formatMoney(preview.discountAmount)}</Descriptions.Item>
                  <Descriptions.Item label="Phải trả">
                    <strong className="text-red-600">{formatMoney(preview.totalAmount)}</strong>
                  </Descriptions.Item>
                </Descriptions>
                {preview.voucherMessage && (
                  <Alert type="warning" showIcon className="mt-3" message={preview.voucherMessage} />
                )}
                {preview.voucherApplied && (
                  <Alert type="success" showIcon className="mt-3" message="Đã áp dụng mã giảm giá" />
                )}
              </Card>
            )}

            <Card>
              <Button type="primary" htmlType="submit" block size="large" loading={submitting}>
                Tạo đơn hàng
              </Button>
            </Card>
          </Col>
        </Row>
      </Form>
    </div>
  );
};

export default OrderCreatePage;
