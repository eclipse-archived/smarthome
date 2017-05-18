/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.dbus.internal;

import org.freedesktop.dbus.DBusConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a temporary class as long as the transport bundle does not bring any function itself to depend on the D-Bus
 * packages.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public class Dependency {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void depend() {
        logger.trace("Ensure DBusConnection class ({}) is available.", DBusConnection.class);
    }

}
