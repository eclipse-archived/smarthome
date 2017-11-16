/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.events;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Abstract implementation of the {@link Event} interface.
 *
 * @author Stefan Bußweiler - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractEvent implements Event {

    private final String topic;

    private final String payload;

    private final @Nullable String source;

    /**
     * Must be called in subclass constructor to create a new event.
     *
     * @param topic the topic
     * @param payload the payload
     * @param source the source, can be null. Can be used to inform the receiver about where the event came from. Must
     *            be pre-negotiated between sender and receiver of an event so both understand the (semantic) meaning.
     */
    public AbstractEvent(String topic, String payload, @Nullable String source) {
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
    public @Nullable String getSource() {
        return source;
    }

}
