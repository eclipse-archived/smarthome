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
package org.eclipse.smarthome.core.net;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This is an interface for listeners who wants to be notified for the change of network address.
 * There are only network address adds, and removes; it makes no effort to correlate
 * which existing network is changed. Listeners should register themselves
 * with this interface as a service.
 *
 * @see NetUtil
 *
 * @author Gary Tse - Initial contribution
 */
@NonNullByDefault
public interface NetworkAddressChangeListener {

    /**
     * When network address is changed, listeners will be notified by this method.
     * When a network interface changes from "up" to "down", it is considered as "removed".
     * When a "loopback" or "down" interface is added, the listeners are not notified.
     *
     * @param added Unmodifiable list of recently added network addresses
     * @param removed Unmodifiable list of recently removed network addresses
     */
    void onChanged(List<CidrAddress> added, List<CidrAddress> removed);

}
