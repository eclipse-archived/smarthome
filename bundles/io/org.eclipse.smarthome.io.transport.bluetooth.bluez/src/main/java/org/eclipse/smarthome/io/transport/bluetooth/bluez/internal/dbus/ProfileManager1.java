package org.eclipse.smarthome.io.transport.bluetooth.bluez.internal.dbus;

import java.util.Map;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;
import org.freedesktop.dbus.Variant;

@DBusInterfaceName("org.bluez.ProfileManager1")
public interface ProfileManager1 extends DBusInterface {

    public void RegisterProfile(DBusInterface profile, String UUID, Map<String, Variant> options);

    public void UnregisterProfile(DBusInterface profile);
}
