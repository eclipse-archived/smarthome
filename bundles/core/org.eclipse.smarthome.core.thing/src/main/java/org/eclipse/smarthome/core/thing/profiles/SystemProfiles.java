/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.profiles;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.thing.DefaultSystemChannelTypeProvider;

/**
 * System profile constants.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public interface SystemProfiles {

    ProfileTypeUID DEFAULT = new ProfileTypeUID(ProfileTypeUID.SYSTEM_SCOPE, "default");
    ProfileTypeUID FOLLOW = new ProfileTypeUID(ProfileTypeUID.SYSTEM_SCOPE, "follow");
    ProfileTypeUID RAWBUTTON_TOGGLE_SWITCH = new ProfileTypeUID(ProfileTypeUID.SYSTEM_SCOPE, "rawbutton-toggle-switch");
    ProfileTypeUID RAWROCKER_TO_ON_OFF = new ProfileTypeUID(ProfileTypeUID.SYSTEM_SCOPE, "rawrocker-to-on-off");
    ProfileTypeUID RAWROCKER_TO_DIMMER = new ProfileTypeUID(ProfileTypeUID.SYSTEM_SCOPE, "rawrocker-to-dimmer");

    StateProfileType DEFAULT_TYPE = ProfileTypeBuilder.newState(DEFAULT, "Default").build();

    StateProfileType FOLLOW_TYPE = ProfileTypeBuilder.newState(FOLLOW, "Follow").build();

    TriggerProfileType RAWBUTTON_TOGGLE_SWITCH_TYPE = ProfileTypeBuilder
            .newTrigger(RAWBUTTON_TOGGLE_SWITCH, "Raw Button Toggle").withSupportedItemTypes(CoreItemFactory.SWITCH)
            .withSupportedChannelTypeUIDs(DefaultSystemChannelTypeProvider.SYSTEM_RAWBUTTON.getUID()).build();

    TriggerProfileType RAWROCKER_TO_ON_OFF_TYPE = ProfileTypeBuilder
            .newTrigger(RAWROCKER_TO_ON_OFF, "Raw Rocker To On Off")
            .withSupportedItemTypes(CoreItemFactory.SWITCH, CoreItemFactory.DIMMER)
            .withSupportedChannelTypeUIDs(DefaultSystemChannelTypeProvider.SYSTEM_RAWROCKER.getUID()).build();

    TriggerProfileType RAWROCKER_TO_DIMMER_TYPE = ProfileTypeBuilder
            .newTrigger(RAWROCKER_TO_DIMMER, "Raw Rocker To Dimmer").withSupportedItemTypes(CoreItemFactory.DIMMER)
            .withSupportedChannelTypeUIDs(DefaultSystemChannelTypeProvider.SYSTEM_RAWROCKER.getUID()).build();
}