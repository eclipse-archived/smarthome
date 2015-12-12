package org.eclipse.smarthome.notification.events;

import org.eclipse.smarthome.notification.dto.NotificationDTO;

public class NotificationAddedEvent extends AbstractNotificationManagerEvent {

    /**
     * The notification added event type.
     */
    public final static String TYPE = NotificationAddedEvent.class.getSimpleName();

    /**
     * Constructs a new notification added event object.
     *
     * @param topic the topic
     * @param payload the payload
     * @param notification the notification data transfer object
     */
    protected NotificationAddedEvent(String topic, String payload, String source, NotificationDTO notification) {
        super(topic, payload, source, notification);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "Notification with ID '" + getNotification().getUID() + "' has been added.";
    }

}
