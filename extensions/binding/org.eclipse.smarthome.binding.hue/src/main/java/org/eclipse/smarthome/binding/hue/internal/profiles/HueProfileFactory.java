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
import static org.eclipse.smarthome.core.library.CoreItemFactory.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.profiles.Profile;
import org.eclipse.smarthome.core.thing.profiles.ProfileAdvisor;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileContext;
import org.eclipse.smarthome.core.thing.profiles.ProfileFactory;
import org.eclipse.smarthome.core.thing.profiles.ProfileType;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeBuilder;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeProvider;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link HueProfileFactory} class defines and provides trigger profiles and its type of this binding.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
@Component
public class HueProfileFactory implements ProfileFactory, ProfileTypeProvider, ProfileAdvisor {

    public static final ProfileTypeUID HUE_GENERIC_COMMAND_PROFILE_TYPE_UID = new ProfileTypeUID(BINDING_ID,
            "generic-command");
    public static final ProfileTypeUID HUE_TOGGLE_PLAYER_PROFILE_TYPE_UID = new ProfileTypeUID(BINDING_ID,
            "toggle-player");
    public static final ProfileTypeUID HUE_TOGGLE_SWITCH_PROFILE_TYPE_UID = new ProfileTypeUID(BINDING_ID,
            "toggle-switch");

    private static final ProfileType HUE_GENERIC_COMMAND_PROFILE_TYPE = ProfileTypeBuilder
            .newTrigger(HUE_GENERIC_COMMAND_PROFILE_TYPE_UID, "Hue Generic Command Profile")
            .withSupportedItemTypes(DIMMER, PLAYER, ROLLERSHUTTER, SWITCH)
            .withSupportedChannelTypeUIDs(CHANNEL_TYPE_EVENT_DIMMER_SWITCH, CHANNEL_TYPE_EVENT_TAP_SWITCH).build();
    private static final ProfileType HUE_TOGGLE_PLAYER_PROFILE_TYPE = ProfileTypeBuilder
            .newTrigger(HUE_TOGGLE_SWITCH_PROFILE_TYPE_UID, "Hue Toggle Player Profile").withSupportedItemTypes(PLAYER)
            .withSupportedChannelTypeUIDs(CHANNEL_TYPE_EVENT_DIMMER_SWITCH, CHANNEL_TYPE_EVENT_TAP_SWITCH).build();
    private static final ProfileType HUE_TOGGLE_SWITCH_PLAYER_TYPE = ProfileTypeBuilder
            .newTrigger(HUE_TOGGLE_SWITCH_PROFILE_TYPE_UID, "Hue Toggle Switch Profile").withSupportedItemTypes(SWITCH)
            .withSupportedChannelTypeUIDs(CHANNEL_TYPE_EVENT_DIMMER_SWITCH, CHANNEL_TYPE_EVENT_TAP_SWITCH).build();

    private static final Set<ProfileTypeUID> SUPPORTED_PROFILE_TYPE_UIDS = Collections
            .unmodifiableSet(Stream.of(HUE_GENERIC_COMMAND_PROFILE_TYPE_UID, HUE_TOGGLE_PLAYER_PROFILE_TYPE_UID,
                    HUE_TOGGLE_SWITCH_PROFILE_TYPE_UID).collect(Collectors.toSet()));

    private static final Set<ProfileType> SUPPORTED_PROFILE_TYPES = Collections.unmodifiableSet(
            Stream.of(HUE_GENERIC_COMMAND_PROFILE_TYPE, HUE_TOGGLE_PLAYER_PROFILE_TYPE, HUE_TOGGLE_SWITCH_PLAYER_TYPE)
                    .collect(Collectors.toSet()));

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext context) {
        if (HUE_GENERIC_COMMAND_PROFILE_TYPE_UID.equals(profileTypeUID)) {
            return new HueGenericCommandTriggerProfile(callback, context);
        } else if (HUE_TOGGLE_PLAYER_PROFILE_TYPE_UID.equals(profileTypeUID)) {
            return new HueTogglePlayerTriggerProfile(callback, context);
        } else if (HUE_TOGGLE_SWITCH_PROFILE_TYPE_UID.equals(profileTypeUID)) {
            return new HueToggleSwitchTriggerProfile(callback, context);
        }
        return null;
    }

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return SUPPORTED_PROFILE_TYPE_UIDS;
    }

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return SUPPORTED_PROFILE_TYPES;
    }

    @Override
    public @Nullable ProfileTypeUID getSuggestedProfileTypeUID(ChannelType channelType, @Nullable String itemType) {
        return getSuggestedProfileTypeUID(channelType.getUID(), itemType);
    }

    @Override
    public @Nullable ProfileTypeUID getSuggestedProfileTypeUID(Channel channel, @Nullable String itemType) {
        return getSuggestedProfileTypeUID(channel.getChannelTypeUID(), itemType);
    }

    private @Nullable ProfileTypeUID getSuggestedProfileTypeUID(@Nullable ChannelTypeUID channelTypeUID,
            @Nullable String itemType) {
        if (CHANNEL_TYPE_EVENT_DIMMER_SWITCH.equals(channelTypeUID)
                || CHANNEL_TYPE_EVENT_TAP_SWITCH.equals(channelTypeUID)) {
            if (DIMMER.equals(itemType) || ROLLERSHUTTER.equals(itemType)) {
                return HUE_GENERIC_COMMAND_PROFILE_TYPE_UID;
            } else if (PLAYER.equals(itemType)) {
                return HUE_TOGGLE_PLAYER_PROFILE_TYPE_UID;
            } else if (SWITCH.equals(itemType)) {
                return HUE_TOGGLE_SWITCH_PROFILE_TYPE_UID;
            }
        }
        return null;
    }
}
