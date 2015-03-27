/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.setup.test.discovery

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import java.util.Collection;

import org.eclipse.smarthome.config.discovery.DiscoveryListener
import org.eclipse.smarthome.config.discovery.DiscoveryResult
import org.eclipse.smarthome.config.discovery.DiscoveryService
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry
import org.eclipse.smarthome.config.discovery.ScanListener
import org.eclipse.smarthome.config.discovery.inbox.Inbox
import org.eclipse.smarthome.config.discovery.internal.DiscoveryServiceRegistryImpl
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.test.AsyncResultWrapper
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.framework.ServiceRegistration


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

    def ANY_BINDING_ID_1 = 'anyBindingId1'
    def ANY_THING_TYPE_1 = 'anyThingType1'

    def ANY_BINDING_ID_2 = 'anyBindingId2'
    def ANY_THING_TYPE_2 = 'anyThingType2'

    def FAULTY_BINDING_ID = 'faultyBindingId'
    def FAULTY_THING_TYPE = 'faultyThingType'

    DiscoveryService discoveryServiceMockForBinding1
    DiscoveryService discoveryServiceMockForBinding2
    DiscoveryService discoveryServiceFaultyMock
    DiscoveryServiceRegistry discoveryServiceRegistry
    List<ServiceRegistration<?>> serviceRegs = []

    @Before
    void setUp() {
        registerVolatileStorageService()

        discoveryServiceMockForBinding1 = new DiscoveryServiceMock(
                new ThingTypeUID(ANY_BINDING_ID_1,ANY_THING_TYPE_1), 1)
        discoveryServiceMockForBinding2 = new DiscoveryServiceMock(
                new ThingTypeUID(ANY_BINDING_ID_2,ANY_THING_TYPE_2), 1)

        discoveryServiceFaultyMock = new DiscoveryServiceMock(
                new ThingTypeUID(FAULTY_BINDING_ID, FAULTY_THING_TYPE), 1, true)

        serviceRegs.add(registerService(discoveryServiceMockForBinding1, DiscoveryService.class.name))
        serviceRegs.add(registerService(discoveryServiceMockForBinding2, DiscoveryService.class.name))
        serviceRegs.add(registerService(discoveryServiceFaultyMock, DiscoveryService.class.name))

        discoveryServiceRegistry = getService(DiscoveryServiceRegistry)
    }

    @After
    void cleanUp() {
        discoveryServiceFaultyMock.abortScan()
        discoveryServiceMockForBinding1.abortScan()
        discoveryServiceMockForBinding2.abortScan()

        serviceRegs.each {
            try {
                it.unregister()
            } catch(Exception ex) {
            }
        }

        ((DiscoveryServiceRegistryImpl)discoveryServiceRegistry).listeners.each { discoveryServiceRegistry.removeDiscoveryListener(it) }

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

        state = discoveryServiceRegistry.startScan(new ThingTypeUID(ANY_BINDING_ID_1,ANY_THING_TYPE_1), [onFinished: {}, onErrorOccurred: {}] as ScanListener)
        assertTrue(state)
    }

    @Test
    void 'assert that a faulty known DiscoveryService cannot be forced for a discovery' () {
        boolean state

        state = discoveryServiceRegistry.startScan(new ThingTypeUID(FAULTY_BINDING_ID,FAULTY_THING_TYPE), [] as ScanListener)
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
        state = discoveryServiceRegistry.startScan(new ThingTypeUID(ANY_BINDING_ID_1,ANY_THING_TYPE_1), [
            onErrorOccurred: {onErrorOccuredCalled = true}
        ] as ScanListener)
        assertTrue(state)

        state = discoveryServiceRegistry.abortScan(new ThingTypeUID(ANY_BINDING_ID_1,ANY_THING_TYPE_1))
        assertTrue(state)

        waitForAssert { assertTrue onErrorOccuredCalled }
    }

    @Test
    void 'assert that an added listener is notified about DiscoveryResults' () {
        AsyncResultWrapper<Boolean> scanListenerResult = new AsyncResultWrapper<Boolean>()
        AsyncResultWrapper<DiscoveryResult> discoveryListenerResult = new AsyncResultWrapper<Boolean>()
        DiscoveryListener discoveryListenerMock = [
            thingDiscovered: { DiscoveryService source, DiscoveryResult result ->
                discoveryListenerResult.set result
            }
        ] as DiscoveryListener

        discoveryServiceRegistry.addDiscoveryListener(discoveryListenerMock);
        discoveryServiceRegistry.startScan(new ThingTypeUID(ANY_BINDING_ID_1, ANY_THING_TYPE_1), [
            onFinished: {scanListenerResult.set(Void.TYPE)},
            onErrorOccurred: {
            }
        ] as ScanListener)

        waitForAssert({ assertTrue scanListenerResult.isSet }, 2000)
        assertTrue discoveryListenerResult.isSet
    }

    @Test
    void 'assert that an listener is notified about removeOlderResults' () {
        boolean removeOlderResultsCalled = false;
        DiscoveryListener discoveryListenerMock = [
            removeOlderResults: { DiscoveryService src, long timestamp, Collection<ThingTypeUID> thingTypeIds ->
                removeOlderResultsCalled = true
            }
        ] as DiscoveryListener

        discoveryServiceRegistry.addDiscoveryListener(discoveryListenerMock);
        discoveryServiceMockForBinding1.removeOlderResults(discoveryServiceMockForBinding1.getTimestampOfLastScan());

        assertTrue removeOlderResultsCalled
    }

    @Test
    void 'assert that removeOlderResults works as expected' () {
        AsyncResultWrapper<Boolean> scanListenerResult1 = new AsyncResultWrapper<Boolean>()
        AsyncResultWrapper<Boolean> scanListenerResult2 = new AsyncResultWrapper<Boolean>()
        AsyncResultWrapper<DiscoveryResult> discoveryListenerResult = new AsyncResultWrapper<Boolean>()
        DiscoveryListener discoveryListenerMock = [
            thingDiscovered: { DiscoveryService source, DiscoveryResult result ->
                discoveryListenerResult.set result
            },
            removeOlderThings: { DiscoveryService source, long timestamp, Collection<ThingTypeUID> thingTypeUIDs ->
                []
            }
        ] as DiscoveryListener

        discoveryServiceRegistry.addDiscoveryListener(discoveryListenerMock);
        discoveryServiceRegistry.startScan(new ThingTypeUID(ANY_BINDING_ID_1, ANY_THING_TYPE_1), [
            onFinished: {
                scanListenerResult1.set(Void.TYPE)
            },
            onErrorOccurred: {
            }
        ] as ScanListener)
        discoveryServiceRegistry.startScan(new ThingTypeUID(ANY_BINDING_ID_2, ANY_THING_TYPE_2), [
            onFinished: {
                scanListenerResult2.set(Void.TYPE)
            },
            onErrorOccurred: {
            }
        ] as ScanListener)

        waitForAssert({ assertTrue scanListenerResult1.isSet }, 2000)
        waitForAssert({ assertTrue scanListenerResult2.isSet }, 2000)
        assertTrue discoveryListenerResult.isSet

        Inbox inbox = getService(Inbox)
        assertThat inbox.getAll().size(), is(2)
        // should not remove anything
        discoveryServiceMockForBinding1.removeOlderResults(discoveryServiceMockForBinding1.getTimestampOfLastScan())
        assertThat inbox.getAll().size(), is(2)

        // start discovery again
        discoveryServiceRegistry.startScan(new ThingTypeUID(ANY_BINDING_ID_1, ANY_THING_TYPE_1), [
            onFinished: {scanListenerResult1.set(Void.TYPE)},
            onErrorOccurred: {
            }
        ] as ScanListener)
        waitForAssert({ assertTrue scanListenerResult1.isSet }, 2000)
        assertTrue discoveryListenerResult.isSet

        assertThat inbox.getAll().size(), is(3)
        // should remove one entry
        discoveryServiceMockForBinding1.removeOlderResults(discoveryServiceMockForBinding1.getTimestampOfLastScan())
        assertThat inbox.getAll().size(), is(2)
    }

    @Test
    void 'assert that a removed listener is not notified about DiscoveryResults anymore' () {
        AsyncResultWrapper<DiscoveryResult> discoveryResult = new AsyncResultWrapper<Boolean>()
        boolean discoveryFinished = false;

        DiscoveryListener discoveryListenerMock = [
            thingDiscovered: { DiscoveryService source, DiscoveryResult result ->
                discoveryResult.set result
            }
        ] as DiscoveryListener

        discoveryServiceRegistry.addDiscoveryListener(discoveryListenerMock);
        discoveryServiceRegistry.removeDiscoveryListener(discoveryListenerMock);
        discoveryServiceRegistry.startScan(new ThingTypeUID(ANY_BINDING_ID_1, ANY_THING_TYPE_1),  [
            onFinished: { discoveryFinished = true },
            onErrorOccurred: {
            }
        ] as ScanListener)

        waitForAssert ({ assertTrue(discoveryFinished) }, 2000 )
        assertFalse discoveryResult.isSet
    }

    @Test
    void 'assert that two discovery services are started' () {
        def anotherDiscoveryServiceMock = new DiscoveryServiceMock(
                new ThingTypeUID(ANY_BINDING_ID_1,ANY_THING_TYPE_1), 1)

        serviceRegs.add(registerService(anotherDiscoveryServiceMock, DiscoveryService.class.name))

        def numberOfDiscoveredThings = 0;
        AsyncResultWrapper<Boolean> listenerResult = new AsyncResultWrapper<Boolean>()

        DiscoveryListener discoveryListenerMock = [
            thingDiscovered: { DiscoveryService source, DiscoveryResult result -> numberOfDiscoveredThings++}
        ] as DiscoveryListener

        discoveryServiceRegistry.addDiscoveryListener(discoveryListenerMock);

        discoveryServiceRegistry.startScan(new ThingTypeUID(ANY_BINDING_ID_1, ANY_THING_TYPE_1),  [
            onFinished: {listenerResult.set(true)},
            onErrorOccurred: {
            }
        ] as ScanListener)

        waitForAssert ({ assertTrue(listenerResult.isSet) }, 2000 )
        assertEquals 2, numberOfDiscoveredThings
    }

    @Test
    void 'assert start discovery for binding id works' () {

        AsyncResultWrapper<Boolean> listenerResult = new AsyncResultWrapper<Boolean>()

        discoveryServiceRegistry.startScan(ANY_BINDING_ID_1,  [
            onFinished: {listenerResult.set(true)},
            onErrorOccurred: {
            }
        ] as ScanListener)

        waitForAssert ({ assertTrue(listenerResult.isSet) }, 2000 )
    }

    @Test
    void 'assert supportsDiscovery works' () {

        assertTrue discoveryServiceRegistry.supportsDiscovery(new ThingTypeUID(ANY_BINDING_ID_1, ANY_THING_TYPE_1))
        assertFalse discoveryServiceRegistry.supportsDiscovery(new ThingTypeUID(ANY_BINDING_ID_1, 'unknownType'))

        assertTrue discoveryServiceRegistry.supportsDiscovery(ANY_BINDING_ID_1)
        assertFalse discoveryServiceRegistry.supportsDiscovery('unknownBindingId')
    }
}
