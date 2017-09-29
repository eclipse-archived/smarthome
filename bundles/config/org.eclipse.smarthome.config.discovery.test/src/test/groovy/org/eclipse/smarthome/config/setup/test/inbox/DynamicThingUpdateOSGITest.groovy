/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.setup.test.inbox

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.config.discovery.DiscoveryResult
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry
import org.eclipse.smarthome.config.discovery.inbox.Inbox
import org.eclipse.smarthome.config.discovery.internal.DiscoveryResultImpl
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder
import org.eclipse.smarthome.core.thing.type.ThingType
import org.eclipse.smarthome.core.thing.type.ThingTypeBuilder
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.core.types.State
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test


/**
 * The {@link DynamicThingUpdateTest} checks if a {@link Thing} configuration is updated
 * correctly when it was found via a {@link DiscoveryService}.
 * <p>
 * This implementation creates a {@link Thing} and adds it to the {@link ManagedThingProvider}.
 * A {@link DiscoveryResult} object is created and added to the {@link Inbox}. The {@link Inbox}
 * has to figure out if the configuration must be updated or not and triggers a further process
 * chain to update the {@link Thing} at the according {@link ThingHandler} if needed.
 * A dummy {@link ThingHandler} and {@link ThingHandlerFactory} is used to detect an updated event.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Thomas Höfer - Added representation
 * @author Andre Fuechsel - Added tests for device id
 */
class DynamicThingUpdateOSGITest extends OSGiTest {

    def DEFAULT_TTL = 60

    final BINDING_ID = 'dynamicUpdateBindingId'
    final THING_TYPE_ID = 'dynamicUpdateThingType'
    final THING_ID = 'dynamicUpdateThingId'
    final THING_ID2 = 'dynamicUpdateThingId2'
    final DEVICE_ID = 'deviceId'
    final DEVICE_ID_KEY = 'deviceIdKey'

    final ThingTypeUID THING_TYPE_UID = new ThingTypeUID(BINDING_ID, THING_TYPE_ID)
    final ThingUID THING_UID = new ThingUID(THING_TYPE_UID, THING_ID)
    final ThingUID THING_UID2 = new ThingUID(THING_TYPE_UID, THING_ID2)
    final ThingType THING_TYPE = ThingTypeBuilder.instance(THING_TYPE_UID, "label").withRepresentationProperty(DEVICE_ID_KEY).isListed(true).build();

    Inbox inbox
    DiscoveryServiceRegistry discoveryServiceRegistry
    ManagedThingProvider managedThingProvider

    ThingHandler thingHandler
    ThingHandlerCallback callback

    boolean thingUpdated
    Thing updatedThing


    @Before
    void setUp() {
        registerVolatileStorageService()

        def thingTypeProvider = [ "getThingType" : { uid, locale -> THING_TYPE } ] as ThingTypeProvider
        registerService(thingTypeProvider)

        inbox = getService Inbox
        discoveryServiceRegistry = getService DiscoveryServiceRegistry
        managedThingProvider = getService ManagedThingProvider

        thingUpdated = false
        updatedThing = null
    }

    @After
    void cleanUp() {
        managedThingProvider.all.each {
            managedThingProvider.remove(it.getUID())
        }

        unregisterMocks()
    }

    ThingHandler createThingHandler(Thing thing) {
        ThingHandler thingHandler = ( [
            'initialize' : { },
            'dispose' : { },
            'getThing' : { return thing },
            'handleCommand' : { ChannelUID channelUID, Command command ->
            },
            'handleUpdate' : { ChannelUID channelUID, State newState ->
            },
            'thingUpdated' : { Thing updatedThing ->
                this.thingUpdated = true
                this.updatedThing = updatedThing
            },
            'setCallback' : { callbackArg -> callback = callbackArg }
        ] as ThingHandler )

        return thingHandler
    }

    ThingHandlerFactory createThingHandlerFactory() {
        ThingHandlerFactory thingHandlerFactory = ( [
            'supportsThingType' : { ThingTypeUID thingTypeUID ->
                return THING_TYPE_UID.equals(thingTypeUID)
            },
            'registerHandler' : { Thing thing ->
                thingHandler = createThingHandler(thing)
                thingHandler
            },
            'unregisterHandler' : { Thing thing ->
            },
            'createThing' : { ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID, ThingUID bridgeUID ->
                return null
            },
            'removeThing' : { ThingUID thingUID ->
            }
        ] as ThingHandlerFactory )

        return thingHandlerFactory;
    }

    @Test
    void 'assert that an already existing Thing with another configuration is updated'() {
        assertThat inbox.getAll().size(), is(0)

        final String CFG_IP_ADDRESS_KEY = "ipAddress";
        final String CFG_IP_ADDRESS_VALUE = "127.0.0.1";

        ThingHandlerFactory thingHandlerFactory = createThingHandlerFactory()
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        Thing thing = ThingBuilder.create(THING_TYPE_UID, THING_ID).build()
        thing.getConfiguration().put(CFG_IP_ADDRESS_KEY, null);
        managedThingProvider.add thing
        waitForAssert {
            assertThat callback, is(notNullValue())
        }
        callback.statusUpdated(thing, ThingStatusInfoBuilder.create(ThingStatus.ONLINE).build())

        final Map<String, Object> discoveryResultProps = new HashMap<>();
        discoveryResultProps.put(CFG_IP_ADDRESS_KEY, CFG_IP_ADDRESS_VALUE);
        DiscoveryResult discoveryResult = new DiscoveryResultImpl(THING_UID, null, discoveryResultProps, "DummyRepr", "DummyLabel1", DEFAULT_TTL)

        inbox.add discoveryResult

        assertThat inbox.getAll().size(), is(0)
        assertThat thingUpdated, is(true)
        assertThat updatedThing, not(null)
        assertThat updatedThing.configuration.get(CFG_IP_ADDRESS_KEY), is(CFG_IP_ADDRESS_VALUE)

        unregisterService(thingHandlerFactory)
    }

    @Test
    void 'assert that an already existing Thing with the same configuration is fully ignored'() {
        assertThat inbox.getAll().size(), is(0)

        ThingHandlerFactory thingHandlerFactory = createThingHandlerFactory()
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        managedThingProvider.add ThingBuilder.create(THING_TYPE_UID, THING_ID).build()

        DiscoveryResult discoveryResult = new DiscoveryResultImpl(THING_UID, null, [:], null, "DummyLabel", DEFAULT_TTL)

        inbox.add discoveryResult

        assertThat inbox.getAll().size(), is(0)
        assertThat thingUpdated, is(false)

        unregisterService(thingHandlerFactory)
    }

    @Test
    void 'assert that an thing with different thing uid as the already existing thing is added'() {
        assertThat inbox.getAll().size(), is(0)

        ThingHandlerFactory thingHandlerFactory = createThingHandlerFactory()
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        managedThingProvider.add ThingBuilder.create(THING_TYPE_UID, THING_ID).build()

        DiscoveryResult discoveryResult = new DiscoveryResultImpl(THING_UID2, null, [:], null, "DummyLabel", DEFAULT_TTL)

        inbox.add discoveryResult

        assertThat inbox.getAll().size(), is(1)

        unregisterService(thingHandlerFactory)
    }
}
