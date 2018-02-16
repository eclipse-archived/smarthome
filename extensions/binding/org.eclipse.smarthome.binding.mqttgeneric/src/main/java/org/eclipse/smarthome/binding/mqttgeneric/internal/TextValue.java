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
package org.eclipse.smarthome.binding.mqttgeneric.internal;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

/**
 * Implements a text/string value.
 *
 * @author David Graeff - Initial contribution
 */
public class TextValue implements AbstractMqttThingValue {
    private StringType strValue;

    public TextValue() {
        strValue = new StringType();
    }

    public TextValue(String text) {
        strValue = new StringType(text);
    }

    @Override
    public State getValue() {
        return strValue;
    }

    @Override
    public String update(Command command) throws IllegalArgumentException {
        strValue = new StringType(command.toString());
        return strValue.toString();
    }

    @Override
    public State update(String updatedValue) throws IllegalArgumentException {
        strValue = new StringType(updatedValue);
        return strValue;
    }

}
