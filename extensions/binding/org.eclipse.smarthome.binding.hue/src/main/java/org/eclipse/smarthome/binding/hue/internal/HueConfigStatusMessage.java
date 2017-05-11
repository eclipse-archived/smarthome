/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal;

import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;

/**
 * The {@link HueConfigStatusMessage} defines
 * the keys to be used for {@link ConfigStatusMessage}s.
 *
 * @author Alexander Kostadinov - Initial contribution
 * @author Kai Kreuzer - Changed from enum to interface
 *
 */
public interface HueConfigStatusMessage {

    final static String IP_ADDRESS_MISSING = "missing-ip-address-configuration";

}
