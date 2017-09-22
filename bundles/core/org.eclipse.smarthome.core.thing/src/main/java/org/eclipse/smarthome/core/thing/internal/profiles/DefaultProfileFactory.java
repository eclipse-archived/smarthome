/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal.profiles;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.DefaultSystemChannelTypeProvider;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.profiles.Profile;
import org.eclipse.smarthome.core.thing.profiles.ProfileAdvisor;
import org.eclipse.smarthome.core.thing.profiles.ProfileFactory;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.osgi.service.component.annotations.Component;

/**
 * A factory and advisor for default profiles.
 *
 * This {@link ProfileAdvisor} and {@link ProfileFactory} implementation handles all default {@link Profile}s.
 * It will be used as an advisor if the link is not configured and no other advisor returned a result (in that order).
 * The same applies to the creation of profile instances: This factory will be used of no other factory supported the
 * required profile type.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@Component(service = DefaultProfileFactory.class)
public class DefaultProfileFactory implements ProfileFactory, ProfileAdvisor {

    private static final Set<ProfileTypeUID> SUPPORTED_PROFILE_TYPES = Stream
            .of(DefaultMasterProfile.UID, DefaultSlaveProfile.UID, RawButtonToggleProfile.UID)
            .collect(Collectors.toSet());

    @Override
    public Profile createProfile(ProfileTypeUID profileTypeUID) {
        if (DefaultMasterProfile.UID.equals(profileTypeUID)) {
            return new DefaultMasterProfile();
        } else if (DefaultSlaveProfile.UID.equals(profileTypeUID)) {
            return new DefaultSlaveProfile();
        } else if (RawButtonToggleProfile.UID.equals(profileTypeUID)) {
            return new RawButtonToggleProfile();
        } else {
            return null;
        }
    }

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return SUPPORTED_PROFILE_TYPES;
    }

    @Override
    public Collection<ProfileTypeUID> getApplicableProfileTypeUIDs(ItemChannelLink link, Item item, Channel channel) {
        switch (channel.getKind()) {
            case STATE:
                return Stream.of(DefaultMasterProfile.UID, DefaultSlaveProfile.UID).collect(Collectors.toList());
            case TRIGGER:
                if (DefaultSystemChannelTypeProvider.SYSTEM_RAWBUTTON.getUID().equals(channel.getChannelTypeUID())) {
                    return Collections.singletonList(RawButtonToggleProfile.UID);
                }
                break;
            default:
                throw new NotImplementedException();
        }
        return Collections.emptyList();
    }

    @Override
    public ProfileTypeUID getSuggestedProfileTypeUID(ItemChannelLink link, Item item, Channel channel) {
        switch (channel.getKind()) {
            case STATE:
                return DefaultMasterProfile.UID;
            case TRIGGER:
                if (DefaultSystemChannelTypeProvider.SYSTEM_RAWBUTTON.getUID().equals(channel.getChannelTypeUID())) {
                    return RawButtonToggleProfile.UID;
                }
                break;
            default:
                throw new NotImplementedException();
        }
        return null;
    }

}
