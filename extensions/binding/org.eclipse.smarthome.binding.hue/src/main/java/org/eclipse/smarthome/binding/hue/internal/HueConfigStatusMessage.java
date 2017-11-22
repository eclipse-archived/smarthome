/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
