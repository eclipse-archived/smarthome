/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.inbox.events;

import org.eclipse.smarthome.config.discovery.dto.DiscoveryResultDTO;
import org.eclipse.smarthome.config.discovery.inbox.Inbox;
import org.eclipse.smarthome.core.events.AbstractEvent;

/**
 * Abstract implementation of an inbox event which will be posted by the {@link Inbox} for added, removed
 * and updated discovery results.
 *
 * @author Stefan Bußweiler - Initial contribution
 */
public abstract class AbstractInboxEvent extends AbstractEvent {

    private final DiscoveryResultDTO discoveryResult;

    /**
     * Must be called in subclass constructor to create an inbox event.
     *
     * @param topic the topic
     * @param payload the payload
     * @param discoveryResult the discovery-result data transfer object
     */
    public AbstractInboxEvent(String topic, String payload, DiscoveryResultDTO discoveryResult) {
        super(topic, payload, null);
        this.discoveryResult = discoveryResult;
    }

    /**
     * Gets the discovery result as data transfer object.
     * 
     * @return the discoveryResult
     */
    public DiscoveryResultDTO getDiscoveryResult() {
        return discoveryResult;
    }

}
