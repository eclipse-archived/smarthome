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
import org.eclipse.smarthome.core.transform.TransformationHelper;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link StateProfile} which transforms a state value send from the handler to the framework based on the provided
 * {@link ProfileTypeUID}. The direction from the framework toward the handler is left untouched.
 *
 * @author Stefan Triller - initial contribution
 *
 */
@NonNullByDefault
public class TransformationProfile implements StateProfile {

    private static Logger logger = LoggerFactory.getLogger(TransformationProfile.class);

    public static final String PROFILE_NAME = "Transformation";
    private final ProfileTypeUID profileTypeUID;
    private final TransformationService service;

    private static String PATTERN_PARAM = "pattern";

    @NonNullByDefault({})
    private final String function;
    @NonNullByDefault({})
    private final String format;

    private final ProfileCallback callback;

    public TransformationProfile(ProfileCallback callback, ProfileContext context, TransformationService service,
            ProfileTypeUID typeUID) {

        this.service = service;
        this.profileTypeUID = typeUID;
        this.callback = callback;

        Object paramValue = context.getConfiguration().get(PATTERN_PARAM);
        logger.debug("Value for parameter '{}' is '{}'", PATTERN_PARAM, paramValue);
        if (paramValue instanceof String) {
            String pattern = (String) paramValue;
            if (pattern.contains(TransformationHelper.FUNCTION_VALUE_DELIMITER)) {
                function = pattern.split(TransformationHelper.FUNCTION_VALUE_DELIMITER)[0];
                format = pattern.split(TransformationHelper.FUNCTION_VALUE_DELIMITER)[1];
            } else {
                logError();
                function = null;
                format = null;
            }
        } else {
            logError();
            function = null;
            format = null;

        }
    }

    private void logError() {
        logger.error("Parameter '{}' has to be a String containing separator '{}'. Profile will be inactive.",
                PATTERN_PARAM, TransformationHelper.FUNCTION_VALUE_DELIMITER);
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
        if (function == null || format == null) {
            return;
        }
        callback.sendCommand((Command) transformState(command));
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        if (function == null || format == null) {
            return;
        }
        callback.sendUpdate((State) transformState(state));
    }

    private Type transformState(Type state) {
        if (state instanceof StringType) {
            String result = state.toFullString();
            try {
                String newFormat = String.format(format, state);
                result = service.transform(function, newFormat);
            } catch (TransformationException e) {
                logger.debug("Could not transform state '{}' with function '{}' and format '{}'", state, function,
                        format);
            }
            StringType resultType = new StringType(result);
            logger.debug("Transformed '{}' into '{}'", state, resultType);
            return resultType;
        } else {
            logger.debug("State '{}' is not of type StringType, skipping transformation.", state);
            return state;
        }

    }
}
