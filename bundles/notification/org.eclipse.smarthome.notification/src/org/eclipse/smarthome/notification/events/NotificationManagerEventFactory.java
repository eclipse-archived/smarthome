package org.eclipse.smarthome.notification.events;

import java.util.List;

import org.eclipse.smarthome.core.events.AbstractEventFactory;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.notification.Notification;
import org.eclipse.smarthome.notification.dto.NotificationDTO;
import org.eclipse.smarthome.notification.dto.NotificationDTOMapper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * A {@link NotificationManagerEventFactory} is responsible for creating notification manager event instances, e.g.
 * {@link NotificationAddedEvent}s.
 *
 * @author Karel Goderis - Initial contribution
 */
public class NotificationManagerEventFactory extends AbstractEventFactory {

    private static final String NOTIFICATION_ADDED_EVENT_TOPIC = "smarthome/notifications/{notificationUID}/added";

    private static final String NOTIFICATION_REMOVED_EVENT_TOPIC = "smarthome/notifications/{notificationUID}/removed";

    private static final String NOTIFICATION_UPDATED_EVENT_TOPIC = "smarthome/notifications/{notificationUID}/updated";

    /**
     * Constructs a new NotificationEventFactory.
     */
    public NotificationManagerEventFactory() {
        super(Sets.newHashSet(NotificationAddedEvent.TYPE, NotificationRemovedEvent.TYPE,
                NotificationUpdatedEvent.TYPE));
    }

    @Override
    protected Event createEventByType(String eventType, String topic, String payload, String source) throws Exception {
        Event event = null;
        if (eventType.equals(NotificationAddedEvent.TYPE)) {
            event = createAddedEvent(topic, payload, source);
        } else if (eventType.equals(NotificationRemovedEvent.TYPE)) {
            event = createRemovedEvent(topic, payload, source);
        } else if (eventType.equals(NotificationUpdatedEvent.TYPE)) {
            event = createUpdatedEvent(topic, payload, source);
        }
        return event;
    }

    private Event createAddedEvent(String topic, String payload, String source) throws Exception {
        NotificationDTO notificationDTO = deserializePayload(payload, NotificationDTO.class);
        return new NotificationAddedEvent(topic, payload, source, notificationDTO);
    }

    private Event createRemovedEvent(String topic, String payload, String source) throws Exception {
        NotificationDTO notificationDTO = deserializePayload(payload, NotificationDTO.class);
        return new NotificationRemovedEvent(topic, payload, source, notificationDTO);
    }

    private Event createUpdatedEvent(String topic, String payload, String source) throws Exception {
        NotificationDTO[] notificationDTO = deserializePayload(payload, NotificationDTO[].class);
        if (notificationDTO.length != 2) {
            throw new IllegalArgumentException("NotificationUpdateEvent creation failed, invalid payload: " + payload);
        }
        return new NotificationUpdatedEvent(topic, payload, source, notificationDTO[0], notificationDTO[1]);
    }

    /**
     * Creates a notification added event.
     *
     * @param notification the notification
     *
     * @return the created notification added event
     *
     * @throws IllegalArgumentException if notification is null
     */
    public static NotificationAddedEvent createAddedEvent(Notification notification, String source) {
        assertValidArgument(notification);
        String topic = buildTopic(NOTIFICATION_ADDED_EVENT_TOPIC, notification.getUID());
        NotificationDTO notificationDTO = map(notification);
        String payload = serializePayload(notificationDTO);
        return new NotificationAddedEvent(topic, payload, source, notificationDTO);
    }

    /**
     * Creates a notification removed event.
     *
     * @param notification the notification
     *
     * @return the created notification removed event
     *
     * @throws IllegalArgumentException if notification is null
     */
    public static NotificationRemovedEvent createRemovedEvent(Notification notification, String source) {
        assertValidArgument(notification);
        String topic = buildTopic(NOTIFICATION_REMOVED_EVENT_TOPIC, notification.getUID());
        NotificationDTO notificationDTO = map(notification);
        String payload = serializePayload(notificationDTO);
        return new NotificationRemovedEvent(topic, payload, source, notificationDTO);
    }

    /**
     * Creates a notification updated event.
     *
     * @param notification the notification
     * @param oldNotification the old notification
     *
     * @return the created notification updated event
     *
     * @throws IllegalArgumentException if notification or oldNotification is null
     */
    public static NotificationUpdatedEvent createUpdatedEvent(Notification notification, Notification oldNotification,
            String source) {
        assertValidArgument(notification);
        assertValidArgument(oldNotification);
        String topic = buildTopic(NOTIFICATION_UPDATED_EVENT_TOPIC, notification.getUID());
        NotificationDTO notificationDTO = map(notification);
        NotificationDTO oldNotificationDTO = map(oldNotification);
        List<NotificationDTO> notificationDTOs = Lists.newLinkedList();
        notificationDTOs.add(notificationDTO);
        notificationDTOs.add(oldNotificationDTO);
        String payload = serializePayload(notificationDTOs);
        return new NotificationUpdatedEvent(topic, payload, source, notificationDTO, oldNotificationDTO);
    }

    private static void assertValidArgument(Notification notification) {
        Preconditions.checkArgument(notification != null, "The argument 'notification' must not be null.");
        Preconditions.checkArgument(notification.getUID() != null,
                "The notification UID of a notification must not be null.");
    }

    private static String buildTopic(String topic, String notificationUID) {
        return topic.replace("{notificationUID}", notificationUID);
    }

    private static NotificationDTO map(Notification notification) {
        return NotificationDTOMapper.map(notification);
    }

}
