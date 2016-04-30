package org.eclipse.smarthome.io.transport.bluetooth.bluez.internal.dbus;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;
import org.freedesktop.dbus.UInt16;

@DBusInterfaceName("org.bluez.Alert1")
public interface Alert1 extends DBusInterface {

    public void RegisterAlert(String category, DBusInterface agent);

    public void NewAlert(String category, UInt16 count, String description);

    public void UnreadAlert(String category, UInt16 count);
}
