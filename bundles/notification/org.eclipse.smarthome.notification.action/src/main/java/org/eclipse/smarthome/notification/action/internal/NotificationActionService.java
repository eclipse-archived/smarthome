package org.eclipse.smarthome.notification.action.internal;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.scriptengine.action.ActionService;
import org.eclipse.smarthome.notification.Notification;
import org.eclipse.smarthome.notification.manager.NotificationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class registers an OSGi service for the Notification action.
 *
 * @author Karel Goderis - Initial contribution
 */
public class NotificationActionService implements ActionService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationActionService.class);

    public NotificationActionService() {
        // nothing to do
    }

    public void setNotificationManager(NotificationManager notificationManager) {
        NotificationAction.notificationManager = notificationManager;

        int id = 0;

        List<Notification> notifications = NotificationAction.notificationManager.getAll();
        for (Notification notification : notifications) {
            if (notification.getSource().equals("ScriptEngine")) {
                String ID = notification.getUID();
                int notificationId = Integer.valueOf(StringUtils.split(ID, ":")[1]);
                if (notificationId > id) {
                    id = notificationId;
                }
            }
        }

        logger.debug("New Notifications will be numbered from '{}'", id + 1);
        NotificationAction.idCounter = id;

    }

    public void unsetNotificationManager(NotificationManager notificationManager) {
        NotificationAction.notificationManager = null;
    }

    public void activate() {
        logger.debug("Notification action service activated");
    }

    public void deactivate() {
        logger.debug("Notification action service deactivated");
    }

    @Override
    public String getActionClassName() {
        return NotificationAction.class.getCanonicalName();
    }

    @Override
    public Class<?> getActionClass() {
        return NotificationAction.class;
    }

}
