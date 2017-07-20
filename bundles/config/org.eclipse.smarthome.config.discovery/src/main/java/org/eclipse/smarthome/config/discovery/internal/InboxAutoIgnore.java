/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.internal;

import java.util.List;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.config.discovery.inbox.Inbox;
import org.eclipse.smarthome.config.discovery.inbox.InboxFilterCriteria;
import org.eclipse.smarthome.core.events.AbstractTypedEventSubscriber;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.events.ThingStatusInfoChangedEvent;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a service to automatically ignore {@link Inbox} entries of newly created things.
 * <p>
 * The {@link InboxAutoIgnore} service implements a {@link EventSubscriber}, that is triggered
 * for each thing when coming ONLINE. {@link Inbox} entries with the same device id like the
 * newly created thing will be automatically set to {@link DiscoveryResultFlag#IGNORED}.
 * </p>
 * <p>
 * If a thing is being removed, possibly existing {@link Inbox} entries with the same device id
 * are removed from the {@link Inbox} so they could be discovered again afterwards.
 * </p>
 * <p>
 * This service can be enabled or disabled by setting the {@code autoIgnore} property to either
 * {@code true} or {@code false} via ConfigAdmin.
 * </p>
 *
 * @author Andre Fuechsel - Initial Contribution
 */
public class InboxAutoIgnore extends AbstractTypedEventSubscriber<ThingStatusInfoChangedEvent> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ThingRegistry thingRegistry;
    private boolean autoIgnore = true;
    private Inbox inbox;

    public InboxAutoIgnore() {
        super(ThingStatusInfoChangedEvent.TYPE);
    }

    @Override
    public void receiveTypedEvent(ThingStatusInfoChangedEvent event) {
        if (autoIgnore) {
            if (ThingStatus.ONLINE.equals(event.getStatusInfo().getStatus())) {
                checkIfThingShouldBeIgnored(event);
            } else if (ThingStatus.REMOVING.equals(event.getStatusInfo().getStatus())) {
                removePossibleIgnoredResultInInbox(event);
            }
        }
    }

    private void checkIfThingShouldBeIgnored(ThingStatusInfoChangedEvent event) {
        Thing thing = thingRegistry.get(event.getThingUID());
        if (thing != null) {
            String deviceUid = thing.getHandler().getDeviceId();
            if (deviceUid != null) {
                ignoreInInbox(deviceUid);
            }
        }
    }

    private void ignoreInInbox(String deviceUid) {
        List<DiscoveryResult> results = inbox.getAll();
        for (DiscoveryResult result : results) {
            if (deviceUid.equals(result.getRepresentationPropertyValue())) {
                logger.debug("Auto-ignoring the inbox entry for the device uid {}", deviceUid);
                inbox.setFlag(result.getThingUID(), DiscoveryResultFlag.IGNORED);
            }
        }
    }

    private void removePossibleIgnoredResultInInbox(ThingStatusInfoChangedEvent event) {
        Thing thing = thingRegistry.get(event.getThingUID());
        if (thing != null) {
            String deviceUid = thing.getHandler().getDeviceId();
            if (deviceUid != null) {
                removeFromInbox(deviceUid);
            }
        }
    }

    private void removeFromInbox(String deviceUid) {
        List<DiscoveryResult> results = inbox.get(new InboxFilterCriteria(DiscoveryResultFlag.IGNORED));
        for (DiscoveryResult result : results) {
            if (deviceUid.equals(result.getRepresentationPropertyValue())) {
                logger.debug("Removing the ignored result from the inbox for the device uid {}", deviceUid);
                inbox.remove(result.getThingUID());
            }
        }
    }

    protected void activate(ComponentContext context) {
        modified(context);
    }

    protected void modified(ComponentContext context) {
        if (context != null) {
            Object value = context.getProperties().get("autoIgnore");
            autoIgnore = value == null || !value.toString().equals("false");
        }
    }

    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

    protected void setInbox(Inbox inbox) {
        this.inbox = inbox;
    }

    protected void unsetInbox(Inbox inbox) {
        this.inbox = null;
    }
}
