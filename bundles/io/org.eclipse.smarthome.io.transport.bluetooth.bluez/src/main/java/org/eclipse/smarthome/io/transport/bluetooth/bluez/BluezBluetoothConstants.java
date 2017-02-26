/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth.bluez;

/**
 * The {@link BluezBluetoothConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class BluezBluetoothConstants {

    public final static String BLUEZ_DBUS_CONFIGURATION = "smarthome.bluetooth.bluez.dbus";
    public final static String BLUEZ_DBUS_SERVICE = "org.bluez";
    public final static String BLUEZ_DBUS_PATH = "/org/bluez";

    public final static String BLUEZ_DBUS_INTERFACE_ADAPTER1 = "org.bluez.Adapter1";
    public final static String BLUEZ_DBUS_INTERFACE_DEVICE1 = "org.bluez.Device1";
    public final static String BLUEZ_DBUS_INTERFACE_GATTSERVICE1 = "org.bluez.GattService1";
    public final static String BLUEZ_DBUS_INTERFACE_GATTCHARACTERISTIC1 = "org.bluez.GattCharacteristic1";
    public final static String BLUEZ_DBUS_INTERFACE_GATTDESCRIPTOR1 = "org.bluez.GattDescriptor1";

    public final static String BLUEZ_DBUS_ADAPTER_PROPERTY_NAME = "Name";
    public final static String BLUEZ_DBUS_ADAPTER_PROPERTY_ALIAS = "Alias";
    public final static String BLUEZ_DBUS_ADAPTER_PROPERTY_ADDRESS = "Address";
    public final static String BLUEZ_DBUS_ADAPTER_PROPERTY_CLASS = "Class";
    public final static String BLUEZ_DBUS_ADAPTER_PROPERTY_UUIDS = "UUIDs";
    public final static String BLUEZ_DBUS_ADAPTER_PROPERTY_DISCOVERING = "Discovering";
    public final static String BLUEZ_DBUS_ADAPTER_PROPERTY_DISCOVERABLE = "Discoverable";
    public final static String BLUEZ_DBUS_ADAPTER_PROPERTY_POWERED = "Powered";

    public final static String BLUEZ_DBUS_ADAPTER_FILTER_TRANSPORT = "Transport";
    public final static String BLUEZ_DBUS_ADAPTER_FILTER_PATHLOSS = "Pathloss";
    public final static String BLUEZ_DBUS_ADAPTER_FILTER_RSSI = "RSSI";
    public final static String BLUEZ_DBUS_ADAPTER_FILTER_UUID = "UUIDs";

    public final static String BLUEZ_DBUS_ADAPTER_TRANSPORT_LE = "le";
    public final static String BLUEZ_DBUS_ADAPTER_TRANSPORT_BREDR = "bredr";
    public final static String BLUEZ_DBUS_ADAPTER_TRANSPORT_AUTO = "auto";

    public final static String BLUEZ_DBUS_DEVICE_PROPERTY_NAME = "Name";
    public final static String BLUEZ_DBUS_DEVICE_PROPERTY_ALIAS = "Alias";
    public final static String BLUEZ_DBUS_DEVICE_PROPERTY_ADDRESS = "Address";
    public final static String BLUEZ_DBUS_DEVICE_PROPERTY_ADAPTER = "Adapter";
    public final static String BLUEZ_DBUS_DEVICE_PROPERTY_MANUFACTURER = "ManufacturerData";
    public final static String BLUEZ_DBUS_DEVICE_PROPERTY_CLASS = "Class";
    public final static String BLUEZ_DBUS_DEVICE_PROPERTY_CONNECTED = "Connected";
    public final static String BLUEZ_DBUS_DEVICE_PROPERTY_GATTSERVICES = "GattServices";
    public final static String BLUEZ_DBUS_DEVICE_PROPERTY_PAIRED = "Paired";
    public final static String BLUEZ_DBUS_DEVICE_PROPERTY_TRUSTED = "Trusted";
    public final static String BLUEZ_DBUS_DEVICE_PROPERTY_RSSI = "RSSI";
    public final static String BLUEZ_DBUS_DEVICE_PROPERTY_UUID = "UUIDs";
    public final static String BLUEZ_DBUS_DEVICE_PROPERTY_TXPOWER = "TxPower";

    public final static String BLUEZ_DBUS_GATTSERVICE_PROPERTY_DEVICE = "Device";
    public final static String BLUEZ_DBUS_GATTSERVICE_PROPERTY_UUID = "UUID";
    public final static String BLUEZ_DBUS_GATTSERVICE_PROPERTY_PRIMARY = "Primary";
    public final static String BLUEZ_DBUS_GATTSERVICE_PROPERTY_CHARACTERISTICS = "Characteristics";

    public final static String BLUEZ_DBUS_GATTCHARACTERISTIC_PROPERTY_VALUE = "Value";
    public final static String BLUEZ_DBUS_GATTCHARACTERISTIC_PROPERTY_DESCRIPTORS = "Descriptors";
    public final static String BLUEZ_DBUS_GATTCHARACTERISTIC_PROPERTY_FLAGS = "Flags";
    public final static String BLUEZ_DBUS_GATTCHARACTERISTIC_PROPERTY_NOTIFYING = "Notifying";
    public final static String BLUEZ_DBUS_GATTCHARACTERISTIC_PROPERTY_SERVICE = "Service";
    public final static String BLUEZ_DBUS_GATTCHARACTERISTIC_PROPERTY_UUID = "UUID";

    public final static String BLUEZ_DBUS_GATTDESCRIPTOR_PROPERTY_UUID = "UUID";
    public final static String BLUEZ_DBUS_GATTDESCRIPTOR_PROPERTY_VALUE = "Value";

    public final static Object BLUEZ_DBUS_SIGNAL_PROPERTIESCHANGED = "PropertiesChanged";
    public final static Object BLUEZ_DBUS_SIGNAL_INTERFACESADDED = "InterfacesAdded";

    public static final long bleUuid = 0x800000805f9b34fbL;
}
