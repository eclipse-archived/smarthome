package org.eclipse.smarthome.io.transport.bluetooth.bluez.internal.dbus;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;

@DBusInterfaceName("org.bluez.AgentManager1")
public interface AgentManager1 extends DBusInterface {

    public void RegisterAgent(DBusInterface agent, String capability);

    public void UnregisterAgent(DBusInterface agent);

    public void RequestDefaultAgent(DBusInterface agent);
}
