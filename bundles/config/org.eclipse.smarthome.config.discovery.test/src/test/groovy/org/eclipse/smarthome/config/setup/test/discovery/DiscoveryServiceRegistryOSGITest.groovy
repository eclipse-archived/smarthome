/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.setup.test.discovery

import static org.junit.Assert.*

import org.eclipse.smarthome.config.discovery.DiscoveryListener
import org.eclipse.smarthome.config.discovery.DiscoveryResult
import org.eclipse.smarthome.config.discovery.DiscoveryService
import org.eclipse.smarthome.config.discovery.DiscoveryServiceInfo
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry
import org.eclipse.smarthome.config.discovery.inbox.Inbox
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.test.AsyncResultWrapper
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test


/**
 * The {@link DiscoveryServiceRegistryOSGITest} test checks if the concrete
 * {@link DiscoveryServiceRegistry} implementation tracks all registered
 * {@link DiscoveryService}s and if a registered listener is notified
 * about the events fired by the according{@link DiscoveryService}.
 * <p>
 * This implementation creates two {@link DiscoveryService} mocks and registers
 * them as service at the <i>OSGi</i> service registry. Since this test creates
 * {@link DiscoveryResult}s which are added to the {@link Inbox},
 * the {@link Inbox} is cleared again after this test returns. 
 * 
 * @author Michael Grammling - Initial Contribution
 */
class DiscoveryServiceRegistryOSGITest extends OSGiTest {

    DiscoveryService discoveryServiceMock
    DiscoveryService discoveryServiceFaultyMock

    DiscoveryServiceRegistry discoveryServiceRegistry


    @Before
    void setup() {
        registerVolatileStorageService()

        discoveryServiceMock = new DiscoveryServiceMock(
                new ThingTypeUID('anyBindingId','anyThingType'), 10)

        discoveryServiceFaultyMock = new DiscoveryServiceMock(
                new ThingTypeUID('faultyBindingId', 'faultyThingType'), 5, true)

        registerService(discoveryServiceMock, DiscoveryService.class.name)
        registerService(discoveryServiceFaultyMock, DiscoveryService.class.name)

        discoveryServiceRegistry = getService(DiscoveryServiceRegistry)
    }

    @After
    void cleanUp() {
        unregisterService(discoveryServiceFaultyMock)
        unregisterService(discoveryServiceMock)

        Inbox inbox = getService(Inbox)

        DiscoveryResult[] discoveryResults = inbox.getAll()
        discoveryResults.each { inbox.remove(it.getThingUID()) }
    }

    @Test
    void 'assert that a not existing DiscoveryService is not found' () {
        DiscoveryServiceInfo info

        info = discoveryServiceRegistry.getDiscoveryInfo(null)
        assertNull(info)

        info = discoveryServiceRegistry.getDiscoveryInfo(new ThingTypeUID('bindingId','thingType'))
        assertNull(info)
    }

    @Test
    void 'assert that a known DiscoveryService is found' () {
        DiscoveryServiceInfo info

        info = discoveryServiceRegistry.getDiscoveryInfo(new ThingTypeUID('anyBindingId','anyThingType'))
        assertNotNull(info)

        info = discoveryServiceRegistry.getDiscoveryInfo(new ThingTypeUID('faultyBindingId','faultyThingType'))
        assertNotNull(info)
    }

    @Test
    void 'assert that an not existing DiscoveryService can not be forced for a discovery' () {
        boolean state

        state = discoveryServiceRegistry.forceDiscovery(new ThingTypeUID('bindingId','thingType'))
        assertFalse(state)
    }

    @Test
    void 'assert that a known DiscoveryService can be forced for a discovery' () {
        boolean state

        state = discoveryServiceRegistry.forceDiscovery(new ThingTypeUID('anyBindingId','anyThingType'))
        assertTrue(state)
    }

    @Test
    void 'assert that a faulty known DiscoveryService cannot be forced for a discovery' () {
        boolean state

        state = discoveryServiceRegistry.forceDiscovery(new ThingTypeUID('faultyBindingId','faultyThingType'))
        assertFalse(state)
    }

    @Test
    void 'assert that a discovery cannot be aborted for a not existing DiscoveryService' () {
        boolean state

        state = discoveryServiceRegistry.abortForcedDiscovery(new ThingTypeUID('bindingId','thingType'))
        assertFalse(state)
    }

    @Test
    void 'assert that a discovery can be aborted for a known DiscoveryService' () {
        boolean state

        state = discoveryServiceRegistry.abortForcedDiscovery(new ThingTypeUID('anyBindingId','anyThingType'))
        assertTrue(state)
    }

    @Test
    void 'assert that a discovery cannot be aborted for a faulty known DiscoveryService' () {
        boolean state

        state = discoveryServiceRegistry.abortForcedDiscovery(new ThingTypeUID('faultyBindingId','faultyThingType'))
        assertFalse(state)
    }

    @Test
    void 'assert that an added listener is notified about DiscoveryResults' () {
        AsyncResultWrapper<Boolean> listenerResult = new AsyncResultWrapper<Boolean>()

        DiscoveryListener discoveryListenerMock = [
            thingDiscovered: { DiscoveryService source, DiscoveryResult result -> },
            discoveryFinished: { DiscoveryService source ->
                listenerResult.set(true)
            }
        ] as DiscoveryListener

        discoveryServiceRegistry.addDiscoveryListener(discoveryListenerMock);
        discoveryServiceRegistry.forceDiscovery(new ThingTypeUID('anyBindingId', 'anyThingType'))

        assertTrue(listenerResult.isSet)
    }

    @Test
    void 'assert that a removed listener is not notified about DiscoveryResults anymore' () {
        AsyncResultWrapper<Boolean> listenerResult = new AsyncResultWrapper<Boolean>()

        DiscoveryListener discoveryListenerMock = [
            thingDiscovered: { DiscoveryService source, DiscoveryResult result -> },
            discoveryFinished: { DiscoveryService source ->
                listenerResult.set(true)
            }
        ] as DiscoveryListener

        discoveryServiceRegistry.addDiscoveryListener(discoveryListenerMock);
        discoveryServiceRegistry.removeDiscoveryListener(discoveryListenerMock);
        discoveryServiceRegistry.forceDiscovery(new ThingTypeUID('anyBindingId', 'anyThingType'))

        assertFalse(listenerResult.isSet)
    }

}
