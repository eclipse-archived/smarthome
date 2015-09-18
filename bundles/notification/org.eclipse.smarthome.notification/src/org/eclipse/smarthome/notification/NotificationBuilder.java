package org.eclipse.smarthome.notification;

import java.net.URI;

import org.eclipse.smarthome.notification.internal.NotificationImpl;

/**
 * The {@link NotificationBuilder} helps creating a {@link Notification} through the builder pattern.
 *
 * @author Karel Goderis - Initial Contribution
 *
 *
 * @see Notification
 */
public class NotificationBuilder {

    private String UID;
    private String text;
    private String icon;
    private String type;
    private int priority;
    private URI context;
    private URI action;
    private boolean sticky;
    private long timestamp = 0;
    private String source;

    private NotificationBuilder(String UID) {
        this.UID = UID;
    };

    /**
     * Creates a new builder for a given notification UID.
     *
     * @param UID the notification UID for which the builder should be created-
     *
     * @return a new instance of a {@link NotificationBuilder}
     */
    public static NotificationBuilder create(String UID) {
        return new NotificationBuilder(UID);
    }

    public NotificationBuilder withText(String text) {
        this.text = text;
        return this;
    }

    public NotificationBuilder withIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public NotificationBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public NotificationBuilder withPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public NotificationBuilder withContext(URI context) {
        this.context = context;
        return this;
    }

    public NotificationBuilder withAction(URI action) {
        this.action = action;
        return this;
    }

    public NotificationBuilder withSticky(boolean sticky) {
        this.sticky = sticky;
        return this;
    }

    public NotificationBuilder withTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public NotificationBuilder withSource(String source) {
        this.source = source;
        return this;
    }

    /**
     * Builds a notification with the settings of this builder.
     *
     * @return the desired notification
     */
    public Notification build() {
        return new NotificationImpl(UID, text, icon, type, priority, context, action, sticky, timestamp, source);
    }

}
