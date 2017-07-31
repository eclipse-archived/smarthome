/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.setup.test.discovery;

import static org.junit.Assert.*

import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag
import org.eclipse.smarthome.config.discovery.internal.DiscoveryResultImpl
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.junit.Test


/**
 * The {@link DiscoveryResultTest} checks if any invalid input parameters
 * and the synchronization of {@link DiscoveryResult}s work in a correct way.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Thomas Höfer - Added representation
 */
class DiscoveryResultImplTest {

    def DEFAULT_TTL = 60

    @Test
    public void testInvalidConstructorForThingType() {
        try {
            new DiscoveryResultImpl(new ThingUID("aa"), null, null, null, null, DEFAULT_TTL)
            fail "The constructor must throw an IllegalArgumentException if null is used"
            + " as Thing type!"
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testInvalidConstructorForTTL() {
        try {
            def thingTypeUID = new ThingTypeUID("bindingId", "thingType")

            DiscoveryResultImpl discoveryResult =
                    new DiscoveryResultImpl(new ThingUID(thingTypeUID, "thingId"), null, null, null, null, -2)
            fail "The constructor must throw an IllegalArgumentException if negative value is used"
            + " as ttl!"
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testValidConstructor() {
        def thingTypeUID = new ThingTypeUID("bindingId", "thingType")

        DiscoveryResultImpl discoveryResult =
                new DiscoveryResultImpl(new ThingUID(thingTypeUID, "thingId"), null, null, null, null, DEFAULT_TTL)

        assertEquals("bindingId:thingType", discoveryResult.getThingTypeUID().toString())
        assertEquals("bindingId:thingType:thingId", discoveryResult.getThingUID().toString())
        assertEquals("bindingId", discoveryResult.getBindingId())
        assertEquals("", discoveryResult.getLabel())
        assertEquals(DiscoveryResultFlag.NEW, discoveryResult.getFlag())

        assertNotNull("The properties must never be null!", discoveryResult.getProperties())
        assertNull(discoveryResult.getRepresentationProperty())
    }

    @Test
    public void testInvalidSynchronize() {

        def thingTypeUID = new ThingTypeUID("bindingId", "thingType")
        def discoveryResultSourceMap = [ "ipAddress" : "127.0.0.1" ]
        DiscoveryResultImpl discoveryResult =
                new DiscoveryResultImpl(new ThingUID(thingTypeUID, "thingId"), null, discoveryResultSourceMap, "ipAddress", "TARGET", DEFAULT_TTL)

        discoveryResult.setFlag(DiscoveryResultFlag.IGNORED)

        discoveryResult.synchronize(null)

        assertEquals("127.0.0.1", discoveryResult.getProperties().get("ipAddress"))
        assertEquals("ipAddress", discoveryResult.getRepresentationProperty())
        assertEquals("TARGET", discoveryResult.getLabel())
        assertEquals(DiscoveryResultFlag.IGNORED, discoveryResult.getFlag())
    }

    @Test
    public void testIrrelevantSynchronize() {

        def thingTypeUID = new ThingTypeUID("bindingId", "thingType")
        def discoveryResultSourceMap = [ "ipAddress" : "127.0.0.1" ]
        DiscoveryResultImpl discoveryResult =
                new DiscoveryResultImpl(new ThingUID(thingTypeUID, "thingId"), null, discoveryResultSourceMap, "ipAddress", "TARGET", DEFAULT_TTL)

        discoveryResult.setFlag(DiscoveryResultFlag.IGNORED)

        DiscoveryResultImpl discoveryResultSource =
                new DiscoveryResultImpl(new ThingUID(thingTypeUID, "anotherThingId"), null, null, null, null, DEFAULT_TTL)


        discoveryResult.synchronize(discoveryResultSource)

        assertEquals("127.0.0.1", discoveryResult.getProperties().get("ipAddress"))
        assertEquals("ipAddress", discoveryResult.getRepresentationProperty())
        assertEquals("TARGET", discoveryResult.getLabel())
        assertEquals(DiscoveryResultFlag.IGNORED, discoveryResult.getFlag())
    }

    @Test
    public void testSynchronize() {

        def thingTypeUID = new ThingTypeUID("bindingId", "thingType")
        def discoveryResultSourceMap = [ "ipAddress" : "127.0.0.1" ]
        DiscoveryResultImpl discoveryResult =
                new DiscoveryResultImpl(new ThingUID(thingTypeUID, "thingId"), null, discoveryResultSourceMap, "ipAddress", "TARGET", DEFAULT_TTL)

        discoveryResult.setFlag(DiscoveryResultFlag.IGNORED)

        def discoveryResultMap = [ "ipAddress" : "192.168.178.1", "macAddress" : "AA:BB:CC:DD:EE:FF" ]
        DiscoveryResultImpl discoveryResultSource =
                new DiscoveryResultImpl(new ThingUID(thingTypeUID, "thingId"), null, discoveryResultMap, "macAddress", "SOURCE", DEFAULT_TTL)


        discoveryResultSource.setFlag(DiscoveryResultFlag.NEW)

        discoveryResult.synchronize(discoveryResultSource);

        assertEquals("192.168.178.1", discoveryResult.getProperties().get("ipAddress"))
        assertEquals("AA:BB:CC:DD:EE:FF", discoveryResult.getProperties().get("macAddress"))
        assertEquals("macAddress", discoveryResult.getRepresentationProperty())
        assertEquals("SOURCE", discoveryResult.getLabel())
        assertEquals(DiscoveryResultFlag.IGNORED, discoveryResult.getFlag())
    }

    @Test
    public void testThingTypeCompatibility() {
        def thingTypeUID = new ThingTypeUID("bindingId", "thingType")
        DiscoveryResultImpl discoveryResult = new DiscoveryResultImpl(null, new ThingUID(thingTypeUID, "thingId"), null, null, "nothing", "label", DEFAULT_TTL)
        assertNotNull(discoveryResult.getThingTypeUID())
        assertEquals(discoveryResult.getThingTypeUID(), thingTypeUID)
    }
}
