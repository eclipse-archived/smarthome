/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.wemo.internal.handler.test;

import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import org.eclipse.smarthome.binding.wemo.WemoBindingConstants;
import org.eclipse.smarthome.binding.wemo.handler.WemoHandler;
import org.eclipse.smarthome.binding.wemo.internal.http.WemoHttpCall;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.State;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link WemoHandler}.
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Stefan Triller - Ported Tests from Groovy to Java
 */
public class WemoHandlerTest {

    private final ThingTypeUID THING_TYPE = WemoBindingConstants.THING_TYPE_INSIGHT;
    private final String THING_ID = "test";

    private MockWemoHandler handler;

    private final String SERVICE_ID = "insight";
    private final String PARAMS_NAME = "InsightParams";
    private WemoInsightParams insightParams;

    /** Used for all tests, where expected value is time in seconds **/
    private final int TIME_PARAM = 4702;

    /** Represents a state parameter, where 1 stays for ON and 0 stays for OFF **/
    private final int STATE_PARAM = 1;

    /** Represents power in Wats **/
    private final int POWER_PARAM = 54;

    private final Thing thing = mock(Thing.class);

    @Before
    public void setUp() {
        insightParams = new WemoInsightParams();
        when(thing.getUID()).thenReturn(new ThingUID(THING_TYPE, THING_ID));
        when(thing.getThingTypeUID()).thenReturn(THING_TYPE);
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);
    }

    @After
    public void clear() {
        handler.channelState = null;
        handler.channelToWatch = null;
    }

    @Test
    public void assertThatChannelSTATEisUpdatedOnReceivedValue() {
        insightParams.state = STATE_PARAM;
        State expectedStateType = OnOffType.ON;
        String expectedChannel = CHANNEL_STATE;

        testOnValueReceived(expectedChannel, expectedStateType, insightParams.toString());
    }

    @Test
    public void assertThatChannelLASTONFORIsUpdatedOnReceivedValue() {
        insightParams.lastOnFor = TIME_PARAM;
        State expectedStateType = new DecimalType(TIME_PARAM);
        String expectedChannel = CHANNEL_LASTONFOR;

        testOnValueReceived(expectedChannel, expectedStateType, insightParams.toString());
    }

    @Test
    public void assertThatChannelONTODAYIsUpdatedOnReceivedValue() {
        insightParams.onToday = TIME_PARAM;
        State expectedStateType = new DecimalType(TIME_PARAM);
        String expectedChannel = CHANNEL_ONTODAY;

        testOnValueReceived(expectedChannel, expectedStateType, insightParams.toString());
    }

    @Test
    public void assertThatChannelONTOTALIsUpdatedOnReceivedValue() {
        insightParams.onTotal = TIME_PARAM;
        State expectedStateType = new DecimalType(TIME_PARAM);
        String expectedChannel = CHANNEL_ONTOTAL;

        testOnValueReceived(expectedChannel, expectedStateType, insightParams.toString());
    }

    @Test
    public void assertThatChannelTIMESPANIsUpdatedOnReceivedValue() {
        insightParams.timespan = TIME_PARAM;
        State expectedStateType = new DecimalType(TIME_PARAM);
        String expectedChannel = CHANNEL_TIMESPAN;

        testOnValueReceived(expectedChannel, expectedStateType, insightParams.toString());
    }

    @Test
    public void assertThatChannelAVERAGEPOWERIsUpdatedOnReceivedValue() {
        insightParams.avgPower = POWER_PARAM;
        State expectedStateType = new DecimalType(POWER_PARAM);
        String expectedChannel = CHANNEL_AVERAGEPOWER;

        testOnValueReceived(expectedChannel, expectedStateType, insightParams.toString());
    }

    private void testOnValueReceived(String expectedChannel, State expectedState, String insightParams) {
        handler = new MockWemoHandler(thing, expectedChannel);

        handler.onValueReceived(PARAMS_NAME, insightParams, SERVICE_ID);
        assertThat(handler.channelState, is(notNullValue()));
        assertThat(handler.channelState, is(expectedState));
    }

    class MockWemoHandler extends WemoHandler {
        State channelState;
        String channelToWatch;

        public MockWemoHandler(Thing thing, String channelToWatch) {
            super(thing, null, new WemoHttpCall());
            this.channelToWatch = channelToWatch;
        }

        @Override
        protected void updateState(String channelID, State channelState) {
            if (channelID.equals(channelToWatch)) {
                this.channelState = channelState;
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
        int state, lastChangedAt, lastOnFor, onToday, onTotal, timespan, avgPower, currPower, todayEnergy, totalEnergy,
                standbyLimit;

        @Override
        public String toString() {
            // Example string looks like "1|1427230660|4702|25528|82406|1209600|39|40880|15620649|54450534.000000|8000"
            return state + "|" + lastChangedAt + "|" + lastOnFor + "|" + onToday + "|" + onTotal + "|" + timespan + "|"
                    + avgPower + "|" + currPower + "|" + todayEnergy + "|" + totalEnergy + "|" + standbyLimit;
        }
    }
}
