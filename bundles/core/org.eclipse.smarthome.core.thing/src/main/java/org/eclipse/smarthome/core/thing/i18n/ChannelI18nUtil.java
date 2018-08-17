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
package org.eclipse.smarthome.core.thing.i18n;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.osgi.framework.Bundle;

/**
 * A utility service which localises {@link ChannelDefinition}.
 * Falls back to a localised {@link ChannelType} for label and description when not given otherwise.
 *
 * @see {@link ThingTypeI18nLocalizationService}
 * @see {@link ChannelGroupTypeI18nLocalizationService}
 *
 * @author Henning Treu - initial contribution
 *
 */
@NonNullByDefault
public interface ChannelI18nUtil {

    /**
     * Localise a list of {@link ChannelDefinition}s.
     *
     * @param bundle the bundle used to look up translation resources.
     * @param channelDefinitions the list of {@link ChannelDefinition} to localise.
     * @param channelLabelResolver resolve the channel label with this {@link Function}.
     * @param channelDescriptionResolver resolve the description with this {@link Function}.
     * @param locale the locale.
     * @return a list of localised {@link ChannelDefinition}s.
     */
    List<ChannelDefinition> createLocalizedChannelDefinitions(final Bundle bundle,
            final List<ChannelDefinition> channelDefinitions,
            final Function<ChannelDefinition, @Nullable String> channelLabelResolver,
            final Function<ChannelDefinition, @Nullable String> channelDescriptionResolver,
            final @Nullable Locale locale);
}
