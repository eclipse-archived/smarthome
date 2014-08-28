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
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry
import org.eclipse.smarthome.config.discovery.ScanListener
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
                new ThingTypeUID('anyBindingId','anyThingType'), 1)

        discoveryServiceFaultyMock = new DiscoveryServiceMock(
                new ThingTypeUID('faultyBindingId', 'faultyThingType'), 1, true)

        registerService(discoveryServiceMock, DiscoveryService.class.name)
        registerService(discoveryServiceFaultyMock, DiscoveryService.class.name)

        discoveryServiceRegistry = getService(DiscoveryServiceRegistry)
    }

    @After
    void cleanUp() {
        discoveryServiceFaultyMock.abortScan()
        discoveryServiceMock.abortScan()
        unregisterService(discoveryServiceFaultyMock)
        unregisterService(discoveryServiceMock)

        Inbox inbox = getService(Inbox)

        DiscoveryResult[] discoveryResults = inbox.getAll()
        discoveryResults.each { inbox.remove(it.getThingUID()) }
    }

    @Test
    void 'assert that an not existing DiscoveryService can not be forced for a discovery' () {
        boolean state

        state = discoveryServiceRegistry.startScan(new ThingTypeUID('bindingId','thingType'), [] as ScanListener)
        assertFalse(state)
    }

    @Test
    void 'assert that a known DiscoveryService can be forced for a discovery' () {
        boolean state

        state = discoveryServiceRegistry.startScan(new ThingTypeUID('anyBindingId','anyThingType'), [onFinished: {}, onErrorOccurred: {}] as ScanListener)
        assertTrue(state)
    }

    @Test
    void 'assert that a faulty known DiscoveryService cannot be forced for a discovery' () {
        boolean state

        state = discoveryServiceRegistry.startScan(new ThingTypeUID('faultyBindingId','faultyThingType'), [] as ScanListener)
        assertFalse(state)
    }

    @Test
    void 'assert that a discovery cannot be aborted for a not existing DiscoveryService' () {
        boolean state

        state = discoveryServiceRegistry.abortScan(new ThingTypeUID('bindingId','thingType'))
        assertFalse(state)
    }

    @Test
    void 'assert that a discovery can be aborted for a known DiscoveryService' () {
        def onErrorOccuredCalled = false
        
        boolean state
        state = discoveryServiceRegistry.startScan(new ThingTypeUID('anyBindingId','anyThingType'), [
            onErrorOccurred: {a,b -> onErrorOccuredCalled = true}
        ] as ScanListener)
        assertTrue(state)
        
        state = discoveryServiceRegistry.abortScan(new ThingTypeUID('anyBindingId','anyThingType'))
        assertTrue(state)
        
        waitForAssert { assertTrue onErrorOccuredCalled }
    }

    @Test
    void 'assert that an added listener is notified about DiscoveryResults' () {
        AsyncResultWrapper<Boolean> scanListenerResult = new AsyncResultWrapper<Boolean>()
        AsyncResultWrapper<DiscoveryResult> discoveryListenerResult = new AsyncResultWrapper<Boolean>()
        DiscoveryListener discoveryListenerMock = [
            thingDiscovered: { DiscoveryService source, DiscoveryResult result -> discoveryListenerResult.set result}            
        ] as DiscoveryListener

        discoveryServiceRegistry.addDiscoveryListener(discoveryListenerMock);
        discoveryServiceRegistry.startScan(new ThingTypeUID('anyBindingId', 'anyThingType'), [
            onFinished: {scanListenerResult.set(Void.TYPE)},
            onErrorOccurred: {a,b ->}
        ] as ScanListener)

        waitForAssert({ assertTrue scanListenerResult.isSet }, 2000)
        assertTrue discoveryListenerResult.isSet
    }

    @Test
    void 'assert that a removed listener is not notified about DiscoveryResults anymore' () {
        AsyncResultWrapper<Boolean> listenerResult = new AsyncResultWrapper<Boolean>()

        DiscoveryListener discoveryListenerMock = [
            thingDiscovered: { DiscoveryService source, DiscoveryResult result -> }
        ] as DiscoveryListener

        discoveryServiceRegistry.addDiscoveryListener(discoveryListenerMock);
        discoveryServiceRegistry.removeDiscoveryListener(discoveryListenerMock);
        discoveryServiceRegistry.startScan(new ThingTypeUID('anyBindingId', 'anyThingType'),  [
            onFinished: {listenerResult.isSet}     
        ] as ScanListener)

        assertFalse(listenerResult.isSet)
    }

}
