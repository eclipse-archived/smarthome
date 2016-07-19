/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.inbox.events;

import org.eclipse.smarthome.config.discovery.dto.DiscoveryResultDTO;

/**
 * An {@link InboxUpdatedEvent} notifies subscribers that a discovery result has been updated in the inbox.
 * Inbox updated events must be created with the {@link InboxEventFactory}.
 *
 * @author Stefan Bußweiler - Initial contribution
 */
public class InboxUpdatedEvent extends AbstractInboxEvent {

    /**
     * The inbox updated event type.
     */
    public final static String TYPE = InboxUpdatedEvent.class.getSimpleName();

    /**
     * Constructs a new inbox updated event object.
     *
     * @param topic the topic
     * @param payload the payload
     * @param discoveryResult the discovery-result data transfer object
     */
    protected InboxUpdatedEvent(String topic, String payload, DiscoveryResultDTO discoveryResult) {
        super(topic, payload, discoveryResult);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "Discovery Result with UID '" + getDiscoveryResult().thingUID + "' has been updated.";
    }
}
