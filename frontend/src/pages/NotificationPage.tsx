import { useCallback, useEffect, useState } from 'react';
import { Button, Card, List, Tag, Typography, message } from 'antd';
import { CheckOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import PageHeader from '@/components/common/PageHeader';
import { notificationApi } from '@/api/services';
import { formatDateTime } from '@/utils/format';
import type { NotificationItem } from '@/types';

const NotificationPage = () => {
  const navigate = useNavigate();
  const [items, setItems] = useState<NotificationItem[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const result = await notificationApi.search({ page, size: 20 });
      setItems(result.content);
      setTotal(result.totalElements);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    void load();
  }, [load]);

  const handleMarkAll = async () => {
    const count = await notificationApi.markAllRead();
    message.success(`Đã đánh dấu ${count} thông báo là đã đọc`);
    void load();
  };

  return (
    <div>
      <PageHeader
        title="Thông báo"
        subtitle="Thông báo được lưu trong hệ thống và đẩy realtime qua WebSocket"
        extra={
          <Button icon={<CheckOutlined />} onClick={handleMarkAll}>
            Đánh dấu tất cả đã đọc
          </Button>
        }
      />

      <Card>
        <List
          loading={loading}
          dataSource={items}
          locale={{ emptyText: 'Chưa có thông báo' }}
          pagination={{
            current: page + 1,
            pageSize: 20,
            total,
            onChange: (nextPage) => setPage(nextPage - 1),
          }}
          renderItem={(item) => (
            <List.Item
              className="cursor-pointer"
              onClick={async () => {
                if (!item.isRead) {
                  await notificationApi.markRead(item.id);
                  void load();
                }
                if (item.referenceId) {
                  navigate(`/orders/${item.referenceId}`);
                }
              }}
              actions={[item.isRead ? <Tag key="read">Đã đọc</Tag> : <Tag key="unread" color="red">Mới</Tag>]}
            >
              <List.Item.Meta
                title={<span style={{ fontWeight: item.isRead ? 400 : 600 }}>{item.title}</span>}
                description={
                  <>
                    <div>{item.content}</div>
                    <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                      {item.type.description} · {formatDateTime(item.createdDate)}
                      {item.referenceCode ? ` · ${item.referenceCode}` : ''}
                    </Typography.Text>
                  </>
                }
              />
            </List.Item>
          )}
        />
      </Card>
    </div>
  );
};

export default NotificationPage;
