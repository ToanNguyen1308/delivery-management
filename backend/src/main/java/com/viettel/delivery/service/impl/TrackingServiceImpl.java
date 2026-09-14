package com.viettel.delivery.service.impl;

import com.viettel.delivery.constant.AppConstants;
import com.viettel.delivery.constant.ErrorCode;
import com.viettel.delivery.constant.enums.TrackingEventType;
import com.viettel.delivery.dto.request.LocationUpdateRequest;
import com.viettel.delivery.dto.response.EnumResponse;
import com.viettel.delivery.dto.response.OrderTrackingResponse;
import com.viettel.delivery.dto.response.ShipperLocationResponse;
import com.viettel.delivery.entity.Order;
import com.viettel.delivery.entity.Shipper;
import com.viettel.delivery.entity.ShipperLocation;
import com.viettel.delivery.entity.TrackingEvent;
import com.viettel.delivery.exception.BusinessException;
import com.viettel.delivery.exception.ResourceNotFoundException;
import com.viettel.delivery.mapper.TrackingMapper;
import com.viettel.delivery.repository.OrderRepository;
import com.viettel.delivery.repository.ShipperLocationRepository;
import com.viettel.delivery.repository.TrackingEventRepository;
import com.viettel.delivery.security.SecurityUtil;
import com.viettel.delivery.service.OrderService;
import com.viettel.delivery.service.ShipperService;
import com.viettel.delivery.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingServiceImpl implements TrackingService {

    private static final int MASK_VISIBLE_CHARS = 1;

    private final OrderRepository orderRepository;
    private final ShipperLocationRepository shipperLocationRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final TrackingMapper trackingMapper;
    private final ShipperService shipperService;
    private final OrderService orderService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public ShipperLocationResponse pushLocation(LocationUpdateRequest request) {
        Shipper shipper = shipperService.getShipperEntityByUserId(SecurityUtil.getCurrentUserId());
        LocalDateTime now = LocalDateTime.now();

        shipper.setCurrentLatitude(request.getLatitude());
        shipper.setCurrentLongitude(request.getLongitude());
        shipper.setLastLocationAt(now);

        Order order = request.getOrderId() == null
                ? null
                : orderRepository.findByIdAndIsDeletedFalse(request.getOrderId()).orElse(null);
        if (order != null && (order.getCurrentShipper() == null
                || !shipper.getId().equals(order.getCurrentShipper().getId()))) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, HttpStatus.FORBIDDEN);
        }

        shipperLocationRepository.save(ShipperLocation.builder()
                .shipper(shipper)
                .orderId(order == null ? null : order.getId())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .speedKmh(request.getSpeedKmh())
                .recordedAt(now)
                .build());

        ShipperLocationResponse response = ShipperLocationResponse.builder()
                .shipperId(shipper.getId())
                .shipperCode(shipper.getShipperCode())
                .shipperName(shipper.getUser().getFullName())
                .orderId(order == null ? null : order.getId())
                .orderCode(order == null ? null : order.getOrderCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .speedKmh(request.getSpeedKmh())
                .recordedAt(now)
                .build();

        broadcastLocation(response, order);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderTrackingResponse getTrackingById(Long orderId) {
        Order order = orderService.getAccessibleOrder(orderId);
        return buildTracking(order, false);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderTrackingResponse getPublicTracking(String orderCode) {
        Order order = orderRepository.findByOrderCodeWithDetails(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));
        return buildTracking(order, true);
    }

    @Override
    @Transactional
    public void recordEvent(Order order, TrackingEventType eventType, String description,
                            BigDecimal latitude, BigDecimal longitude) {
        TrackingEvent event = trackingEventRepository.save(TrackingEvent.builder()
                .order(order)
                .shipperId(order.getCurrentShipper() == null ? null : order.getCurrentShipper().getId())
                .eventType(eventType)
                .description(description)
                .latitude(latitude)
                .longitude(longitude)
                .occurredAt(LocalDateTime.now())
                .build());

        broadcastEvent(order.getOrderCode(), event);
    }

    private OrderTrackingResponse buildTracking(Order order, boolean publicView) {
        List<TrackingEvent> events = trackingEventRepository.findByOrderId(order.getId());
        Shipper shipper = order.getCurrentShipper();

        List<OrderTrackingResponse.RoutePoint> route = publicView
                ? Collections.emptyList()
                : shipperLocationRepository.findRouteByOrderId(order.getId()).stream()
                        .map(location -> OrderTrackingResponse.RoutePoint.builder()
                                .latitude(location.getLatitude())
                                .longitude(location.getLongitude())
                                .recordedAt(location.getRecordedAt())
                                .build())
                        .toList();

        return OrderTrackingResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .status(EnumResponse.of(order.getStatus()))
                .serviceType(EnumResponse.of(order.getServiceType()))
                .receiverName(publicView ? maskName(order.getReceiverName()) : order.getReceiverName())
                .deliveryAddress(publicView ? maskAddress(order.getDeliveryAddress()) : order.getDeliveryAddress())
                .deliveryLatitude(order.getDeliveryLatitude())
                .deliveryLongitude(order.getDeliveryLongitude())
                .pickupLatitude(publicView ? null : order.getPickupLatitude())
                .pickupLongitude(publicView ? null : order.getPickupLongitude())
                .expectedDeliveryAt(order.getExpectedDeliveryAt())
                .pickedUpAt(order.getPickedUpAt())
                .deliveredAt(order.getDeliveredAt())
                .shipperName(shipper == null ? null : shipper.getUser().getFullName())
                .shipperPhone(shipper == null || publicView ? null : shipper.getUser().getPhoneNumber())
                .currentLatitude(shipper == null ? null : shipper.getCurrentLatitude())
                .currentLongitude(shipper == null ? null : shipper.getCurrentLongitude())
                .lastLocationAt(shipper == null ? null : shipper.getLastLocationAt())
                .proofImageUrl(publicView ? null : order.getProofImageUrl())
                .events(trackingMapper.toEventResponseList(events))
                .route(route)
                .build();
    }

    private void broadcastLocation(ShipperLocationResponse response, Order order) {
        try {
            if (order != null) {
                messagingTemplate.convertAndSend(AppConstants.WS_TOPIC_ORDER + order.getOrderCode(), response);
            }
            messagingTemplate.convertAndSend(AppConstants.WS_TOPIC_SHIPPER_LOCATION, response);
        } catch (Exception ex) {
            log.warn("Khong day duoc vi tri realtime: {}", ex.getMessage());
        }
    }

    private void broadcastEvent(String orderCode, TrackingEvent event) {
        try {
            messagingTemplate.convertAndSend(AppConstants.WS_TOPIC_ORDER + orderCode,
                    trackingMapper.toEventResponse(event));
        } catch (Exception ex) {
            log.warn("Khong day duoc su kien tracking realtime: {}", ex.getMessage());
        }
    }

    /**
     * Che bot ho ten khi tra cuu cong khai, vi du "Nguyen Van A" thanh "N**** V** A".
     */
    private String maskName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return java.util.Arrays.stream(name.trim().split("\\s+"))
                .map(this::maskWord)
                .reduce((a, b) -> a + " " + b)
                .orElse(name);
    }

    private String maskWord(String word) {
        if (word.length() <= MASK_VISIBLE_CHARS) {
            return word;
        }
        return word.charAt(0) + "*".repeat(word.length() - MASK_VISIBLE_CHARS);
    }

    /**
     * Chi hien thi phan quan huyen va tinh thanh, an so nha cu the.
     */
    private String maskAddress(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }
        String[] parts = address.split(",");
        if (parts.length <= 2) {
            return parts[parts.length - 1].trim();
        }
        return "%s, %s".formatted(parts[parts.length - 2].trim(), parts[parts.length - 1].trim());
    }
}
