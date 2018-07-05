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

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.types.EventDescription;
import org.eclipse.smarthome.core.types.StateDescription;

/**
 * Builder for {@link ChannelType}s
 *
 * @author Stefan Triller- Initial contribution
 *
 */
@NonNullByDefault
public class ChannelTypeBuilder {

    // mandatory
    private final String bindingId;
    private final String channelTypeId;
    private final String label;

    // optional
    private boolean advanced;
    private @Nullable String itemType;
    private ChannelKind kind = ChannelKind.STATE;
    private @Nullable String description;
    private @Nullable String category;
    private final Set<String> tags = new HashSet<>();
    private @Nullable StateDescription stateDescription;
    private @Nullable EventDescription eventDescription;
    private @Nullable URI configDescriptionURI;

    public ChannelTypeBuilder(ChannelTypeUID channelTypeUID, String label) {
        this(channelTypeUID.getBindingId(), channelTypeUID.getId(), label);
    }

    public ChannelTypeBuilder(String bindingId, String channelTypeId, String label) {
        if (StringUtils.isBlank(bindingId)) {
            throw new IllegalArgumentException("The bindingId must neither be null nor empty.");
        }
        if (StringUtils.isBlank(channelTypeId)) {
            throw new IllegalArgumentException("The channelTypeId must neither be null nor empty.");
        }
        if (StringUtils.isBlank(label)) {
            throw new IllegalArgumentException("The label must neither be null nor empty.");
        }

        this.bindingId = bindingId;
        this.channelTypeId = channelTypeId;
        this.label = label;
    }

    /**
     * Build the ChannelType with the given values
     *
     * @return the created ChannelType
     */
    public ChannelType build() {
        return new ChannelType(new ChannelTypeUID(bindingId, channelTypeId), advanced, itemType, kind, label,
                description, category, tags.isEmpty() ? null : tags, stateDescription, eventDescription,
                configDescriptionURI);
    }

    // optional values
    /**
     * Specify whether this is an advanced channel, default is false
     *
     * @param advanced true is this is an advanced {@link ChannelType}
     * @return this Builder
     */
    public ChannelTypeBuilder isAdvanced(boolean advanced) {
        this.advanced = advanced;
        return this;
    }

    /**
     * Only for ChannelType of kind STATE
     *
     * @param itemType
     * @return this Builder
     */
    public ChannelTypeBuilder withItemType(String itemType) {
        this.itemType = itemType;
        return this;
    }

    /**
     * Set the {@link ChannelKind} of the channel, if unset STATE will be used
     *
     * @param kind the {@link ChannelKind}
     * @return this Builder
     */
    public ChannelTypeBuilder withKind(ChannelKind kind) {
        this.kind = kind;
        return this;
    }

    /**
     * Sets the Description for the ChannelType
     *
     * @param description StateDescription for the ChannelType
     * @return this Builder
     */
    public ChannelTypeBuilder withDescription(@Nullable String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the Category for the ChannelType
     *
     * @param category Category for the ChannelType
     * @return this Builder
     */
    public ChannelTypeBuilder withCategory(@Nullable String category) {
        this.category = category;
        return this;
    }

    /**
     * Adds a tag to the ChannelType
     *
     * @param tag Tag to be added to the ChannelType
     * @return this Builder
     */
    public ChannelTypeBuilder withTag(String tag) {
        this.tags.add(tag);
        return this;
    }

    /**
     * Adds a Sets the StateDescription for the ChannelType
     *
     * @param tags Collection of tags to be added to the ChannelType
     * @return this Builder
     */
    public ChannelTypeBuilder withTags(Collection<String> tags) {
        this.tags.addAll(tags);
        return this;
    }

    /**
     * Sets the StateDescription for the ChannelType (only for ChannelType of kind STATE)
     *
     * @param stateDescription StateDescription for the ChannelType
     * @return this Builder
     */
    public ChannelTypeBuilder withStateDescription(@Nullable StateDescription stateDescription) {
        this.stateDescription = stateDescription;
        return this;
    }

    /**
     * Sets the EventDescription for the ChannelType (only for ChannelType of kind TRIGGER)
     *
     * @param eventDescription EventDescription for the ChannelType
     * @return this Builder
     */
    public ChannelTypeBuilder withEventDescription(@Nullable EventDescription eventDescription) {
        this.eventDescription = eventDescription;
        return this;
    }

    /**
     * Sets the ConfigDescriptionURI for the ChannelType
     *
     * @param configDescriptionURI URI that references the ConfigDescription of the ChannelType
     * @return this Builder
     */
    public ChannelTypeBuilder withConfigDescriptionURI(@Nullable URI configDescriptionURI) {
        this.configDescriptionURI = configDescriptionURI;
        return this;
    }
}
