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
package org.eclipse.smarthome.binding.hue.internal.profiles;

import static org.eclipse.smarthome.binding.hue.internal.profiles.HueProfileFactory.HUE_GENERIC_COMMAND_PROFILE_TYPE_UID;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileContext;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HueGenericCommandTriggerProfile} class implements the toggle PLAY/PAUSE behavior when being linked to a
 * Player item.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HueGenericCommandTriggerProfile extends AbstractHueTriggerProfile {

    private final Logger logger = LoggerFactory.getLogger(HueGenericCommandTriggerProfile.class);

    private static final List<Class<? extends Command>> SUPPORTED_COMMANDS = new ArrayList<>();
    static {
        // TODO item.getAcceptedCommandTypes()
        SUPPORTED_COMMANDS.add(IncreaseDecreaseType.class);
        SUPPORTED_COMMANDS.add(NextPreviousType.class);
        SUPPORTED_COMMANDS.add(OnOffType.class);
        SUPPORTED_COMMANDS.add(PlayPauseType.class);
        SUPPORTED_COMMANDS.add(RewindFastforwardType.class);
        SUPPORTED_COMMANDS.add(StopMoveType.class);
        SUPPORTED_COMMANDS.add(UpDownType.class);
    }

    private static final String PARAM_COMMAND = "command";

    private @Nullable Command command;

    HueGenericCommandTriggerProfile(ProfileCallback callback, ProfileContext context) {
        super(callback, context);

        Object paramValue = context.getConfiguration().get(PARAM_COMMAND);
        logger.debug("Configuring profile '{}' with 'command' parameter: '{}'", getProfileTypeUID(), paramValue);
        if (paramValue instanceof String) {
            command = TypeParser.parseCommand(SUPPORTED_COMMANDS, (String) paramValue);
            if (command == null) {
                logger.error("Parameter 'command' is a not supported command: '{}'", paramValue);
            }
        } else {
            logger.error("Parameter 'command' is not of type String");
        }
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return HUE_GENERIC_COMMAND_PROFILE_TYPE_UID;
    }

    @Override
    public void onTriggerFromHandler(String payload) {
        if (payload.equals(event) && command != null) {
            Command c = command;
            callback.sendCommand(c);
        }
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        // do nothing
    }
}
