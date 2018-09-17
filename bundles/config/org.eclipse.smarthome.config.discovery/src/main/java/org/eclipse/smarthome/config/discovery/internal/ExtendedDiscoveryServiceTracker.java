/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.config.discovery.internal;

import static org.eclipse.smarthome.config.discovery.inbox.InboxPredicates.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.config.discovery.inbox.Inbox;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ExtendedDiscoveryServiceTracker} adds the {@link DiscoveryServiceCallback} to all
 * {@link ExtendedDiscoveryService} instances.
 *
 * @author Henning Treu - initial contribution
 *
 */
@NonNullByDefault
@Component(immediate = true)
public class ExtendedDiscoveryServiceTracker {

    private @Nullable Inbox inbox;
    private @Nullable ThingRegistry thingRegistry;
    private volatile @Nullable DiscoveryServiceCallbackImpl discoveryServiceCallback;

    private final List<ExtendedDiscoveryService> extendedDiscoveryServices = new ArrayList<>();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addDiscoveryService(DiscoveryService discoveryService) {
        if (discoveryService instanceof ExtendedDiscoveryService) {
            ExtendedDiscoveryService extendedDiscoveryService = (ExtendedDiscoveryService) discoveryService;
            extendedDiscoveryServices.add(extendedDiscoveryService);

            DiscoveryServiceCallback dsc = discoveryServiceCallback;
            if (dsc != null) {
                extendedDiscoveryService.setDiscoveryServiceCallback(dsc);
            }
        }
    }

    protected void removeDiscoveryService(DiscoveryService discoveryService) {
        if (discoveryService instanceof ExtendedDiscoveryService) {
            ExtendedDiscoveryService extendedDiscoveryService = (ExtendedDiscoveryService) discoveryService;
            extendedDiscoveryServices.remove(extendedDiscoveryService);
        }
    }

    @Reference
    protected void setInbox(Inbox inbox) {
        this.inbox = inbox;
    }

    protected void unsetInbox(Inbox inbox) {
        this.inbox = null;
    }

    @Reference
    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

    @Activate
    protected void activate() {
        DiscoveryServiceCallbackImpl dsc = createDiscoveryServiceCallback();
        this.discoveryServiceCallback = dsc;

        for (ExtendedDiscoveryService extendedDiscoveryService : extendedDiscoveryServices) {
            extendedDiscoveryService.setDiscoveryServiceCallback(dsc);
        }

    }

    @Deactivate
    protected void deactivate() {
        extendedDiscoveryServices.clear();
        discoveryServiceCallback.inbox = null;
        discoveryServiceCallback.thingRegistry = null;
        discoveryServiceCallback = null;
    }

    private DiscoveryServiceCallbackImpl createDiscoveryServiceCallback() {
        Inbox localInbox = inbox;
        ThingRegistry localThingRegistry = thingRegistry;

        if (localInbox != null && localThingRegistry != null) {
            return new DiscoveryServiceCallbackImpl(localInbox, localThingRegistry);
        }

        throw new IllegalStateException(String.format("Lifecylcle error creating instance of DiscoveryServiceCallback. "
                + "Required dependency inbox is '%s', thingRegistry is '%s'", inbox, thingRegistry));
    }

    private class DiscoveryServiceCallbackImpl implements DiscoveryServiceCallback {

        private final Logger logger = LoggerFactory.getLogger(DiscoveryServiceCallbackImpl.class);

        private @Nullable Inbox inbox;
        private @Nullable ThingRegistry thingRegistry;

        public DiscoveryServiceCallbackImpl(Inbox inbox, ThingRegistry thingRegistry) {
            this.inbox = inbox;
            this.thingRegistry = thingRegistry;
        }

        @Override
        public @Nullable Thing getExistingThing(ThingUID thingUID) {
            ThingRegistry localThingRegistry = thingRegistry;
            if (localThingRegistry == null) {
                logger.error("Lifecycle error accessing thingRegistry. The DiscoveryServiceCallback is unusable.");
                return null;
            }

            return localThingRegistry.get(thingUID);
        }

        @Override
        public @Nullable DiscoveryResult getExistingDiscoveryResult(ThingUID thingUID) {
            Inbox localInbox = inbox;
            if (localInbox == null) {
                logger.error("Lifecycle error accessing inbox. The DiscoveryServiceCallback is unusable.");
                return null;
            }
            List<DiscoveryResult> ret = new ArrayList<>();
            ret = localInbox.stream().filter(withFlag(DiscoveryResultFlag.NEW).and(forThingUID(thingUID)))
                    .collect(Collectors.toList());
            if (ret.size() > 0) {
                return ret.get(0);
            } else {
                return null;
            }
        }
    }

}
