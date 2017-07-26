/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.eclipse.smarthome.config.xml.AbstractXmlBasedProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.thing.i18n.ThingTypeI18nUtil;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * {@link XmlChannelTypeProvider} provides channel types from XML files.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Kai Kreuzer - fixed concurrency issues
 * @author Simon Kaufmann - factored out common aspects into {@link AbstractXmlBasedProvider}
 */
@Component(immediate = true, property = { "esh.scope=core.xml.channels" })
public class XmlChannelTypeProvider extends AbstractXmlBasedProvider<UID, ChannelType> implements ChannelTypeProvider {

    private ThingTypeI18nUtil thingTypeI18nUtil;

    @Override
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
        return null;
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(Locale locale) {
        return Collections.emptyList();
    }

    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        return get(channelTypeUID, locale);
    }

    @Override
    public synchronized Collection<ChannelType> getChannelTypes(Locale locale) {
        return getAll(locale);
    }

    @Reference
    public void setTranslationProvider(TranslationProvider i18nProvider) {
        this.thingTypeI18nUtil = new ThingTypeI18nUtil(i18nProvider);
    }

    public void unsetTranslationProvider(TranslationProvider i18nProvider) {
        this.thingTypeI18nUtil = null;
    }

    private StateDescription createLocalizedChannelState(Bundle bundle, ChannelType channelType,
            ChannelTypeUID channelTypeUID, Locale locale) {

        StateDescription state = channelType.getState();

        if (state != null) {
            String pattern = this.thingTypeI18nUtil.getChannelStatePattern(bundle, channelTypeUID, state.getPattern(),
                    locale);

            List<StateOption> localizedOptions = new ArrayList<>();
            List<StateOption> options = state.getOptions();
            for (StateOption stateOption : options) {
                String optionLabel = this.thingTypeI18nUtil.getChannelStateOption(bundle, channelTypeUID,
                        stateOption.getValue(), stateOption.getLabel(), locale);
                localizedOptions.add(new StateOption(stateOption.getValue(), optionLabel));
            }

            return new StateDescription(state.getMinimum(), state.getMaximum(), state.getStep(), pattern,
                    state.isReadOnly(), localizedOptions);
        }
        return null;
    }

    @Override
    protected ChannelType localize(Bundle bundle, ChannelType channelType, Locale locale) {
        if (thingTypeI18nUtil == null) {
            return null;
        }

        ChannelTypeUID channelTypeUID = channelType.getUID();
        String label = thingTypeI18nUtil.getChannelLabel(bundle, channelTypeUID, channelType.getLabel(), locale);
        String description = thingTypeI18nUtil.getChannelDescription(bundle, channelTypeUID,
                channelType.getDescription(), locale);
        StateDescription state = createLocalizedChannelState(bundle, channelType, channelTypeUID, locale);

        return new ChannelType(channelTypeUID, channelType.isAdvanced(), channelType.getItemType(),
                channelType.getKind(), label, description, channelType.getCategory(), channelType.getTags(), state,
                channelType.getEvent(), channelType.getConfigDescriptionURI());
    }

}
