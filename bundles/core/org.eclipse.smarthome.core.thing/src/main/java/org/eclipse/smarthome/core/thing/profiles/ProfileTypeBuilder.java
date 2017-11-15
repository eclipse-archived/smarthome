/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.profiles;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.internal.profiles.StateProfileTypeImpl;
import org.eclipse.smarthome.core.thing.internal.profiles.TriggerProfileTypeImpl;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * Builder for {@link ProfileType} instances.
 *
 * It can be used to obtain instances instead of implementing any of the interfaces derived from {@link ProfileType}.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 * @param <T> the concrete {@link ProfileType} sub-interface.
 */
@NonNullByDefault
public final class ProfileTypeBuilder<T extends ProfileType> {

    @FunctionalInterface
    private interface ProfileTypeFactory<T extends ProfileType> {
        T create(ProfileTypeUID profileTypeUID, String label, Collection<String> supportedItemTypes,
                Collection<ChannelTypeUID> supportedChannelTypeUIDs);
    }

    private final ProfileTypeFactory<T> profileTypeFactory;
    private final ProfileTypeUID profileTypeUID;
    private final Collection<String> supportedItemTypes = new HashSet<>();
    private final Collection<ChannelTypeUID> supportedChannelTypeUIDs = new HashSet<>();
    private final String label;

    private ProfileTypeBuilder(ProfileTypeUID profileTypeUID, String label, ProfileTypeFactory<T> profileTypeFactory) {
        this.profileTypeFactory = profileTypeFactory;
        this.profileTypeUID = profileTypeUID;
        this.label = label;
    }

    /**
     * Obtain a new builder for a {@link StateProfileType} instance.
     *
     * @param profileTypeUID the {@link ProfileTypeUID}
     * @param label a human-readable label
     * @return the new builder instance
     */
    public static ProfileTypeBuilder<StateProfileType> newState(ProfileTypeUID profileTypeUID, String label) {
        return new ProfileTypeBuilder<>(profileTypeUID, label,
                (leProfileTypeUID, leLabel, leSupportedItemTypes,
                        leSupportedChannelTypeUIDs) -> new StateProfileTypeImpl(leProfileTypeUID, leLabel,
                                leSupportedItemTypes));
    }

    /**
     * Obtain a new builder for a {@link TriggerProfileType} instance.
     *
     * @param profileTypeUID the {@link ProfileTypeUID}
     * @param label a human-readable label
     * @return the new builder instance
     */
    public static ProfileTypeBuilder<TriggerProfileType> newTrigger(ProfileTypeUID profileTypeUID, String label) {
        return new ProfileTypeBuilder<>(profileTypeUID, label,
                (leProfileTypeUID, leLabel, leSupportedItemTypes,
                        leSupportedChannelTypeUIDs) -> new TriggerProfileTypeImpl(leProfileTypeUID, leLabel,
                                leSupportedItemTypes, leSupportedChannelTypeUIDs));
    }

    /**
     * Declare that the given item type(s) are supported by a profile of this type.
     *
     * @param itemType
     * @return the builder itself
     */
    public ProfileTypeBuilder<T> withSupportedItemTypes(String... itemType) {
        supportedItemTypes.addAll(Arrays.asList(itemType));
        return this;
    }

    /**
     * Declare that the given item type(s) are supported by a profile of this type.
     *
     * @param itemType
     * @return the builder itself
     */
    public ProfileTypeBuilder<T> withSupportedItemTypes(Collection<String> itemTypes) {
        supportedItemTypes.addAll(itemTypes);
        return this;
    }

    /**
     * Declare that the given channel type(s) are supported by a profile of this type.
     *
     * @param channelTypeUIDs
     * @return the builder itself
     */
    public ProfileTypeBuilder<T> withSupportedChannelTypeUIDs(ChannelTypeUID... channelTypeUIDs) {
        supportedChannelTypeUIDs.addAll(Arrays.asList(channelTypeUIDs));
        return this;
    }

    /**
     * Declare that the given channel type(s) are supported by a profile of this type.
     *
     * @param channelTypeUIDs
     * @return the builder itself
     */
    public ProfileTypeBuilder<T> withSupportedChannelTypeUIDs(Collection<ChannelTypeUID> channelTypeUIDs) {
        supportedChannelTypeUIDs.addAll(channelTypeUIDs);
        return this;
    }

    /**
     * Create a profile type instance with the previously given parameters.
     *
     * @return the according subtype of
     */
    public T build() {
        return profileTypeFactory.create(profileTypeUID, label, supportedItemTypes, supportedChannelTypeUIDs);
    }

}
