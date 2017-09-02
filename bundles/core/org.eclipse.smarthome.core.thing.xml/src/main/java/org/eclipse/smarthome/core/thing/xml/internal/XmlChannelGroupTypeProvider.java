/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal;

import java.util.Collection;
import java.util.Collections;
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
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * {@link XmlChannelGroupTypeProvider} provides channel group types from XML files.
 *
 * @author Simon Kaufmann - factored out from {@link XmlChannelTypeProvider}
 */
@Component(immediate = true, property = { "esh.scope=core.xml.channelGroups" })
public class XmlChannelGroupTypeProvider extends AbstractXmlBasedProvider<UID, ChannelGroupType>
        implements ChannelTypeProvider {

    private ThingTypeI18nUtil thingTypeI18nUtil;

    @Override
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
        return get(channelGroupTypeUID, locale);
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(Locale locale) {
        return getAll(locale);
    }

    @Reference
    public void setI18nProvider(TranslationProvider i18nProvider) {
        this.thingTypeI18nUtil = new ThingTypeI18nUtil(i18nProvider);
    }

    public void unsetI18nProvider(TranslationProvider i18nProvider) {
        this.thingTypeI18nUtil = null;
    }

    @Override
    protected ChannelGroupType localize(Bundle bundle, ChannelGroupType channelGroupType, Locale locale) {
        if (this.thingTypeI18nUtil == null) {
            return null;
        }

        ChannelGroupTypeUID channelGroupTypeUID = channelGroupType.getUID();
        String label = this.thingTypeI18nUtil.getChannelGroupLabel(bundle, channelGroupTypeUID,
                channelGroupType.getLabel(), locale);
        String description = this.thingTypeI18nUtil.getChannelGroupDescription(bundle, channelGroupTypeUID,
                channelGroupType.getDescription(), locale);

        ChannelGroupType localizedChannelGroupType = new ChannelGroupType(channelGroupTypeUID,
                channelGroupType.isAdvanced(), label, description, channelGroupType.getCategory(),
                channelGroupType.getChannelDefinitions());
        return localizedChannelGroupType;
    }

    @Override
    public Collection<ChannelType> getChannelTypes(Locale locale) {
        return Collections.emptyList();
    }

    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        return null;
    }

}
