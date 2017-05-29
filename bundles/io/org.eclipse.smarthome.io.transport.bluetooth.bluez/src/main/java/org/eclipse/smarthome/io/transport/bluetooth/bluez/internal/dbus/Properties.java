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
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;

@DBusInterfaceName("org.freedesktop.DBus.Properties")
public interface Properties extends DBusInterface {

    /**
     * Properties changed signal.
     *
     * @author Chris Jackson
     */
    public static class PropertiesChanged extends DBusSignal {
        public final String interface_name;
        public final Map<String, Variant> changed_properties;
        public final List<String> invalidated_properties;

        public PropertiesChanged(String path, String interface_name, Map<String, Variant> changed_properties,
                List<String> invalidated_properties) throws DBusException {
            super(path, changed_properties, invalidated_properties);
            this.interface_name = interface_name;
            this.changed_properties = changed_properties;
            this.invalidated_properties = invalidated_properties;
        }
    }
}
