/**
 * Kiem tra luong realtime: subscribe vao topic cua don hang, sau do goi API day vi tri GPS
 * va xac nhan ban tin duoc broadcast toi.
 *
 * Cach dung: node scripts/ws-check.mjs [BASE_URL]
 * Yeu cau: backend dang chay, da cai node_modules trong frontend/
 */
import SockJS from '../frontend/node_modules/sockjs-client/dist/sockjs.js';
import { Client } from '../frontend/node_modules/@stomp/stompjs/esm6/index.js';

const BASE = process.argv[2] ?? 'http://localhost:8080/api/v1';

const login = async (username, password) => {
  const response = await fetch(`${BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });
  const body = await response.json();
  if (!body.data) {
    throw new Error(`Dang nhap that bai: ${body.message}`);
  }
  return body.data.accessToken;
};

const post = async (path, token, payload) => {
  const response = await fetch(`${BASE}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify(payload),
  });
  return response.json();
};

const main = async () => {
  const shipperToken = await login('shipper02', 'Shipper@123');
  const customerToken = await login('customer01', 'Customer@123');

  const orders = await post('/orders/search', customerToken, { size: 1 });
  const order = orders.data?.content?.[0];
  if (!order) {
    throw new Error('Chua co don hang nao de kiem tra');
  }
  console.log(`Theo doi don hang ${order.orderCode}`);

  const received = [];
  const client = new Client({
    webSocketFactory: () => new SockJS(`${BASE}/ws?access_token=${customerToken}`),
    reconnectDelay: 0,
    debug: () => {},
  });

  await new Promise((resolve, reject) => {
    client.onConnect = () => {
      client.subscribe(`/topic/orders/${order.orderCode}`, (frame) => {
        received.push(JSON.parse(frame.body));
      });
      console.log('Da ket noi STOMP va subscribe thanh cong');
      resolve();
    };
    client.onStompError = (frame) => reject(new Error(frame.headers.message));
    client.onWebSocketError = (error) => reject(error);
    client.activate();
    setTimeout(() => reject(new Error('Het thoi gian cho ket noi WebSocket')), 10000);
  });

  await post('/tracking/location', shipperToken, {
    latitude: 21.0271,
    longitude: 105.8355,
    orderId: order.id,
    speedKmh: 31.5,
  });
  console.log('Da goi API day vi tri GPS, dang cho ban tin realtime...');

  await new Promise((resolve) => setTimeout(resolve, 2500));
  await client.deactivate();

  if (received.length === 0) {
    console.error('KHONG nhan duoc ban tin nao qua WebSocket');
    process.exit(1);
  }
  console.log(`Nhan duoc ${received.length} ban tin realtime:`);
  received.forEach((item) => console.log('  ', JSON.stringify(item)));
};

main().catch((error) => {
  console.error('Loi:', error.message);
  process.exit(1);
});
