/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.io.webaudio.internal;

import java.util.Collections;

import org.eclipse.smarthome.core.events.AbstractEventFactory;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFactory;
import org.osgi.service.component.annotations.Component;

/**
 * This is an {@link EventFactory} for creating web audio events.
 * The only currently supported event type is {@link PlayURLEvent}.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
@Component(service = EventFactory.class, immediate = true)
public class WebAudioEventFactory extends AbstractEventFactory {

    private static final String PLAY_URL_TOPIC = "smarthome/webaudio/playurl";

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
        String payload = serializePayload(url);
        return new PlayURLEvent(PLAY_URL_TOPIC, payload, url);
    }

}
