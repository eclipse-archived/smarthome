package org.eclipse.smarthome.notification.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.eclipse.smarthome.notification.Notification;
import org.eclipse.smarthome.notification.events.NotificationManagerEventFactory;
import org.eclipse.smarthome.notification.manager.NotificationManager;
import org.eclipse.smarthome.notification.manager.NotificationManagerFilterCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NotificationManagerImpl} class is a concrete implementation of the {@link NotificationManager}.
 *
 * @author Karel Goderis - Initial Contribution
 *
 */
public class NotificationManagerImpl implements NotificationManager {

    /**
     * Internal enumeration to identify the correct type of the event to be fired.
     */
    private enum EventType {
        added,
        removed,
        updated
    }

    private final Logger logger = LoggerFactory.getLogger(NotificationManager.class);

    private Storage<Notification> notificationStorage;

    private EventPublisher eventPublisher;

    @Override
    public synchronized boolean add(Notification notification) throws IllegalStateException {
        if (notification != null) {

            Notification notificationResult = get(notification.getUID());

            if (notificationResult == null) {
                notificationStorage.put(notification.getUID(), notification);
                postEvent(notification, null, EventType.added);
                logger.info("Added new notification with ID '{}' of type '{}' to the notification manager.",
                        notification.getUID(), notification.getType());
                return true;
            } else {
                if (notificationResult instanceof NotificationImpl) {
                    NotificationImpl notificationImpl = (NotificationImpl) notificationResult;
                    notificationImpl.synchronize(notification);
                    notificationStorage.put(notification.getUID(), notificationImpl);
                    postEvent(notificationImpl, notification, EventType.updated);
                    logger.debug("Updated the nofification with ID '{}'.", notification.getUID());
                    return true;
                } else {
                    logger.warn("Cannot synchronize result with implementation class '{}'.",
                            notificationResult.getClass().getName());
                }
            }

        }

        return false;
    }

    @Override
    public synchronized boolean remove(String notificationUID) {
        if (notificationUID != null) {
            Notification notification = get(notificationUID);
            if (notification != null) {
                logger.info("Removing a notification with ID '{}' from the notification manager.", notificationUID);
                this.notificationStorage.remove(notificationUID);
                postEvent(notification, null, EventType.removed);
                return true;
            }
        }

        return false;
    }

    @Override
    public List<Notification> get(NotificationManagerFilterCriteria criteria) throws IllegalStateException {
        List<Notification> filteredEntries = new ArrayList<>();

        for (Notification notification : this.notificationStorage.getValues()) {
            if (matchFilter(notification, criteria)) {
                filteredEntries.add(notification);
            }
        }

        Comparator<Notification> notificationComparator = new Comparator<Notification>() {
            @Override
            public int compare(Notification o1, Notification o2) {
                if (o1.getTimestamp() < o2.getTimestamp())
                    return -1;
                if (o1.getTimestamp() == o2.getTimestamp())
                    return 0;
                return 1;
            }
        };

        Collections.sort(filteredEntries, notificationComparator);

        return filteredEntries;
    }

    @Override
    public List<Notification> getAll() {
        return get((NotificationManagerFilterCriteria) null);
    }

    /**
     * Returns the {@link Notification} in this {@link NotificationManager} associated with
     * the specified {@code Notification} ID, or {@code null}, if no {@link Notification} could be found.
     *
     * @param notificationUID
     *            the Notification ID to which the notification should be returned
     *
     * @return the notification associated with the specified Notification ID, or
     *         null, if no notification could be found
     */
    private Notification get(String notificationUID) {
        if (notificationUID != null) {
            return notificationStorage.get(notificationUID);
        }

        return null;
    }

    private void postEvent(Notification notification, Notification oldNotification, EventType eventType) {
        if (eventPublisher != null) {
            try {
                switch (eventType) {
                    case added:
                        eventPublisher.post(NotificationManagerEventFactory.createAddedEvent(notification));
                        break;
                    case removed:
                        eventPublisher.post(NotificationManagerEventFactory.createRemovedEvent(notification));
                        break;
                    case updated:
                        eventPublisher.post(
                                NotificationManagerEventFactory.createUpdatedEvent(notification, oldNotification));
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                logger.error("Could not post event of type '" + eventType.name() + "'.", ex);
            }
        }
    }

    private boolean matchFilter(Notification notification, NotificationManagerFilterCriteria criteria) {
        if (criteria != null) {

            String notificationUID = criteria.getNotificationUID();
            if (notificationUID != null) {
                if (!notification.getUID().equals(notificationUID)) {
                    return false;
                }
            }

            String notificationType = criteria.getType();
            if (notificationType != null) {
                if (!notification.getType().equals(notificationType)) {
                    return false;
                }
            }

            int priority = criteria.getPriority();
            if (notification.getPriority() != priority) {
                return false;
            }
        }

        return true;
    }

    protected void setStorageService(StorageService storageService) {
        this.notificationStorage = storageService.getStorage(Notification.class.getName(),
                this.getClass().getClassLoader());
    }

    protected void unsetStorageService(StorageService storageService) {
        this.notificationStorage = null;
    }

    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    public void activate() {
        logger.debug("Activating the Notification Manager");
        List<Notification> existingNotifications = getAll();

        Map<String, Integer> beanCounter = new HashMap<String, Integer>();
        for (Notification notification : existingNotifications) {
            beanCounter.put(notification.getSource(), beanCounter.containsKey(notification.getSource())
                    ? beanCounter.get(notification.getSource()) + 1 : 1);
        }
        for (String service : beanCounter.keySet()) {
            logger.debug("The Notification Manager still holds {} notifications for source '{}'",
                    beanCounter.get(service), service);
        }
    }

    public void deactivate() {
        logger.debug("Deactivating the Notification Manager");
        List<Notification> existingNotifications = getAll();

        Map<String, Integer> beanCounter = new HashMap<String, Integer>();
        for (Notification notification : existingNotifications) {
            beanCounter.put(notification.getSource(), beanCounter.containsKey(notification.getSource())
                    ? beanCounter.get(notification.getSource()) + 1 : 1);
        }
        for (String service : beanCounter.keySet()) {
            logger.debug("The Notification Manager contains {} undelivered notifications for source '{}'",
                    beanCounter.get(service), service);
        }
    }
}
