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
package org.eclipse.smarthome.core.thing.type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.internal.type.StateChannelTypeBuilderImpl;
import org.eclipse.smarthome.core.thing.internal.type.TriggerChannelTypeBuilderImpl;

/**
 * Builder for {@link ChannelType}s
 *
 * @author Stefan Triller- Initial contribution
 *
 */
@NonNullByDefault
public class ChannelTypeBuilder {

    /**
     * Create an instance of a ChannelTypeBuilder for {@link ChannelType}s of type STATE
     *
     * @param channelTypeUID UID of the ChannelType
     * @param label Label for the ChannelType
     * @param itemType ItemType that can be linked to the ChannelType
     * @return ChannelTypeBuilder for {@link ChannelType}s of type STATE
     */
    public static StateChannelTypeBuilder state(ChannelTypeUID channelTypeUID, String label, String itemType) {
        return new StateChannelTypeBuilderImpl(channelTypeUID, label, itemType);
    }

    /**
     * Create an instance of a ChannelTypeBuilder for {@link ChannelType}s of type TRIGGER
     *
     * @param channelTypeUID UID of the ChannelType
     * @param label Label for the ChannelType
     * @return ChannelTypeBuilder for {@link ChannelType}s of type TRGIGGER
     */
    public static TriggerChannelTypeBuilder trigger(ChannelTypeUID channelTypeUID, String label) {
        return new TriggerChannelTypeBuilderImpl(channelTypeUID, label);
    }
}
