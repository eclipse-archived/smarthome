/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemCommandEvent;
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.DefaultSystemChannelTypeProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.events.ChannelTriggeredEvent;
import org.eclipse.smarthome.core.thing.events.ThingEventFactory;
import org.eclipse.smarthome.core.thing.internal.profiles.DefaultMasterProfile;
import org.eclipse.smarthome.core.thing.internal.profiles.RawButtonTriggerProfile;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.profiles.Profile;
import org.eclipse.smarthome.core.thing.profiles.ProfileFactory;
import org.eclipse.smarthome.core.thing.profiles.StateProfile;
import org.eclipse.smarthome.core.thing.profiles.TriggerProfile;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages the state related communication between bindings and the framework.
 *
 * It mainly mediates commands, state updates and triggers from ThingHandlers to the framework and vice versa.
 *
 * @author Simon Kaufmann - initial contribution and API, factored out of ThingManger
 *
 */
@Component(service = { EventSubscriber.class, CommunicationManager.class }, immediate = true)
public class CommunicationManager implements EventSubscriber, RegistryChangeListener<ItemChannelLink> {

    private final Logger logger = LoggerFactory.getLogger(CommunicationManager.class);
    private final Set<String> subscribedEventTypes = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(ItemStateEvent.TYPE, ItemCommandEvent.TYPE, ChannelTriggeredEvent.TYPE)));

    private ItemChannelLinkRegistry itemChannelLinkRegistry;
    private ThingRegistry thingRegistry;
    private ItemRegistry itemRegistry;
    private EventPublisher eventPublisher;
    private final Map<ChannelUID, Profile> profiles = new ConcurrentHashMap<>();
    private final Map<ProfileFactory, Set<ChannelUID>> profileFactories = new ConcurrentHashMap<>();

    @Override
    public Set<String> getSubscribedEventTypes() {
        return subscribedEventTypes;
    }

    @Override
    public EventFilter getEventFilter() {
        return null;
    }

    @Override
    public void receive(Event event) {
        if (event instanceof ItemStateEvent) {
            receiveUpdate((ItemStateEvent) event);
        } else if (event instanceof ItemCommandEvent) {
            receiveCommand((ItemCommandEvent) event);
        } else if (event instanceof ChannelTriggeredEvent) {
            receiveTrigger((ChannelTriggeredEvent) event);
        }
    }

    private Thing getThing(ThingUID thingUID) {
        return thingRegistry.get(thingUID);
    }

    private Profile getProfile(ItemChannelLink link, Item item, Thing thing) {
        if (thing == null) {
            return new NoOpProfile();
        }

        Channel channel = thing.getChannel(link.getLinkedUID().getId());
        if (channel == null) {
            return new NoOpProfile();
        }

        Profile profile = null;
        synchronized (profiles) {
            profile = profiles.get(link.getLinkedUID());
            if (profile == null) {
                profile = getProfileFromFactories(link, item, channel);
                if (profile == null) {
                    logger.trace("No profile factory found for link {}, falling back to the defaults", link);
                    profile = createDefaultProfile(channel);
                }
                if (profile != null) {
                    profiles.put(link.getLinkedUID(), profile);
                }
            }
        }
        return profile != null ? profile : new NoOpProfile();
    }

    private Profile getProfileFromFactories(ItemChannelLink link, Item item, Channel channel) {
        for (ProfileFactory profileFactory : profileFactories.keySet()) {
            Profile profile = profileFactory.createProfile(link, item, channel);
            if (profile != null) {
                profileFactories.get(profileFactory).add(link.getLinkedUID());
                logger.trace("Going to use profile {} for link {}", profile, link);
                return profile;
            }
        }
        return null;
    }

    private Profile createDefaultProfile(Channel channel) {
        switch (channel.getKind()) {
            case STATE:
                return new DefaultMasterProfile();
            case TRIGGER:
                if (DefaultSystemChannelTypeProvider.SYSTEM_RAWBUTTON.getUID().equals(channel.getChannelTypeUID())) {
                    return new RawButtonTriggerProfile();
                }
                break;
            default:
                throw new NotImplementedException();
        }
        return null;
    }

    private void receiveCommand(ItemCommandEvent commandEvent) {
        final String itemName = commandEvent.getItemName();
        final Command command = commandEvent.getItemCommand();
        final Item item = itemRegistry.get(itemName);

        itemChannelLinkRegistry.stream().filter(link -> {
            // all links for the item
            return link.getItemName().equals(itemName);
        }).filter(link -> {
            // make sure a command event is not sent back to its source
            return !link.getLinkedUID().toString().equals(commandEvent.getSource());
        }).forEach(link -> {
            ChannelUID channelUID = link.getLinkedUID();
            Thing thing = getThing(channelUID.getThingUID());
            Profile profile = getProfile(link, item, thing);
            if (profile instanceof StateProfile) {
                ((StateProfile) profile).onCommand(link, thing, command);
            }
        });
    }

    private void receiveUpdate(ItemStateEvent updateEvent) {
        final String itemName = updateEvent.getItemName();
        final State newState = updateEvent.getItemState();
        final Item item = itemRegistry.get(itemName);

        itemChannelLinkRegistry.stream().filter(link -> {
            // all links for the item
            return link.getItemName().equals(itemName);
        }).filter(link -> {
            // make sure a command event is not sent back to its source
            return !link.getLinkedUID().toString().equals(updateEvent.getSource());
        }).forEach(link -> {
            ChannelUID channelUID = link.getLinkedUID();
            Thing thing = getThing(channelUID.getThingUID());
            Profile profile = getProfile(link, item, thing);
            if (profile instanceof StateProfile) {
                ((StateProfile) profile).onUpdate(link, thing, newState);
            }
        });
    }

    private void receiveTrigger(ChannelTriggeredEvent channelTriggeredEvent) {
        final ChannelUID channelUID = channelTriggeredEvent.getChannel();
        final String event = channelTriggeredEvent.getEvent();
        final Thing thing = getThing(channelUID.getThingUID());

        itemChannelLinkRegistry.stream().filter(link -> {
            // all links for the channel
            return link.getLinkedUID().equals(channelUID);
        }).forEach(link -> {
            Item item = itemRegistry.get(link.getItemName());
            if (item != null) {
                Profile profile = getProfile(link, item, thing);
                if (profile instanceof TriggerProfile) {
                    ((TriggerProfile) profile).onTrigger(eventPublisher, link, event, item);
                }
            }
        });
    }

    public void stateUpdated(ChannelUID channelUID, State state) {
        final Thing thing = getThing(channelUID.getThingUID());

        itemChannelLinkRegistry.stream().filter(link -> {
            // all links for the channel
            return link.getLinkedUID().equals(channelUID);
        }).forEach(link -> {
            Item item = itemRegistry.get(link.getItemName());
            if (item != null) {
                Profile profile = getProfile(link, item, thing);
                if (profile instanceof StateProfile) {
                    ((StateProfile) profile).stateUpdated(eventPublisher, link, state, item);
                }
            }
        });
    }

    public void postCommand(ChannelUID channelUID, Command command) {
        final Thing thing = getThing(channelUID.getThingUID());

        itemChannelLinkRegistry.stream().filter(link -> {
            // all links for the channel
            return link.getLinkedUID().equals(channelUID);
        }).forEach(link -> {
            Item item = itemRegistry.get(link.getItemName());
            if (item != null) {
                Profile profile = getProfile(link, item, thing);
                if (profile instanceof StateProfile) {
                    ((StateProfile) profile).postCommand(eventPublisher, link, command, item);
                }
            }
        });
    }

    public void channelTriggered(Thing thing, ChannelUID channelUID, String event) {
        eventPublisher.post(ThingEventFactory.createTriggerEvent(event, channelUID));
    }

    private void cleanup(ChannelUID channelUID) {
        synchronized (profiles) {
            profiles.remove(channelUID);
        }
        profileFactories.values().forEach(list -> list.remove(channelUID));
    }

    @Override
    public void added(ItemChannelLink element) {
        // nothing to do
    }

    @Override
    public void removed(ItemChannelLink element) {
        cleanup(element.getLinkedUID());
    }

    @Override
    public void updated(ItemChannelLink oldElement, ItemChannelLink element) {
        cleanup(oldElement.getLinkedUID());
    }

    @Reference
    protected void setItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        itemChannelLinkRegistry.addRegistryChangeListener(this);
    }

    protected void unsetItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = null;
    }

    @Reference
    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

    @Reference
    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    @Reference
    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addProfileFactory(ProfileFactory profileFactory) {
        this.profileFactories.put(profileFactory, ConcurrentHashMap.newKeySet());
    }

    protected void removeProfileFactory(ProfileFactory profileFactory) {
        Set<ChannelUID> channelUIDs = this.profileFactories.remove(profileFactory);
        synchronized (profiles) {
            channelUIDs.forEach(channelUID -> profiles.remove(channelUID));
        }
    }

    private static class NoOpProfile implements Profile {
    }

}
