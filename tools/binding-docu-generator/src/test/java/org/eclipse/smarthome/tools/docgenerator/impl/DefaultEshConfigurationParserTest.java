package org.eclipse.smarthome.tools.docgenerator.impl;

import org.apache.maven.plugin.logging.Log;
import org.eclipse.smarthome.tools.docgenerator.EshConfigurationParser;
import org.eclipse.smarthome.tools.docgenerator.ParserException;
import org.eclipse.smarthome.tools.docgenerator.data.*;
import org.eclipse.smarthome.tools.docgenerator.models.*;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class DefaultEshConfigurationParserTest {
    @Test
    public void testParseEshConfiguration() throws ParserException {
        Path eshPath = Paths.get("src/test/resources/testESH-INF/");

        EshConfigurationParser parser = new DefaultEshConfigurationParser(mock(Log.class));

        ConfigurationParseResult result = parser.parseEshConfiguration(eshPath);

        assertNotNull(result);
        assertNotNull(result.getBinding());
        assertNotNull(result.getBridges());
        assertNotNull(result.getChannelGroups());
        assertNotNull(result.getChannels());
        assertNotNull(result.getConfigList());
        assertNotNull(result.getThings());

        Binding binding = result.getBinding();
        assertThat(binding.id(), is("binding-1"));
        assertThat(binding.description(), is("This is the binding number one"));
        assertThat(binding.author(), is("Author #1"));
        assertThat(binding.name(), is("Binding #1"));

        BridgeList bridgeList = result.getBridges();
        assertThat(bridgeList.size(), is(1));
        assertThat(bridgeList.get(0).getModel(), instanceOf(Bridge.class));
        Bridge bridge = (Bridge) bridgeList.get(0).getModel();
        assertThat(bridge.label(), is("Bridge #1"));
        assertThat(bridge.description(), is("The bridge number one."));
        assertThat(bridge.id(), is("bridge"));

        ChannelGroupList channelGroupList = result.getChannelGroups();
        assertThat(channelGroupList.size(), is(0));

        ChannelList channelList = result.getChannels();
        assertThat(channelList.size(), is(18));
        assertThat(channelList.get(0).getModel(), instanceOf(Channel.class));
        Channel channel = (Channel) channelList.get(0).getModel();
        assertThat(channel.id(), is("roomSetpoint"));
        assertThat(channel.label(), is("Room setpoint"));
        assertThat(channel.description(), is("The room temperature setpoint."));
        assertThat(channel.category(), is("Temperature"));
        assertThat(channel.itemType(), is("Number"));
        assertFalse(channel.state().readOnly());

        ConfigurationList configurationList = result.getConfigList();
        assertThat(configurationList.size(), is(0));

        ThingList thingList = result.getThings();
        assertThat(thingList.size(), is(2));
        assertThat(thingList.get(0).getModel(), instanceOf(Thing.class));
        Thing thing = (Thing) thingList.get(0).getModel();
        assertThat(thing.id(), is("thermostat"));
        assertThat(thing.label(), is("Thermostat"));
        assertThat(thing.description(), is("An thermostat."));
        assertThat(thing.channels().size(), is(16));
    }
}
