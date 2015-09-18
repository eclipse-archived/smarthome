package org.eclipse.smarthome.notification.events;

import org.eclipse.smarthome.notification.dto.NotificationDTO;

public class NotificationUpdatedEvent extends AbstractNotificationManagerEvent {

    /**
     * The notification updated event type.
     */
    public final static String TYPE = NotificationUpdatedEvent.class.getSimpleName();

    private final NotificationDTO oldNotification;

    /**
     * Constructs a new notification updated event object.
     *
     * @param topic the topic
     * @param payload the payload
     * @param notification the notification data transfer object
     * @param oldNotification the old notification data transfer object
     */
    protected NotificationUpdatedEvent(String topic, String payload, NotificationDTO notification,
            NotificationDTO oldNotification) {
        super(topic, payload, null, notification);
        this.oldNotification = oldNotification;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public NotificationDTO getOldNotification() {
        return oldNotification;
    }

    @Override
    public String toString() {
        return "Notification with ID '" + getNotification().getUID() + "' has been updated.";
    }

}
