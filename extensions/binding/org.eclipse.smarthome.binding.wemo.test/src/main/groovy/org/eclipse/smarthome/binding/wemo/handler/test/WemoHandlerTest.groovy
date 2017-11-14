/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.wemo.handler.test;

import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.*
import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.binding.wemo.WemoBindingConstants
import org.eclipse.smarthome.binding.wemo.handler.WemoHandler
import org.eclipse.smarthome.core.library.types.DateTimeType
import org.eclipse.smarthome.core.library.types.DecimalType
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.types.State
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

/**
 * Tests for {@link WemoHandler}.
 *
 * @author Svilen Valkanov - Initial contribution
 */
public class WemoHandlerTest {

    def THING_TYPE = WemoBindingConstants.THING_TYPE_INSIGHT
    def THING_ID = "test"

    MockWemoHandler handler

    def SERVICE_ID = "insight"
    def PARAMS_NAME = "InsightParams"
    def insightParams

    /** Used for all tests, where expected value is time in seconds**/
    def TIME_PARAM = 4702

    /** Represents a state parameter, where 1 stays for ON and 0 stays for OFF **/
    def STATE_PARAM = 1

    /** Represents power in Wats**/
    def POWER_PARAM = 54

    def thing = [
        getUID: {
            ->
            return new ThingUID(THING_TYPE, THING_ID)
        },
        getThingTypeUID: { -> return THING_TYPE },
        getStatus: { return ThingStatus.ONLINE }

    ] as Thing

    @Before
    void setUp () {
        insightParams = new WemoInsightParams()
    }

    @After
    void clear() {
        handler.channelState = null;
        handler.channelToWatch = null;
    }

    @Test
    void 'assert that channel STATE is updated on recevied value' (){
        insightParams.state = STATE_PARAM
        State expectedStateType = OnOffType.ON
        String expectedChannel = CHANNEL_STATE

        testOnValueRecevied(expectedChannel, expectedStateType, insightParams.toString())
    }

    @Test
    void 'assert that channel LASTONFOR is updated on recevied value' (){
        insightParams.lastOnFor = TIME_PARAM
        State expectedStateType = new DecimalType(TIME_PARAM)
        String expectedChannel = CHANNEL_LASTONFOR

        testOnValueRecevied(expectedChannel, expectedStateType, insightParams.toString())
    }

    @Test
    void 'assert that channel ONTODAY is updated on recevied value' (){
        insightParams.onToday = TIME_PARAM
        State expectedStateType = new DecimalType(TIME_PARAM)
        String expectedChannel = CHANNEL_ONTODAY

        testOnValueRecevied(expectedChannel, expectedStateType, insightParams.toString())
    }

    @Test
    void 'assert that channel ONTOTAL is updated on recevied value' (){
        insightParams.onTotal = TIME_PARAM
        State expectedStateType = new DecimalType(TIME_PARAM)
        String expectedChannel = CHANNEL_ONTOTAL

        testOnValueRecevied(expectedChannel, expectedStateType, insightParams.toString())
    }
    @Test
    void 'assert that channel TIMESPAN is updated on recevied value' (){
        insightParams.timespan = TIME_PARAM
        State expectedStateType = new DecimalType(TIME_PARAM)
        String expectedChannel = CHANNEL_TIMESPAN

        testOnValueRecevied(expectedChannel, expectedStateType, insightParams.toString())
    }
    @Test
    void 'assert that channel AVERAGEPOWER is updated on recevied value' (){
        insightParams.avgPower = POWER_PARAM
        State expectedStateType = new DecimalType(POWER_PARAM)
        String expectedChannel = CHANNEL_AVERAGEPOWER

        testOnValueRecevied(expectedChannel, expectedStateType, insightParams.toString())
    }

    void testOnValueRecevied(String expectedChannel, State expectedState, String insightParams) {
        handler = new MockWemoHandler(thing, expectedChannel);

        handler.onValueReceived(PARAMS_NAME, insightParams, SERVICE_ID);
        assertThat handler.channelState, is(notNullValue())
        assertThat "Incorrect channel state for channel {$expectedChannel} on received value {$insightParams}.", handler.channelState, is(expectedState)
    }

    class MockWemoHandler extends WemoHandler {
        def channelState
        def channelToWatch

        public MockWemoHandler(Thing thing, String channelToWatch) {
            super(thing, null);
            this.channelToWatch = channelToWatch
        }

        @Override
        protected void updateState(String channelID, State channelState) {
            if(channelID.equals(channelToWatch)) {
                this.channelState = channelState
            }
        }

        @Override
        protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, String description) {
        }

        @Override
        protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail) {
        }

        @Override
        protected void updateStatus(ThingStatus status) {
            
        }
    }

    class WemoInsightParams {
        int state, lastChangedAt, lastOnFor, onToday, onTotal, timespan, avgPower, currPower, todayEnergy, totalEnergy, standbyLimit

        @Override
        public String toString() {
            // Example string looks like "1|1427230660|4702|25528|82406|1209600|39|40880|15620649|54450534.000000|8000"
            return "$state|$lastChangedAt|$lastOnFor|$onToday|$onTotal|$timespan|$avgPower|$currPower|$todayEnergy|$totalEnergy|$standbyLimit"
        }
    }
}
