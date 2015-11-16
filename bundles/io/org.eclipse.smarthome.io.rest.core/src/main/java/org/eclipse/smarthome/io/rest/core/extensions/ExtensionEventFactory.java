/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.extensions;

import org.eclipse.smarthome.core.events.AbstractEventFactory;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFactory;

import com.google.common.collect.Sets;

/**
 * This is an {@link EventFactory} for creating extension events. The following event types are supported by this
 * factory:
 *
 * {@link ExtensionEventFactory#TYPE}
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class ExtensionEventFactory extends AbstractEventFactory {

    static final String EXTENSION_INSTALLED_EVENT_TOPIC = "smarthome/extensions/{id}/installed";

    static final String EXTENSION_UNINSTALLED_EVENT_TOPIC = "smarthome/extensions/{id}/uninstalled";

    static final String EXTENSION_FAILURE_EVENT_TOPIC = "smarthome/extensions/{id}/failed";

    /**
     * Constructs a new ExtensionEventFactory.
     */
    public ExtensionEventFactory() {
        super(Sets.newHashSet(ExtensionEvent.TYPE));
    }

    @Override
    protected Event createEventByType(String eventType, String topic, String payload, String source) throws Exception {
        if (topic.equals(ExtensionEventFactory.EXTENSION_FAILURE_EVENT_TOPIC)) {
            String[] properties = deserializePayload(payload, String[].class);
            Event event = new ExtensionEvent(topic, payload, properties[0], properties[1]);
            return event;
        } else {
            String id = deserializePayload(payload, String.class);
            Event event = new ExtensionEvent(topic, payload, id);
            return event;
        }
    }

    /**
     * Creates an "extension installed" event.
     *
     * @param id the id of the installed extension
     * @return the according event
     */
    public static ExtensionEvent createExtensionInstalledEvent(String id) {
        String topic = buildTopic(EXTENSION_INSTALLED_EVENT_TOPIC, id);
        String payload = serializePayload(id);
        return new ExtensionEvent(topic, payload, id);
    }

    /**
     * Creates an "extension uninstalled" event.
     *
     * @param id the id of the uninstalled extension
     * @return the according event
     */
    public static ExtensionEvent createExtensionUninstalledEvent(String id) {
        String topic = buildTopic(EXTENSION_UNINSTALLED_EVENT_TOPIC, id);
        String payload = serializePayload(id);
        return new ExtensionEvent(topic, payload, id);
    }

    /**
     * Creates an "extension failure" event.
     *
     * @param id the id of the extension that caused a failure
     * @param msg the message text of the failure
     * @return the according event
     */
    public static ExtensionEvent createExtensionFailureEvent(String id, String msg) {
        String topic = buildTopic(EXTENSION_FAILURE_EVENT_TOPIC, id);
        String[] properties = new String[] { id, msg };
        String payload = serializePayload(properties);
        return new ExtensionEvent(topic, payload, id, msg);
    }

    static String buildTopic(String topic, String id) {
        return topic.replace("{id}", id);
    }

}
