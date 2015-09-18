package org.eclipse.smarthome.notification.manager;

import java.util.List;

import javax.management.NotificationListener;

import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.notification.Notification;
import org.eclipse.smarthome.notification.events.NotificationAddedEvent;

/**
 * The {@link NotificationManager} is a service interface providing a persistent store for {@code Notification}s
 * <p>
 * {@link Notification}s can be directly added, removed or updated by producers. Whenever such an event occurs, the
 * {@link NotificationManager} will trigger a {@link Event} with a payload that contains the localized and enriched
 * Notification. Therefore, consumers that want to receive {@link Notification}s, should implement the
 * {@link EventSubscriber} interface
 *
 * @author Karel Goderis - Initial Contribution
 *
 * @see NotificationListener
 */
public interface NotificationManager {

    /**
     * Adds the specified {@link Notification} to this {@link NotificationManager} and sends an
     * <i>{@link NotificationAddedEvent}</i> event
     * <p>
     * If there is already a {@link Notification} with the same {@code Notification} ID in this
     * {@link NotificationManager}, the specified {@link Notification} is synchronized with the existing one overriding
     * the specific properties. In that case an <i>{@link NotificationUpdatedEvent}</i> event is sent
     * <p>
     * This method returns silently, if the specified {@link Notification} is {@code null}.
     *
     * @param notification the notification to be added to this NotificationManager (could be null)
     * @return true if the specified notification could be added or updated, otherwise false
     */
    boolean add(Notification notification);

    /**
     * Removes the {@link Notification} associated with the specified {@code Notification} ID from
     * this {@link NotificationManager} and sends a <i>{@link NotificationRemovedEvent}</i> event.
     * <p>
     * This method returns silently, if the specified {@code Notification} ID is {@code null}, empty, invalid, or no
     * associated
     * {@link Notification} exists in this {@link NotificationManager}.
     *
     * @param notificationUID the Notification UID pointing to the notification to be removed from this
     *            NotificationManager
     *            (could be null or invalid)
     *
     * @return true if the specified discovery result could be removed, otherwise false
     */
    boolean remove(String notificationUID);

    /**
     * Returns all {@link Notification}s in this {@link NotificationManager} which fit to the specified
     * {@link NotificationManagerFilterCriteria}
     * .
     * <p>
     * If the specified {@link NotificationManagerFilterCriteria} is {@code null}, all {@link Notification}s in this
     * {@link NotificationManager}
     * are returned.
     *
     * @param criteria the filter criteria to be used for filtering all notification
     *            (could be null)
     *
     * @return all notification in this NotificationManager which fit to the specified filter criteria
     *         (not null, could be empty)
     */
    List<Notification> get(NotificationManagerFilterCriteria criteria);

    /**
     * Returns all {@link Notification}s in this {@link NotificationManager}.
     *
     * @return all notifications in this NotificationManager (not null, could be empty)
     */
    List<Notification> getAll();

}
