package org.eclipse.smarthome.binding.mqttgeneric.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;

public interface ChannelStateUpdateListener {
    void channelStateUpdated(ChannelUID channelUID, State value);
}
