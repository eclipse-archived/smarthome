package org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homeassistant;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.junit.Test;

public class HaIDTests {

    @Test
    public void testWithoutNode() {
        HaID subject = new HaID("homeassistant/switch/name/config");

        assertThat(subject.getThingID(), is("name"));
        assertThat(subject.getChannelGroupTypeID(), is("name_switch"));
        assertThat(subject.getChannelTypeID("channel"), is(new ChannelTypeUID("mqtt:name_switch_channel")));
        assertThat(subject.getChannelGroupID(), is("switch_"));
    }

    @Test
    public void testWithNode() {
        HaID subject = new HaID("homeassistant/switch/node/name/config");

        assertThat(subject.getThingID(), is("name"));
        assertThat(subject.getChannelGroupTypeID(), is("name_switchnode"));
        assertThat(subject.getChannelTypeID("channel"), is(new ChannelTypeUID("mqtt:name_switchnode_channel")));
        assertThat(subject.getChannelGroupID(), is("switch_node"));
    }

}
