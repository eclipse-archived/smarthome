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
package org.eclipse.smarthome.binding.mqtt.internal.ssl;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Implement this to be notified by the {@link PinTrustManager} if a connection was
 * accepted or denied and if a Pin switched from learning mode to checking mode.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface PinnedCallback {
    /**
     * A public key or certificate hash has been learned. The given pin can be switched
     * to checking mode now.
     *
     * @param pin Public Key or Certificate pin
     */
    void pinnedLearnedHash(Pin pin);

    /**
     * A connection has been accepted
     */
    void pinnedConnectionAccepted();

    /**
     * A connection has been denied
     *
     * @param pin The pin object that denied the connection
     */
    void pinnedConnectionDenied(Pin pin);
}