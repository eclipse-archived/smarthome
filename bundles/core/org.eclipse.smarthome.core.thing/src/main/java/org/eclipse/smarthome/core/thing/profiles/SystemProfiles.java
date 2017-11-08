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

import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.thing.DefaultSystemChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * System profile constants.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public interface SystemProfiles {

    public static final ProfileTypeUID MASTER = new ProfileTypeUID(ProfileTypeUID.SYSTEM_SCOPE, "master");
    public static final ProfileTypeUID SLAVE = new ProfileTypeUID(ProfileTypeUID.SYSTEM_SCOPE, "slave");
    public static final ProfileTypeUID RAWBUTTON_TOGGLE_SWITCH = new ProfileTypeUID(ProfileTypeUID.SYSTEM_SCOPE,
            "rawbutton-toggle-switch");

    public static final StateProfileType MASTER_TYPE = new StateProfileType() {
        @Override
        public ProfileTypeUID getUID() {
            return MASTER;
        }

        @Override
        public Collection<String> getSupportedItemTypes() {
            return ANY_ITEM_TYPE;
        }

        @Override
        public String getLabel() {
            return "Master";
        }
    };

    public static final StateProfileType SLAVE_TYPE = new StateProfileType() {
        @Override
        public ProfileTypeUID getUID() {
            return SLAVE;
        }

        @Override
        public Collection<String> getSupportedItemTypes() {
            return ANY_ITEM_TYPE;
        }

        @Override
        public String getLabel() {
            return "Slave";
        }
    };

    public static final TriggerProfileType RAWBUTTON_TOGGLE_SWITCH_TYPE = new TriggerProfileType() {
        @Override
        public ProfileTypeUID getUID() {
            return SLAVE;
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
