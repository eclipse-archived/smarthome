package org.eclipse.smarthome.core.notification;

import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.core.events.EventFilter;

public interface EventFilterFactory {

    /**
     * Create a new event instance of a specific event type.
     *
     * @param eventType the event type
     * @param topic the topic
     * @param payload the payload
     * @param source the source (can be null)
     *
     * @return the created event instance (not null)
     *
     * @throws IllegalArgumentException if eventType, topic or payload is null or empty
     * @throws IllegalArgumentException if the eventType is not supported
     * @throws Exception if the creation of the event has failed
     */
    EventFilter createEventFilter(String eventFilterType, List<String> options) throws Exception;

    /**
     * Returns a list of all supported event types of this factory.
     *
     * @return the supported event types (not null)
     */
    Set<String> getSupportedEventFilterTypes();

}
