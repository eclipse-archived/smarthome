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
package org.eclipse.smarthome.core.thing.xml.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.eclipse.smarthome.config.xml.AbstractXmlBasedProvider;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.thing.i18n.ThingTypeI18nLocalizationService;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * {@link XmlChannelTypeProvider} provides channel types from XML files.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Kai Kreuzer - fixed concurrency issues
 * @author Simon Kaufmann - factored out common aspects into {@link AbstractXmlBasedProvider}
 * @author Henning Treu - QuantityType implementation
 * @author Christoph Weitkamp - factored out common aspects into {@link ThingTypeI18nLocalizationService}
 */
@Component(immediate = true, property = { "esh.scope=core.xml.channels" })
public class XmlChannelTypeProvider extends AbstractXmlBasedProvider<UID, ChannelType> implements ChannelTypeProvider {

    private ThingTypeI18nLocalizationService thingTypeI18nLocalizationService;

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
    public void setThingTypeI18nLocalizationService(
            final ThingTypeI18nLocalizationService thingTypeI18nLocalizationService) {
        this.thingTypeI18nLocalizationService = thingTypeI18nLocalizationService;
    }

    public void unsetThingTypeI18nLocalizationService(
            final ThingTypeI18nLocalizationService thingTypeI18nLocalizationService) {
        this.thingTypeI18nLocalizationService = null;
    }

    @Override
    protected ChannelType localize(Bundle bundle, ChannelType channelType, Locale locale) {
        if (thingTypeI18nLocalizationService == null) {
            return null;
        }
        return thingTypeI18nLocalizationService.createLocalizedChannelType(bundle, channelType, locale);
    }

}
