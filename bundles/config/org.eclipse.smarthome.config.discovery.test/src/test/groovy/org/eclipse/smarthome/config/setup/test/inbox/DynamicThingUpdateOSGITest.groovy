/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
import org.eclipse.smarthome.core.thing.ThingStatusDetail
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder
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
 */
class DynamicThingUpdateOSGITest extends OSGiTest {

    def DEFAULT_TTL = 60

    final BINDING_ID = 'dynamicUpdateBindingId'
    final THING_TYPE_ID = 'dynamicUpdateThingType'
    final THING_ID = 'dynamicUpdateThingId'

    final ThingTypeUID THING_TYPE_UID = new ThingTypeUID(BINDING_ID, THING_TYPE_ID)
    final ThingUID THING_UID = new ThingUID(THING_TYPE_UID, THING_ID)

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
            setCallback: {callbackArg -> 
                callback = callbackArg 
            },
        ] as ThingHandler )

        return thingHandler
    }

    ThingHandlerFactory createThingHandlerFactory() {
        ThingHandlerFactory thingHandlerFactory = ( [
            'supportsThingType' : { ThingTypeUID thingTypeUID ->
                return THING_TYPE_UID.equals(thingTypeUID)
            },
            'registerHandler' : { Thing thing, ThingHandlerCallback callback ->
                thingHandler = createThingHandler(thing)

                Hashtable<String, Object> properties = [
                    'thing.id': THING_UID, 'thing.type' : THING_TYPE_UID.toString() ]

                registerService(thingHandler, ThingHandler.class.name, properties)
            },
            'unregisterHandler' : { Thing thing ->
                unregisterService(thingHandler)
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

        ThingHandlerFactory thingHandlerFactory = createThingHandlerFactory()
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        Thing thing = ThingBuilder.create(THING_TYPE_UID, THING_ID).build()
        managedThingProvider.add thing
        callback.statusUpdated(thing, ThingStatusInfoBuilder.create(ThingStatus.ONLINE).build())
        
        Hashtable discoveryResultProps = [ "ipAddress" : "127.0.0.1" ]
        DiscoveryResult discoveryResult = new DiscoveryResultImpl(THING_UID, null, discoveryResultProps, "ipAddress", "DummyLabel1", DEFAULT_TTL)

        inbox.add discoveryResult

        assertThat inbox.getAll().size(), is(0)
        assertThat thingUpdated, is(true)
        assertThat updatedThing, not(null)
        assertThat updatedThing.configuration.get('ipAddress'), is('127.0.0.1')

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
}
