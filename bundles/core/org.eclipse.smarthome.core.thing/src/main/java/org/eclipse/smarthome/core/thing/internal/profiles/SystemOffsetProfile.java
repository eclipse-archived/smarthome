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
package org.eclipse.smarthome.core.thing.internal.profiles;

import java.math.BigDecimal;

import javax.measure.UnconvertibleException;
import javax.measure.quantity.Dimensionless;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileContext;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.StateProfile;
import org.eclipse.smarthome.core.thing.profiles.SystemProfiles;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tec.uom.se.AbstractUnit;

/**
 * Applies the given parameter "offset" to a QuantityType or DecimalType state
 *
 * @author Stefan Triller - initial contribution
 *
 */
@NonNullByDefault
public class SystemOffsetProfile implements StateProfile {

    private final Logger logger = LoggerFactory.getLogger(SystemOffsetProfile.class);
    private static final String OFFSET_PARAM = "offset";

    private final ProfileCallback callback;
    private final ProfileContext context;

    @Nullable
    private QuantityType<?> offset;

    public SystemOffsetProfile(ProfileCallback callback, ProfileContext context) {
        this.callback = callback;
        this.context = context;

        Object paramValue = this.context.getConfiguration().get(OFFSET_PARAM);
        logger.debug("Configuring profile with {} parameter '{}'", OFFSET_PARAM, paramValue);
        if (paramValue instanceof String) {
            try {
                offset = new QuantityType<>((String) paramValue);
            } catch (IllegalArgumentException e) {
                logger.error("Cannot convert value '{}' of parameter '{}' into a valid offset of type QuantityType.",
                        paramValue, OFFSET_PARAM);
            }
        } else if (paramValue instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) paramValue;
            try {
                offset = new QuantityType<>(bd.toString());
            } catch (IllegalArgumentException e) {
                logger.error("Cannot convert value '{}' of parameter '{}' into a valid offset of type QuantityType.",
                        paramValue, OFFSET_PARAM);
            }
        } else {
            logger.error("Parameter '{}' is not of type String or BigDecimal", OFFSET_PARAM);
        }
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return SystemProfiles.OFFSET;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        callback.handleCommand((Command) applyOffset(state, false));
    }

    @Override
    public void onCommandFromItem(Command command) {
        callback.handleCommand((Command) applyOffset(command, false));
    }

    @Override
    public void onCommandFromHandler(Command command) {
        callback.sendCommand((Command) applyOffset(command, true));
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        callback.sendUpdate((State) applyOffset(state, true));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Type applyOffset(Type state, boolean towardsItem) {
        QuantityType finalOffset = offset;
        if (finalOffset == null) {
            logger.error(
                    "Offset not configured correctly, please make sure it is of type QuantityType, e.g. \"3\", \"-1.4\", \"3.2°C\". Using offset 0 now.");
            return new QuantityType<Dimensionless>("0");
        }

        if (!towardsItem) {
            finalOffset = finalOffset.negate();
        }

        Type result = UnDefType.UNDEF;

        if (state instanceof QuantityType) {
            QuantityType qtState = (QuantityType) state;
            try {
                if (finalOffset.getUnit() == AbstractUnit.ONE) {
                    // allow offsets without unit -> implicitly assume its the same as the one from the state
                    // TODO: do we want this? User would have to look up the original unit on the channel in the
                    // binding...
                    finalOffset = new QuantityType<>(finalOffset.toBigDecimal(), qtState.getUnit());
                }
                result = qtState.add(finalOffset);
            } catch (UnconvertibleException e) {
                logger.warn("Cannot apply offset '{}' to state '{}' because types do not match.", finalOffset, qtState);
            }
        } else if (state instanceof DecimalType && finalOffset.getUnit().equals(AbstractUnit.ONE)) {
            DecimalType decState = (DecimalType) state;
            result = new DecimalType(decState.doubleValue() + finalOffset.doubleValue());
        } else {
            logger.warn(
                    "Offset '{}' has a unit, but the binding only sends states without units (DecimalTypes). Returning original state.",
                    offset);
            result = state;
        }

        return result;
    }
}
