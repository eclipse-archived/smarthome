/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
 */
class DiscoveryResultImplTest {

    @Test
    public void testInvalidConstructor() {
        try {
            new DiscoveryResultImpl(new ThingUID("aa"), null, null, null)
            fail "The constructor must throw an IllegalArgumentException if null is used"
            + " as Thing type!"
        } catch (IllegalArgumentException iae) {
        }
    }

    @Test
    public void testValidConstructor() {
        def thingTypeUID = new ThingTypeUID("bindingId", "thingType")

        DiscoveryResultImpl discoveryResult =
                new DiscoveryResultImpl(new ThingUID(thingTypeUID, "thingId"), null, null, null)

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
        def discoveryResultSourceMap = [ "ipAddress" : "127.0.0.1" ]
        DiscoveryResultImpl discoveryResult =
                new DiscoveryResultImpl(new ThingUID(thingTypeUID, "thingId"), null, discoveryResultSourceMap, "TARGET")

        discoveryResult.setFlag(DiscoveryResultFlag.IGNORED)

        discoveryResult.synchronize(null)

        assertEquals("127.0.0.1", discoveryResult.getProperties().get("ipAddress"))
        assertEquals("TARGET", discoveryResult.getLabel())
        assertEquals(DiscoveryResultFlag.IGNORED, discoveryResult.getFlag())
    }

    @Test
    public void testIrrelevantSynchronize() {
        
        def thingTypeUID = new ThingTypeUID("bindingId", "thingType")
        def discoveryResultSourceMap = [ "ipAddress" : "127.0.0.1" ]
        DiscoveryResultImpl discoveryResult =
                new DiscoveryResultImpl(new ThingUID(thingTypeUID, "thingId"), null, discoveryResultSourceMap, "TARGET")

        discoveryResult.setFlag(DiscoveryResultFlag.IGNORED)

        DiscoveryResultImpl discoveryResultSource =
                new DiscoveryResultImpl(new ThingUID(thingTypeUID, "anotherThingId"), null, null, null)
        

        discoveryResult.synchronize(discoveryResultSource)

        assertEquals("127.0.0.1", discoveryResult.getProperties().get("ipAddress"))
        assertEquals("TARGET", discoveryResult.getLabel())
        assertEquals(DiscoveryResultFlag.IGNORED, discoveryResult.getFlag())
    }

    @Test
    public void testSynchronize() {
        
        def thingTypeUID = new ThingTypeUID("bindingId", "thingType")
        def discoveryResultSourceMap = [ "ipAddress" : "127.0.0.1" ]
        DiscoveryResultImpl discoveryResult =
                new DiscoveryResultImpl(new ThingUID(thingTypeUID, "thingId"), null, discoveryResultSourceMap, "TARGET")

        discoveryResult.setFlag(DiscoveryResultFlag.IGNORED)
        
        def discoveryResultMap = [ "ipAddress" : "192.168.178.1" ]
        DiscoveryResultImpl discoveryResultSource =
                new DiscoveryResultImpl(new ThingUID(thingTypeUID, "thingId"), null, discoveryResultMap, "SOURCE")

  
        discoveryResultSource.setFlag(DiscoveryResultFlag.NEW)

        discoveryResult.synchronize(discoveryResultSource);

        assertEquals("192.168.178.1", discoveryResult.getProperties().get("ipAddress"))
        assertEquals("SOURCE", discoveryResult.getLabel())
        assertEquals(DiscoveryResultFlag.IGNORED, discoveryResult.getFlag())
    }

}
