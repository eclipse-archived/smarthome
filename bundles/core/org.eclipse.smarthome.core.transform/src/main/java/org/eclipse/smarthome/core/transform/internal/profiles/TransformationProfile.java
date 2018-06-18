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
package org.eclipse.smarthome.core.transform.internal.profiles;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileContext;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.StateProfile;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link StateProfile} which transforms a state value sent from the handler to the framework based on the provided
 * {@link ProfileTypeUID}. The state value in the direction from the framework towards the handler is left untouched.
 *
 * @author Stefan Triller - initial contribution
 *
 */
@NonNullByDefault
public class TransformationProfile implements StateProfile {

    private final Logger logger = LoggerFactory.getLogger(TransformationProfile.class);

    public static final String PROFILE_NAME = "Transformation";
    private final ProfileTypeUID profileTypeUID;
    private final TransformationService service;

    private static final String FUNCTION_PARAM = "function";
    private static final String SOURCE_PARAM = "sourceFormat";

    @NonNullByDefault({})
    private final String function;
    @NonNullByDefault({})
    private final String sourceFormat;

    private final ProfileCallback callback;

    public TransformationProfile(ProfileCallback callback, ProfileContext context, TransformationService service,
            ProfileTypeUID typeUID) {

        this.service = service;
        this.profileTypeUID = typeUID;
        this.callback = callback;

        Object paramFunction = context.getConfiguration().get(FUNCTION_PARAM);
        Object paramSource = context.getConfiguration().get(SOURCE_PARAM);

        logger.debug("Profile configured with '{}'='{}', '{}'={}", FUNCTION_PARAM, paramFunction, SOURCE_PARAM,
                paramSource);
        // paramSource is an advanced parameter and we assume "%s" if it is not set
        if (paramSource == null) {
            paramSource = "%s";
        }
        if (paramFunction instanceof String && paramSource instanceof String) {
            function = (String) paramFunction;
            sourceFormat = (String) paramSource;
        } else {
            logger.warn("Parameter '{}' and '{}' have to be Strings. Profile will be inactive.", FUNCTION_PARAM,
                    SOURCE_PARAM);
            function = null;
            sourceFormat = null;
        }
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return profileTypeUID;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        callback.handleCommand((Command) state);
    }

    @Override
    public void onCommandFromItem(Command command) {
        callback.handleCommand(command);
    }

    @Override
    public void onCommandFromHandler(Command command) {
        if (function == null || sourceFormat == null) {
            logger.error(
                    "Please specify a function and a source format for this Profile in the '{}', and '{}' parameters, e.g \"translation.map\"  and \"%s\". Returning the original command now.",
                    FUNCTION_PARAM, SOURCE_PARAM);
            callback.sendCommand(command);
            return;
        }
        callback.sendCommand((Command) transformState(command));
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        if (function == null || sourceFormat == null) {
            logger.error(
                    "Please specify a function and a source format for this Profile in the '{}' and '{}' parameters, e.g \"translation.map\" and \"%s\". Returning the original state now.",
                    FUNCTION_PARAM, SOURCE_PARAM);
            callback.sendUpdate(state);
            return;
        }
        callback.sendUpdate((State) transformState(state));
    }

    private Type transformState(Type state) {
        String result = state.toFullString();
        try {
            String newFormat = String.format(sourceFormat, state);
            result = service.transform(function, newFormat);
        } catch (TransformationException e) {
            logger.debug("Could not transform state '{}' with function '{}' and format '{}'", state, function,
                    sourceFormat);
        }
        StringType resultType = new StringType(result);
        logger.debug("Transformed '{}' into '{}'", state, resultType);
        return resultType;
    }
}
