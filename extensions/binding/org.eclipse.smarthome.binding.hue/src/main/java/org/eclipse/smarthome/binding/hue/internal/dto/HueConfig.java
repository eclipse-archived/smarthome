/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.hue.internal.dto;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Detailed bridge info available if authenticated.
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding, minor code cleanup
 */
@NonNullByDefault
public class HueConfig {
    public String name = "";
    public String swversion = "";
    public String mac = "";
    public String bridgeid = "";
    public boolean dhcp = false;
    public String ipaddress = "";
    public String netmask = "";
    public String gateway = "";
    public @Nullable String proxyaddress;
    public int proxyport = 0;
    public Date UTC = Date.from(Instant.now());
    public boolean linkbutton = false;
    public Map<String, User> whitelist = Collections.emptyMap();
    public SoftwareUpdate swupdate = new SoftwareUpdate();
}
