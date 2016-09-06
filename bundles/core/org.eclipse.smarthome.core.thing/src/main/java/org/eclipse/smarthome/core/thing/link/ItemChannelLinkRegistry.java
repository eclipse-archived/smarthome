/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.smarthome.core.common.registry.Provider;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.thing.link.events.LinkEventFactory;
import org.osgi.service.component.ComponentContext;

/**
 * {@link ItemChannelLinkRegistry} tracks all {@link ItemChannelLinkProvider}s
 * and aggregates all {@link ItemChannelLink}s.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Markus Rathgeb - Respect item existence
 *
 */
public class ItemChannelLinkRegistry extends AbstractLinkRegistry<ItemChannelLink> {

    /*
     * Methods of the AbstractRegistry does not respect the component life cycle.
     * The method AbstractRegistry.addProvider executes logic if the component has not been activated, too.
     * This could result in some wired runtime behavior because member variables that are injected by the service
     * management could be present or not.
     */
    boolean active = false;
    private final LinkedList<Provider<ItemChannelLink>> waitingProviders = new LinkedList<>();

    private ThingRegistry thingRegistry;
    private ItemRegistry itemRegistry;

    protected void activate(ComponentContext context) {
        itemRegistry.addRegistryChangeListener(itemRegistryChangeListener);
        synchronized (this) {
            active = true;
            while (!waitingProviders.isEmpty()) {
                super.addProvider(waitingProviders.removeFirst());
            }
        }
    }

    protected void deactivate() {
        synchronized (this) {
            active = false;
        }
        itemRegistry.removeRegistryChangeListener(itemRegistryChangeListener);
    }

    private final RegistryChangeListener<Item> itemRegistryChangeListener = new RegistryChangeListener<Item>() {
        @Override
        public void added(Item element) {
            final String itemName = element.getName();
            for (final ItemChannelLink itemChannelLink : getAll()) {
                if (itemChannelLink.getItemName().equals(itemName)) {
                    notifyListenersAboutAddedElement(itemChannelLink, true);
                }
            }
        }

        @Override
        public void removed(Item element) {
            final String itemName = element.getName();
            for (final ItemChannelLink itemChannelLink : getAll()) {
                if (itemChannelLink.getItemName().equals(itemName)) {
                    notifyListenersAboutRemovedElement(itemChannelLink, true);
                }
            }
        }

        @Override
        public void updated(Item oldElement, Item element) {
            if (!oldElement.equals(element)) {
                this.removed(oldElement);
                this.added(element);
            }
        }

    };

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

    @Override
    public Set<String> getLinkedItems(UID uid) {
        final Set<String> linkedItems = new LinkedHashSet<>();
        for (final AbstractLink link : getAll()) {
            final String itemName = link.getItemName();
            if (link.getUID().equals(uid) && itemRegistry.containsItem(itemName)) {
                linkedItems.add(itemName);
            }
        }
        return linkedItems;
    }

    /**
     * Returns a set of bound things for the given item name.
     *
     * @deprecated Will be removed soon. Use {@link ItemThingLinkRegistry#getLinkedThings(String)} instead.
     *
     * @param itemName
     *            item name
     * @return set of bound things for the given item name
     */
    @Deprecated
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

    protected void setItemRegistry(final ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void unsetItemRegistry(final ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    @Override
    protected void addProvider(Provider<ItemChannelLink> provider) {
        // Workaround for addProvider: see description of member 'active' above
        synchronized (this) {
            if (active) {
                super.addProvider(provider);
            } else {
                waitingProviders.add(provider);
            }
        }
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
        notifyListenersAboutAddedElement(element, itemRegistry.containsItem(element.getItemName()));
    }

    @Override
    protected void notifyListenersAboutRemovedElement(ItemChannelLink element) {
        // Workaround for removeProvider: see description of member 'active' above
        final ItemRegistry itemRegistry = this.itemRegistry;
        final boolean contains;
        if (itemRegistry != null) {
            contains = itemRegistry.containsItem(element.getItemName());
        } else {
            contains = true;
        }
        notifyListenersAboutRemovedElement(element, contains);
    }

    @Override
    protected void notifyListenersAboutUpdatedElement(ItemChannelLink oldElement, ItemChannelLink element) {
        super.notifyListenersAboutUpdatedElement(oldElement, element);
        // it is not needed to send an event, because links can not be updated
    }

    protected void notifyListenersAboutAddedElement(final ItemChannelLink element, boolean itemExists) {
        if (itemExists) {
            super.notifyListenersAboutAddedElement(element);
            postEvent(LinkEventFactory.createItemChannelLinkAddedEvent(element));
        }
    }

    protected void notifyListenersAboutRemovedElement(ItemChannelLink element, boolean itemExists) {
        if (itemExists) {
            super.notifyListenersAboutRemovedElement(element);
            postEvent(LinkEventFactory.createItemChannelLinkRemovedEvent(element));
        }
    }

}
