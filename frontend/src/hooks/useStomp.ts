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
 * STOMP qua SockJS. Token gửi query param vì handshake không gắn được header Authorization.
 */
export const useStomp = (subscriptions: Subscription[], enabled = true): void => {
  const subscriptionsRef = useRef(subscriptions);
  subscriptionsRef.current = subscriptions;

  useEffect(() => {
    if (!enabled || subscriptionsRef.current.length === 0) {
      return undefined;
    }

    const apiBase = import.meta.env.VITE_API_BASE_URL ?? '/api/v1';
    const client = new Client({
      webSocketFactory: () => {
        const token = localStorage.getItem(STORAGE_KEYS.accessToken);
        const socketUrl = `${apiBase}/ws${token ? `?access_token=${token}` : ''}`;
        return new SockJS(socketUrl) as WebSocket;
      },
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
