package org.eclipse.smarthome.notification.internal;

import java.net.URI;
import java.util.Date;

import org.eclipse.smarthome.notification.Notification;

public class NotificationImpl implements Notification {

    private String UID;
    private String text;
    private String icon;
    private String type;
    private int priority;
    private URI context;
    private URI action;
    private boolean sticky;
    private long timestamp;
    private String source;

    public NotificationImpl(String UID, String text, String icon, String type, int priority, URI context, URI action,
            boolean sticky, long timestamp, String source) throws IllegalArgumentException {

        if (UID == null) {
            throw new IllegalArgumentException("The notification UID must not be null!");
        }

        this.UID = UID;
        this.text = text == null ? "" : text;
        this.icon = icon;
        this.type = type;
        this.priority = priority;
        this.context = context;
        this.action = action;
        this.sticky = sticky;
        this.timestamp = (timestamp == 0) ? new Date().getTime() : timestamp;
        this.source = source;

    }

    @Override
    public String getUID() {
        return UID;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getIcon() {
        return icon;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public URI getContext() {
        return context;
    }

    @Override
    public URI getAction() {
        return action;
    }

    @Override
    public boolean isSticky() {
        return sticky;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getSource() {
        return source;
    }

    /**
     * Merges the content of the specified source {@link Notification} into this object.
     * <p>
     * This method returns silently if the specified source {@link Notification} is {@code null} or its
     * {@code Notification}
     * type or ID does not fit to this object.
     * <p>
     * The timestamp of the Notification however, is not updated
     *
     * @param notification the notification which is used as source for the merge
     */
    public void synchronize(Notification notification) {
        if ((notification != null) && (notification.getUID().equals(UID))) {

            this.action = notification.getAction();
            this.context = notification.getContext();
            this.icon = notification.getIcon();
            this.priority = notification.getPriority();
            this.type = notification.getType();
            this.sticky = notification.isSticky();
            this.text = notification.getText();
            this.source = notification.getSource();
        }
    }

    @Override
    public String toString() {
        return "Notification [UID=" + UID + ", type=" + type + ", text=" + text + ", icon=" + icon + ", priority="
                + priority + ", context=" + context + ", action=" + action + ",sticky=" + sticky + ", timestamp="
                + timestamp + ", source=" + source + "]";
    }

}
