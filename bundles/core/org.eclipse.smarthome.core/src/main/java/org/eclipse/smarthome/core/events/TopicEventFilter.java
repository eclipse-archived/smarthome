/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.events;

/**
 * The {@link TopicEventFilter} is a default Eclipse SmartHome {@link EventFilter} implementation that ensures filtering
 * of events based on an event topic.
 * 
 * @author Stefan Bußweiler - Initial contribution
 */
public class TopicEventFilter implements EventFilter {

    private final String topicRegex;
    
    /**
     * Constructs a new topic event filter.
     * 
     * @param topicRegex the regular expression of a topic
     * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html">Java Regex</a>
     */
    public TopicEventFilter(String topicRegex) {
        this.topicRegex = topicRegex;
    }
 
    @Override
    public boolean apply(Event event) {
        return event.getTopic().matches(topicRegex);
    }

}
