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
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.BridgeType;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.GenericChannelTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeRegistry;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.StateChannelTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeBuilder;
import org.eclipse.smarthome.core.thing.type.TriggerChannelTypeBuilder;
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

    private @Nullable StateDescription createLocalizedStateDescription(final Bundle bundle,
            final @Nullable StateDescription state, final ChannelTypeUID channelTypeUID,
            final @Nullable Locale locale) {
        if (state == null) {
            return null;
        }
        String pattern = thingTypeI18nUtil.getChannelStatePattern(bundle, channelTypeUID, state.getPattern(), locale);

        List<StateOption> localizedOptions = new ArrayList<>();
        for (final StateOption options : state.getOptions()) {
            String optionLabel = thingTypeI18nUtil.getChannelStateOption(bundle, channelTypeUID, options.getValue(),
                    options.getLabel(), locale);
            localizedOptions.add(new StateOption(options.getValue(), optionLabel));
        }

        return new StateDescription(state.getMinimum(), state.getMaximum(), state.getStep(), pattern,
                state.isReadOnly(), localizedOptions);
    }

    public ChannelType createLocalizedChannelType(Bundle bundle, ChannelType channelType, @Nullable Locale locale) {
        ChannelTypeUID channelTypeUID = channelType.getUID();
        String defaultLabel = channelType.getLabel();
        String label = thingTypeI18nUtil.getChannelLabel(bundle, channelTypeUID, defaultLabel, locale);
        String description = thingTypeI18nUtil.getChannelDescription(bundle, channelTypeUID,
                channelType.getDescription(), locale);

        switch (channelType.getKind()) {
            case STATE:
                StateDescription state = createLocalizedStateDescription(bundle, channelType.getState(), channelTypeUID,
                        locale);

                StateChannelTypeBuilder stateBuilder = GenericChannelTypeBuilder
                        .state(channelTypeUID, label == null ? defaultLabel : label, channelType.getItemType())
                        .isAdvanced(channelType.isAdvanced()).withCategory(channelType.getCategory())
                        .withConfigDescriptionURI(channelType.getConfigDescriptionURI()).withTags(channelType.getTags())
                        .withStateDescription(state);
                if (description != null) {
                    stateBuilder.withDescription(description);
                }
                return stateBuilder.build();
            case TRIGGER:
                TriggerChannelTypeBuilder triggerBuilder = GenericChannelTypeBuilder
                        .trigger(channelTypeUID, label == null ? defaultLabel : label)
                        .isAdvanced(channelType.isAdvanced()).withCategory(channelType.getCategory())
                        .withConfigDescriptionURI(channelType.getConfigDescriptionURI()).withTags(channelType.getTags())
                        .withEventDescription(channelType.getEvent());
                if (description != null) {
                    triggerBuilder.withDescription(description);
                }
                return triggerBuilder.build();
            default:
                return new ChannelType(channelTypeUID, channelType.isAdvanced(), channelType.getItemType(),
                        channelType.getKind(), label == null ? defaultLabel : label, description,
                        channelType.getCategory(), channelType.getTags(), channelType.getState(),
                        channelType.getEvent(), channelType.getConfigDescriptionURI());
        }
    }

    private List<ChannelDefinition> createLocalizedChannelDefinitions(final Bundle bundle,
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
        return localizedChannelDefinitions;
    }

    public ChannelGroupType createLocalizedChannelGroupType(Bundle bundle, ChannelGroupType channelGroupType,
            @Nullable Locale locale) {
        ChannelGroupTypeUID channelGroupTypeUID = channelGroupType.getUID();
        String defaultLabel = channelGroupType.getLabel();
        String label = thingTypeI18nUtil.getChannelGroupLabel(bundle, channelGroupTypeUID, defaultLabel, locale);
        String description = thingTypeI18nUtil.getChannelGroupDescription(bundle, channelGroupTypeUID,
                channelGroupType.getDescription(), locale);

        List<ChannelDefinition> localizedChannelDefinitions = createLocalizedChannelDefinitions(bundle,
                channelGroupType.getChannelDefinitions(),
                channelDefinition -> thingTypeI18nUtil.getChannelLabel(bundle, channelGroupTypeUID, channelDefinition,
                        channelDefinition.getLabel(), locale),
                channelDefinition -> thingTypeI18nUtil.getChannelDescription(bundle, channelGroupTypeUID,
                        channelDefinition, channelDefinition.getDescription(), locale),
                locale);

        ChannelGroupTypeBuilder builder = ChannelGroupTypeBuilder
                .instance(channelGroupTypeUID, label == null ? defaultLabel : label)
                .isAdvanced(channelGroupType.isAdvanced()).withCategory(channelGroupType.getCategory())
                .withChannelDefinitions(localizedChannelDefinitions);
        if (description != null) {
            builder.withDescription(description);
        }
        return builder.build();
    }

    private List<ChannelGroupDefinition> createLocalizedChannelGroupDefinitions(final Bundle bundle,
            final List<ChannelGroupDefinition> channelGroupDefinitions,
            final Function<ChannelGroupDefinition, @Nullable String> channelGroupLabelResolver,
            final Function<ChannelGroupDefinition, @Nullable String> channelGroupDescriptionResolver,
            final @Nullable Locale locale) {
        List<ChannelGroupDefinition> localizedChannelGroupDefinitions = new ArrayList<>(channelGroupDefinitions.size());
        for (final ChannelGroupDefinition channelGroupDefinition : channelGroupDefinitions) {
            String channelGroupLabel = channelGroupLabelResolver.apply(channelGroupDefinition);
            String channelGroupDescription = channelGroupDescriptionResolver.apply(channelGroupDefinition);
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
        return localizedChannelGroupDefinitions;
    }

    public ThingType createLocalizedThingType(Bundle bundle, ThingType thingType, @Nullable Locale locale) {
        ThingTypeUID thingTypeUID = thingType.getUID();
        String label = thingTypeI18nUtil.getLabel(bundle, thingTypeUID, thingType.getLabel(), locale);
        String description = thingTypeI18nUtil.getDescription(bundle, thingTypeUID, thingType.getDescription(), locale);

        List<ChannelDefinition> localizedChannelDefinitions = createLocalizedChannelDefinitions(bundle,
                thingType.getChannelDefinitions(),
                channelDefinition -> thingTypeI18nUtil.getChannelLabel(bundle, thingTypeUID, channelDefinition,
                        channelDefinition.getLabel(), locale),
                channelDefinition -> thingTypeI18nUtil.getChannelDescription(bundle, thingTypeUID, channelDefinition,
                        channelDefinition.getDescription(), locale),
                locale);

        List<ChannelGroupDefinition> localizedChannelGroupDefinitions = createLocalizedChannelGroupDefinitions(bundle,
                thingType.getChannelGroupDefinitions(),
                channelGroupDefinition -> thingTypeI18nUtil.getChannelGroupLabel(bundle, thingTypeUID,
                        channelGroupDefinition, channelGroupDefinition.getLabel(), locale),
                channelGroupDefinition -> thingTypeI18nUtil.getChannelGroupDescription(bundle, thingTypeUID,
                        channelGroupDefinition, channelGroupDefinition.getDescription(), locale),
                locale);

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
