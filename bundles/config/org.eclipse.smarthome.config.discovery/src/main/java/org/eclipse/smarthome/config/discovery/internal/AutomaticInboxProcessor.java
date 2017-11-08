/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.internal;

import static org.eclipse.smarthome.config.discovery.inbox.InboxPredicates.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.config.discovery.inbox.Inbox;
import org.eclipse.smarthome.config.discovery.inbox.InboxListener;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.events.AbstractTypedEventSubscriber;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.events.ThingStatusInfoChangedEvent;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a service to automatically ignore {@link Inbox} entries of newly created things.
 * <p>
 * The {@link AutomaticInboxProcessor} service implements a {@link EventSubscriber}, that is triggered
 * for each thing when coming ONLINE. {@link Inbox} entries with the same representation value like the
 * newly created thing will be automatically set to {@link DiscoveryResultFlag#IGNORED}.
 * </p>
 * <p>
 * If a thing is being removed, possibly existing {@link Inbox} entries with the same representation value
 * are removed from the {@link Inbox} so they could be discovered again afterwards.
 * </p>
 * <p>
 * This service can be enabled or disabled by setting the {@code autoIgnore} property to either
 * {@code true} or {@code false} via ConfigAdmin.
 * </p>
 *
 * @author Andre Fuechsel - Initial Contribution
 * @author Kai Kreuzer - added auto-approve functionality
 */
@Component(immediate = true, configurationPid = "org.eclipse.smarthome.inbox", service = EventSubscriber.class, property = {
        "service.config.description.uri=system:inbox", "service.config.label=Inbox", "service.config.category=system",
        "service.pid=org.eclipse.smarthome.inbox" })
public class AutomaticInboxProcessor extends AbstractTypedEventSubscriber<ThingStatusInfoChangedEvent>
        implements InboxListener, RegistryChangeListener<Thing> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ThingRegistry thingRegistry;
    private ThingTypeRegistry thingTypeRegistry;
    private Inbox inbox;
    private boolean autoIgnore = true;
    private boolean autoApprove = false;

    public AutomaticInboxProcessor() {
        super(ThingStatusInfoChangedEvent.TYPE);
    }

    @Override
    public void receiveTypedEvent(ThingStatusInfoChangedEvent event) {
        if (autoIgnore) {
            Thing thing = thingRegistry.get(event.getThingUID());
            ThingStatus thingStatus = event.getStatusInfo().getStatus();
            autoIgnore(thing, thingStatus);
        }
    }

    @Override
    public void thingAdded(Inbox inbox, DiscoveryResult result) {
        if (autoIgnore) {
            String value = getRepresentationValue(result);
            if (value != null) {
                Thing thing = thingRegistry.stream()
                        .filter(t -> Objects.equals(value, getRepresentationPropertyValueForThing(t))).findFirst()
                        .orElse(null);
                if (thing != null) {
                    logger.debug("Auto-ignoring the inbox entry for the representation value {}", value);
                    inbox.setFlag(result.getThingUID(), DiscoveryResultFlag.IGNORED);
                }
            }
        }
        if (autoApprove) {
            inbox.approve(result.getThingUID(), result.getLabel());
        }
    }

    @Override
    public void thingUpdated(Inbox inbox, DiscoveryResult result) {
    }

    @Override
    public void thingRemoved(Inbox inbox, DiscoveryResult result) {
    }

    @Override
    public void added(Thing element) {
    }

    @Override
    public void removed(Thing element) {
        removePossiblyIgnoredResultInInbox(element);
    }

    @Override
    public void updated(Thing oldElement, Thing element) {
    }

    private String getRepresentationValue(DiscoveryResult result) {
        return result.getRepresentationProperty() != null
                ? Objects.toString(result.getProperties().get(result.getRepresentationProperty()), null)
                : null;
    }

    private void autoIgnore(Thing thing, ThingStatus thingStatus) {
        if (ThingStatus.ONLINE.equals(thingStatus)) {
            checkAndIgnoreInInbox(thing);
        }
    }

    private void checkAndIgnoreInInbox(Thing thing) {
        if (thing != null) {
            String representationValue = getRepresentationPropertyValueForThing(thing);
            if (representationValue != null) {
                ignoreInInbox(representationValue);
            }
        }
    }

    private void ignoreInInbox(String representationValue) {
        List<DiscoveryResult> results = inbox.stream().filter(withRepresentationPropertyValue(representationValue))
                .collect(Collectors.toList());
        if (results.size() == 1) {
            logger.debug("Auto-ignoring the inbox entry for the representation value {}", representationValue);
            inbox.setFlag(results.get(0).getThingUID(), DiscoveryResultFlag.IGNORED);
        }
    }

    private void removePossiblyIgnoredResultInInbox(Thing thing) {
        if (thing != null) {
            String representationValue = getRepresentationPropertyValueForThing(thing);
            if (representationValue != null) {
                removeFromInbox(representationValue);
            }
        }
    }

    private String getRepresentationPropertyValueForThing(Thing thing) {
        ThingType thingType = thingTypeRegistry.getThingType(thing.getThingTypeUID());
        if (thingType != null) {
            String representationProperty = thingType.getRepresentationProperty();
            if (representationProperty == null) {
                return null;
            }
            Map<String, String> properties = thing.getProperties();
            if (properties.containsKey(representationProperty)) {
                return properties.get(representationProperty);
            }
            Configuration configuration = thing.getConfiguration();
            if (configuration.containsKey(representationProperty)) {
                return String.valueOf(configuration.get(representationProperty));
            }
        }
        return null;
    }

    private void removeFromInbox(String representationValue) {
        List<DiscoveryResult> results = inbox.stream().filter(withRepresentationPropertyValue(representationValue))
                .filter(withFlag(DiscoveryResultFlag.IGNORED)).collect(Collectors.toList());
        if (results.size() == 1) {
            logger.debug("Removing the ignored result from the inbox for the representation value {}",
                    representationValue);
            inbox.remove(results.get(0).getThingUID());
        }
    }

    private void approveAllInboxEntries() {
        for (DiscoveryResult result : inbox.getAll()) {
            if (result.getFlag().equals(DiscoveryResultFlag.NEW)) {
                inbox.approve(result.getThingUID(), result.getLabel());
            }
        }
    }

    protected void activate(Map<String, Object> properties) {
        if (properties != null) {
            Object value = properties.get("autoIgnore");
            autoIgnore = value == null || !value.toString().equals("false");
            value = properties.get("autoApprove");
            autoApprove = value != null && value.toString().equals("true");
            if (autoApprove) {
                approveAllInboxEntries();
            }
        }
    }

    @Reference
    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
        thingRegistry.addRegistryChangeListener(this);
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        thingRegistry.removeRegistryChangeListener(this);
        this.thingRegistry = null;
    }

    @Reference
    protected void setThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = thingTypeRegistry;
    }

    protected void unsetThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = null;
    }

    @Reference
    protected void setInbox(Inbox inbox) {
        this.inbox = inbox;
        inbox.addInboxListener(this);
    }

    protected void unsetInbox(Inbox inbox) {
        inbox.removeInboxListener(this);
        this.inbox = null;
    }
}
