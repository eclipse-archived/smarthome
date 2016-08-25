/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.events;

import java.util.List;

import org.eclipse.smarthome.core.events.AbstractEventFactory;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.dto.ThingDTO;
import org.eclipse.smarthome.core.thing.dto.ThingDTOMapper;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.TypeParser;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * A {@link ThingEventFactory} is responsible for creating thing event instances, e.g. {@link ThingStatusInfoEvent}s.
 *
 * @author Stefan Bußweiler - Initial contribution
 * @author Dennis Nobel - Added status changed event
 */
public class ThingEventFactory extends AbstractEventFactory {

    private static final String CORE_LIBRARY_PACKAGE = "org.eclipse.smarthome.core.library.types.";

    private static final String THING_STATUS_INFO_EVENT_TOPIC = "smarthome/things/{thingUID}/status";

    private static final String THING_STATUS_INFO_CHANGED_EVENT_TOPIC = "smarthome/things/{thingUID}/statuschanged";

    private static final String THING_ADDED_EVENT_TOPIC = "smarthome/things/{thingUID}/added";

    private static final String THING_REMOVED_EVENT_TOPIC = "smarthome/things/{thingUID}/removed";

    private static final String THING_UPDATED_EVENT_TOPIC = "smarthome/things/{thingUID}/updated";

    private static final String THING_TRIGGERED_EVENT_TOPIC = "smarthome/things/{thingUID}/triggered";

    /**
     * Constructs a new ThingEventFactory.
     */
    public ThingEventFactory() {
        super(Sets.newHashSet(ThingStatusInfoEvent.TYPE, ThingStatusInfoChangedEvent.TYPE, ThingAddedEvent.TYPE,
                ThingRemovedEvent.TYPE, ThingUpdatedEvent.TYPE, ThingTriggerEvent.TYPE));
    }

    @Override
    protected Event createEventByType(String eventType, String topic, String payload, String source) throws Exception {
        Event event = null;
        if (eventType.equals(ThingStatusInfoEvent.TYPE)) {
            event = createStatusInfoEvent(topic, payload);
        } else if (eventType.equals(ThingStatusInfoChangedEvent.TYPE)) {
            event = createStatusInfoChangedEvent(topic, payload);
        } else if (eventType.equals(ThingAddedEvent.TYPE)) {
            event = createAddedEvent(topic, payload);
        } else if (eventType.equals(ThingRemovedEvent.TYPE)) {
            event = createRemovedEvent(topic, payload);
        } else if (eventType.equals(ThingUpdatedEvent.TYPE)) {
            event = createUpdatedEvent(topic, payload);
        } else if (eventType.equals(ThingTriggerEvent.TYPE)) {
            event = createTriggerEvent(topic, payload, source);
        }

        return event;
    }

    /**
     * This is a java bean that is used to serialize/deserialize trigger event payload.
     */
    private static class TriggerEventPayloadBean {
        private String type;
        private String value;

        /**
         * Default constructor for deserialization e.g. by Gson.
         */
        protected TriggerEventPayloadBean() {
        }

