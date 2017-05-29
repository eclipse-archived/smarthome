/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.handler.test

import static org.mockito.Mockito.*
import static org.eclipse.smarthome.binding.astro.test.cases.AstroBindingTestsData.*

import org.eclipse.smarthome.binding.astro.AstroBindingConstants
import org.eclipse.smarthome.binding.astro.handler.SunHandler
import org.eclipse.smarthome.binding.astro.test.cases.AstroBindingTestsData;
import org.eclipse.smarthome.core.library.types.DateTimeType
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback
import org.junit.Test
import org.mockito.Matchers

/**
 * OSGi tests for the {@link AstroThingHandler}
 * <p>
 * This class tests the handleUpdate method in the {@link AstroThingHandler}.
 *
 * @author Petar Valchev - Initial implementation
 * @author Svilen Valkanov - Reworked to plain unit tests
 *
 */
class AstroReadOnlyTest {
    @Test
    public void 'test handle command doesn`t update item state'() {
        ThingUID thingUID = new ThingUID(AstroBindingConstants.THING_TYPE_SUN, TEST_SUN_THING_ID)
        ChannelUID channelUID = new ChannelUID(thingUID, DEFAULT_TEST_CHANNEL_ID)
        
        Thing thing = mock(Thing.class)
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class)
        SunHandler sunHandler = new SunHandler(thing);
        sunHandler.setCallback(callback)
        
        def expectedState = new DateTimeType(Calendar.getInstance())
        sunHandler.handleUpdate(channelUID, expectedState)
        verify(callback, times(0)).stateUpdated(channelUID, expectedState)
    }
}
