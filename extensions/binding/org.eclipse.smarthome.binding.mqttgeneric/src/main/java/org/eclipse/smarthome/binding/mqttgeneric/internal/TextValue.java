/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    StringType strValue;

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
