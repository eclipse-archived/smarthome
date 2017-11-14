/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.inbox.events;

import org.eclipse.smarthome.config.discovery.dto.DiscoveryResultDTO;

/**
 * An {@link InboxAddedEvent} notifies subscribers that a discovery result has been added to the inbox.
 * Inbox added events must be created with the {@link InboxEventFactory}.
 *
 * @author Stefan Bußweiler - Initial contribution
 */
public class InboxAddedEvent extends AbstractInboxEvent {

    /**
     * The inbox added event type.
     */
    public final static String TYPE = InboxAddedEvent.class.getSimpleName();

    /**
     * Constructs a new inbox added event object.
     *
     * @param topic the topic
     * @param payload the payload
     * @param discoveryResult the discovery result data transfer object
     */
    InboxAddedEvent(String topic, String payload, DiscoveryResultDTO discoveryResult) {
        super(topic, payload, discoveryResult);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "Discovery Result with UID '" + getDiscoveryResult().thingUID + "' has been added.";
    }
}
