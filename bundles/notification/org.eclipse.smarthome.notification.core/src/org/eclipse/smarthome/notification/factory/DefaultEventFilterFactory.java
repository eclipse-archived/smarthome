package org.eclipse.smarthome.notification.factory;

import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.notification.EventFilterFactory;
import org.eclipse.smarthome.notification.filters.PassthroughFilter;
import org.eclipse.smarthome.notification.filters.RateFilter;

import com.google.common.collect.Sets;

public class DefaultEventFilterFactory implements EventFilterFactory {

    private final Set<String> supportedEventFilterTypes = Sets.newHashSet(PassthroughFilter.TYPE, RateFilter.TYPE);

    @Override
    public EventFilter createEventFilter(String eventFilterType, List<String> options) throws Exception {
        if (supportedEventFilterTypes.contains(eventFilterType)) {
            if (eventFilterType.equals(PassthroughFilter.TYPE)) {
                return new PassthroughFilter();
            } else if (eventFilterType.equals(RateFilter.TYPE)) {
                return new RateFilter(options);
            }
        }
        return null;
    }

    @Override
    public Set<String> getSupportedEventFilterTypes() {
        return supportedEventFilterTypes;
    }

}
