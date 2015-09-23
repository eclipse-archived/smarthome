/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link.events;

import org.eclipse.smarthome.core.events.AbstractEventFactory;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFactory;
import org.eclipse.smarthome.core.thing.link.AbstractLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemThingLink;
import org.eclipse.smarthome.core.thing.link.dto.ItemChannelLinkDTO;
import org.eclipse.smarthome.core.thing.link.dto.ItemThingLinkDTO;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * This is an {@link EventFactory} for creating link events. The following event types are supported by this
 * factory:
 *
 * {@link ItemChannelLinkAddedEvent#TYPE}, {@link ItemChannelLinkRemovedEvent#TYPE},
 * {@link ItemThingLinkAddedEvent#TYPE} and {@link ItemThingLinkRemovedEvent}.
 *
 * @author Dennis Nobel - Initial contribution
 */
public class LinkEventFactory extends AbstractEventFactory {

    private static final String LINK_ADDED_EVENT_TOPIC = "smarthome/links/{linkID}/added";

    private static final String LINK_REMOVED_EVENT_TOPIC = "smarthome/links/{linkID}/removed";

    /**
     * Constructs a new LinkEventFactory.
     */
    public LinkEventFactory() {
        super(Sets.newHashSet(ItemChannelLinkAddedEvent.TYPE, ItemChannelLinkRemovedEvent.TYPE,
                ItemThingLinkAddedEvent.TYPE, ItemThingLinkRemovedEvent.TYPE));
    }

    @Override
    protected Event createEventByType(String eventType, String topic, String payload, String source) throws Exception {
        Event event = null;
        if (eventType.equals(ItemChannelLinkAddedEvent.TYPE)) {
            event = createItemChannelLinkAddedEvent(topic, payload);
        } else if (eventType.equals(ItemChannelLinkRemovedEvent.TYPE)) {
            event = createItemChannelLinkRemovedEvent(topic, payload);
        } else if (eventType.equals(ItemThingLinkAddedEvent.TYPE)) {
            event = createItemThingLinkAddedEvent(topic, payload);
        } else if (eventType.equals(ItemThingLinkRemovedEvent.TYPE)) {
            event = createItemThingLinkRemovedEvent(topic, payload);
        }
        return event;
    }

    private Event createItemChannelLinkAddedEvent(String topic, String payload) throws Exception {
        ItemChannelLinkDTO link = deserializePayload(payload, ItemChannelLinkDTO.class);
        return new ItemChannelLinkAddedEvent(topic, payload, link);
    }

    private Event createItemChannelLinkRemovedEvent(String topic, String payload) throws Exception {
        ItemChannelLinkDTO link = deserializePayload(payload, ItemChannelLinkDTO.class);
        return new ItemChannelLinkRemovedEvent(topic, payload, link);
    }

    private Event createItemThingLinkAddedEvent(String topic, String payload) {
        ItemThingLinkDTO link = deserializePayload(payload, ItemThingLinkDTO.class);
        return new ItemThingLinkAddedEvent(topic, payload, link);
    }

    private Event createItemThingLinkRemovedEvent(String topic, String payload) {
        ItemThingLinkDTO link = deserializePayload(payload, ItemThingLinkDTO.class);
        return new ItemThingLinkRemovedEvent(topic, payload, link);
    }

    /**
     * Creates an item channel link added event.
     *
     * @param itemChannelLink item channel link
     *
     * @return the created item channel link added event
     *
     * @throws IllegalArgumentException if item channel link is null
     */
    public static ItemChannelLinkAddedEvent createItemChannelLinkAddedEvent(ItemChannelLink itemChannelLink) {
        assertValidArgument(itemChannelLink);
        String topic = buildTopic(LINK_ADDED_EVENT_TOPIC, itemChannelLink);
        ItemChannelLinkDTO itemChannelLinkDTO = map(itemChannelLink);
        String payload = serializePayload(itemChannelLinkDTO);
        return new ItemChannelLinkAddedEvent(topic, payload, itemChannelLinkDTO);
    }

    /**
     * Creates an item channel link removed event.
     *
     * @param itemChannelLink item channel link
     *
     * @return the created item channel link removed event
     *
     * @throws IllegalArgumentException if item channel link is null
     */
    public static ItemChannelLinkRemovedEvent createItemChannelLinkRemovedEvent(ItemChannelLink itemChannelLink) {
        assertValidArgument(itemChannelLink);
        String topic = buildTopic(LINK_REMOVED_EVENT_TOPIC, itemChannelLink);
        ItemChannelLinkDTO itemChannelLinkDTO = map(itemChannelLink);
        String payload = serializePayload(itemChannelLinkDTO);
        return new ItemChannelLinkRemovedEvent(topic, payload, itemChannelLinkDTO);
    }

    /**
     * Creates an item thing link added event.
     *
     * @param itemThingLink item thing link
     *
     * @return the created item thing link added event
     *
     * @throws IllegalArgumentException if item thing link is null
     */
    public static ItemThingLinkAddedEvent createItemThingLinkAddedEvent(ItemThingLink itemThingLink) {
        assertValidArgument(itemThingLink);
        String topic = buildTopic(LINK_ADDED_EVENT_TOPIC, itemThingLink);
        ItemThingLinkDTO itemThingLinkDTO = map(itemThingLink);
        String payload = serializePayload(itemThingLinkDTO);
        return new ItemThingLinkAddedEvent(topic, payload, itemThingLinkDTO);
    }

    /**
     * Creates an item thing link removed event.
     *
     * @param itemThingLink item thing link
     *
     * @return the created item thing link removed event
     *
     * @throws IllegalArgumentException if item thing link is null
     */
    public static ItemThingLinkRemovedEvent createItemThingLinkRemovedEvent(ItemThingLink itemThingLink) {
        assertValidArgument(itemThingLink);
        String topic = buildTopic(LINK_REMOVED_EVENT_TOPIC, itemThingLink);
        ItemThingLinkDTO itemThingLinkDTO = map(itemThingLink);
        String payload = serializePayload(itemThingLinkDTO);
        return new ItemThingLinkRemovedEvent(topic, payload, itemThingLinkDTO);
    }

    private static void assertValidArgument(AbstractLink itemChannelLink) {
        Preconditions.checkArgument(itemChannelLink != null, "The argument 'itemChannelLink' must not be null.");
    }

    private static String buildTopic(String topic, AbstractLink itemChannelLink) {
        String targetEntity = itemChannelLink.getItemName() + "-" + itemChannelLink.getUID().toString();
        return topic.replace("{linkID}", targetEntity);
    }

    private static ItemChannelLinkDTO map(ItemChannelLink itemChannelLink) {
        return new ItemChannelLinkDTO(itemChannelLink.getItemName(), itemChannelLink.getUID().toString());
    }

    private static ItemThingLinkDTO map(ItemThingLink itemThingLink) {
        return new ItemThingLinkDTO(itemThingLink.getItemName(), itemThingLink.getUID().toString());
    }

}
