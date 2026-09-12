import { useEffect, useRef } from 'react';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { STORAGE_KEYS } from '@/api/client';

type Handler = (payload: unknown) => void;

interface Subscription {
  destination: string;
  handler: Handler;
}

/**
 * Ket noi STOMP qua SockJS. Token duoc gui bang query param vi handshake
 * cua SockJS khong cho phep dat header Authorization.
 */
export const useStomp = (subscriptions: Subscription[], enabled = true): void => {
  const subscriptionsRef = useRef(subscriptions);
  subscriptionsRef.current = subscriptions;

  useEffect(() => {
    if (!enabled || subscriptionsRef.current.length === 0) {
      return undefined;
    }

    const token = localStorage.getItem(STORAGE_KEYS.accessToken);
    // Endpoint STOMP nam duoi context path cua backend (/api/v1/ws)
    const apiBase = import.meta.env.VITE_API_BASE_URL ?? '/api/v1';
    const socketUrl = `${apiBase}/ws${token ? `?access_token=${token}` : ''}`;

    const client = new Client({
      webSocketFactory: () => new SockJS(socketUrl) as WebSocket,
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        subscriptionsRef.current.forEach(({ destination, handler }) => {
          client.subscribe(destination, (frame: IMessage) => {
            try {
              handler(JSON.parse(frame.body));
            } catch {
              handler(frame.body);
            }
          });
        });
      },
    });

    client.activate();
    return () => {
      void client.deactivate();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [enabled, subscriptions.map((s) => s.destination).join('|')]);
};
