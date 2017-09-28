/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.thing.link.events.LinkEventFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * {@link ItemChannelLinkRegistry} tracks all {@link ItemChannelLinkProvider}s
 * and aggregates all {@link ItemChannelLink}s.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Markus Rathgeb - Linked items returns only existing items
 *
 */
@Component(immediate = true, service = ItemChannelLinkRegistry.class)
public class ItemChannelLinkRegistry extends AbstractLinkRegistry<ItemChannelLink, ItemChannelLinkProvider> {

    private ThingRegistry thingRegistry;
    private ItemRegistry itemRegistry;

    public ItemChannelLinkRegistry() {
        super(ItemChannelLinkProvider.class);
    }

    /**
     * Returns a set of bound channels for the given item name.
     *
     * @param itemName item name
     * @return set of bound channels for the given item name
     */
    public Set<ChannelUID> getBoundChannels(String itemName) {

        Set<ChannelUID> channelUIDs = new HashSet<>();

        for (ItemChannelLink itemChannelLink : getAll()) {
            if (itemChannelLink.getItemName().equals(itemName)) {
                channelUIDs.add(itemChannelLink.getLinkedUID());
            }
        }

        return channelUIDs;
    }

    @Override
    public Set<String> getLinkedItemNames(UID uid) {
        final Set<String> linkedItems = new LinkedHashSet<>();
        for (final AbstractLink link : getAll()) {
            final String itemName = link.getItemName();
            if (link.getLinkedUID().equals(uid) && itemRegistry.get(itemName) != null) {
                linkedItems.add(itemName);
            }
        }
        return linkedItems;
    }

    public Set<Item> getLinkedItems(UID uid) {
        final Set<Item> linkedItems = new LinkedHashSet<>();
        for (final AbstractLink link : getAll()) {
            final String itemName = link.getItemName();
            Item item = itemRegistry.get(itemName);
            if (link.getLinkedUID().equals(uid) && item != null) {
                linkedItems.add(item);
            }
        }
        return linkedItems;
    }

    /**
     * Returns a set of bound things for the given item name.
     *
     * @param itemName item name
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

    @Reference
    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

    @Reference
    protected void setItemRegistry(final ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void unsetItemRegistry(final ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, name = "ManagedItemChannelLinkProvider")
    protected void setManagedProvider(ManagedItemChannelLinkProvider provider) {
        super.setManagedProvider(provider);
    }

    protected void unsetManagedProvider(ManagedItemChannelLinkProvider provider) {
        super.removeManagedProvider(provider);
    }

    @Override
    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setEventPublisher(EventPublisher eventPublisher) {
        super.setEventPublisher(eventPublisher);
    }

    @Override
    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        super.unsetEventPublisher(eventPublisher);
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
