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
package org.eclipse.smarthome.core.thing.internal.i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.i18n.ChannelGroupTypeI18nLocalizationService;
import org.eclipse.smarthome.core.thing.i18n.ChannelI18nUtil;
import org.eclipse.smarthome.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.eclipse.smarthome.core.thing.i18n.ThingTypeI18nLocalizationService;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeRegistry;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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
@Component(immediate = true, service = ChannelI18nUtil.class)
public class ChannelI18nUtilImpl implements ChannelI18nUtil {

    @NonNullByDefault({})
    private ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService;

    @NonNullByDefault({})
    private ChannelTypeRegistry channelTypeRegistry;

    @Reference
    protected void setChannelTypeI18nLocalizationService(
            ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        this.channelTypeI18nLocalizationService = channelTypeI18nLocalizationService;
    }

    protected void unsetChannelTypeI18nLocalizationService(
            ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        this.channelTypeI18nLocalizationService = null;
    }

    @Reference
    protected void setChannelTypeRegistry(ChannelTypeRegistry channelTypeRegistry) {
        this.channelTypeRegistry = channelTypeRegistry;
    }

    protected void unsetChannelTypeRegistry(ChannelTypeRegistry channelTypeRegistry) {
        this.channelTypeRegistry = null;
    }

    @Override
    public List<ChannelDefinition> createLocalizedChannelDefinitions(final Bundle bundle,
            final List<ChannelDefinition> channelDefinitions,
            final Function<ChannelDefinition, @Nullable String> channelLabelResolver,
            final Function<ChannelDefinition, @Nullable String> channelDescriptionResolver,
            final @Nullable Locale locale) {
        List<ChannelDefinition> localizedChannelDefinitions = new ArrayList<>(channelDefinitions.size());
        for (final ChannelDefinition channelDefinition : channelDefinitions) {
            String channelLabel = channelLabelResolver.apply(channelDefinition);
            String channelDescription = channelDescriptionResolver.apply(channelDefinition);
            if (channelLabel == null || channelDescription == null) {
                ChannelTypeUID channelTypeUID = channelDefinition.getChannelTypeUID();
                ChannelType channelType = channelTypeRegistry.getChannelType(channelTypeUID, locale);
                if (channelType != null) {
                    ChannelType localizedChannelType = channelTypeI18nLocalizationService
                            .createLocalizedChannelType(bundle, channelType, locale);
                    if (channelLabel == null) {
                        channelLabel = localizedChannelType.getLabel();
                    }
                    if (channelDescription == null) {
                        channelDescription = localizedChannelType.getDescription();
                    }
                }
            }
            localizedChannelDefinitions
                    .add(new ChannelDefinition(channelDefinition.getId(), channelDefinition.getChannelTypeUID(),
                            channelDefinition.getProperties(), channelLabel, channelDescription));
        }
        return localizedChannelDefinitions;
    }
}
