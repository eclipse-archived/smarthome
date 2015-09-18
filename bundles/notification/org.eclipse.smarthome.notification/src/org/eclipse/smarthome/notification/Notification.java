package org.eclipse.smarthome.notification;

import java.net.URI;

public interface Notification {

    public String getUID();

    public String getText();

    public String getIcon();

    public String getType();

    public int getPriority();

    public URI getContext();

    public URI getAction();

    public boolean isSticky();

    public long getTimestamp();

    public String getSource();

}
