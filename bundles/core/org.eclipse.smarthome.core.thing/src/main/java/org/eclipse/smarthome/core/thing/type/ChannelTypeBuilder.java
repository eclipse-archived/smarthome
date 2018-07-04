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
    private @Nullable String bindingId;
    private @Nullable String channelTypeId;
    private @Nullable String label;

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

    // mandatory values
    public ChannelTypeBuilder withBindingId(String bindingId) {
        this.bindingId = bindingId;
        return this;
    }

    public ChannelTypeBuilder withChannelTypeId(String channelTypeId) {
        this.channelTypeId = channelTypeId;
        return this;
    }

    public ChannelTypeBuilder withLabel(String itemType) {
        this.itemType = itemType;
        return this;
    }

    public ChannelType build() {

        if (StringUtils.isBlank(bindingId)) {
            throw new IllegalArgumentException("The bindingId must neither be null nor empty.");
        }
        if (StringUtils.isBlank(channelTypeId)) {
            throw new IllegalArgumentException("The channelTypeId must neither be null nor empty.");
        }
        if (StringUtils.isBlank(label)) {
            throw new IllegalArgumentException("The label must neither be null nor empty.");
        }

        return new ChannelType(new ChannelTypeUID(bindingId, channelTypeId), advanced, itemType, kind, label,
                description, category, tags.isEmpty() ? null : tags, stateDescription, eventDescription,
                configDescriptionURI);
    }

    // optional values
    /**
     * Specify whether this is an advanced channel, default is false
     *
     * @param advanced true is this is an advanced {@link ChannelType}
     * @return
     */
    public ChannelTypeBuilder isAdvanced(boolean advanced) {
        this.advanced = advanced;
        return this;
    }

    public ChannelTypeBuilder withItemType(String itemType) {
        this.itemType = itemType;
        return this;
    }

    /**
     * Set the {@link ChannelKind} of the channel, if unset STATE will be used
     *
     * @param kind the {@link ChannelKind}
     * @return
     */
    public ChannelTypeBuilder withKind(ChannelKind kind) {
        this.kind = kind;
        return this;
    }

    public ChannelTypeBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public ChannelTypeBuilder withCategory(String category) {
        this.category = category;
        return this;
    }

    public ChannelTypeBuilder withTag(String tag) {
        this.tags.add(tag);
        return this;
    }

    public ChannelTypeBuilder withTags(Collection<String> tags) {
        this.tags.addAll(tags);
        return this;
    }

    public ChannelTypeBuilder withStateDescription(StateDescription stateDescription) {
        this.stateDescription = stateDescription;
        return this;
    }

    public ChannelTypeBuilder withEventDescription(EventDescription eventDescription) {
        this.eventDescription = eventDescription;
        return this;
    }

    public ChannelTypeBuilder withConfigDescriptionURI(URI configDescriptionURI) {
        this.configDescriptionURI = configDescriptionURI;
        return this;
    }
}
