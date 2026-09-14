import { useCallback, useEffect, useState } from 'react';
import { Button, Card, Result, Spin, Descriptions } from 'antd';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { paymentApi } from '@/api/services';
import { formatMoney } from '@/utils/format';
import type { PaymentResult } from '@/types';

/**
 * Trang nhận kết quả VNPay: gửi lại toàn bộ vnp_* cho backend xác thực chữ ký.
 */
const PaymentResultPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [result, setResult] = useState<PaymentResult | null>(null);
  const [error, setError] = useState<string | null>(null);

  const isMockMode = searchParams.get('mock') === 'true';
  const txnRef = searchParams.get('txnRef') ?? searchParams.get('vnp_TxnRef');

  const verifyRealPayment = useCallback(async () => {
    const params: Record<string, string> = {};
    searchParams.forEach((value, key) => {
      if (key.startsWith('vnp_')) {
        params[key] = value;
      }
    });

    if (Object.keys(params).length === 0) {
      setError('Không nhận được dữ liệu thanh toán từ cổng');
      setLoading(false);
      return;
    }

    try {
      setResult(await paymentApi.handleReturn(params));
    } catch {
      setError('Không xác thực được kết quả thanh toán');
    } finally {
      setLoading(false);
    }
  }, [searchParams]);

  useEffect(() => {
    if (isMockMode) {
      setLoading(false);
      return;
    }
    void verifyRealPayment();
  }, [isMockMode, verifyRealPayment]);

  const completeMock = async (success: boolean) => {
    if (!txnRef) {
      return;
    }
    setLoading(true);
    try {
      setResult(await paymentApi.completeMock(txnRef, success));
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Spin size="large" tip="Đang xử lý kết quả thanh toán..." />
      </div>
    );
  }

  if (isMockMode && !result) {
    return (
      <div className="min-h-screen flex items-center justify-center p-4">
        <Card style={{ width: 520 }}>
          <Result
            status="info"
            title="Cổng thanh toán giả lập"
            subTitle="Hệ thống chưa cấu hình VNPay sandbox nên dùng cổng giả lập để hoàn tất quy trình demo."
          />
          <Descriptions column={1} size="small" bordered className="mb-4">
            <Descriptions.Item label="Mã giao dịch">{txnRef}</Descriptions.Item>
            <Descriptions.Item label="Đơn hàng">{searchParams.get('orderCode')}</Descriptions.Item>
            <Descriptions.Item label="Số tiền">
              {formatMoney(Number(searchParams.get('amount') ?? 0))}
            </Descriptions.Item>
          </Descriptions>
          <div className="flex gap-2">
            <Button type="primary" block onClick={() => void completeMock(true)}>
              Thanh toán thành công
            </Button>
            <Button danger block onClick={() => void completeMock(false)}>
              Thanh toán thất bại
            </Button>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-4">
      <Card style={{ width: 560 }}>
        {error ? (
          <Result status="error" title="Không xác thực được giao dịch" subTitle={error} />
        ) : (
          <Result
            status={result?.success ? 'success' : 'error'}
            title={result?.success ? 'Thanh toán thành công' : 'Thanh toán không thành công'}
            subTitle={result?.message}
          />
        )}
        {result && (
          <Descriptions column={1} size="small" bordered className="mb-4">
            <Descriptions.Item label="Mã giao dịch">{result.txnRef}</Descriptions.Item>
            <Descriptions.Item label="Đơn hàng">{result.orderCode}</Descriptions.Item>
            <Descriptions.Item label="Số tiền">{formatMoney(result.amount)}</Descriptions.Item>
            {result.responseCode && (
              <Descriptions.Item label="Mã phản hồi">{result.responseCode}</Descriptions.Item>
            )}
          </Descriptions>
        )}
        <div className="flex gap-2">
          {result?.orderId && (
            <Button type="primary" block onClick={() => navigate(`/orders/${result.orderId}`)}>
              Xem đơn hàng
            </Button>
          )}
          <Button block onClick={() => navigate('/orders')}>
            Về danh sách đơn
          </Button>
        </div>
      </Card>
    </div>
  );
};

export default PaymentResultPage;
