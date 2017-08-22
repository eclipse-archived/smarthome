package org.eclipse.smarthome.binding.bluetooth.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;

/**
 * @author Vlad Kolotov
 */
class IntegerTypeChannelHandler extends SingleChannelHandler<Integer, DecimalType> {

    IntegerTypeChannelHandler(BluetoothHandler handler, String channelID, boolean persistent) {
        super(handler, channelID, persistent);
    }

    IntegerTypeChannelHandler(BluetoothHandler handler, String channelID) {
        super(handler, channelID);
    }

    @Override Integer convert(DecimalType value) {
        return value != null ? value.intValue() : null;
    }

    @Override DecimalType convert(Integer value) {
        return new DecimalType(value);
    }
}
