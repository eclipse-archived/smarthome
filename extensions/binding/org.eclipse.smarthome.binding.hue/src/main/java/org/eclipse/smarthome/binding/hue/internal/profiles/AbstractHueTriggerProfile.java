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

import static org.eclipse.smarthome.binding.hue.internal.HueBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileContext;
import org.eclipse.smarthome.core.thing.profiles.TriggerProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractHueTriggerProfile} class implements the behavior when being linked to an item.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractHueTriggerProfile implements TriggerProfile {

    private final Logger logger = LoggerFactory.getLogger(AbstractHueTriggerProfile.class);

    private static final Set<String> SUPPORTED_EVENTS = Collections.unmodifiableSet(Stream
            .of(EVENT_DIMMER_1000, EVENT_DIMMER_1001, EVENT_DIMMER_1002, EVENT_DIMMER_1003, EVENT_DIMMER_2000,
                    EVENT_DIMMER_2001, EVENT_DIMMER_2002, EVENT_DIMMER_2003, EVENT_DIMMER_3000, EVENT_DIMMER_3001,
                    EVENT_DIMMER_3002, EVENT_DIMMER_3003, EVENT_DIMMER_4000, EVENT_DIMMER_4001, EVENT_DIMMER_4002,
                    EVENT_DIMMER_4003, EVENT_TAP_34, EVENT_TAP_16, EVENT_TAP_17, EVENT_TAP_18)
            .collect(Collectors.toSet()));

    private static final String PARAM_EVENT = "event";

    final ProfileCallback callback;

    @Nullable
    String event;

    AbstractHueTriggerProfile(ProfileCallback callback, ProfileContext context) {
        this.callback = callback;

        Object paramValue = context.getConfiguration().get(PARAM_EVENT);
        logger.debug("Configuring profile '{}' with 'event' parameter: '{}'", getProfileTypeUID(), paramValue);
        if (paramValue instanceof String) {
            String value = (String) paramValue;
            if (SUPPORTED_EVENTS.contains(value)) {
                event = value;
            } else {
                logger.error("Parameter 'event' does not support event: '{}'", paramValue);
            }
        } else {
            logger.error("Parameter 'event' is not of type String");
        }
    }
}
