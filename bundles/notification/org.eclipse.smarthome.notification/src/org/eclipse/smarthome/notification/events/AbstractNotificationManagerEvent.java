package org.eclipse.smarthome.notification.events;

import org.eclipse.smarthome.core.events.AbstractEvent;
import org.eclipse.smarthome.notification.Notification;
import org.eclipse.smarthome.notification.dto.NotificationDTO;
import org.eclipse.smarthome.notification.dto.NotificationDTOMapper;

public abstract class AbstractNotificationManagerEvent extends AbstractEvent {

    private final NotificationDTO notificationDTO;

    /**
     * Must be called in subclass constructor to create a new notification manager event.
     *
     * @param topic the topic
     * @param payload the payload
     * @param source the source, can be null
     * @param notification the notification
     */
    protected AbstractNotificationManagerEvent(String topic, String payload, String source,
            NotificationDTO notificationDTO) {
        super(topic, payload, source);
        this.notificationDTO = notificationDTO;
    }

    /**
     * Gets the notification.
     *
     * @return the notification
     */
    public Notification getNotification() {
        return NotificationDTOMapper.map(notificationDTO);
    }

}
