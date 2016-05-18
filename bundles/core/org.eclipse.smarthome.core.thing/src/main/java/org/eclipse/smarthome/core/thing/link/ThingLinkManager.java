/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link;

import java.util.List;

import org.eclipse.smarthome.core.common.registry.Provider;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.TypeResolver;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ThingLinkManager} manages links for channels.
 * <p>
 * If a Thing is created, it can automatically create links for its non-advanced channels.
 * Upon a Thing deletion, it removes all links of this Thing.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Markus Rathgeb - Handle item registry's all items changed notification
 * @author Kai Kreuzer - Refactored to make it a service and introduced the auto-linking (as a replacement for the
 *         ThingSetupManager)
 */
public class ThingLinkManager {

    private Logger logger = LoggerFactory.getLogger(ThingLinkManager.class);

    private ThingRegistry thingRegistry;
    private ManagedThingProvider managedThingProvider;
    private ItemChannelLinkRegistry itemChannelLinkRegistry;

    private boolean autoLinks = true;

    protected void activate(ComponentContext context) {
        modified(context);
        itemChannelLinkRegistry.addRegistryChangeListener(itemChannelLinkRegistryChangeListener);
        managedThingProvider.addProviderChangeListener(managedThingProviderListener);
    }

    protected void modified(ComponentContext context) {
        // check whether we want to enable the automatic link creation or not
        if (context != null) {
            Object value = context.getProperties().get("autoLinks");
            autoLinks = value == null || !value.toString().equals("false");
        }
    }

    protected void deactivate() {
        itemChannelLinkRegistry.removeRegistryChangeListener(itemChannelLinkRegistryChangeListener);
        managedThingProvider.removeProviderChangeListener(managedThingProviderListener);
    }

    protected void setItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    protected void unsetItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = null;
    }

    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

    protected void setManagedThingProvider(ManagedThingProvider managedThingProvider) {
        this.managedThingProvider = managedThingProvider;
    }

    protected void unsetManagedThingProvider(ManagedThingProvider managedThingProvider) {
        this.managedThingProvider = null;
    }

    public boolean isAutoLinksEnabled() {
        return autoLinks;
    }

    private final RegistryChangeListener<ItemChannelLink> itemChannelLinkRegistryChangeListener = new RegistryChangeListener<ItemChannelLink>() {

        @Override
        public void added(ItemChannelLink itemChannelLink) {
            ChannelUID channelUID = itemChannelLink.getUID();
            Thing thing = thingRegistry.get(channelUID.getThingUID());
            if (thing != null) {
                Channel channel = thing.getChannel(channelUID.getId());
                if (channel != null) {
                    ThingLinkManager.this.informHandlerAboutLinkedChannel(thing, channel);
                }
            }
        }

        @Override
        public void removed(ItemChannelLink itemChannelLink) {
            ChannelUID channelUID = itemChannelLink.getUID();
            Thing thing = thingRegistry.get(channelUID.getThingUID());
            if (thing != null) {
                Channel channel = thing.getChannel(channelUID.getId());
                if (channel != null) {
                    ThingLinkManager.this.informHandlerAboutUnlinkedChannel(thing, channel);
                }
            }
        }

        @Override
        public void updated(ItemChannelLink oldElement, ItemChannelLink element) {
            if (!oldElement.equals(element)) {
                this.removed(oldElement);
                this.added(element);
            }
        }

    };

    private final ProviderChangeListener<Thing> managedThingProviderListener = new ProviderChangeListener<Thing>() {

        @Override
        public void added(Provider<Thing> provider, Thing thing) {
            List<Channel> channels = thing.getChannels();
            for (Channel channel : channels) {
                createLinkIfNotAdvanced(channel);
            }
        }

        private void createLinkIfNotAdvanced(Channel channel) {
            if (autoLinks) {
                if (channel.getChannelTypeUID() != null) {
                    ChannelType type = TypeResolver.resolve(channel.getChannelTypeUID());
                    if (type != null && type.isAdvanced()) {
                        return;
                    }
                }
                ItemChannelLink link = new ItemChannelLink(deriveItemName(channel.getUID()), channel.getUID());
                itemChannelLinkRegistry.add(link);
            }
        }

        @Override
        public void removed(Provider<Thing> provider, Thing thing) {
            List<Channel> channels = thing.getChannels();
            for (Channel channel : channels) {
                ItemChannelLink link = new ItemChannelLink(deriveItemName(channel.getUID()), channel.getUID());
                itemChannelLinkRegistry.remove(link.getID());
            }
        }

        @Override
        public void updated(Provider<Thing> provider, Thing oldThing, Thing newThing) {
            for (Channel channel : oldThing.getChannels()) {
                if (newThing.getChannel(channel.getUID().getId()) == null) {
                    // this channel does not exist anymore, so remove outdated links
                    ItemChannelLink link = new ItemChannelLink(deriveItemName(channel.getUID()), channel.getUID());
                    itemChannelLinkRegistry.remove(link.getID());
                }
            }
            for (Channel channel : newThing.getChannels()) {
                if (oldThing.getChannel(channel.getUID().getId()) == null) {
                    // this channel did not exist before, so add a link
                    createLinkIfNotAdvanced(channel);
                }
            }
        }

        private String deriveItemName(ChannelUID uid) {
            return uid.getAsString().replaceAll("[^a-zA-Z0-9_]", "_");
        }

    };

    private void informHandlerAboutLinkedChannel(Thing thing, Channel channel) {
        ThingHandler handler = thing.getHandler();
        if (handler != null) {
            try {
                handler.channelLinked(channel.getUID());
            } catch (Exception ex) {
                logger.error("Exception occured while informing handler:" + ex.getMessage(), ex);
            }
        } else {
            logger.trace("Can not inform handler about linked channel, because no handler is assigned to the thing {}.",
                    thing.getUID());
        }
    }

    private void informHandlerAboutUnlinkedChannel(Thing thing, Channel channel) {
        ThingHandler handler = thing.getHandler();
        if (handler != null) {
            try {
                handler.channelUnlinked(channel.getUID());
            } catch (Exception ex) {
                logger.error("Exception occured while informing handler:" + ex.getMessage(), ex);
            }
        } else {
            logger.trace(
                    "Can not inform handler about unlinked channel, because no handler is assigned to the thing {}.",
                    thing.getUID());
        }
    }
}
