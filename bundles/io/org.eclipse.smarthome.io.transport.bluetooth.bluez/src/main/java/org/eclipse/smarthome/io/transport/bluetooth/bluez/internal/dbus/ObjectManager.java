/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth.bluez.internal.dbus;

import java.util.List;
import java.util.Map;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;

@DBusInterfaceName("org.freedesktop.DBus.ObjectManager")
public interface ObjectManager extends DBusInterface {

    public static class InterfacesAdded extends DBusSignal {

        public final DBusInterface object_path;
        public final Map<String, Map<String, Variant>> interfaces_and_properties;

        public InterfacesAdded(String path, DBusInterface object_path,
                Map<String, Map<String, Variant>> interfaces_and_properties) throws DBusException {
            super(path, object_path, interfaces_and_properties);
            this.object_path = object_path;
            this.interfaces_and_properties = interfaces_and_properties;
        }
    }

    public static class InterfacesRemoved extends DBusSignal {

        public final DBusInterface object_path;
        public final List<String> interfaces;

        public InterfacesRemoved(String path, DBusInterface object_path, List<String> interfaces) throws DBusException {
            super(path, object_path, interfaces);
            this.object_path = object_path;
            this.interfaces = interfaces;
        }
    }

    public Map<Path, Map<String, Map<String, Variant>>> GetManagedObjects();
}
