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
 * An {@link InboxRemovedEvent} notifies subscribers that a discovery result has been removed from the inbox.
 * Inbox removed events must be created with the {@link InboxEventFactory}.
 *
 * @author Stefan Bußweiler - Initial contribution
 */
public class InboxRemovedEvent extends AbstractInboxEvent {

    /**
     * The inbox removed event type.
     */
    public final static String TYPE = InboxRemovedEvent.class.getSimpleName();

    /**
     * Constructs a new inbox removed event object.
     *
     * @param topic the topic
     * @param payload the payload
     * @param discoveryResult the discovery result data transfer object
     */
    InboxRemovedEvent(String topic, String payload, DiscoveryResultDTO discoveryResult) {
        super(topic, payload, discoveryResult);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "Discovery Result with UID '" + getDiscoveryResult().thingUID + "' has been removed.";
    }
}
