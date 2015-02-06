/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sse.internal.listeners;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingRegistryChangeListener;
import org.eclipse.smarthome.io.rest.core.thing.beans.ThingBean;
import org.eclipse.smarthome.io.rest.core.util.BeanMapper;
import org.eclipse.smarthome.io.rest.sse.EventType;
import org.eclipse.smarthome.io.rest.sse.SseResource;

/**
 * Listener responsible for broadcasting thing registry events to all clients
 * subscribed to them.
 *
 * @author Ivan Iliev - Initial Contribution and API
 *
 */
public class ThingRegistryEventListener implements ThingRegistryChangeListener {

    private ThingRegistry thingRegistry;

    private SseResource sseResource;

    protected void setSseResource(SseResource sseResource) {
        this.sseResource = sseResource;
    }

    protected void unsetSseResource(SseResource sseResource) {
        this.sseResource = null;
    }

    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
        this.thingRegistry.addRegistryChangeListener(this);
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry.removeRegistryChangeListener(this);
        this.thingRegistry = null;
    }

    @Override
    public void added(Thing element) {
        broadcastThingEvent(element.getUID().getId(), EventType.THING_ADDED, element);

    }

    @Override
    public void removed(Thing element) {
        broadcastThingEvent(element.getUID().getId(), EventType.THING_REMOVED, element);
    }

    @Override
    public void updated(Thing oldElement, Thing element) {
        broadcastThingEvent(element.getUID().getId(), EventType.THING_UPDATED, oldElement, element);
    }

    private void broadcastThingEvent(String thingIdentifier, EventType eventType, Thing... elements) {
        Object eventObject = null;
        if (elements != null && elements.length > 0) {
            List<ThingBean> thingBeans = new ArrayList<ThingBean>();

            for (Thing thing : elements) {
                thingBeans.add(BeanMapper.mapThingToBean(thing));
            }

            eventObject = thingBeans;
        }

        sseResource.broadcastEvent(thingIdentifier, eventType, eventObject);
    }

}
