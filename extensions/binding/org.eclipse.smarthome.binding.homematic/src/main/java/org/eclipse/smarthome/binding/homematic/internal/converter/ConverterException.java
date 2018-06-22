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
package org.eclipse.smarthome.binding.homematic.internal.converter;

/**
 * Exception if something goes wrong when converting values between openHAB and the binding.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class ConverterException extends Exception {
    private static final long serialVersionUID = 78045670450002L;

    public ConverterException(String message) {
        super(message);
    }

}
