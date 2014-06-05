package org.eclipse.smarthome.config.setup.test.discovery;

import static org.junit.Assert.*

import org.eclipse.smarthome.config.discovery.DiscoveryResult
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.junit.Test


/**
 * The {@link DiscoveryResultTest} test checks if any invalid input parameters
 * and the synchronization of {@link DiscoveryResult}s work in a correct way.
 * 
 * @author Michael Grammling - Initial Contribution
 */
class DiscoveryResultTest {

    @Test
    public void testInvalidConstructor() {
        try {
            new DiscoveryResult(null, new ThingUID("aa"))
            fail "The constructor must throw an IllegalArgumentException if null is used"
            + " as Thing type!"
        } catch (IllegalArgumentException iae) {
        }

        try {
            new DiscoveryResult(new ThingTypeUID("bindingId", "thingType"), null)
            fail "The constructor must throw an IllegalArgumentException if null is used"
            + " as Thing ID!"
        } catch (IllegalArgumentException iae) {
        }
    }

    @Test
    public void testValidConstructor() {
        def thingTypeUID = new ThingTypeUID("bindingId", "thingType")

        DiscoveryResult discoveryResult =
                new DiscoveryResult(thingTypeUID, new ThingUID(thingTypeUID, "thingId"))

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

        DiscoveryResult discoveryResult =
                new DiscoveryResult(thingTypeUID, new ThingUID(thingTypeUID, "thingId"));

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

        DiscoveryResult discoveryResult =
                new DiscoveryResult(thingTypeUID, new ThingUID(thingTypeUID, "thingId"))

        def discoveryResultSourceMap = [ "ipAddress" : "127.0.0.1" ]
        discoveryResult.setProperties(discoveryResultSourceMap)
        discoveryResult.setLabel("TARGET")
        discoveryResult.setFlag(DiscoveryResultFlag.IGNORED)

        DiscoveryResult discoveryResultSource =
                new DiscoveryResult(thingTypeUID, new ThingUID(thingTypeUID, "anotherThingId"))
        discoveryResultSource.setLabel("SOURCE")

        discoveryResult.synchronize(discoveryResultSource)

        assertEquals("127.0.0.1", discoveryResult.getProperties().get("ipAddress"))
        assertEquals("TARGET", discoveryResult.getLabel())
        assertEquals(DiscoveryResultFlag.IGNORED, discoveryResult.getFlag())
    }

    @Test
    public void testSynchronize() {
        def thingTypeUID = new ThingTypeUID("bindingId", "thingType")

        DiscoveryResult discoveryResult =
                new DiscoveryResult(thingTypeUID, new ThingUID(thingTypeUID, "thingId"));

        def discoveryResultSourceMap = [ "ipAddress" : "127.0.0.1" ]
        discoveryResult.setProperties(discoveryResultSourceMap)
        discoveryResult.setLabel("TARGET")
        discoveryResult.setFlag(DiscoveryResultFlag.IGNORED)

        DiscoveryResult discoveryResultSource =
                new DiscoveryResult(thingTypeUID, new ThingUID(thingTypeUID, "thingId"));

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
