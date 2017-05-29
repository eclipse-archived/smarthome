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
import org.eclipse.smarthome.binding.astro.handler.SunHandler
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingStatusDetail
import org.eclipse.smarthome.core.thing.ThingStatusInfo
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback
import org.junit.Test

/**
 * OSGi tests for the {@link AstroThingHandler}
 * <p>
 * This class tests the required configuration for the astro thing.
 *
 * @author Petar Valchev - Initial implementation
 * @author Svilen Valkanov - Reworked to plain unit tests, removed irrelevant tests
 *
 */
class AstroValidConfigurationTest {
    private final String NULL_LONGITUDE = "51.2,null"
    private final String NULL_LATITUDE = "null,25.4"

    @Test
    public void 'if geolocation is provided for a sun thing, the thing status becomes ONLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE)
        thingConfiguration.put(INTERVAL_PROPERTY, INTERVAL_DEFAULT_VALUE)
        assertThingStatus(thingConfiguration, ThingStatus.ONLINE)
    }

    @Test
    public void 'if geolocation is provided for a moon thing, the thing status becomes ONLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE)
        thingConfiguration.put(INTERVAL_PROPERTY, INTERVAL_DEFAULT_VALUE)
        assertThingStatus(thingConfiguration, ThingStatus.ONLINE)
    }

    @Test
    public void 'if geolocation for a sun thing is null, the thing status becomes OFFLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, null)
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE)
    }

    @Test
    public void 'if geolocation for a moon thing is null, the thing status becomes OFFLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, null)
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE)
    }

    @Test
    public void 'if the latitude for a sun thing is null, the thing status becomes OFFLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, NULL_LATITUDE)
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE)
    }

    @Test
    public void 'if the latitude for a moon thing is null, the thing status becomes OFFLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, NULL_LATITUDE)
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE)
    }

    @Test
    public void 'if the longitude for a sun thing is null, the thing status becomes OFFLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, NULL_LONGITUDE)
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE)
    }

    @Test
    public void 'if the longitude for a moon thing is null, the thing status becomes OFFLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, NULL_LONGITUDE)
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE)
    }

    @Test
    public void 'if the interval for a sun thing is less than 1, the thing status becomes OFFLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE)
        thingConfiguration.put(INTERVAL_PROPERTY, new Integer(0))
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE)
    }

    @Test
    public void 'if the interval for a moon thing is less than 1, the thing status becomes OFFLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE)
        thingConfiguration.put(INTERVAL_PROPERTY, new Integer(0))
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE)
    }

    @Test
    public void 'if the interval for a sun thing is more than 86400, the thing status becomes OFFLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE)
        thingConfiguration.put(INTERVAL_PROPERTY, new Integer(86401))
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE)
    }

    @Test
    public void 'if the interval for a moon thing is more than 86400, the thing status becomes OFFLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE)
        thingConfiguration.put(INTERVAL_PROPERTY, new Integer(86401))
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE)
    }

    private assertThingStatus(Configuration configuration, ThingStatus expectedStatus) {
        ThingUID thingUID = new ThingUID(AstroBindingConstants.THING_TYPE_SUN, TEST_SUN_THING_ID)

        Thing thing = mock(Thing.class)
        when(thing.getConfiguration()).thenReturn(configuration)
        when(thing.getUID()).thenReturn(thingUID)

        ThingHandlerCallback callback = mock(ThingHandlerCallback.class)
        ThingHandler sunHandler = new SunHandler(thing);
        sunHandler.setCallback(callback);

        sunHandler.initialize()

        ThingStatusInfo expectedThingStatus = new ThingStatusInfo(expectedStatus, ThingStatusDetail.NONE, null)
        verify(callback, times(1)).statusUpdated(thing, expectedThingStatus)
    }
}
