/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.webaudio.internal;

import java.util.Collections;

import org.eclipse.smarthome.core.events.AbstractEventFactory;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFactory;

/**
 * This is an {@link EventFactory} for creating web audio events.
 * The only currently supported event type is {@link PlayURLEvent}.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class WebAudioEventFactory extends AbstractEventFactory {

    static final String PLAY_URL_TOPIC = "smarthome/webaudio/playurl";

    /**
     * Constructs a new WebAudioEventFactory.
     */
    public WebAudioEventFactory() {
        super(Collections.singleton(PlayURLEvent.TYPE));
    }

    @Override
    protected Event createEventByType(String eventType, String topic, String payload, String source) throws Exception {
        if (PlayURLEvent.TYPE.equals(eventType)) {
            String url = deserializePayload(payload, String.class);
            return createPlayURLEvent(url);
        }
        return null;
    }

    /**
     * Creates a PlayURLEvent event.
     *
     * @param url the url to play
     * @return the according event
     */
    public static PlayURLEvent createPlayURLEvent(String url) {
        String topic = PLAY_URL_TOPIC;
        String payload = serializePayload(url);
        return new PlayURLEvent(topic, payload, url);
    }

}
