package org.eclipse.smarthome.notification.consumer.filter;

import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.core.events.EventFilter;

public interface EventFilterFactory {

    EventFilter createEventFilter(String eventFilterType, List<String> options) throws Exception;

    Set<String> getSupportedEventFilterTypes();

}
