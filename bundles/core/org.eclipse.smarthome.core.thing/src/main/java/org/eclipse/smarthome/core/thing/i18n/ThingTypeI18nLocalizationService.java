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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.BridgeType;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeRegistry;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeBuilder;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * This OSGi service could be used to localize a thing type using the I18N mechanism of the Eclipse SmartHome
 * framework.
 *
 * @author Markus Rathgeb - Move code from XML thing type provider to separate service
 * @author Laurent Garnier - fix localized label and description for channel group definition
 * @author Christoph Weitkamp - factored out from {@link XmlChannelTypeProvider} and {@link XmlChannelGroupTypeProvider}
 */
@Component(immediate = true, service = ThingTypeI18nLocalizationService.class)
@NonNullByDefault
public class ThingTypeI18nLocalizationService {

    @NonNullByDefault({})
    private ThingTypeI18nUtil thingTypeI18nUtil;

    @NonNullByDefault({})
    private ChannelTypeRegistry channelTypeRegistry;

    @Reference
    protected void setTranslationProvider(TranslationProvider i18nProvider) {
        this.thingTypeI18nUtil = new ThingTypeI18nUtil(i18nProvider);
    }

    protected void unsetTranslationProvider(TranslationProvider i18nProvider) {
        this.thingTypeI18nUtil = null;
    }

    @Reference
    protected void setChannelTypeRegistry(ChannelTypeRegistry channelTypeRegistry) {
        this.channelTypeRegistry = channelTypeRegistry;
    }

    protected void unsetChannelTypeRegistry(ChannelTypeRegistry channelTypeRegistry) {
        this.channelTypeRegistry = null;
    }

    @Nullable
    private StateDescription createLocalizedStateDescription(Bundle bundle, ChannelType channelType,
            ChannelTypeUID channelTypeUID, Locale locale) {
        StateDescription state = channelType.getState();

        if (state != null) {
            String pattern = thingTypeI18nUtil.getChannelStatePattern(bundle, channelTypeUID, state.getPattern(),
                    locale);

            List<StateOption> localizedOptions = new ArrayList<>();
            List<StateOption> options = state.getOptions();
            for (StateOption stateOption : options) {
                String optionLabel = thingTypeI18nUtil.getChannelStateOption(bundle, channelTypeUID,
                        stateOption.getValue(), stateOption.getLabel(), locale);
                localizedOptions.add(new StateOption(stateOption.getValue(), optionLabel));
            }

            return new StateDescription(state.getMinimum(), state.getMaximum(), state.getStep(), pattern,
                    state.isReadOnly(), localizedOptions);
        }
        return null;
    }

    public ChannelType createLocalizedChannelType(Bundle bundle, ChannelType channelType, Locale locale) {
        ChannelTypeUID channelTypeUID = channelType.getUID();
        String label = thingTypeI18nUtil.getChannelLabel(bundle, channelTypeUID, channelType.getLabel(), locale);
        String description = thingTypeI18nUtil.getChannelDescription(bundle, channelTypeUID,
                channelType.getDescription(), locale);

        StateDescription state = createLocalizedStateDescription(bundle, channelType, channelTypeUID, locale);

        return new ChannelType(channelTypeUID, channelType.isAdvanced(), channelType.getItemType(),
                channelType.getKind(), label, description, channelType.getCategory(), channelType.getTags(), state,
                channelType.getEvent(), channelType.getConfigDescriptionURI());
    }

    public ChannelGroupType createLocalizedChannelGroupType(Bundle bundle, ChannelGroupType channelGroupType,
            Locale locale) {
        ChannelGroupTypeUID channelGroupTypeUID = channelGroupType.getUID();
        String label = thingTypeI18nUtil.getChannelGroupLabel(bundle, channelGroupTypeUID, channelGroupType.getLabel(),
                locale);
        String description = thingTypeI18nUtil.getChannelGroupDescription(bundle, channelGroupTypeUID,
                channelGroupType.getDescription(), locale);

        List<ChannelDefinition> localizedChannelDefinitions = new ArrayList<>(
                channelGroupType.getChannelDefinitions().size());
        for (ChannelDefinition channelDefinition : channelGroupType.getChannelDefinitions()) {
            String channelLabel = thingTypeI18nUtil.getChannelLabel(bundle, channelGroupTypeUID, channelDefinition,
                    channelDefinition.getLabel(), locale);
            String channelDescription = thingTypeI18nUtil.getChannelDescription(bundle, channelGroupTypeUID,
                    channelDefinition, channelDefinition.getDescription(), locale);
            if (channelLabel == null || channelDescription == null) {
                ChannelTypeUID channelTypeUID = channelDefinition.getChannelTypeUID();
                ChannelType channelType = channelTypeRegistry.getChannelType(channelTypeUID, locale);
                if (channelType != null) {
                    if (channelLabel == null) {
                        channelLabel = thingTypeI18nUtil.getChannelLabel(bundle, channelTypeUID, channelType.getLabel(),
                                locale);
                    }
                    if (channelDescription == null) {
                        channelDescription = thingTypeI18nUtil.getChannelDescription(bundle, channelTypeUID,
                                channelType.getDescription(), locale);
                    }
                }
            }
            localizedChannelDefinitions
                    .add(new ChannelDefinition(channelDefinition.getId(), channelDefinition.getChannelTypeUID(),
                            channelDefinition.getProperties(), channelLabel, channelDescription));
        }

        return new ChannelGroupType(channelGroupTypeUID, channelGroupType.isAdvanced(), label, description,
                channelGroupType.getCategory(), localizedChannelDefinitions);
    }

