/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link.events;

import org.eclipse.smarthome.core.thing.link.dto.ItemThingLinkDTO;

/**
 * A {@link ItemThingLinkAddedEvent} notifies subscribers that an item thing link has been added.
 * Events must be created with the {@link LinkEventFactory}.
 *
 * @author Dennis Nobel - Initial contribution
 */
public class ItemThingLinkAddedEvent extends AbstractItemThingLinkRegistryEvent {

    /**
     * The link added event type.
     */
    public final static String TYPE = ItemThingLinkAddedEvent.class.getSimpleName();

    public ItemThingLinkAddedEvent(String topic, String payload, ItemThingLinkDTO link) {
        super(topic, payload, link);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        ItemThingLinkDTO link = getLink();
        return "Link '" + link.itemName + "-" + link.thingUID + "' has been added.";
    }

}
