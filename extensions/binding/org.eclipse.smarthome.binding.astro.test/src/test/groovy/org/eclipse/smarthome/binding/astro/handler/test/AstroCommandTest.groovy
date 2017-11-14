/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.handler.test

import static org.eclipse.smarthome.binding.astro.test.cases.AstroBindingTestsData.*
import static org.mockito.Mockito.*

import org.eclipse.smarthome.binding.astro.AstroBindingConstants
import org.eclipse.smarthome.binding.astro.handler.AstroThingHandler
import org.eclipse.smarthome.binding.astro.handler.SunHandler
import org.eclipse.smarthome.binding.astro.internal.model.Sun
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback
import org.eclipse.smarthome.core.types.RefreshType
import org.eclipse.smarthome.core.types.State
import org.junit.Test
import org.mockito.Mockito

/**
 * OSGi test for the {@link AstroThingHandler}
 * <p>
 * This class tests the commands for the astro thing.
 *
 * @author Petar Valchev - Initial implementation
 * @author Svilen Valkanov - Reworked to plain unit tests
 *
 */
class AstroCommandTest {
    @Test
    public void 'refresh command updates the state of the channels'(){
        ThingUID thingUID = new ThingUID(AstroBindingConstants.THING_TYPE_SUN, TEST_SUN_THING_ID)
        ChannelUID channelUID = new ChannelUID(thingUID,DEFAULT_TEST_CHANNEL_ID)
        Channel channel = new Channel(channelUID, DEFAULT_IMEM_TYPE)
        
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE)
        thingConfiguration.put(INTERVAL_PROPERTY, INTERVAL_DEFAULT_VALUE)
        
        Thing thing = mock(Thing.class)
        when(thing.getConfiguration()).thenReturn(thingConfiguration)
        when(thing.getUID()).thenReturn(thingUID)
        when(thing.getChannel(DEFAULT_TEST_CHANNEL_ID)).thenReturn(channel)
        
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class)
        AstroThingHandler sunHandler = Mockito.spy(new SunHandler(thing))
        
        // Required from the AstroThingHandler to send the status update
        Mockito.doReturn(new Sun()).when(sunHandler).getPlanet()
        Mockito.doReturn(true).when(sunHandler).isLinked(any(String.class))
        sunHandler.setCallback(callback);
        
        sunHandler.handleCommand(channelUID, RefreshType.REFRESH)
        verify(callback, times(1)).stateUpdated(eq(channelUID), any(State.class))
    }
}
