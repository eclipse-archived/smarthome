/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
