/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Describes event options and gives information how to interpret it.
 *
 * @author Moritz Kammerer - Initial contribution and API
 */
public class EventDescription {
    private final List<EventOption> options;

    /**
     * Creates an event description object.
     *
     * @param options predefined list of options
     */
    public EventDescription(List<EventOption> options) {
        if (options != null) {
            this.options = Collections.unmodifiableList(new ArrayList<EventOption>(options));
        } else {
            this.options = Collections.unmodifiableList(new ArrayList<EventOption>(0));
        }
    }

    /**
     * Returns a list of predefined events with their label.
     *
     * @return list of predefined events with their label
     */
    public List<EventOption> getOptions() {
        return options;
    }

    @Override
    public String toString() {
        return "EventDescription [options=" + options + "]";
    }

}
