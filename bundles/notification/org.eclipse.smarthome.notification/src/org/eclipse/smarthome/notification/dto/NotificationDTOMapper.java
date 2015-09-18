package org.eclipse.smarthome.notification.dto;

import org.eclipse.smarthome.notification.Notification;
import org.eclipse.smarthome.notification.internal.NotificationImpl;

/**
 * The {@link NotificationDTOMapper} is an utility class to map notifications into data transfer objects (DTO).
 *
 * @author Karel Goderis - Initial contribution
 */
public class NotificationDTOMapper {

    /**
     * Maps notification into thing data transfer object (DTO).
     *
     * @param notification the thing
     * @return the notification DTO object
     */
    public static NotificationDTO map(Notification notification) {

        NotificationDTO notificationDTO = new NotificationDTO();

        notificationDTO.UID = notification.getUID();
        notificationDTO.text = notification.getText();
        notificationDTO.icon = notification.getIcon();
        notificationDTO.type = notification.getType();
        notificationDTO.priority = notification.getPriority();
        notificationDTO.context = notification.getContext();
        notificationDTO.action = notification.getAction();
        notificationDTO.sticky = notification.isSticky();
        notificationDTO.timestamp = notification.getTimestamp();
        notificationDTO.source = notification.getSource();

        return notificationDTO;
    }

    public static Notification map(NotificationDTO notificationDTO) {

        // TODO: Notification get timestamp upon creation. The timestamp is included in the DTO, but when mapping back,
        // which timestamp do we really want? a new one, or the transported one?

        return new NotificationImpl(notificationDTO.UID, notificationDTO.text, notificationDTO.icon,
                notificationDTO.type, notificationDTO.priority, notificationDTO.context, notificationDTO.action,
                notificationDTO.sticky, notificationDTO.timestamp, notificationDTO.source);
    }

}
