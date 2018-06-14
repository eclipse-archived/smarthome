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
package org.eclipse.smarthome.binding.homematic.internal.converter.type;

import static org.eclipse.smarthome.binding.homematic.internal.misc.HomematicConstants.DATAPOINT_NAME_SENSOR;

import org.eclipse.smarthome.binding.homematic.internal.converter.ConverterException;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapoint;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.Type;

/**
 * Converts between a Homematic datapoint value and a openHAB OpenClosedType.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class OpenClosedTypeConverter extends AbstractTypeConverter<OpenClosedType> {
    @Override
    protected boolean toBindingValidation(HmDatapoint dp, Class<? extends Type> typeClass) {
        return dp.isBooleanType() && typeClass.isAssignableFrom(OpenClosedType.class);
    }

    @Override
    protected Object toBinding(OpenClosedType type, HmDatapoint dp) throws ConverterException {
        return (type == OpenClosedType.CLOSED ? Boolean.FALSE : Boolean.TRUE) != isInvert(dp);
    }

    @Override
    protected boolean fromBindingValidation(HmDatapoint dp) {
        return dp.isBooleanType() && dp.getValue() instanceof Boolean;
    }

    @Override
    protected OpenClosedType fromBinding(HmDatapoint dp) throws ConverterException {
        return (((Boolean) dp.getValue()) == Boolean.FALSE) != isInvert(dp) ? OpenClosedType.CLOSED
                : OpenClosedType.OPEN;
    }

    /**
     * Invert only values which are not from a sensor or a state from some devices.
     */
    private boolean isInvert(HmDatapoint dp) {
        return !DATAPOINT_NAME_SENSOR.equals(dp.getName()) && !isStateInvertDatapoint(dp);
    }

}
