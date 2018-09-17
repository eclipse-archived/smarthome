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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.config.discovery.inbox.Inbox;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

/**
 * Test the {@link ExtendedDiscoveryServiceTracker}.
 *
 * @author Henning Treu - initial contribution
 *
 */
public class ExtendedDiscoveryServiceTrackerTest {

    private ExtendedDiscoveryServiceTracker discoveryServiceTracker;

    @Mock
    private Inbox inbox;

    @Mock
    private ThingRegistry thingRegistry;

    @Before
    public void setup() {
        initMocks(this);
        discoveryServiceTracker = new ExtendedDiscoveryServiceTracker();
        discoveryServiceTracker.setInbox(inbox);
        discoveryServiceTracker.setThingRegistry(thingRegistry);
    }

    @Test
    public void testDeactivatedTracker() {
        MockDiscoveryService discoveryService = mock(MockDiscoveryService.class);

        discoveryServiceTracker.addDiscoveryService(discoveryService);

        verifyZeroInteractions(discoveryService);
    }

    @Test
    public void testActivatedTracker_addDiscoveryServiceBefore() {
        MockDiscoveryService discoveryService = mock(MockDiscoveryService.class);
        discoveryServiceTracker.addDiscoveryService(discoveryService);

        discoveryServiceTracker.activate();

        verify(discoveryService).setDiscoveryServiceCallback(any(DiscoveryServiceCallback.class));
    }

    @Test
    public void testActivatedTracker_addDiscoveryServiceLater() {
        discoveryServiceTracker.activate();

        MockDiscoveryService discoveryService = mock(MockDiscoveryService.class);
        discoveryServiceTracker.addDiscoveryService(discoveryService);

        verify(discoveryService).setDiscoveryServiceCallback(any(DiscoveryServiceCallback.class));
    }

    @Test
    public void testDiscoveryServiceCallback_getExistingThing() {
        MockDiscoveryService discoveryService = mock(MockDiscoveryService.class);

        discoveryServiceTracker.activate();
        discoveryServiceTracker.addDiscoveryService(discoveryService);

        ArgumentCaptor<DiscoveryServiceCallback> callbackCaptor = ArgumentCaptor
                .forClass(DiscoveryServiceCallback.class);

        verify(discoveryService).setDiscoveryServiceCallback(callbackCaptor.capture());

        ThingUID thingUID = new ThingUID("binding:thing-type:thingId");
        callbackCaptor.getValue().getExistingThing(thingUID);

        verify(thingRegistry).get(thingUID);
    }

    @Test
    public void testDiscoveryServiceCallback_getExistingDiscoveryResult() {
        MockDiscoveryService discoveryService = mock(MockDiscoveryService.class);

        discoveryServiceTracker.activate();
        discoveryServiceTracker.addDiscoveryService(discoveryService);

        ArgumentCaptor<DiscoveryServiceCallback> callbackCaptor = ArgumentCaptor
                .forClass(DiscoveryServiceCallback.class);

        verify(discoveryService).setDiscoveryServiceCallback(callbackCaptor.capture());

        ThingUID thingUID = new ThingUID("binding:thing-type:thingId");
        callbackCaptor.getValue().getExistingDiscoveryResult(thingUID);

        verify(inbox).stream();
    }

    /**
     * This mock interface circumvents the fact that ExtendedDiscoveryService does not extend DiscoveryService.
     *
     * @author Henning Treu
     *
     */
    private interface MockDiscoveryService extends DiscoveryService, ExtendedDiscoveryService {
        // Just combine both super interface declarations.
    }

}
