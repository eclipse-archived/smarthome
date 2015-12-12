package org.eclipse.smarthome.notification.producer;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.model.notification.notification.ActionID;
import org.eclipse.smarthome.model.notification.notification.AllActions;
import org.eclipse.smarthome.model.notification.notification.AllEntities;
import org.eclipse.smarthome.model.notification.notification.AllNamespaces;
import org.eclipse.smarthome.model.notification.notification.AllTargets;
import org.eclipse.smarthome.model.notification.notification.EntityTypeID;
import org.eclipse.smarthome.model.notification.notification.EventConfiguration;
import org.eclipse.smarthome.model.notification.notification.Filter;
import org.eclipse.smarthome.model.notification.notification.NamespaceID;
import org.eclipse.smarthome.model.notification.notification.Target;
import org.eclipse.smarthome.model.notification.notification.TargetID;
import org.eclipse.smarthome.notification.Notification;
import org.eclipse.smarthome.notification.NotificationBuilder;
import org.eclipse.smarthome.notification.events.AbstractNotificationManagerEvent;
import org.eclipse.smarthome.notification.events.NotificationRemovedEvent;
import org.eclipse.smarthome.notification.manager.NotificationManager;
import org.eclipse.smarthome.notification.manager.NotificationManagerFilterCriteria;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

public class EventNotificationBridge implements EventSubscriber {

    private final Logger logger = LoggerFactory.getLogger(EventNotificationBridge.class);

    private List<EventConfiguration> eventConfigurations;
    private Map<String, EventFilter> eventFilters;
    private String serviceID;

    private long idCounter = 0;
    private boolean firstRegularEventReceived = false;

    private NotificationManager notificationManager;
    private BundleContext bundleContext;
    private ServiceRegistration<?> eventSubscriberReg;

    public EventNotificationBridge(String serviceID, Map<String, EventFilter> eventFilters,
            List<EventConfiguration> eventConfigurations, BundleContext bundleContext) {
        this.serviceID = serviceID;
        this.eventFilters = eventFilters;
        this.eventConfigurations = eventConfigurations;
        this.bundleContext = bundleContext;
    }

    @Override
    public void receive(Event event) {
        logger.debug("Received an event from '{}' of type '{}' with topic '{}' and payload '{}'",
                new Object[] { event.getSource(), event.getType(), event.getTopic(), event.getPayload() });
        if (event instanceof AbstractNotificationManagerEvent) {
            // Events containing Notification should not trigger new Notifications, but it is an interesting way to feed
            // back what happened
            if (event instanceof NotificationRemovedEvent) {
                Notification notification = ((AbstractNotificationManagerEvent) event).getNotification();

                if (firstRegularEventReceived && notification.getSource()
                        .equals(EventNotificationBridge.class.getSimpleName() + ":" + serviceID)) {
                    logger.trace("The notification with ID '{}' was delivered in {} ms", notification.getUID(),
                            new Date().getTime() - notification.getTimestamp());
                }
            }
        } else {
            firstRegularEventReceived = true;
            synchronized (eventConfigurations) {
                for (EventConfiguration config : eventConfigurations) {
                    if (matchTopic(event, config)) {
                        if (applyFilters(event, config.getFilters(), eventFilters)) {
                            for (Target target : config.getTargets()) {

                                idCounter++;
                                NotificationBuilder builder = NotificationBuilder.create(serviceID + ":" + idCounter);
                                notificationManager.add(builder.withText(event.toString())
                                        // .withType(NotificationServiceBridge.class.getSimpleName() + ":" + serviceID
                                        // + ":" + target.getName())
                                        .withType(serviceID + ":" + target.getName()).withPriority(0)
                                        // .withSource(NotificationServiceBridge.class.getSimpleName() + ":" +
                                        // serviceID)
                                        .withSource("EventNotificationBridge").build());
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean matchTopic(Event event, EventConfiguration config) {

        boolean namespaceMatch = false;
        boolean entityTypeMatch = false;
        boolean targetMatch = false;
        boolean actionMatch = false;

        String[] topicElements = event.getTopic().split("/");
        if (topicElements.length != 4) {
            throw new IllegalArgumentException("Invalid topic: " + event.getTopic());
        }

        if (config.getNamespacedetail() instanceof AllNamespaces) {
            namespaceMatch = true;
        }
        if (config.getNamespacedetail() instanceof NamespaceID) {
            NamespaceID namespaceDetail = (NamespaceID) config.getNamespacedetail();
            if (StringUtils.contains(topicElements[0], namespaceDetail.getNamespace())) {
                namespaceMatch = true;
            }
        }

        if (config.getEntitydetail() instanceof AllEntities) {
            entityTypeMatch = true;
        }
        if (config.getEntitydetail() instanceof EntityTypeID) {
            EntityTypeID entityTypeDetail = (EntityTypeID) config.getEntitydetail();
            if (StringUtils.contains(topicElements[1], entityTypeDetail.getEntity())) {
                entityTypeMatch = true;
            }
        }

        if (config.getTargetdetail() instanceof AllTargets) {
            targetMatch = true;
        }
        if (config.getTargetdetail() instanceof TargetID) {
            TargetID targetDetail = (TargetID) config.getTargetdetail();
            if (StringUtils.contains(topicElements[2], targetDetail.getTarget())) {
                targetMatch = true;
            }
        }

        if (config.getActiondetail() instanceof AllActions) {
            actionMatch = true;
        }
        if (config.getActiondetail() instanceof ActionID) {
            ActionID actionDetail = (ActionID) config.getActiondetail();
            if (StringUtils.contains(topicElements[3], actionDetail.getAction())) {
                actionMatch = true;
            }
        }

        return namespaceMatch && entityTypeMatch && targetMatch && actionMatch;

    }

    private boolean applyFilters(Event event, EList<Filter> configFilters, Map<String, EventFilter> eventFilters) {

        for (Filter filter : configFilters) {
            EventFilter eventFilter = eventFilters.get(filter.getName());

            if (eventFilter == null || !eventFilter.apply(event)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return ImmutableSet.of(ALL_EVENT_TYPES);
    }

    @Override
    public EventFilter getEventFilter() {
        // we want to received all Types of events as we do our own filtering
        return null;
    }

    public void activate() {
        logger.debug("Activating the Event Notification Bridge for service '{}'", serviceID);
        ServiceReference<?> reference = bundleContext.getServiceReference(NotificationManager.class.getName());
        notificationManager = (NotificationManager) bundleContext.getService(reference);
        eventSubscriberReg = bundleContext.registerService(EventSubscriber.class.getName(), this,
                new Hashtable<String, Object>());

        // prune all old notifications

        NotificationManagerFilterCriteria filter = new NotificationManagerFilterCriteria(0,
                EventNotificationBridge.class.getSimpleName() + ":" + serviceID);

        List<Notification> obsoleteNotifications = notificationManager.get(filter);

        for (Notification notification : obsoleteNotifications) {
            logger.debug("Pruning obsolete notification with ID '{}'", notification.getUID());
            notificationManager.remove(notification.getUID());
        }
    }

    public void deactivate() {
        logger.debug("Deactivating the Event Notification Bridge for service '{}'", serviceID);
        if (eventSubscriberReg != null) {
            eventSubscriberReg.unregister();
        }
    }
}
