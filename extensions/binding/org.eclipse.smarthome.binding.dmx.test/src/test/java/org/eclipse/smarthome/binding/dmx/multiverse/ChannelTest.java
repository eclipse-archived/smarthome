/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.dmx.multiverse;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.smarthome.binding.dmx.internal.action.BaseAction;
import org.eclipse.smarthome.binding.dmx.internal.action.FadeAction;
import org.eclipse.smarthome.binding.dmx.internal.multiverse.Channel;
import org.junit.Test;

/**
 * Tests cases for Channel
 *
 * @author Jan N. Klug - Initial contribution
 */
public class ChannelTest {

    @Test
    public void setAndGetValues() {
        Channel channel = new Channel(0, 1);

        // value is set
        channel.setValue(100);
        assertThat(channel.getValue(), is(100));

        // limits are observed
        channel.setValue(300);
        assertThat(channel.getValue(), is(Channel.MAX_VALUE));

        channel.setValue(-1);
        assertThat(channel.getValue(), is(Channel.MIN_VALUE));
    }

    @Test
    public void setAndClearAction() {
        Channel channel = new Channel(0, 1);
        BaseAction action = new FadeAction(0, 100, -1);

        // has action
        channel.setChannelAction(action);
        assertThat(channel.hasRunningActions(), is(true));

        // clear action
        channel.clearAction();
        assertThat(channel.hasRunningActions(), is(false));
    }

    @Test
    public void suspendAndResumeAction() {
        Channel channel = new Channel(0, 1);
        BaseAction action = new FadeAction(0, 100, -1);

        // has action
        channel.setChannelAction(action);
        channel.setValue(100);
        assertThat(channel.getValue(), is(100));
        assertThat(channel.hasRunningActions(), is(true));

        // suspend with action
        channel.suspendAction();
        channel.clearAction();
        assertThat(channel.isSuspended(), is(true));
        assertThat(channel.hasRunningActions(), is(false));

        // resume
        channel.resumeAction();
        assertThat(channel.isSuspended(), is(false));
        assertThat(channel.hasRunningActions(), is(true));

        // suspend without action
        channel.clearAction();
        channel.suspendAction();
        channel.setValue(200);
        assertThat(channel.getValue(), is(200));

        // resume without action
        channel.resumeAction();
        assertThat(channel.getValue(), is(100));

    }

}
