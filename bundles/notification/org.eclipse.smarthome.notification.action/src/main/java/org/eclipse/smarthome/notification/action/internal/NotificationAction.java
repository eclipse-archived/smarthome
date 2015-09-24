package org.eclipse.smarthome.notification.action.internal;

import java.net.URI;

import org.eclipse.smarthome.core.scriptengine.action.ActionDoc;
import org.eclipse.smarthome.core.scriptengine.action.ParamDoc;
import org.eclipse.smarthome.notification.NotificationBuilder;
import org.eclipse.smarthome.notification.manager.NotificationManager;

/**
 * This class contains the methods that are made available in scripts and rules
 * for sending notification
 *
 * @author Karel Goderis
 */
public class NotificationAction {

    static String defaultType = "";
    static URI defaultAction;
    static URI defaultContext;
    static boolean defaultIsSticky = false;
    static String defaultIcon;
    static int defaultPriority = 0;

    static NotificationManager notificationManager;
    static public long idCounter = 0;

    @ActionDoc(text = "Send a notification.", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
    public static boolean notify(@ParamDoc(name = "text", text = "The notification message to display") String text) {
        return notify(defaultType, defaultAction, text, defaultContext, defaultIsSticky, defaultIcon, defaultPriority);
    }

    @ActionDoc(text = "Send a notification.", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
    public static boolean notify(@ParamDoc(name = "text", text = "The notification message to display") String text,
            @ParamDoc(name = "priority", text = "The priority of the notification") int priority) {
        return notify(defaultType, defaultAction, text, defaultContext, defaultIsSticky, defaultIcon, priority);
    }

    @ActionDoc(text = "Send a notification.", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
    public static boolean notify(@ParamDoc(name = "text", text = "The notification message to display") String text,
            @ParamDoc(name = "priority", text = "The priority of the notification") int priority,
            @ParamDoc(name = "icon", text = "The icon to use when displaying the notification") String icon) {
        return notify(defaultType, defaultAction, text, defaultContext, defaultIsSticky, icon, priority);
    }

    @ActionDoc(text = "Send a notification.", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
    public static boolean notify(@ParamDoc(name = "text", text = "The notification message to display") String text,
            @ParamDoc(name = "context", text = "The notification context URI") URI context,
            @ParamDoc(name = "priority", text = "The priority of the notification") int priority) {
        return notify(defaultType, defaultAction, text, context, defaultIsSticky, defaultIcon, priority);
    }

    @ActionDoc(text = "Send a notification.", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
    public static boolean notify(@ParamDoc(name = "text", text = "The notification message to display") String text,
            @ParamDoc(name = "context", text = "The notification context URI") URI context,
            @ParamDoc(name = "priority", text = "The priority of the notification") int priority,
            @ParamDoc(name = "icon", text = "The icon to use when displaying the notification") String icon) {
        return notify(defaultType, defaultAction, text, context, defaultIsSticky, icon, priority);
    }

    @ActionDoc(text = "Send a notification.", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
    public static boolean notify(@ParamDoc(name = "type", text = "Your application's API token.") String type,
            @ParamDoc(name = "action", text = "The notification action URI") URI action,
            @ParamDoc(name = "text", text = "The notification message to display") String text) {
        return notify(type, action, text, defaultContext, defaultIsSticky, defaultIcon, defaultPriority);
    }

    @ActionDoc(text = "Send a notification.", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
    public static boolean notify(@ParamDoc(name = "type", text = "Your application's API token.") String type,
            @ParamDoc(name = "action", text = "The notification action URI") URI action,
            @ParamDoc(name = "text", text = "The notification message to display") String text,
            @ParamDoc(name = "context", text = "The notification context URI") URI context) {
        return notify(type, action, text, context, defaultIsSticky, defaultIcon, defaultPriority);
    }

    @ActionDoc(text = "Send a notification.", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
    public static boolean notify(@ParamDoc(name = "type", text = "Your application's API token.") String type,
            @ParamDoc(name = "text", text = "The notification message to display") String text) {
        return notify(type, defaultAction, text, defaultContext, defaultIsSticky, defaultIcon, defaultPriority);
    }

    @ActionDoc(text = "Send a notification.", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
    public static boolean notify(@ParamDoc(name = "type", text = "Your application's API token.") String type,
            @ParamDoc(name = "action", text = "The notification action URI") URI action,
            @ParamDoc(name = "text", text = "The notification message to display") String text,
            @ParamDoc(name = "priority", text = "The priority of the notification") int priority) {
        return notify(type, action, text, defaultContext, defaultIsSticky, defaultIcon, priority);
    }

    @ActionDoc(text = "Send a notification.", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
    public static boolean notify(@ParamDoc(name = "type", text = "Your application's API token.") String type,
            @ParamDoc(name = "action", text = "The notification action URI") URI action,
            @ParamDoc(name = "text", text = "The notification message to display") String text,
            @ParamDoc(name = "context", text = "The notification context URI") URI context,
            @ParamDoc(name = "priority", text = "The priority of the notification") int priority) {
        return notify(type, action, text, context, defaultIsSticky, defaultIcon, priority);
    }

    @ActionDoc(text = "Send a notification.", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
    public static boolean notify(@ParamDoc(name = "type", text = "Your application's API token.") String type,
            @ParamDoc(name = "action", text = "The notification action URI") URI action,
            @ParamDoc(name = "text", text = "The notification message to display") String text,
            @ParamDoc(name = "context", text = "The notification context URI") URI context,
            @ParamDoc(name = "sticky", text = "The notification stickiness indicator") boolean isSticky,
            @ParamDoc(name = "icon", text = "The icon to use when displaying the notification") String icon,
            @ParamDoc(name = "priority", text = "The notification priority level") int priority) {

        idCounter++;
        NotificationBuilder builder = NotificationBuilder
                .create(NotificationAction.class.getSimpleName() + ":" + idCounter);
        builder.withText(text).withAction(action).withType(type).withContext(context).withSticky(isSticky)
                .withIcon(icon).withPriority(priority).withSource("ScriptEngine");

        return notificationManager.add(builder.build());

    }
}
