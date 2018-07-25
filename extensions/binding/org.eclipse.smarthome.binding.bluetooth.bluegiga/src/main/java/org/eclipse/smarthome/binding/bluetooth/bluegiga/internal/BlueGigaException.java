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
package org.eclipse.smarthome.binding.bluetooth.bluegiga.internal;

/**
 * A runtime exception used in the internal code of this bundle.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
public class BlueGigaException extends RuntimeException {

    private static final long serialVersionUID = 58882813509800169L;

    public BlueGigaException() {
        super();
    }

    public BlueGigaException(String message) {
        super(message);
    }

    public BlueGigaException(String message, Throwable cause) {
        super(message, cause);
    }

}
