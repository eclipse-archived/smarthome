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
package org.eclipse.smarthome.core.thing.internal.type;

import java.net.URI;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.type.ChannelType;

/**
 * Interface for ChannelTypeBuilder
 *
 * @author Stefan Triller - Initial contribution
 *
 */
@NonNullByDefault
public interface IChannelTypeBuilder<T extends IChannelTypeBuilder<T>> {
    /**
     * Specify whether this is an advanced channel, default is false
     *
     * @param advanced true is this is an advanced {@link ChannelType}
     * @return this Builder
     */
    T isAdvanced(boolean advanced);

    /**
     * Sets the Description for the ChannelType
     *
     * @param description StateDescription for the ChannelType
     * @return this Builder
     */
    T withDescription(String description);

    /**
     * Sets the Category for the ChannelType
     *
     * @param category Category for the ChannelType
     * @return this Builder
     */
    T withCategory(String category);

    /**
     * Adds a tag to the ChannelType
     *
     * @param tag Tag to be added to the ChannelType
     * @return this Builder
     */
    T withTag(String tag);

    /**
     * Sets the StateDescription for the ChannelType
     *
     * @param tags Collection of tags to be added to the ChannelType
     * @return this Builder
     */
    T withTags(Collection<String> tags);

    /**
     * Sets the ConfigDescriptionURI for the ChannelType
     *
     * @param configDescriptionURI URI that references the ConfigDescription of the ChannelType
     * @return this Builder
     */
    T withConfigDescriptionURI(URI configDescriptionURI);

    /**
     * Build the ChannelType with the given values
     *
     * @return the created ChannelType
     */
    ChannelType build();
}
