/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link.events;

import org.eclipse.smarthome.core.events.AbstractEvent;
import org.eclipse.smarthome.core.thing.link.dto.ItemThingLinkDTO;

/**
 * {@link AbstractItemThingLinkRegistryEvent} is an abstract class for item thing link events.
 *
 * @author Dennis Nobel - Initial contribution
 *
 */
public abstract class AbstractItemThingLinkRegistryEvent extends AbstractEvent {

    private final ItemThingLinkDTO link;

    public AbstractItemThingLinkRegistryEvent(String topic, String payload, ItemThingLinkDTO link) {
        super(topic, payload, null);
        this.link = link;
    }

    public ItemThingLinkDTO getLink() {
        return link;
    }

}
