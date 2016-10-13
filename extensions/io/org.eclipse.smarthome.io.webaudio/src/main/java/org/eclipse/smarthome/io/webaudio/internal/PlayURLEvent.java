/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.webaudio.internal;

import org.eclipse.smarthome.core.events.AbstractEvent;

/**
 * This is an {@link Event} that is sent when a web client should play an audio stream from a url.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class PlayURLEvent extends AbstractEvent {

    /**
     * The extension event type.
     */
    public final static String TYPE = PlayURLEvent.class.getSimpleName();

    private String url;

    /**
     * Constructs a new extension event object.
     *
     * @param topic the topic
     * @param payload the payload
     * @param url the url to play
     */
    public PlayURLEvent(String topic, String payload, String url) {
        super(topic, payload, null);
        this.url = url;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "Play URL '" + url + "'.";
    }
}
