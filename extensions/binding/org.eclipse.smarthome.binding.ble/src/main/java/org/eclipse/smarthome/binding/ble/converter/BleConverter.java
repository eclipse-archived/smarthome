package org.eclipse.smarthome.binding.ble.converter;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGatt;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattCharacteristic;

/**
 * BluetoothConverter class. Abstract base class for all converters that convert between Bluetooth characteristics and
 * openHAB channels.
 *
 * @author Chris Jackson - Initial Contribution
 */
public abstract class BleConverter {

    private static Map<String, Class<? extends BleConverter>> converters = null;

    /**
     * Perform any periodic polling of this characteristic
     *
     * @param gatt the {@link BluetoothGatt} client to use to send the request.
     * @param characteristic the {@link BluetoothGattCharacteristic} to update
     */
    public void handlePoll(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
    }

    /**
     * Handles the processing of a characteristics data. This is normally called when the characteristic read completes,
     * or the characteristic data updates for some reason.
     *
     * @param characteristic the received {@link BluetoothGattCharacteristic}.
     * @return
     */
    public State handleEvent(BluetoothGattCharacteristic characteristic) {
        return null;
    }

    /**
     * Receives a command from openHAB and translates it to an operation on the Z-Wave network.
     *
     * @param gatt {@link BluetoothGatt} the gatt client to use for sending
     * @param command the {@link Command} to convert
     */
    public void handleCommand(BluetoothGatt gatt, Command command) {
    }

    public static BleConverter getConverter(String converterName) {
        if (converters == null) {
            converters = new HashMap<String, Class<? extends BleConverter>>();

            // converters.put("", ZWaveAlarmConverter.class);
        }

        Constructor<? extends BleConverter> constructor;
        try {
            if (converters.get(converterName) == null) {
                // logger.warn("CommandClass converter {} is not implemented!", commandClass.getLabel());
                return null;
            }
            constructor = converters.get(converterName).getConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            // logger.error("Command processor error");
        }

        return null;
    }
}
