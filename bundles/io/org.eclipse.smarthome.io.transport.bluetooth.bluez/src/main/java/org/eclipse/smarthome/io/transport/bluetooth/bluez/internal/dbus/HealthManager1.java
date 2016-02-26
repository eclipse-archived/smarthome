package org.eclipse.smarthome.io.transport.bluetooth.bluez.internal.dbus;

import java.util.Map;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;
import org.freedesktop.dbus.Variant;

@DBusInterfaceName("org.bluez.HealthManager1")
public interface HealthManager1 extends DBusInterface {

    public DBusInterface CreateApplication(Map<String, Variant> config);

    public void DestroyApplication(DBusInterface application);
}
