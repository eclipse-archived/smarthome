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
package org.eclipse.smarthome.core.thing.binding;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.validation.ConfigValidationException;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

/**
 * {@link ThingHandlerCallback} is callback interface for {@link ThingHandler}s. The implementation of a
 * {@link ThingHandler} must use the callback to inform the framework about changes like state updates, status updated
 * or an update of the whole thing.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Stefan Bußweiler - Added new thing status info, added new configuration update info
 * @author Christoph Weitkamp - Moved OSGI ServiceTracker from BaseThingHandler to ThingHandlerCallback
 */
@NonNullByDefault
public interface ThingHandlerCallback {

    /**
     * Informs about an updated state for a channel.
     *
     * @param channelUID channel UID (must not be null)
     * @param state state (must not be null)
     */
    void stateUpdated(ChannelUID channelUID, State state);

    /**
     * Informs about a command, which is sent from the channel.
     *
     * @param channelUID channel UID
     * @param command command
     */
    void postCommand(ChannelUID channelUID, Command command);

    /**
     * Informs about an updated status of a thing.
     *
     * @param thing thing (must not be null)
     * @param thingStatus thing status (must not be null)
     */
    void statusUpdated(Thing thing, ThingStatusInfo thingStatus);

    /**
     * Informs about an update of the whole thing.
     *
     * @param thing thing that was updated (must not be null)
     * @throws IllegalStateException if the {@link Thing} is read-only.
     */
    void thingUpdated(Thing thing);

    /**
     * Validates the given configuration parameters against the configuration description.
     *
     * @param thing thing with the updated configuration (must no be null)
     * @param configurationParameters the configuration parameters to be validated
     * @throws ConfigValidationException if one or more of the given configuration parameters do not match
     *             their declarations in the configuration description
     */
    void validateConfigurationParameters(Thing thing, Map<String, Object> configurationParameters);

    /**
     * Informs about an updated configuration of a thing.
     *
     * @param thing thing with the updated configuration (must no be null)
     */
    void configurationUpdated(Thing thing);

    /**
     * Informs the framework that the ThingType of the given {@link Thing} should be changed.
     *
     * @param thing thing that should be migrated to another ThingType (must not be null)
     * @param thingTypeUID the new type of the thing (must not be null)
     * @param configuration a configuration that should be applied to the given {@link Thing}
     */
    void migrateThingType(Thing thing, ThingTypeUID thingTypeUID, Configuration configuration);

    /**
     * Informs the framework that a channel has been triggered.
     *
     * @param thing thing (must not be null)
     * @param channelUID UID of the channel over which has been triggered.
     * @param event Event.
     */
    void channelTriggered(Thing thing, ChannelUID channelUID, String event);

    /**
     * Create a {@link ChannelBuilder} which is preconfigured with values from the given channel type.
     *
     * @param channelUID the UID of the channel to be created
     * @param channelTypeUID the channel type UID for which the channel should be created
     * @return a preconfigured ChannelBuilder
     * @throw {@link IllegalArgumentException} if the referenced channel type is not known
     */
    ChannelBuilder createChannelBuilder(ChannelUID channelUID, ChannelTypeUID channelTypeUID);

    /**
     * Returns whether at least one item is linked for the given UID of the channel.
     *
     * @param channelUID UID of the channel (must not be null)
     * @return true if at least one item is linked, false otherwise
     */
    boolean isChannelLinked(ChannelUID channelUID);
}
