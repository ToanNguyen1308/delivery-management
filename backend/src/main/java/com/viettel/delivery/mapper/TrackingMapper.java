package com.viettel.delivery.mapper;

import com.viettel.delivery.dto.response.NotificationResponse;
import com.viettel.delivery.dto.response.TrackingEventResponse;
import com.viettel.delivery.entity.Notification;
import com.viettel.delivery.entity.TrackingEvent;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = EnumMapper.class)
public interface TrackingMapper {

    TrackingEventResponse toEventResponse(TrackingEvent event);

    List<TrackingEventResponse> toEventResponseList(List<TrackingEvent> events);

    NotificationResponse toNotificationResponse(Notification notification);

    List<NotificationResponse> toNotificationResponseList(List<Notification> notifications);
}
