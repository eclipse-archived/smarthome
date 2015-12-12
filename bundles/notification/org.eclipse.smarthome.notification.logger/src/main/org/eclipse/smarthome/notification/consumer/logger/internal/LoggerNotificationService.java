package org.eclipse.smarthome.notification.consumer.logger.internal;

import java.util.Dictionary;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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

        LogLevel level = LogLevel.TRACE;

        if (options.size() > 0) {
            level = LogLevel.valueOf(StringUtils.upperCase(options.get(0)));
        }

        switch (level) {
            case INFO: {
                logger.info("Source '{}', Text '{}'",
                        new Object[] { notification.getSource(), notification.getText() });
                break;
            }
            case TRACE: {
                logger.trace("Target '{}', Options '{}', Notification '{}'",
                        new Object[] { target, options, notification.toString() });
                break;
            }
            case WARN: {
                logger.warn("Type '{}', Source '{}', Text '{}', Target '{}', Options '{}'",
                        new Object[] { notification.getType(), notification.getSource(), notification.getText(), target,
                                options.toString() });
                break;
            }
            case DEBUG: {
                logger.debug("Type '{}', Source '{}', Text '{}', Target '{}', Options '{}'",
                        new Object[] { notification.getType(), notification.getSource(), notification.getText(), target,
                                options.toString() });
                break;
            }
            case ERROR: {
                logger.error("Type '{}', Source '{}', Text '{}', Target '{}', Options '{}'",
                        new Object[] { notification.getType(), notification.getSource(), notification.getText(), target,
                                options.toString() });
                break;
            }
        }

    }

    enum LogLevel {
        INFO,
        TRACE,
        WARN,
        DEBUG,
        ERROR
    };
}
