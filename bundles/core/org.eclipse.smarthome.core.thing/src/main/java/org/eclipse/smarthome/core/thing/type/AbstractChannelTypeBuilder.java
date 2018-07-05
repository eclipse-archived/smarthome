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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Abstract base class with common methods for {@link ChannelTypeBuilder}
 *
 * @author Stefan Triller - Initial contribution
 *
 */
@NonNullByDefault
abstract class AbstractChannelTypeBuilder<T extends IChannelTypeBuilder<T>> implements IChannelTypeBuilder<T> {

    protected final ChannelTypeUID channelTypeUID;
    protected final String label;
    protected boolean advanced;
    protected @Nullable String description;
    protected @Nullable String category;
    protected final Set<String> tags = new HashSet<>();
    protected @Nullable URI configDescriptionURI;

    public AbstractChannelTypeBuilder(ChannelTypeUID channelTypeUID, String label) {
        this.channelTypeUID = channelTypeUID;
        this.label = label;
    }

    @Override
    public T isAdvanced(boolean advanced) {
        this.advanced = advanced;
        return (T) this;
    }

    @Override
    public T withDescription(String description) {
        this.description = description;
        return (T) this;
    }

    @Override
    public T withCategory(String category) {
        this.category = category;
        return (T) this;
    }

    @Override
    public T withTag(String tag) {
        this.tags.add(tag);
        return (T) this;
    }

    @Override
    public T withTags(Collection<String> tags) {
        this.tags.addAll(tags);
        return (T) this;
    }

    @Override
    public T withConfigDescriptionURI(URI configDescriptionURI) {
        this.configDescriptionURI = configDescriptionURI;
        return (T) this;
    }

    @Override
    public abstract ChannelType build();
}
