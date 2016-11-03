/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.setup.test.discovery

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.discovery.DiscoveryListener
import org.eclipse.smarthome.config.discovery.DiscoveryResult
import org.eclipse.smarthome.config.discovery.DiscoveryService
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry
import org.eclipse.smarthome.config.discovery.ScanListener
import org.eclipse.smarthome.config.discovery.inbox.Inbox
import org.eclipse.smarthome.config.discovery.internal.DiscoveryServiceRegistryImpl
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
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
 * @author Simon Kaufmann - added tests for ExtendedDiscoveryService
 */
class DiscoveryServiceRegistryOSGITest extends OSGiTest {

    def ANY_BINDING_ID_1 = 'anyBindingId1'
    def ANY_THING_TYPE_1 = 'anyThingType1'

    def ANY_BINDING_ID_2 = 'anyBindingId2'
    def ANY_THING_TYPE_2 = 'anyThingType2'

    def FAULTY_BINDING_ID = 'faultyBindingId'
    def FAULTY_THING_TYPE = 'faultyThingType'

    def EXTENDED_BINDING_ID = 'extendedBindingId'
    def EXTENDED_THING_TYPE = 'extendedThingType'

    class AnotherDiscoveryService extends DiscoveryServiceMock {
        public AnotherDiscoveryService(Object thingType, int timeout) {
            super(thingType, timeout);
        }
    }

    DiscoveryService discoveryServiceMockForBinding1
    DiscoveryService discoveryServiceMockForBinding2
    DiscoveryService discoveryServiceFaultyMock
    ExtendedDiscoveryServiceMock extendedDiscoveryServiceMock
    DiscoveryServiceRegistry discoveryServiceRegistry
    List<ServiceRegistration<?>> serviceRegs = []
    ThingRegistry thingRegistry
    Inbox inbox

    @Before
    void setUp() {
        registerVolatileStorageService()

        thingRegistry = getService(ThingRegistry)
        assertNotNull thingRegistry

        inbox = getService(Inbox)
        assertNotNull(inbox)

        discoveryServiceMockForBinding1 = new DiscoveryServiceMock(
                new ThingTypeUID(ANY_BINDING_ID_1,ANY_THING_TYPE_1), 1)
        discoveryServiceMockForBinding2 = new ExtendedDiscoveryServiceMock(
                new ThingTypeUID(ANY_BINDING_ID_2,ANY_THING_TYPE_2), 3)

        discoveryServiceFaultyMock = new DiscoveryServiceMock(
                new ThingTypeUID(FAULTY_BINDING_ID, FAULTY_THING_TYPE), 1, true)

        extendedDiscoveryServiceMock = new ExtendedDiscoveryServiceMock(
                new ThingTypeUID(EXTENDED_BINDING_ID, EXTENDED_THING_TYPE), 1, true)

        serviceRegs.add(registerService(discoveryServiceMockForBinding1, DiscoveryService.class.name))
        serviceRegs.add(registerService(discoveryServiceMockForBinding2, DiscoveryService.class.name))
        serviceRegs.add(registerService(discoveryServiceFaultyMock, DiscoveryService.class.name))
        serviceRegs.add(registerService(extendedDiscoveryServiceMock, DiscoveryService.class.name))

        discoveryServiceRegistry = getService(DiscoveryServiceRegistry)
    }

