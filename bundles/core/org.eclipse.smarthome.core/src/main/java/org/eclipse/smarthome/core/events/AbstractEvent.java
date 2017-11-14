/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.events;

/**
 * Abstract implementation of the {@link Event} interface.
 * 
 * @author Stefan Bußweiler - Initial contribution
 */
public abstract class AbstractEvent implements Event {

    private final String topic;

    private final String payload;

    private final String source;

    /**
     * Must be called in subclass constructor to create a new event.
     * 
     * @param topic the topic
     * @param payload the payload
     * @param source the source
     */
    public AbstractEvent(String topic, String payload, String source) {
        this.topic = topic;
        this.payload = payload;
        this.source = source;
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public String getPayload() {
        return payload;
    }

    @Override
    public String getSource() {
        return source;
    }

}
