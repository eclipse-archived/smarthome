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
import org.eclipse.smarthome.core.types.EventDescription;

/**
 * Implementation of {@link TriggerChannelTypeBuilder} to build {@link ChannelType}s of kind TRIGGER
 *
 * @author Stefan Triller - Initial contribution
 *
 */
@NonNullByDefault
class TriggerChannelTypeBuilderImpl extends AbstractChannelTypeBuilder<TriggerChannelTypeBuilder>
        implements TriggerChannelTypeBuilder {

    private @Nullable EventDescription eventDescription;

    public TriggerChannelTypeBuilderImpl(ChannelTypeUID channelTypeUID, String label) {
        super(channelTypeUID, label);
    }

    @Override
    public ChannelType build() {
        return new ChannelType(channelTypeUID, advanced, null, ChannelKind.TRIGGER, label, description, category,
                tags.isEmpty() ? null : tags, null, eventDescription, configDescriptionURI);
    }

    @Override
    public TriggerChannelTypeBuilder withEventDescription(EventDescription eventDescription) {
        this.eventDescription = eventDescription;
        return this;
    }
}
