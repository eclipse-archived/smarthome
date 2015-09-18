package org.eclipse.smarthome.notification.manager;

/**
 * The {@link NotificationManagerFilterCriteria} specifies the filter for {@link NotificationManager} <i>GET</i>
 * requests.
 * <p>
 * The according property is filtered in the {@link NotificationManager} if it's <i>NEITHER</i> {@code null} <i>NOR</i>
 * empty. All specified properties are filtered with an <i>AND</i> operator.
 *
 * @author Karel Goderis - Initial Contribution
 *
 * @see NotificationManager
 */

public class NotificationManagerFilterCriteria {

    private final String notificationType;
    private final String notificationUID;
    private final int priority;
    private final String source;

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param priority the priority level to be filtered
     */
    public NotificationManagerFilterCriteria(int priority) {
        this.notificationType = null;
        this.notificationUID = null;
        this.priority = priority;
        this.source = null;
    }

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param source the source to be filtered
     */
    public NotificationManagerFilterCriteria(int priority, String source) {
        this.notificationType = null;
        this.notificationUID = null;
        this.priority = priority;
        this.source = source;
    }

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param notificationType
     *            the notification type to be filtered (could be null or empty)
     * @param priority
     *            priority the priority level to be filtered
     */
    public NotificationManagerFilterCriteria(String notificationType, int priority) {
        this.notificationType = notificationType;
        this.notificationUID = null;
        this.priority = priority;
        this.source = null;
    }

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param notificationUID
     *            the Notification UID to be filtered (could be null or empty)
     * @param priority
     *            priority the priority level to be filtered
     */
    public NotificationManagerFilterCriteria(String notificationType, String notificationUID, int priority) {
        this.notificationType = notificationType;
        this.notificationUID = notificationUID;
        this.priority = priority;
        this.source = null;
    }

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param notificationUID
     *            the Notification UID to be filtered (could be null or empty)
     * @param priority
     *            priority the priority level to be filtered
     */
    public NotificationManagerFilterCriteria(String notificationType, String notificationUID, int priority,
            String source) {
        this.notificationType = notificationType;
        this.notificationUID = notificationUID;
        this.priority = priority;
        this.source = source;
    }

    /**
     * Returns the {@code Notification} type to be filtered.
     *
     * @return the Notification type to be filtered (could be null or empty)
     */
    public String getType() {
        return this.notificationType;
    }

    /**
     * Returns the {@code Notification} UID to be filtered.
     *
     * @return the Notification UID to be filtered (could be null or empty)
     */
    public String getNotificationUID() {
        return this.notificationUID;
    }

    /**
     * Return the {@code priority} to be filtered.
     *
     * @return the priority level to be filtered (could be null)
     */
    public int getPriority() {
        return this.priority;
    }

}
