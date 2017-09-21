/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.sonos.internal.config;

/**
 *
 * @author Karel Goderis - Initial contribution
 */
public class ZonePlayerConfiguration {

    public static final String UDN = "udn";
    public static final String REFRESH = "refresh";
    public static final String NOTIFICATION_TIMEOUT = "notificationTimeout";

    public String udn;
    public Integer refresh;
    public Integer notificationTimeout;

}
