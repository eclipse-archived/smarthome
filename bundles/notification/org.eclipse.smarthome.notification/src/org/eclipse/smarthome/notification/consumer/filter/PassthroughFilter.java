package org.eclipse.smarthome.notification.consumer.filter;

import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;

public class PassthroughFilter implements EventFilter {

    public final static String TYPE = "Passthrough";

    @Override
    public boolean apply(Event event) {
        return true;
    }

}
