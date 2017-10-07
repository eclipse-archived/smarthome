package org.eclipse.smarthome.binding.bluetooth.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;

/**
 * @author Vlad Kolotov
 */
class BooleanTypeChannelHandler extends SingleChannelHandler<Boolean, OnOffType> {

    BooleanTypeChannelHandler(BluetoothHandler handler, String channelID, boolean persistent) {
        super(handler, channelID, persistent);
    }

    BooleanTypeChannelHandler(BluetoothHandler handler, String channelID) {
        super(handler, channelID);
    }

    @Override Boolean convert(OnOffType value) {
        return value != null && value == OnOffType.ON;
    }

    @Override OnOffType convert(Boolean value) {
        return value != null && value ? OnOffType.ON : OnOffType.OFF;
    }
}