    @After
    void cleanUp() {
        extendedDiscoveryServiceMock.abortScan()
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
    void 'assert that removeOlderResults removes only the entries of the same discovery service' () {
        AsyncResultWrapper<Boolean> scanListenerResult1 = new AsyncResultWrapper<Boolean>()
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

        waitForAssert({ assertTrue scanListenerResult1.isSet }, 2000)
        assertTrue discoveryListenerResult.isSet

        Inbox inbox = getService(Inbox)
        assertThat inbox.getAll().size(), is(1)

        // register another discovery service for the same thing type
        AnotherDiscoveryService anotherDiscoveryServiceMockForBinding1 = new AnotherDiscoveryService(
                new ThingTypeUID(ANY_BINDING_ID_1,ANY_THING_TYPE_1), 1)
        serviceRegs.add(registerService(anotherDiscoveryServiceMockForBinding1, DiscoveryService.class.name))

        // start discovery again
        discoveryServiceRegistry.startScan(new ThingTypeUID(ANY_BINDING_ID_1, ANY_THING_TYPE_1), [
            onFinished: {scanListenerResult1.set(Void.TYPE)},
            onErrorOccurred: {
            }
        ] as ScanListener)
        waitForAssert({ assertTrue scanListenerResult1.isSet }, 2000)
        assertTrue discoveryListenerResult.isSet

        assertThat inbox.getAll().size(), is(3)

        // should remove no entry, as there is no older entry discovery of this specific discovery service
        anotherDiscoveryServiceMockForBinding1.removeOlderResults(anotherDiscoveryServiceMockForBinding1.getTimestampOfLastScan())
        assertThat inbox.getAll().size(), is(3)

        // should remove only one entry
        discoveryServiceMockForBinding1.removeOlderResults(discoveryServiceMockForBinding1.getTimestampOfLastScan())
        assertThat inbox.getAll().size(), is(2)

        anotherDiscoveryServiceMockForBinding1.abortScan()
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

    @Test
    void 'assert getMaxScanTimeout works' () {
        assertEquals 1, discoveryServiceRegistry.getMaxScanTimeout(new ThingTypeUID(ANY_BINDING_ID_1, ANY_THING_TYPE_1))
        assertEquals 0, discoveryServiceRegistry.getMaxScanTimeout(new ThingTypeUID(ANY_BINDING_ID_1, 'unknownType'))

        assertEquals 3, discoveryServiceRegistry.getMaxScanTimeout(ANY_BINDING_ID_2)
        assertEquals 0, discoveryServiceRegistry.getMaxScanTimeout('unknownBindingId')
    }

    @Test
    void 'assert an existing Thing can be accessed by ExtendedDiscoveryService implementations'() {
        ThingUID thingUID = new ThingUID(EXTENDED_BINDING_ID, "foo")

        // verify that the callback has been set
        assertNotNull extendedDiscoveryServiceMock.discoveryServiceCallback

        // verify that the thing cannot be found if it's not there
        assertNull extendedDiscoveryServiceMock.discoveryServiceCallback.getExistingThing(thingUID)

        thingRegistry.add(ThingBuilder.create(new ThingTypeUID(EXTENDED_BINDING_ID, EXTENDED_THING_TYPE), thingUID).build())

        // verify that the existing Thing can be accessed
        assertNotNull extendedDiscoveryServiceMock.discoveryServiceCallback.getExistingThing(thingUID)
    }

    @Test
    void 'assert that existing DiscoveryResults can be accessed by ExtendedDiscoveryService implementations'() {
        ThingUID thingUID = new ThingUID(EXTENDED_BINDING_ID, EXTENDED_THING_TYPE, "foo")

        // verify that the callback has been set
        assertNotNull extendedDiscoveryServiceMock.discoveryServiceCallback

        // verify that the DiscoveryResult cannot be found if it's not there
        assertNull extendedDiscoveryServiceMock.discoveryServiceCallback.getExistingDiscoveryResult(thingUID)

        boolean discoveryFinished = false;
        discoveryServiceRegistry.startScan(new ThingTypeUID(EXTENDED_BINDING_ID, EXTENDED_THING_TYPE),  [
            onFinished: { discoveryFinished = true },
            onErrorOccurred: {
            }
        ] as ScanListener)
        waitForAssert ({ assertTrue(discoveryFinished) }, 2000 )


        // verify that the existing DiscoveryResult can be accessed
        assertNotNull extendedDiscoveryServiceMock.discoveryServiceCallback.getExistingDiscoveryResult(thingUID)
    }


}
