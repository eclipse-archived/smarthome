/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.xml.AbstractXmlBasedProvider;
import org.eclipse.smarthome.config.xml.AbstractXmlConfigDescriptionProvider;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentBundleTracker;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentProvider;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentProviderFactory;
import org.eclipse.smarthome.config.xml.util.XmlDocumentReader;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.i18n.ThingTypeI18nLocalizationService;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link XmlThingTypeProvider} is a concrete implementation of the {@link ThingTypeProvider} service interface.
 * <p>
 * This implementation manages any {@link ThingType} objects associated to specific modules. If a specific module
 * disappears, any registered {@link ThingType} objects associated with that module are released.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Dennis Nobel - Added locale support, Added cache for localized thing types
 * @author Ivan Iliev - Added support for system wide channel types
 * @author Kai Kreuzer - fixed concurrency issues
 * @author Simon Kaufmann - factored out common aspects into {@link AbstractXmlBasedProvider}
 */
@Component(immediate = true, property = { "esh.scope=core.xml.thing" })
public class XmlThingTypeProvider extends AbstractXmlBasedProvider<UID, ThingType>
        implements ThingTypeProvider, XmlDocumentProviderFactory<List<?>> {

    private static final String XML_DIRECTORY = "/ESH-INF/thing/";
    public static final String READY_MARKER = "esh.xmlThingTypes";

    private ThingTypeI18nLocalizationService thingTypeI18nLocalizationService;
    private AbstractXmlConfigDescriptionProvider configDescriptionProvider;
    private XmlChannelTypeProvider channelTypeProvider;
    private XmlChannelGroupTypeProvider channelGroupTypeProvider;

    private XmlDocumentBundleTracker<List<?>> thingTypeTracker;

    @Activate
    protected void activate(BundleContext bundleContext) {
        XmlDocumentReader<List<?>> thingTypeReader = new ThingDescriptionReader();

        thingTypeTracker = new XmlDocumentBundleTracker<List<?>>(bundleContext, XML_DIRECTORY, thingTypeReader, this,
                READY_MARKER);
        thingTypeTracker.open();
    }

    @Deactivate
    protected void deactivate() {
        thingTypeTracker.close();
        thingTypeTracker = null;
    }

    @Override
    public ThingType getThingType(ThingTypeUID thingTypeUID, Locale locale) {
        return get(thingTypeUID, locale);
    }

    @Override
    public synchronized Collection<ThingType> getThingTypes(Locale locale) {
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

    @Reference(target = "(esh.scope=core.xml.thing)")
    public void setConfigDescriptionProvider(ConfigDescriptionProvider configDescriptionProvider) {
        this.configDescriptionProvider = (AbstractXmlConfigDescriptionProvider) configDescriptionProvider;
    }

    public void unsetConfigDescriptionProvider(ConfigDescriptionProvider configDescriptionProvider) {
        this.configDescriptionProvider = null;
    }

    @Reference(target = "(esh.scope=core.xml.channels)")
    public void setChannelTypeProvider(ChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = (XmlChannelTypeProvider) channelTypeProvider;
    }

    public void unsetChannelTypeProvider(ChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = null;
    }

    @Reference(target = "(esh.scope=core.xml.channelGroups)")
    public void setChannelGroupTypeProvider(ChannelTypeProvider channelGroupTypeProvider) {
        this.channelGroupTypeProvider = (XmlChannelGroupTypeProvider) channelGroupTypeProvider;
    }

    public void unsetChannelGroupTypeProvider(ChannelTypeProvider channelGroupTypeProvider) {
        this.channelGroupTypeProvider = null;
    }

    @Override
    protected ThingType localize(Bundle bundle, ThingType thingType, Locale locale) {
        if (thingTypeI18nLocalizationService == null) {
            return null;
        }
        return thingTypeI18nLocalizationService.createLocalizedThingType(bundle, thingType, locale);
    }

    @Override
    public XmlDocumentProvider<List<?>> createDocumentProvider(Bundle bundle) {
        return new ThingTypeXmlProvider(bundle, configDescriptionProvider, this, channelTypeProvider,
                channelGroupTypeProvider);
    }

}
