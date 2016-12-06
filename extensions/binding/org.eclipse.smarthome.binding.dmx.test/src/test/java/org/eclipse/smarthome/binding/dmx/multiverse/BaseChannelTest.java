/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.dmx.multiverse;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.eclipse.smarthome.binding.dmx.internal.multiverse.BaseChannel;
import org.junit.Test;

/**
 * Tests cases for BaseChannel
 *
 * @author Jan N. Klug - Initial contribution
 */
public class BaseChannelTest {

    @Test
    public void creatingBaseChannelFromIntegers() {
        // overrange
        BaseChannel channel = new BaseChannel(0, 600);
        assertThat(channel.getChannelId(), is(BaseChannel.MAX_CHANNEL_ID));

        // underrange
        channel = new BaseChannel(0, -1);
        assertThat(channel.getChannelId(), is(BaseChannel.MIN_CHANNEL_ID));

        // inrange & universe
        channel = new BaseChannel(5, 100);
        assertThat(channel.getChannelId(), is(100));
        assertThat(channel.getUniverseId(), is(5));

        // set universe
        channel.setUniverseId(1);
        assertThat(channel.getUniverseId(), is(1));
    }

    @Test
    public void creatingBaseChannelfromBaseChannel() {
        BaseChannel baseChannel = new BaseChannel(5, 100);
        BaseChannel copyChannel = new BaseChannel(baseChannel);

        assertThat(copyChannel.getChannelId(), is(100));
        assertThat(copyChannel.getUniverseId(), is(5));
    }

    @Test
    public void comparingChannels() {
        BaseChannel channel1 = new BaseChannel(5, 100);
        BaseChannel channel2 = new BaseChannel(7, 140);

        assertThat(channel1.compareTo(channel2), is(-1));
        assertThat(channel2.compareTo(channel1), is(1));
        assertThat(channel1.compareTo(channel1), is(0));
    }

    @Test
    public void stringConversion() {
        // to string
        BaseChannel baseChannel = new BaseChannel(5, 100);
        assertThat(baseChannel.toString(), is(equalTo("5:100")));

        // single channel from string with universe
        String parseString = new String("2:100");
        List<BaseChannel> channelList = BaseChannel.fromString(parseString, 0);
        assertThat(channelList.size(), is(1));
        assertThat(channelList.get(0).toString(), is(equalTo("2:100")));

        // single channel from string without universe
        parseString = new String("100");
        channelList = BaseChannel.fromString(parseString, 2);
        assertThat(channelList.size(), is(1));
        assertThat(channelList.get(0).toString(), is(equalTo("2:100")));

        // two channels with channel width
        parseString = new String("100/2");
        channelList = BaseChannel.fromString(parseString, 2);
        assertThat(channelList.size(), is(2));
        assertThat(channelList.get(0).toString(), is(equalTo("2:100")));
        assertThat(channelList.get(1).toString(), is(equalTo("2:101")));

        // to channels with comma
        parseString = new String("100,102");
        channelList = BaseChannel.fromString(parseString, 2);
        assertThat(channelList.size(), is(2));
        assertThat(channelList.get(0).toString(), is(equalTo("2:100")));
        assertThat(channelList.get(1).toString(), is(equalTo("2:102")));

    }

}
