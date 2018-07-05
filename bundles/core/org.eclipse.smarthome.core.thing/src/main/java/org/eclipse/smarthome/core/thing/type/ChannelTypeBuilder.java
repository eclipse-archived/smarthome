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

/**
 * Builder for {@link ChannelType}s
 *
 * @author Stefan Triller- Initial contribution
 *
 */
@NonNullByDefault
public class ChannelTypeBuilder {
    public static StateChannelTypeBuilder state(ChannelTypeUID channelTypeUID, String label, String itemType) {
        return new StateChannelTypeBuilderImpl(channelTypeUID, label, itemType);
    }

    public static TriggerChannelTypeBuilder trigger(ChannelTypeUID channelTypeUID, String label) {
        return new TriggerChannelTypeBuilderImpl(channelTypeUID, label);
    }
}
