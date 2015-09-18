package org.eclipse.smarthome.notification.dto;

import java.net.URI;

public class NotificationDTO {

    public String UID;
    public String text;
    public String icon;
    public String type;
    public int priority;
    public URI context;
    public URI action;
    public boolean sticky;
    public long timestamp;
    public String source;
}
