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
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.types.StateDescription;

/**
 * StateChannelTypeBuilder to create {@link ChannelType}s of kind STATE
 *
 * @author Stefan Triller - Initial contribution
 *
 */
@NonNullByDefault
class StateChannelTypeBuilderImpl extends AbstractChannelTypeBuilder<StateChannelTypeBuilder>
        implements StateChannelTypeBuilder {

    private @Nullable StateDescription stateDescription;
    private final String itemType;

    public StateChannelTypeBuilderImpl(ChannelTypeUID channelTypeUID, String label, String itemType) {
        super(channelTypeUID, label);
        this.itemType = itemType;
    }

    @Override
    public StateChannelTypeBuilder withStateDescription(@Nullable StateDescription stateDescription) {
        this.stateDescription = stateDescription;
        return this;
    }

    @Override
    public ChannelType build() {
        return new ChannelType(channelTypeUID, advanced, itemType, ChannelKind.STATE, label, description, category,
                tags.isEmpty() ? null : tags, stateDescription, null, configDescriptionURI);
    }

}
