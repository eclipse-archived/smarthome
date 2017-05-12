/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.handler.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.binding.astro.AstroBindingConstants
import org.eclipse.smarthome.binding.astro.test.AstroOSGiTest
import org.eclipse.smarthome.binding.astro.test.AstroOSGiTest.AcceptedItemType
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.items.events.AbstractItemEventSubscriber
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.types.RefreshType
import org.junit.After
import org.junit.Test

/**
 * OSGi test for the {@link AstroThingHandler}
 * <p>
 * This class tests the commands for the astro thing.
 *
 * @author Petar Valchev - Initial Implementation
 * @author Svilen Valkanov - Added missing configuration parameters, replaced headers, use groovy for mocking
 *
 */
class AstroCommandTest extends AstroOSGiTest {
    
    private final String expectedEventSource = "$AstroBindingConstants.BINDING_ID:$AstroBindingConstants.SUN:$TEST_SUN_THING_ID:$DEFAULT_TEST_CHANNEL_ID"
    private boolean isEventReceived = false
    
    // a listener for item state update events
    private EventSubscriber eventSubscriberMock = [
        receive: { Event event ->
            if(event.getSource().equals(expectedEventSource)){
                isEventReceived = true
            }
        }
        
    ] as AbstractItemEventSubscriber

    @Test
    public void 'refresh command updates the state of the channels'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE)
        thingConfiguration.put(INTERVAL_PROPERTY, INTERVAL_DEFAULT_VALUE)
        initialize(TEST_SUN_THING_ID, DEFAULT_TEST_CHANNEL_ID, AcceptedItemType.DATE_TIME, thingConfiguration)

        waitForAssert({
            assertThat astroThing.getStatus(), is(equalTo(ThingStatus.ONLINE))
        })

        registerService(eventSubscriberMock, EventSubscriber.class.getName())

        ChannelUID testItemChannelUID = getChannelUID(DEFAULT_TEST_CHANNEL_ID)

        astroHandler.handleCommand(testItemChannelUID, RefreshType.REFRESH)
        waitForAssert({
            assertThat isEventReceived, is(true)
        })
    }

    @After
    public void tearDown(){
        isEventReceived = false
    }
}
