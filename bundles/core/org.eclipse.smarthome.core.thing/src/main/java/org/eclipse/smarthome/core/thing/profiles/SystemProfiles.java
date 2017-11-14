/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.profiles;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.thing.DefaultSystemChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

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

    StateProfileType DEFAULT_TYPE = new StateProfileType() {
        @Override
        public ProfileTypeUID getUID() {
            return DEFAULT;
        }

        @Override
        public Collection<String> getSupportedItemTypes() {
            return ANY_ITEM_TYPE;
        }

        @Override
        public String getLabel() {
            return "Default";
        }
    };

    StateProfileType FOLLOW_TYPE = new StateProfileType() {
        @Override
        public ProfileTypeUID getUID() {
            return FOLLOW;
        }

        @Override
        public Collection<String> getSupportedItemTypes() {
            return ANY_ITEM_TYPE;
        }

        @Override
        public String getLabel() {
            return "Follow";
        }
    };

    TriggerProfileType RAWBUTTON_TOGGLE_SWITCH_TYPE = new TriggerProfileType() {
        @Override
        public ProfileTypeUID getUID() {
            return RAWBUTTON_TOGGLE_SWITCH;
        }

        @Override
        public Collection<String> getSupportedItemTypes() {
            return Collections.singleton(CoreItemFactory.SWITCH);
        }

        @Override
        public Collection<ChannelTypeUID> getSupportedChannelTypeUIDs() {
            return Collections.singleton(DefaultSystemChannelTypeProvider.SYSTEM_RAWBUTTON.getUID());
        }

        @Override
        public String getLabel() {
            return "Raw Button Toggle";
        }
    };

}
