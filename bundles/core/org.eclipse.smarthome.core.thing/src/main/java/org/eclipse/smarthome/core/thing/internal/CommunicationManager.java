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
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

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
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.thing.events.ChannelTriggeredEvent;
import org.eclipse.smarthome.core.thing.events.ThingEventFactory;
import org.eclipse.smarthome.core.thing.internal.link.ItemChannelLinkConfigDescriptionProvider;
import org.eclipse.smarthome.core.thing.internal.profiles.DefaultProfileFactory;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.profiles.Profile;
import org.eclipse.smarthome.core.thing.profiles.ProfileAdvisor;
import org.eclipse.smarthome.core.thing.profiles.ProfileFactory;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
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

    private static final Set<String> SUBSCRIBED_EVENT_TYPES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(ItemStateEvent.TYPE, ItemCommandEvent.TYPE, ChannelTriggeredEvent.TYPE)));

    private final Logger logger = LoggerFactory.getLogger(CommunicationManager.class);

    private DefaultProfileFactory defaultProfileFactory;
    private ItemChannelLinkRegistry itemChannelLinkRegistry;
    private ThingRegistry thingRegistry;
    private ItemRegistry itemRegistry;
    private EventPublisher eventPublisher;
    private final Map<ChannelUID, Profile> profiles = new ConcurrentHashMap<>();
    private final Map<ProfileFactory, Set<ChannelUID>> profileFactories = new ConcurrentHashMap<>();
    private final Set<ProfileAdvisor> profileAdvisors = new CopyOnWriteArraySet<>();

    @Override
    public Set<String> getSubscribedEventTypes() {
        return SUBSCRIBED_EVENT_TYPES;
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
        ProfileTypeUID profileTypeUID = determineProfileTypeUID(link, item, thing);
        Profile profile = getProfile(link, profileTypeUID);
        return profile != null ? profile : new NoOpProfile();
    }

    private Profile getProfile(ItemChannelLink link, ProfileTypeUID profileTypeUID) {
        Profile profile = null;
        if (profileTypeUID != null) {
            logger.trace("Going to use profile {} for link {}", profileTypeUID, link);
            synchronized (profiles) {
                profile = profiles.get(link.getLinkedUID());
                if (profile == null) {
                    profile = getProfileFromFactories(profileTypeUID, link.getLinkedUID());
                    if (profile != null) {
                        profiles.put(link.getLinkedUID(), profile);
                    }
                }
            }
        }
        return profile;
    }

    private ProfileTypeUID determineProfileTypeUID(ItemChannelLink link, Item item, Thing thing) {
        ProfileTypeUID profileTypeUID = getConfiguredProfileTypeUID(link);
        Channel channel = null;
        if (profileTypeUID == null) {
            if (thing == null) {
                return null;
            }

            channel = thing.getChannel(link.getLinkedUID().getId());
            if (channel == null) {
                return null;
            }

            if (profileTypeUID == null) {
                // ask advisors
                profileTypeUID = getAdvice(link, item, channel);
            }

            if (profileTypeUID == null) {
                // ask default advisor
                logger.trace("No profile advisor found for link {}, falling back to the defaults", link);
                profileTypeUID = defaultProfileFactory.getSuggestedProfileTypeUID(link, item, channel);
            }
        }
        return profileTypeUID;
    }

    private ProfileTypeUID getAdvice(ItemChannelLink link, Item item, Channel channel) {
        ProfileTypeUID ret;
        for (ProfileAdvisor advisor : profileAdvisors) {
            ret = advisor.getSuggestedProfileTypeUID(link, item, channel);
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }

    private ProfileTypeUID getConfiguredProfileTypeUID(ItemChannelLink link) {
        String profileName = (String) link.getConfiguration()
                .get(ItemChannelLinkConfigDescriptionProvider.PARAM_PROFILE);
        if (profileName != null && !profileName.trim().isEmpty()) {
            profileName = normalizeProfileName(profileName);
            return new ProfileTypeUID(profileName);
        }
        return null;
    }

    private String normalizeProfileName(String profileName) {
        if (!profileName.contains(UID.SEPARATOR)) {
            return ProfileTypeUID.SYSTEM_SCOPE + UID.SEPARATOR + profileName;
        }
        return profileName;
    }

    private Profile getProfileFromFactories(ProfileTypeUID profileTypeUID, ChannelUID channelUID) {
        if (defaultProfileFactory.getSupportedProfileTypeUIDs().contains(profileTypeUID)) {
            return defaultProfileFactory.createProfile(profileTypeUID);
        }
        for (Entry<ProfileFactory, Set<ChannelUID>> entry : profileFactories.entrySet()) {
            ProfileFactory factory = entry.getKey();
            if (factory.getSupportedProfileTypeUIDs().contains(profileTypeUID)) {
                Profile profile = factory.createProfile(profileTypeUID);
                if (profile == null) {
                    logger.error("ProfileFactory {} returned 'null' although it claimed it supports {}", factory,
                            profileTypeUID);
                } else {
                    entry.getValue().add(channelUID);
                    return profile;
                }
            }
        }
        return null;
    }

    private void receiveCommand(ItemCommandEvent commandEvent) {
        final String itemName = commandEvent.getItemName();
        final Command command = commandEvent.getItemCommand();
        final Item item = itemRegistry.get(itemName);
        if (item == null) {
            logger.debug("Received an ItemCommandEvent for item {} which does not exist", itemName);
            return;
        }

        itemChannelLinkRegistry.stream().filter(link -> {
            // all links for the item
            return link.getItemName().equals(itemName);
        }).filter(link -> {
            // make sure the command event is not sent back to its source
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
        if (item == null) {
            logger.debug("Received an ItemStateEvent for item {} which does not exist", itemName);
            return;
        }

        itemChannelLinkRegistry.stream().filter(link -> {
            // all links for the item
            return link.getItemName().equals(itemName);
        }).filter(link -> {
            // make sure the update event is not sent back to its source
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

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addProfileAdvisor(ProfileAdvisor profileAdvisor) {
        profileAdvisors.add(profileAdvisor);
    }

    protected void removeProfileAdvisor(ProfileAdvisor profileAdvisor) {
        profileAdvisors.remove(profileAdvisor);
    }

    @Reference
    protected void setDefaultProfileFactory(DefaultProfileFactory defaultProfileFactory) {
        this.defaultProfileFactory = defaultProfileFactory;
    }

    protected void unsetDefaultProfileFactory(DefaultProfileFactory defaultProfileFactory) {
        this.defaultProfileFactory = null;
    }

    private static class NoOpProfile implements Profile {
    }

}
