/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.setup.test.discovery;

import static org.junit.Assert.*

import org.eclipse.smarthome.config.discovery.internal.DiscoveryResultImpl
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.junit.Test


/**
 * The {@link DiscoveryResultTest} checks if any invalid input parameters
 * and the synchronization of {@link DiscoveryResult}s work in a correct way.
 * 
 * @author Michael Grammling - Initial Contribution
 */
class DiscoveryResultImplTest {

    @Test
    public void testInvalidConstructor() {
        try {
            new DiscoveryResultImpl(new ThingUID("aa"))
            fail "The constructor must throw an IllegalArgumentException if null is used"
            + " as Thing type!"
        } catch (IllegalArgumentException iae) {
        }

        try {
            new DiscoveryResultImpl(new ThingTypeUID("bindingId", "thingType"), null)
            fail "The constructor must throw an IllegalArgumentException if null is used"
            + " as Thing ID!"
        } catch (IllegalArgumentException iae) {
        }
    }

    @Test
    public void testValidConstructor() {
        def thingTypeUID = new ThingTypeUID("bindingId", "thingType")

        DiscoveryResultImpl discoveryResult =
                new DiscoveryResultImpl(new ThingUID(thingTypeUID, "thingId"))

        assertEquals("bindingId:thingType", discoveryResult.getThingTypeUID().toString())
        assertEquals("bindingId:thingType:thingId", discoveryResult.getThingUID().toString())
        assertEquals("bindingId", discoveryResult.getBindingId())
        assertEquals("", discoveryResult.getLabel())
        assertEquals(DiscoveryResultFlag.NEW, discoveryResult.getFlag())

        assertNotNull("The properties must never be null!", discoveryResult.getProperties())
    }

    @Test
    public void testInvalidSynchronize() {

        def thingTypeUID = new ThingTypeUID("bindingId", "thingType")

        DiscoveryResultImpl discoveryResult =
                new DiscoveryResultImpl(thingTypeUID, new ThingUID(thingTypeUID, "thingId"));

        def discoveryResultSourceMap = [ "ipAddress" : "127.0.0.1" ]
        discoveryResult.setProperties(discoveryResultSourceMap)
        discoveryResult.setLabel("TARGET")
        discoveryResult.setFlag(DiscoveryResultFlag.IGNORED)

        discoveryResult.synchronize(null)

        assertEquals("127.0.0.1", discoveryResult.getProperties().get("ipAddress"))
        assertEquals("TARGET", discoveryResult.getLabel())
        assertEquals(DiscoveryResultFlag.IGNORED, discoveryResult.getFlag())
    }

    @Test
    public void testIrrelevantSynchronize() {
        def thingTypeUID = new ThingTypeUID("bindingId", "thingType")

        DiscoveryResultImpl discoveryResult =
                new DiscoveryResultImpl(thingTypeUID, new ThingUID(thingTypeUID, "thingId"))

        def discoveryResultSourceMap = [ "ipAddress" : "127.0.0.1" ]
        discoveryResult.setProperties(discoveryResultSourceMap)
        discoveryResult.setLabel("TARGET")
        discoveryResult.setFlag(DiscoveryResultFlag.IGNORED)

        DiscoveryResultImpl discoveryResultSource =
                new DiscoveryResultImpl(thingTypeUID, new ThingUID(thingTypeUID, "anotherThingId"))
        discoveryResultSource.setLabel("SOURCE")

        discoveryResult.synchronize(discoveryResultSource)

        assertEquals("127.0.0.1", discoveryResult.getProperties().get("ipAddress"))
        assertEquals("TARGET", discoveryResult.getLabel())
        assertEquals(DiscoveryResultFlag.IGNORED, discoveryResult.getFlag())
    }

    @Test
    public void testSynchronize() {
        def thingTypeUID = new ThingTypeUID("bindingId", "thingType")

        DiscoveryResultImpl discoveryResult =
                new DiscoveryResultImpl(thingTypeUID, new ThingUID(thingTypeUID, "thingId"));

        def discoveryResultSourceMap = [ "ipAddress" : "127.0.0.1" ]
        discoveryResult.setProperties(discoveryResultSourceMap)
        discoveryResult.setLabel("TARGET")
        discoveryResult.setFlag(DiscoveryResultFlag.IGNORED)

        DiscoveryResultImpl discoveryResultSource =
                new DiscoveryResultImpl(thingTypeUID, new ThingUID(thingTypeUID, "thingId"));

        def discoveryResultMap = [ "ipAddress" : "192.168.178.1" ]
        discoveryResultSource.setProperties(discoveryResultMap)
        discoveryResultSource.setLabel("SOURCE")
        discoveryResultSource.setFlag(DiscoveryResultFlag.NEW)

        discoveryResult.synchronize(discoveryResultSource);

        assertEquals("192.168.178.1", discoveryResult.getProperties().get("ipAddress"))
        assertEquals("SOURCE", discoveryResult.getLabel())
        assertEquals(DiscoveryResultFlag.IGNORED, discoveryResult.getFlag())
    }

}