        public TriggerEventPayloadBean(String type, String value) {
            this.type = type;
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Creates a trigger event from a {@link Type}.
     *
     * @param event Event.
     *              @param thing thing, which triggered the event.
     * @param source Source.
     * @return Created trigger event.
     */
    public static ThingTriggerEvent createTriggerEvent(Type event, ThingUID thing, String source) {
        TriggerEventPayloadBean bean = new TriggerEventPayloadBean(event.getClass().getSimpleName(), event.toString());
        String payload = serializePayload(bean);
        String topic = buildTopic(THING_TRIGGERED_EVENT_TOPIC, thing.getAsString());
        return new ThingTriggerEvent(topic, payload, source, event);
    }

    /**
     * Creates a trigger event from a payload.
     *
     * @param topic Event topic
     * @param source Event source
     * @param payload Payload
     * @return created trigger event
     */
    public ThingTriggerEvent createTriggerEvent(String topic, String payload, String source) {
        TriggerEventPayloadBean bean = deserializePayload(payload, TriggerEventPayloadBean.class);

        Type event = TypeParser.parseType(bean.getType(), bean.getValue());
        return new ThingTriggerEvent(topic, payload, source, event);
    }

    private Event createStatusInfoEvent(String topic, String payload) throws Exception {
        String[] topicElements = getTopicElements(topic);
        if (topicElements.length != 4) {
            throw new IllegalArgumentException("ThingStatusInfoEvent creation failed, invalid topic: " + topic);
        }
        ThingUID thingUID = new ThingUID(topicElements[2]);
        ThingStatusInfo thingStatusInfo = deserializePayload(payload, ThingStatusInfo.class);
        return new ThingStatusInfoEvent(topic, payload, thingUID, thingStatusInfo);
    }

    private Event createStatusInfoChangedEvent(String topic, String payload) throws Exception {
        String[] topicElements = getTopicElements(topic);
        if (topicElements.length != 4) {
            throw new IllegalArgumentException("ThingStatusInfoChangedEvent creation failed, invalid topic: " + topic);
        }
        ThingUID thingUID = new ThingUID(topicElements[2]);
        ThingStatusInfo[] thingStatusInfo = deserializePayload(payload, ThingStatusInfo[].class);
        return new ThingStatusInfoChangedEvent(topic, payload, thingUID, thingStatusInfo[0], thingStatusInfo[1]);
    }

    private Event createAddedEvent(String topic, String payload) throws Exception {
        ThingDTO thingDTO = deserializePayload(payload, ThingDTO.class);
        return new ThingAddedEvent(topic, payload, thingDTO);
    }

    private Event createRemovedEvent(String topic, String payload) throws Exception {
        ThingDTO thingDTO = deserializePayload(payload, ThingDTO.class);
        return new ThingRemovedEvent(topic, payload, thingDTO);
    }

    private Event createUpdatedEvent(String topic, String payload) throws Exception {
        ThingDTO[] thingDTO = deserializePayload(payload, ThingDTO[].class);
        if (thingDTO.length != 2) {
            throw new IllegalArgumentException("ThingUpdateEvent creation failed, invalid payload: " + payload);
        }
        return new ThingUpdatedEvent(topic, payload, thingDTO[0], thingDTO[1]);
    }

    /**
     * Creates a new thing status info event based on a thing UID and a thing status info object.
     *
     * @param thingUID the thing UID
     * @param thingStatusInfo the thing status info object
     *
     * @return the created thing status info event
     *
     * @throws IllegalArgumentException if thingUID or thingStatusInfo is null
     */
    public static ThingStatusInfoEvent createStatusInfoEvent(ThingUID thingUID, ThingStatusInfo thingStatusInfo) {
        Preconditions.checkArgument(thingUID != null, "The argument 'thingUID' must not be null.");
        Preconditions.checkArgument(thingStatusInfo != null, "The argument 'thingStatusInfo' must not be null.");

        String topic = buildTopic(THING_STATUS_INFO_EVENT_TOPIC, thingUID.getAsString());
        String payload = serializePayload(thingStatusInfo);
        return new ThingStatusInfoEvent(topic, payload, thingUID, thingStatusInfo);
    }

    /**
     * Creates a new thing status info changed event based on a thing UID, a thing status info and the old thing status
     * info object.
     *
     * @param thingUID the thing UID
     * @param thingStatusInfo the thing status info object
     * @param oldThingStatusInfo the old thing status info object
     *
     * @return the created thing status info changed event
     *
     * @throws IllegalArgumentException if thingUID or thingStatusInfo is null
     */
    public static ThingStatusInfoChangedEvent createStatusInfoChangedEvent(ThingUID thingUID,
            ThingStatusInfo thingStatusInfo, ThingStatusInfo oldThingStatusInfo) {
        Preconditions.checkArgument(thingUID != null, "The argument 'thingUID' must not be null.");
        Preconditions.checkArgument(thingStatusInfo != null, "The argument 'thingStatusInfo' must not be null.");
        Preconditions.checkArgument(oldThingStatusInfo != null, "The argument 'oldThingStatusInfo' must not be null.");

        String topic = buildTopic(THING_STATUS_INFO_CHANGED_EVENT_TOPIC, thingUID.getAsString());
        String payload = serializePayload(new ThingStatusInfo[] { thingStatusInfo, oldThingStatusInfo });
        return new ThingStatusInfoChangedEvent(topic, payload, thingUID, thingStatusInfo, oldThingStatusInfo);
    }

    /**
     * Creates a thing added event.
     *
     * @param thing the thing
     *
     * @return the created thing added event
     *
     * @throws IllegalArgumentException if thing is null
     */
    public static ThingAddedEvent createAddedEvent(Thing thing) {
        assertValidArgument(thing);
        String topic = buildTopic(THING_ADDED_EVENT_TOPIC, thing.getUID().getAsString());
        ThingDTO thingDTO = map(thing);
        String payload = serializePayload(thingDTO);
        return new ThingAddedEvent(topic, payload, thingDTO);
    }

    /**
     * Creates a thing removed event.
     *
     * @param thing the thing
     *
     * @return the created thing removed event
     *
     * @throws IllegalArgumentException if thing is null
     */
    public static ThingRemovedEvent createRemovedEvent(Thing thing) {
        assertValidArgument(thing);
        String topic = buildTopic(THING_REMOVED_EVENT_TOPIC, thing.getUID().getAsString());
        ThingDTO thingDTO = map(thing);
        String payload = serializePayload(thingDTO);
        return new ThingRemovedEvent(topic, payload, thingDTO);
    }

    /**
     * Creates a thing updated event.
     *
     * @param thing the thing
     * @param oldThing the old thing
     *
     * @return the created thing updated event
     *
     * @throws IllegalArgumentException if thing or oldThing is null
     */
    public static ThingUpdatedEvent createUpdateEvent(Thing thing, Thing oldThing) {
        assertValidArgument(thing);
        assertValidArgument(oldThing);
        String topic = buildTopic(THING_UPDATED_EVENT_TOPIC, thing.getUID().getAsString());
        ThingDTO thingDTO = map(thing);
        ThingDTO oldThingDTO = map(oldThing);
        List<ThingDTO> thingDTOs = Lists.newLinkedList();
        thingDTOs.add(thingDTO);
        thingDTOs.add(oldThingDTO);
        String payload = serializePayload(thingDTOs);
        return new ThingUpdatedEvent(topic, payload, thingDTO, oldThingDTO);
    }

    private static void assertValidArgument(Thing thing) {
        Preconditions.checkArgument(thing != null, "The argument 'thing' must not be null.");
        Preconditions.checkArgument(thing.getUID() != null, "The thingUID of a thing must not be null.");
    }

    private static String buildTopic(String topic, String thingUID) {
        return topic.replace("{thingUID}", thingUID);
    }

    private static ThingDTO map(Thing thing) {
        return ThingDTOMapper.map(thing);
    }

}
