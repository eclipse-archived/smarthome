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
package org.eclipse.smarthome.core.net;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface that provides access to configured network addresses
 *
 * @author Stefan Triller - initial contribution
 *
 */
public interface NetworkAddressService {

    /**
     * Returns the user configured primary IPv4 address of the system
     *
     * @return IPv4 address as a String in format xxx.xxx.xxx.xxx or
     *         <code>null</code> if there is no interface or an error occurred
     */
    @Nullable
    String getPrimaryIpv4HostAddress();
}
