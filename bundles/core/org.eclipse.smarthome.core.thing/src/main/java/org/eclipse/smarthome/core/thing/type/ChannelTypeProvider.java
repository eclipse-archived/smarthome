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
package org.eclipse.smarthome.core.thing.type;

import java.util.Collection;
import java.util.Locale;

/**
 * The {@link ChannelTypeProvider} is responsible for providing channel types.
 *
 * @see ChannelTypeRegistry
 *
 * @author Dennis Nobel - Initial contribution
 */
public interface ChannelTypeProvider {

    /**
     * @see ChannelTypeRegistry#getChannelTypes(Locale)
     */
    Collection<ChannelType> getChannelTypes(Locale locale);

    /**
     * @see ChannelTypeRegistry#getChannelType(ChannelTypeUID, Locale)
     */
    ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale);

    /**
     * @see ChannelTypeRegistry#getChannelGroupType(ChannelGroupTypeUID, Locale)
     */
    ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale);

    /**
     * @see ChannelTypeRegistry#getChannelGroupTypes(Locale)
     */
    Collection<ChannelGroupType> getChannelGroupTypes(Locale locale);
}
