/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.core.thing.internal.profiles;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.TriggerProfileType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * Default implementation of a {@link TriggerProfileTypeImpl}.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public class TriggerProfileTypeImpl extends StateProfileTypeImpl implements TriggerProfileType {

    private final Collection<ChannelTypeUID> supportedChannelTypeUIDs;

    public TriggerProfileTypeImpl(ProfileTypeUID profileTypeUID, String label, Collection<String> supportedItemTypes,
            Collection<ChannelTypeUID> supportedChannelTypeUIDs) {
        super(profileTypeUID, label, supportedItemTypes);
        this.supportedChannelTypeUIDs = supportedChannelTypeUIDs;
    }

    @Override
    public Collection<ChannelTypeUID> getSupportedChannelTypeUIDs() {
        return supportedChannelTypeUIDs;
    }

}