    public ThingType createLocalizedThingType(Bundle bundle, ThingType thingType, @Nullable Locale locale) {
        ThingTypeUID thingTypeUID = thingType.getUID();
        String label = thingTypeI18nUtil.getLabel(bundle, thingTypeUID, thingType.getLabel(), locale);
        String description = thingTypeI18nUtil.getDescription(bundle, thingTypeUID, thingType.getDescription(), locale);

        List<ChannelDefinition> localizedChannelDefinitions = new ArrayList<>(thingType.getChannelDefinitions().size());
        for (ChannelDefinition channelDefinition : thingType.getChannelDefinitions()) {
            String channelLabel = thingTypeI18nUtil.getChannelLabel(bundle, thingTypeUID, channelDefinition,
                    channelDefinition.getLabel(), locale);
            String channelDescription = thingTypeI18nUtil.getChannelDescription(bundle, thingTypeUID, channelDefinition,
                    channelDefinition.getDescription(), locale);
            if (channelLabel == null || channelDescription == null) {
                ChannelTypeUID channelTypeUID = channelDefinition.getChannelTypeUID();
                ChannelType channelType = channelTypeRegistry.getChannelType(channelTypeUID, locale);
                if (channelType != null) {
                    if (channelLabel == null) {
                        channelLabel = thingTypeI18nUtil.getChannelLabel(bundle, channelTypeUID, channelType.getLabel(),
                                locale);
                    }
                    if (channelDescription == null) {
                        channelDescription = thingTypeI18nUtil.getChannelDescription(bundle, channelTypeUID,
                                channelType.getDescription(), locale);
                    }
                }
            }
            localizedChannelDefinitions
                    .add(new ChannelDefinition(channelDefinition.getId(), channelDefinition.getChannelTypeUID(),
                            channelDefinition.getProperties(), channelLabel, channelDescription));
        }

        List<ChannelGroupDefinition> localizedChannelGroupDefinitions = new ArrayList<>(
                thingType.getChannelGroupDefinitions().size());
        for (ChannelGroupDefinition channelGroupDefinition : thingType.getChannelGroupDefinitions()) {
            String channelGroupLabel = thingTypeI18nUtil.getChannelGroupLabel(bundle, thingTypeUID,
                    channelGroupDefinition, channelGroupDefinition.getLabel(), locale);
            String channelGroupDescription = thingTypeI18nUtil.getChannelGroupDescription(bundle, thingTypeUID,
                    channelGroupDefinition, channelGroupDefinition.getDescription(), locale);
            if (channelGroupLabel == null || channelGroupDescription == null) {
                ChannelGroupTypeUID channelGroupTypeUID = channelGroupDefinition.getTypeUID();
                ChannelGroupType channelGroupType = channelTypeRegistry.getChannelGroupType(channelGroupTypeUID,
                        locale);
                if (channelGroupType != null) {
                    if (channelGroupLabel == null) {
                        channelGroupLabel = thingTypeI18nUtil.getChannelGroupLabel(bundle, channelGroupTypeUID,
                                channelGroupType.getLabel(), locale);
                    }
                    if (channelGroupDescription == null) {
                        channelGroupDescription = thingTypeI18nUtil.getChannelGroupDescription(bundle,
                                channelGroupTypeUID, channelGroupType.getDescription(), locale);
                    }
                }
            }
            localizedChannelGroupDefinitions.add(new ChannelGroupDefinition(channelGroupDefinition.getId(),
                    channelGroupDefinition.getTypeUID(), channelGroupLabel, channelGroupDescription));
        }

        ThingTypeBuilder builder = ThingTypeBuilder.instance(thingType);
        if (label != null) {
            builder.withLabel(label);
        }
        if (description != null) {
            builder.withDescription(description);
        }
        builder.withChannelDefinitions(localizedChannelDefinitions)
                .withChannelGroupDefinitions(localizedChannelGroupDefinitions);

        if (thingType instanceof BridgeType) {
            return builder.buildBridge();
        }

        return builder.build();
    }

}
