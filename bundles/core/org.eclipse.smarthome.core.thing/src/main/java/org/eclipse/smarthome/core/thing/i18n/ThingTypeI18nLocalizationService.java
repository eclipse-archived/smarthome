/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.thing.type.BridgeType;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeBuilder;
import org.eclipse.smarthome.core.thing.type.TypeResolver;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * This OSGi service could be used to localize a thing type using the I18N mechanism of the Eclipse SmartHome
 * framework.
 *
 * @author Markus Rathgeb - Move code from XML thing type provider to separate service
 * @author Laurent Garnier - fix localized label and description for channel group definition
 */
@Component(immediate = true, service = ThingTypeI18nLocalizationService.class)
@NonNullByDefault
public class ThingTypeI18nLocalizationService {

    private @Nullable ThingTypeI18nUtil thingTypeI18nUtil;

    @Reference
    protected void setTranslationProvider(TranslationProvider i18nProvider) {
        this.thingTypeI18nUtil = new ThingTypeI18nUtil(i18nProvider);
    }

    protected void unsetTranslationProvider(TranslationProvider i18nProvider) {
        this.thingTypeI18nUtil = null;
    }

    public ThingType createLocalizedThingType(Bundle bundle, ThingType thingType, Locale locale) {
        final String label = this.thingTypeI18nUtil.getLabel(bundle, thingType.getUID(), thingType.getLabel(), locale);
        final String description = this.thingTypeI18nUtil.getDescription(bundle, thingType.getUID(),
                thingType.getDescription(), locale);

        final List<ChannelDefinition> localizedChannelDefinitions = new ArrayList<>(
                thingType.getChannelDefinitions().size());

        for (final ChannelDefinition channelDefinition : thingType.getChannelDefinitions()) {
            String channelLabel = this.thingTypeI18nUtil.getChannelLabel(bundle, channelDefinition.getChannelTypeUID(),
                    channelDefinition.getLabel(), locale);
            String channelDescription = this.thingTypeI18nUtil.getChannelDescription(bundle,
                    channelDefinition.getChannelTypeUID(), channelDefinition.getDescription(), locale);
            if (channelLabel == null || channelDescription == null) {
                ChannelType channelType = TypeResolver.resolve(channelDefinition.getChannelTypeUID(), locale);
                if (channelType != null) {
                    if (channelLabel == null) {
                        channelLabel = this.thingTypeI18nUtil.getChannelLabel(bundle, channelType.getUID(),
                                channelType.getLabel(), locale);
                    }
                    if (channelDescription == null) {
                        channelDescription = this.thingTypeI18nUtil.getChannelDescription(bundle, channelType.getUID(),
                                channelType.getDescription(), locale);
                    }
                }
            }
            localizedChannelDefinitions
                    .add(new ChannelDefinition(channelDefinition.getId(), channelDefinition.getChannelTypeUID(),
                            channelDefinition.getProperties(), channelLabel, channelDescription));
        }

        final List<@NonNull ChannelGroupDefinition> localizedChannelGroupDefinitions = new ArrayList<>(
                thingType.getChannelGroupDefinitions().size());
        for (final ChannelGroupDefinition channelGroupDefinition : thingType.getChannelGroupDefinitions()) {
            String channelGroupLabel = this.thingTypeI18nUtil.getChannelGroupLabel(bundle, thingType.getUID(),
                    channelGroupDefinition, channelGroupDefinition.getLabel(), locale);
            String channelGroupDescription = this.thingTypeI18nUtil.getChannelGroupDescription(bundle,
                    thingType.getUID(), channelGroupDefinition, channelGroupDefinition.getDescription(), locale);
            if (channelGroupLabel == null || channelGroupDescription == null) {
                ChannelGroupType channelGroupType = TypeResolver.resolve(channelGroupDefinition.getTypeUID(), locale);
                if (channelGroupType != null) {
                    if (channelGroupLabel == null) {
                        channelGroupLabel = this.thingTypeI18nUtil.getChannelGroupLabel(bundle,
                                channelGroupType.getUID(), channelGroupType.getLabel(), locale);
                    }
                    if (channelGroupDescription == null) {
                        channelGroupDescription = this.thingTypeI18nUtil.getChannelGroupDescription(bundle,
                                channelGroupType.getUID(), channelGroupType.getDescription(), locale);
                    }
                }
            }
            localizedChannelGroupDefinitions.add(new ChannelGroupDefinition(channelGroupDefinition.getId(),
                    channelGroupDefinition.getTypeUID(), channelGroupLabel, channelGroupDescription));
        }

        ThingTypeBuilder builder = ThingTypeBuilder.instance(thingType).withLabel(label).withDescription(description)
                .withChannelDefinitions(localizedChannelDefinitions)
                .withChannelGroupDefinitions(localizedChannelGroupDefinitions);

        if (thingType instanceof BridgeType) {
            return builder.buildBridge();
        }

        return builder.build();
    }

}
