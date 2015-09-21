/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link.events;

import org.eclipse.smarthome.core.events.AbstractEvent;
import org.eclipse.smarthome.core.thing.link.dto.ItemChannelLinkDTO;

/**
 * {@link AbstractItemChannelLinkRegistryEvent} is an abstract class for item channel link events.
 *
 * @author Dennis Nobel - Initial contribution
 *
 */
public abstract class AbstractItemChannelLinkRegistryEvent extends AbstractEvent {

    private final ItemChannelLinkDTO link;

    public AbstractItemChannelLinkRegistryEvent(String topic, String payload, ItemChannelLinkDTO link) {
        super(topic, payload, null);
        this.link = link;
    }

    public ItemChannelLinkDTO getLink() {
        return link;
    }

}
