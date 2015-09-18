package org.eclipse.smarthome.notification.consumer.logger.internal;

import java.util.Dictionary;
import java.util.List;

import org.eclipse.smarthome.notification.Notification;
import org.eclipse.smarthome.notification.consumer.NotificationService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerNotificationService implements NotificationService {

    private final Logger logger = LoggerFactory.getLogger(LoggerNotificationService.class);

    public void activate(ComponentContext componentContext) {
        Dictionary<String, Object> properties = componentContext.getProperties();
    }

    public void deactivate() {
    }

    @Override
    public String getName() {
        return "logger";
    }

    @Override
    public void notify(String target, List<String> options, Notification notification) {
        logger.debug("Source '{}' Target '{}', Type '{}', Text '{}'",
                new Object[] { notification.getSource(), target, notification.getType(), notification.getText() });
    }
}
