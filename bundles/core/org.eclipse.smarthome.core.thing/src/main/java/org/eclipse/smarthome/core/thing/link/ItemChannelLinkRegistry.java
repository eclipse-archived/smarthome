/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.link.events.LinkEventFactory;

/**
 * {@link ItemChannelLinkRegistry} tracks all {@link ItemChannelLinkProvider}s
 * and aggregates all {@link ItemChannelLink}s.
 *
 * @author Dennis Nobel - Initial contribution
 *
 */
public class ItemChannelLinkRegistry extends AbstractLinkRegistry<ItemChannelLink> {

    private ThingRegistry thingRegistry;

    /**
     * Returns a set of bound channels for the given item name.
     *
     * @param itemName
     *            item name
     * @return set of bound channels for the given item name
     */
    public Set<ChannelUID> getBoundChannels(String itemName) {

        Set<ChannelUID> channelUIDs = new HashSet<>();

        for (ItemChannelLink itemChannelLink : getAll()) {
            if (itemChannelLink.getItemName().equals(itemName)) {
                channelUIDs.add(itemChannelLink.getUID());
            }
        }

        return channelUIDs;
    }

    /**
     * Channels can not be updated, so this methods throws an {@link UnsupportedOperationException}.
     */
    @Override
    public ItemChannelLink update(ItemChannelLink element) {
        throw new UnsupportedOperationException("Channels can not be updated.");
    }

    /**
     * Returns a set of bound things for the given item name.
     *
     * @param itemName
     *            item name
     * @return set of bound things for the given item name
     */
    public Set<Thing> getBoundThings(String itemName) {

        Set<Thing> things = new HashSet<>();
        Collection<ChannelUID> boundChannels = getBoundChannels(itemName);

        for (ChannelUID channelUID : boundChannels) {
            Thing thing = thingRegistry.get(channelUID.getThingUID());
            if (thing != null) {
                things.add(thing);
            }
        }

        return things;
    }

    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

    public void removeLinksForThing(ThingUID thingUID) {
        if (this.managedProvider != null) {
            ((ManagedItemChannelLinkProvider) this.managedProvider).removeLinksForThing(thingUID);
        } else {
            throw new IllegalStateException("ManagedProvider is not available");
        }
    }

    @Override
    protected void notifyListenersAboutAddedElement(ItemChannelLink element) {
        super.notifyListenersAboutAddedElement(element);
        postEvent(LinkEventFactory.createItemChannelLinkAddedEvent(element));
    }

    @Override
    protected void notifyListenersAboutRemovedElement(ItemChannelLink element) {
        super.notifyListenersAboutRemovedElement(element);
        postEvent(LinkEventFactory.createItemChannelLinkRemovedEvent(element));
    }

    @Override
    protected void notifyListenersAboutUpdatedElement(ItemChannelLink oldElement, ItemChannelLink element) {
        super.notifyListenersAboutUpdatedElement(oldElement, element);
        // it is not needed to send an event, because links can not be updated
    }
}
