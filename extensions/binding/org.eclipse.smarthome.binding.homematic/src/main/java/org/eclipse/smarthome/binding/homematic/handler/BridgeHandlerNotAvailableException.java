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
package org.eclipse.smarthome.binding.homematic.handler;

/**
 * Exception if the BridgeHandler is not available.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class BridgeHandlerNotAvailableException extends Exception {
    private static final long serialVersionUID = 95628391238530L;

    public BridgeHandlerNotAvailableException(String message) {
        super(message);
    }

}
