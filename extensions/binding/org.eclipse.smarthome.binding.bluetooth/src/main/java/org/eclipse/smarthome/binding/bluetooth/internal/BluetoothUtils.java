package org.eclipse.smarthome.binding.bluetooth.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.smarthome.binding.bluetooth.BluetoothBindingConstants;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.sputnikdev.bluetooth.URL;
import org.sputnikdev.bluetooth.manager.transport.CharacteristicAccessType;

/**
 * @author Vlad Kolotov
 */
public class BluetoothUtils {

    private static final String MAC_PART_REGEXP = "(\\w{2}(?=(\\w{2})))";

    private BluetoothUtils() {}

    public static ThingUID getAdapterUID(URL url) {
        return new ThingUID(BluetoothBindingConstants.THING_TYPE_ADAPTER,
                BluetoothUtils.getUID(url.getAdapterAddress()));
    }

    public static ThingUID getGenericDeviceUID(URL url) {
        return new ThingUID(BluetoothBindingConstants.THING_TYPE_GENERIC, getAdapterUID(url),
                BluetoothUtils.getUID(url.getDeviceAddress()));
    }

    public static ThingUID getBleDeviceUID(URL url) {
        return new ThingUID(BluetoothBindingConstants.THING_TYPE_BLE, getAdapterUID(url),
                BluetoothUtils.getUID(url.getDeviceAddress()));
    }

    public static URL getURL(Thing thing) {
        if (BluetoothBindingConstants.THING_TYPE_ADAPTER.equals(thing.getThingTypeUID())) {
            return new URL(getAddressFromUID(thing.getUID().getId()), null);
        } else {
            String adapterAddress = Optional.of(thing).map(Thing::getBridgeUID).map(ThingUID::getId)
                    .map(BluetoothUtils::getAddressFromUID).orElse(null);
            return new URL(adapterAddress, getAddressFromUID(thing.getUID().getId()));
        }
    }

    public static String getChannelUID(URL url) {
        String channelUID = (getShortUUID(url.getServiceUUID()) +
                "-" + getShortUUID(url.getCharacteristicUUID()));

        if (url.getFieldName() != null) {
            channelUID += "-" + url.getFieldName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        }

        return channelUID;
    }

    public static String getItemUID(URL url) {
        return BluetoothBindingConstants.THING_TYPE_BLE.getAsString().replace(":", "_") + "_" +
                getUID(url.getAdapterAddress()) + "_" + getUID(url.getDeviceAddress()) + "_" +
                getChannelUID(url).replaceAll("-", "_");
    }

    public static boolean hasNotificationAccess(Set<CharacteristicAccessType> flags) {
        return flags.contains(CharacteristicAccessType.NOTIFY) ||
                flags.contains(CharacteristicAccessType.INDICATE);
    }

    public static boolean hasReadAccess(Set<CharacteristicAccessType> flags) {
        return flags.contains(CharacteristicAccessType.READ);
    }

    public static boolean hasWriteAccess(Set<CharacteristicAccessType> flags) {

        return flags.contains(CharacteristicAccessType.WRITE) ||
                flags.contains(CharacteristicAccessType.WRITE_WITHOUT_RESPONSE);
    }

    private static String getUID(String address) {
        return address.replaceAll("/", "").replaceAll(":", "");
    }

    private static String getShortUUID(String longUUID) {
        if (longUUID.length() < 8) {
            return longUUID;
        }
        return Long.toHexString(Long.valueOf(longUUID.substring(0, 8), 16)).toUpperCase();
    }

    private static String getAddressFromUID(String uid) {
        return uid.replaceAll(MAC_PART_REGEXP, "$1:");
    }
}
