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
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.internal.profiles.StateProfileTypeImpl;
import org.eclipse.smarthome.core.thing.internal.profiles.TriggerProfileTypeImpl;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
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

    private final ChannelKind channelKind;
    private final ProfileTypeUID profileTypeUID;
    private final Collection<String> supportedItemTypes = new HashSet<>();
    private final Collection<ChannelTypeUID> supportedChannelTypeUIDs = new HashSet<>();

    @Nullable
    private String label;

    private ProfileTypeBuilder(ProfileTypeUID profileTypeUID, ChannelKind channelKind) {
        this.profileTypeUID = profileTypeUID;
        this.channelKind = channelKind;
    }

    /**
     * Obtain a new builder for a {@link StateProfileType} instance.
     *
     * @param profileTypeUID the {@link ProfileTypeUID}
     * @return the new builder instance
     */
    public static ProfileTypeBuilder<StateProfileType> newState(ProfileTypeUID profileTypeUID) {
        ProfileTypeBuilder<StateProfileType> ret = new ProfileTypeBuilder<>(profileTypeUID, ChannelKind.STATE);
        return ret;
    }

    /**
     * Obtain a new builder for a {@link TriggerProfileType} instance.
     *
     * @param profileTypeUID the {@link ProfileTypeUID}
     * @return the new builder instance
     */
    public static ProfileTypeBuilder<TriggerProfileType> newTrigger(ProfileTypeUID profileTypeUID) {
        ProfileTypeBuilder<TriggerProfileType> ret = new ProfileTypeBuilder<>(profileTypeUID, ChannelKind.TRIGGER);
        return ret;
    }

    /**
     * Define the human-readable label
     *
     * @param label
     * @return the builder itself
     */
    public ProfileTypeBuilder<T> withLabel(String label) {
        this.label = label;
        return this;
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
    @SuppressWarnings("unchecked")
    public T build() {
        final String lbl = label;
        if (lbl == null) {
            throw new IllegalStateException("The label has not been set yet");
        }
        switch (channelKind) {
            case STATE:
                return (T) new StateProfileTypeImpl(profileTypeUID, lbl, supportedItemTypes);
            case TRIGGER:
                return (T) new TriggerProfileTypeImpl(profileTypeUID, lbl, supportedItemTypes,
                        supportedChannelTypeUIDs);
            default:
                throw new IllegalArgumentException("Unknown type " + channelKind);
        }
    }

}
