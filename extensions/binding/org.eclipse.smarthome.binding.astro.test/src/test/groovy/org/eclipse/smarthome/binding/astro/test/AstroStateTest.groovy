/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.test

import static org.eclipse.smarthome.binding.astro.test.cases.AstroParametrizedTestCases.*
import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import java.time.LocalDateTime
import java.time.ZonedDateTime

import org.eclipse.smarthome.binding.astro.AstroBindingConstants
import org.eclipse.smarthome.binding.astro.internal.calc.MoonCalc
import org.eclipse.smarthome.binding.astro.internal.calc.SunCalc
import org.eclipse.smarthome.binding.astro.internal.config.AstroChannelConfig
import org.eclipse.smarthome.binding.astro.internal.model.Planet
import org.eclipse.smarthome.binding.astro.internal.util.PropertyUtils
import org.eclipse.smarthome.binding.astro.test.cases.AstroBindingTestsData;
import org.eclipse.smarthome.binding.astro.test.cases.AstroParametrizedTestCases
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.types.State
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

/**
 * Tests for the Astro Channels state
 *
 * @See {@link AstroParametrizedTestCases}
 * @author Petar Valchev - Initial implementation
 * @author Svilen Valkanov -  Reworked to plain unit tests
 *
 */
@RunWith(Parameterized.class)
class AstroStateTest {
    private String thingID
    private String channelId
    private State expectedState

    public AstroStateTest(String thingID, String channelId, State expectedState){
        this.thingID = thingID
        this.channelId = channelId
        this.expectedState = expectedState
    }

    @Parameters
    public static Collection<Object[]> data() {
        AstroParametrizedTestCases cases = new AstroParametrizedTestCases()
        cases.getCases()
    }

    @Test
    public void testParametrized(){
        assertStateUpdate(thingID, channelId, expectedState)
    }

    private void assertStateUpdate(String thingID, String channelId, State expectedState){
        LocalDateTime time = LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY,0,0)
        ZonedDateTime zonedTime = ZonedDateTime.ofLocal(time,TEST_ZONE_OFSET,null)
        Calendar calendar = GregorianCalendar.from(zonedTime)
        
        Planet planet
        ThingUID thingUID
        switch(thingID) {
            case (AstroBindingTestsData.TEST_SUN_THING_ID) :
                SunCalc sunCalc = new SunCalc();
                planet = sunCalc.getSunInfo(calendar, TEST_LATITUDE, TEST_LONGITUDE, null);
                thingUID = new ThingUID (AstroBindingConstants.THING_TYPE_SUN,thingID)
                break
            case(AstroBindingTestsData.TEST_MOON_THING_ID) :
                MoonCalc moonCalc = new MoonCalc();
                planet = moonCalc.getMoonInfo(calendar, TEST_LATITUDE, TEST_LONGITUDE);
                thingUID = new ThingUID (AstroBindingConstants.THING_TYPE_MOON,thingID)
                break
        }

        ChannelUID testItemChannelUID = new ChannelUID(thingUID, channelId)
        State state = PropertyUtils.getState(testItemChannelUID, new AstroChannelConfig(), planet)
        assertThat state, is(equalTo(expectedState))
    }
}
