/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.events;

import org.eclipse.smarthome.core.events.AbstractEvent;
import org.eclipse.smarthome.core.types.Type;

/**
 * {@link ThingTriggerEvent}s can be used to deliver triggers through the Eclipse SmartHome event bus.
 * Trigger events must be created with the {@link ThingEventFactory}.
 */
public class ThingTriggerEvent extends AbstractEvent {

    /**
     * The thing trigger event type.
     */
    public final static String TYPE = ThingTriggerEvent.class.getSimpleName();

    /**
     * The event.
     */
    private final Type event;

    /**
     * Constructs a new thing trigger event.
     *
     * @param topic the topic. The topic includes the thing UID, see
     *            {@link ThingEventFactory#THING_TRIGGERED_EVENT_TOPIC}
     * @param payload the payload. Contains a serialized {@link ThingEventFactory.TriggerEventPayloadBean}.
     * @param source the source
     */
    protected ThingTriggerEvent(String topic, String payload, String source, Type event) {
        super(topic, payload, source);
        this.event = event;
    }

    /**
     * Returns the event.
     *
     * @return the event
     */
    public Type getEvent() {
        return event;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return event.toString();
    }

}
