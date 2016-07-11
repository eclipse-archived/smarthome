package org.eclipse.smarthome.tools.docgenerator.models;

import org.eclipse.smarthome.tools.docgenerator.data.*;
import org.eclipse.smarthome.tools.docgenerator.schemas.*;

public class ConfigurationParseResult {

    private final ChannelList channels;
    private final ThingList things;
    private final ChannelGroupList channelGroups;
    private final BridgeList bridges;
    private final ConfigurationList configList;
    private final Binding binding;

    public ConfigurationParseResult() {
        channels = new ChannelList();
        things = new ThingList();
        channelGroups = new ChannelGroupList();
        bridges = new BridgeList();
        configList = new ConfigurationList();
        binding = new Binding();
    }

    /**
     * Adds the channel type to the channel list.
     *
     * @param channelType the channel type
     */
    public void putChannel(ChannelType channelType) {
        channels.put(channelType);
    }

    /**
     * Adds the thing type to the thing list.
     *
     * @param thingType the thing type
     */
    public void putThing(ThingType thingType) {
        things.put(thingType);
    }

    /**
     * Adds the channel group type to the channel group list.
     *
     * @param channelGroupType the channel group type
     */
    public void putChannelGroup(ChannelGroupType channelGroupType) {
        channelGroups.put(channelGroupType);
    }

    /**
     * Adds the bridge type to the bridge list.
     *
     * @param bridgeType the bridge type
     */
    public void putBridge(BridgeType bridgeType) {
        bridges.put(bridgeType);
    }

    /**
     * Adds the config description to the config list.
     *
     * @param configDescription the config description
     */
    public void putConfigDescription(org.eclipse.smarthome.tools.docgenerator.schemas.ConfigDescription configDescription) {
        configList.put(configDescription);
    }

    /**
     * Sets the binding definition to the result.
     *
     * @param binding the binding object
     */
    public void setBinding(org.eclipse.smarthome.tools.docgenerator.schemas.Binding binding) {
        this.binding.setModel(binding);
    }

    public ChannelList getChannels() {
        return channels;
    }

    public ThingList getThings() {
        return things;
    }

    public ChannelGroupList getChannelGroups() {
        return channelGroups;
    }

    public BridgeList getBridges() {
        return bridges;
    }

    public ConfigurationList getConfigList() {
        return configList;
    }

    public Binding getBinding() {
        return binding;
    }
}
