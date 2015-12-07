package org.eclipse.smarthome.io.rest.core.discovery

import static org.junit.Assert.*

import java.net.URI
import java.net.URISyntaxException
import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap
import java.util.List
import java.util.Map

import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.*

import org.eclipse.smarthome.config.core.ConfigDescription
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.config.discovery.DiscoveryResult
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag
import org.eclipse.smarthome.config.discovery.inbox.Inbox
import org.eclipse.smarthome.config.discovery.inbox.InboxFilterCriteria
import org.eclipse.smarthome.config.discovery.inbox.InboxListener
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.setup.ThingSetupManager
import org.eclipse.smarthome.core.thing.type.ThingType
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry
import org.eclipse.smarthome.io.rest.core.discovery.InboxResource
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test

class InboxResourceOSGITest extends OSGiTest {

    ThingSetupManager thingSetupManager
    ThingTypeRegistry thingTypeRegistry = new ThingTypeRegistry()
    ConfigDescriptionRegistry configDescRegistry
    Inbox inbox
    InboxResource resource

    final URI testURI = new URI("http:dummy")
    final String testThingLabel = "dummy_thing"
    final ThingUID testUID = new ThingUID("binding:type:id")
    final ThingTypeUID testTypeUID = new ThingTypeUID("binding:type")
    final Thing testThing = ThingBuilder.create(testUID).build()
    final Map<String, Object> discoveryResultProperties =
    ["ip":"192.168.3.99",
        "pnr": 1234455,
        "snr":12345,
        "manufacturer":"huawei",
        "manufactured":new Date(12344)
    ]
    final List<DiscoveryResult> inboxContent = []
    final Map<ThingUID, Thing> thingsOfSetupManager = [:]
    final DiscoveryResult testDiscoveryResult = DiscoveryResultBuilder.create(testThing.getUID()).withProperties(discoveryResultProperties)
    .build()
    final ThingType testThingType = new ThingType(testTypeUID, null, "label", "", null, null, null, testURI)
    final ConfigDescriptionParameter[] configDescriptionParameter = [[discoveryResultProperties.keySet().getAt(0), Type.TEXT], [discoveryResultProperties.keySet().getAt(1), Type.INTEGER]]
    final ConfigDescription testConfigDescription  = new ConfigDescription(testURI, Arrays.asList(configDescriptionParameter))
    final String[] keysInConfigDescription = [discoveryResultProperties.keySet().getAt(0), discoveryResultProperties.keySet().getAt(1)]
    final String[] keysNotInConfigDescription = [discoveryResultProperties.keySet().getAt(2), discoveryResultProperties.keySet().getAt(3), discoveryResultProperties.keySet().getAt(4)]

    @Before
    void setup() throws Exception {
        thingSetupManager = new ThingSetupManager() {
            @Override
            public Thing getThing(ThingUID thingUID) {
                thingsOfSetupManager.get(thingUID)
            }

            @Override
            public Thing addThing(ThingUID thingUID, Configuration conf, ThingUID bridgeUID, String label,
            List<String> groupNames, boolean enableChannels, Map<String, String> properties) {
                Thing newThing = ThingBuilder.create(thingUID).withConfiguration(conf).withBridge(bridgeUID)
                .withProperties(properties).build()
                thingsOfSetupManager.put(thingUID, newThing)
                newThing
            }
        }
        inbox = [
            get :{
                criteria -> inboxContent
            }
        ] as Inbox
        registerService inbox
        inbox = getService(Inbox.class)
        configDescRegistry = getService ConfigDescriptionRegistry
        registerService new InboxResource(), InboxResource.class.getName()
        resource = getService InboxResource
        resource.setThingSetupManager(thingSetupManager)
        resource.setInbox(inbox)
        resource.setThingTypeRegistry(new ThingTypeRegistry())
        assertFalse thingSetupManager == null
        assertFalse thingTypeRegistry == null
        assertFalse configDescRegistry == null
        assertFalse inbox == null
        assertFalse resource == null
        assertTrue inboxContent.isEmpty()
    }

    @Test
    void 'assert that approve dont approve Things which are not in the Inbox' () {
        Response reponse = resource.approve(testThing.getUID().toString(), testThingLabel, false)
        assertTrue reponse.getStatusInfo() == Status.NOT_FOUND
        assertTrue thingSetupManager.getThing(testThing.getUID()) == null
    }

    @Test
    void 'assert that approve adds all properties of DiscorveryResult to Thing properties if no ConfigDescriptionParameters for the ThingType are available' () {
        inboxContent.add(testDiscoveryResult)

        Response reponse = resource.approve(testThing.getUID().toString(), testThingLabel, false)
        Thing addedThing = thingSetupManager.getThing(testThing.getUID())

        assertTrue reponse.getStatusInfo() == Status.OK
        assertFalse addedThing == null
        discoveryResultProperties.keySet().each{
            String thingProperty = addedThing.getProperties().get(it)
            String descResultParam = String.valueOf(discoveryResultProperties.get(it))
            assertFalse thingProperty == null
            assertFalse descResultParam == null
            assertTrue thingProperty.equals(descResultParam)
        }
    }

    @Test
    void 'assert that approve adds properties of DiscorveryResult which are ConfigDescriptionParameters as Thing Configuration properties and properties which are no ConfigDescriptionParameters as Thing properties'() {
        thingTypeRegistry = new ThingTypeRegistry() {
                    @Override
                    public ThingType getThingType(ThingTypeUID thingTypeUID) {
                        return testThingType
                    }
                }
        resource.setThingTypeRegistry(thingTypeRegistry)
        configDescRegistry = new ConfigDescriptionRegistry() {
                    @Override
                    public ConfigDescription getConfigDescription(URI uri) {
                        return testConfigDescription
                    }
                }
        resource.setConfigDescriptionRegistry(configDescRegistry)
        inboxContent.add(testDiscoveryResult)

        Response reponse = resource.approve(testThing.getUID().toString(), testThingLabel, false)
        Thing addedThing = thingSetupManager.getThing(testThing.getUID())

        assertTrue reponse.getStatusInfo() == Status.OK
        assertFalse addedThing == null
        keysInConfigDescription.each {
            Object thingConfItem = addedThing.getConfiguration().get(it)
            Object descResultParam = discoveryResultProperties.get(it)
            assertFalse thingConfItem == null
            assertFalse descResultParam == null
            assertTrue thingConfItem.equals(descResultParam)
        }
        keysNotInConfigDescription.each {
            String thingProperty = addedThing.getProperties().get(it)
            String descResultParam = String.valueOf(discoveryResultProperties.get(it))
            assertFalse thingProperty == null
            assertFalse descResultParam == null
            assertTrue thingProperty.equals(descResultParam)
        }
    }
}
