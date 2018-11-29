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
package org.eclipse.smarthome.binding.dmx.multiverse;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.smarthome.binding.dmx.handler.DimmerThingHandler;
import org.eclipse.smarthome.binding.dmx.internal.DmxBindingConstants.ListenerType;
import org.eclipse.smarthome.binding.dmx.internal.action.FadeAction;
import org.eclipse.smarthome.binding.dmx.internal.action.ResumeAction;
import org.eclipse.smarthome.binding.dmx.internal.multiverse.DmxChannel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests cases for Channel
 *
 * @author Jan N. Klug - Initial contribution
 */
public class DmxChannelTest {

    private final ChannelUID valueChannelUID = new ChannelUID("dmx:testBridge:testThing:valueChannel");

    DmxChannel dmxChannel;
    DimmerThingHandler dimmerThingHandler;
    long currentTime;

    @Before
    public void setup() {
        dimmerThingHandler = Mockito.mock(DimmerThingHandler.class);
        dmxChannel = new DmxChannel(0, 1, 0);
        dmxChannel.addListener(valueChannelUID, dimmerThingHandler, ListenerType.VALUE);
        dmxChannel.setValue(0);
        currentTime = System.currentTimeMillis();
    }

    @Test
    public void checkValueSettingAndReporting() {
        dmxChannel.setValue(125);
        dmxChannel.getNewValue(currentTime);

        assertThat(dmxChannel.getValue(), is(125));
        Mockito.verify(dimmerThingHandler).updateChannelValue(valueChannelUID, 125);
    }

    @Test
    public void checkLimitsAreObserved() {
        dmxChannel.setValue(300);
        assertThat(dmxChannel.getValue(), is(DmxChannel.MAX_VALUE));

        dmxChannel.setValue(-1);
        assertThat(dmxChannel.getValue(), is(DmxChannel.MIN_VALUE));
    }

    @Test
    public void setAndClearAction() {
        // has action
        dmxChannel.setChannelAction(new FadeAction(0, 100, -1));
        assertThat(dmxChannel.hasRunningActions(), is(true));

        // clear action
        dmxChannel.clearAction();
        assertThat(dmxChannel.hasRunningActions(), is(false));
    }

    @Test
    public void checkSingleFadeAction() {
        dmxChannel.addChannelAction(new FadeAction(1000, 243, -1));

        dmxChannel.getNewValue(currentTime);
        assertThat(dmxChannel.hasRunningActions(), is(true));
        Mockito.verify(dimmerThingHandler).updateChannelValue(valueChannelUID, 0);

        dmxChannel.getNewValue(currentTime + 1000);
        assertThat(dmxChannel.hasRunningActions(), is(false));
        Mockito.verify(dimmerThingHandler).updateChannelValue(valueChannelUID, 243);
    }

    @Test
    public void checkMultipleInfiniteFadeAction() {
        dmxChannel.addChannelAction(new FadeAction(1000, 243, 0));
        dmxChannel.addChannelAction(new FadeAction(1000, 127, 0));

        dmxChannel.getNewValue(currentTime);
        assertThat(dmxChannel.hasRunningActions(), is(true));
        Mockito.verify(dimmerThingHandler).updateChannelValue(valueChannelUID, 0);

        // check first action completes
        dmxChannel.getNewValue(currentTime);
        currentTime += 1000;
        dmxChannel.getNewValue(currentTime);
        assertThat(dmxChannel.hasRunningActions(), is(true));
        Mockito.verify(dimmerThingHandler).updateChannelValue(valueChannelUID, 243);

        // check second action completes
        dmxChannel.getNewValue(currentTime);
        currentTime += 1000;
        dmxChannel.getNewValue(currentTime);
        assertThat(dmxChannel.hasRunningActions(), is(true));
        Mockito.verify(dimmerThingHandler).updateChannelValue(valueChannelUID, 127);

        // check first action completes again
        currentTime += 1000;
        dmxChannel.getNewValue(currentTime);
        assertThat(dmxChannel.hasRunningActions(), is(true));
        Mockito.verify(dimmerThingHandler).updateChannelValue(valueChannelUID, 243);
    }

    @Test
    public void checkFadeActionWithResume() {
        dmxChannel.setValue(127);
        dmxChannel.suspendAction();

        dmxChannel.addChannelAction(new FadeAction(1000, 243, 0));
        dmxChannel.addChannelAction(new ResumeAction());

        dmxChannel.getNewValue(currentTime);
        assertThat(dmxChannel.hasRunningActions(), is(true));
        Mockito.verify(dimmerThingHandler).updateChannelValue(valueChannelUID, 127);

        // check action completes
        dmxChannel.getNewValue(currentTime);
        currentTime += 1000;
        dmxChannel.getNewValue(currentTime);
        assertThat(dmxChannel.hasRunningActions(), is(true));
        Mockito.verify(dimmerThingHandler).updateChannelValue(valueChannelUID, 243);

        // check state is restored
        dmxChannel.getNewValue(currentTime);
        assertThat(dmxChannel.hasRunningActions(), is(false));
        Mockito.verify(dimmerThingHandler).updateChannelValue(valueChannelUID, 127);
    }

}
