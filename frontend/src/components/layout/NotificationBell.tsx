import { useCallback, useEffect, useMemo, useState } from 'react';
import { Badge, Button, Dropdown, Empty, List, Typography, notification as antdNotification } from 'antd';
import { BellOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { notificationApi } from '@/api/services';
import { useStomp } from '@/hooks/useStomp';
import { formatDateTime } from '@/utils/format';
import type { NotificationItem } from '@/types';

const NotificationBell = () => {
  const navigate = useNavigate();
  const [items, setItems] = useState<NotificationItem[]>([]);
  const [unread, setUnread] = useState(0);

  const reload = useCallback(async () => {
    try {
      const [page, count] = await Promise.all([
        notificationApi.search({ size: 8 }),
        notificationApi.unreadCount(),
      ]);
      setItems(page.content);
      setUnread(count);
    } catch {
      // Loi tai thong bao khong anh huong den man hinh chinh
    }
  }, []);

  useEffect(() => {
    void reload();
  }, [reload]);

  // Nhan thong bao day realtime tu backend
  const subscriptions = useMemo(
    () => [
      {
        destination: '/user/queue/notifications',
        handler: (payload: unknown) => {
          const incoming = payload as NotificationItem;
          setItems((prev) => [incoming, ...prev].slice(0, 8));
          setUnread((prev) => prev + 1);
          antdNotification.info({
            message: incoming.title,
            description: incoming.content,
            placement: 'bottomRight',
          });
        },
      },
    ],
    [],
  );
  useStomp(subscriptions);

  const handleOpenAll = async () => {
    await notificationApi.markAllRead();
    setUnread(0);
    void reload();
    navigate('/notifications');
  };

  const dropdownContent = (
    <div className="bg-white rounded-md shadow-lg w-96 p-2">
      <div className="flex items-center justify-between px-2 pb-2">
        <Typography.Text strong>Thông báo</Typography.Text>
        <Button type="link" size="small" onClick={handleOpenAll}>
          Đánh dấu đã đọc & xem tất cả
        </Button>
      </div>
      {items.length === 0 ? (
        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Chưa có thông báo" />
      ) : (
        <List
          size="small"
          dataSource={items}
          style={{ maxHeight: 360, overflowY: 'auto' }}
          renderItem={(item) => (
            <List.Item
              className="cursor-pointer"
              onClick={() => item.referenceId && navigate(`/orders/${item.referenceId}`)}
            >
              <List.Item.Meta
                title={
                  <span style={{ fontWeight: item.isRead ? 400 : 600 }}>
                    {item.title}
                  </span>
                }
                description={
                  <>
                    <div>{item.content}</div>
                    <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                      {formatDateTime(item.createdDate)}
                    </Typography.Text>
                  </>
                }
              />
            </List.Item>
          )}
        />
      )}
    </div>
  );

  return (
    <Dropdown popupRender={() => dropdownContent} trigger={['click']} placement="bottomRight">
      <Badge count={unread} size="small">
        <Button type="text" icon={<BellOutlined style={{ fontSize: 18 }} />} />
      </Badge>
    </Dropdown>
  );
};

export default NotificationBell;
