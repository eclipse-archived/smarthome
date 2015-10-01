/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal;

import java.util.List;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.xml.XmlConfigDescriptionProvider;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentBundleTracker;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentProviderFactory;
import org.eclipse.smarthome.config.xml.util.XmlDocumentReader;
import org.eclipse.smarthome.core.common.osgi.ServiceBinder;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The {@link Activator} class is responsible to activate this module.
 * <p>
 * This module tracks any XML documents of other bundles in the {@link #XML_DIRECTORY} folder and tries to extract thing
 * description information from them. Any {@link ThingType} objects are registered at the {@link XmlThingTypeProvider}
 * which itself is registered as service at the <i>OSGi</i> service registry and unregistered again, if the providing
 * bundle or this bundle is stopped.
 * <p>
 * If the thing description information contains a {@code config-description} section, the according
 * {@link ConfigDescription} object is added to the {@link XmlConfigDescriptionProvider} and removed again, if the
 * providing bundle or this bundle is stopped.<br>
 * The {@link XmlConfigDescriptionProvider} is registered itself as service at the <i>OSGi</i> service registry.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Ivan Iliev - Added support for system wide channel types
 * @author Dennis Nobel - Added XMLChannelTypeProvider
 */
public class Activator implements BundleActivator {

    private static final String XML_DIRECTORY = "/ESH-INF/thing/";

    private ServiceBinder channelTypeI18nProviderServiceBinder;
    private ServiceRegistration<?> channelTypeProviderReg;

    private ServiceBinder configDescriptionI18nProviderServiceBinder;
    private ServiceRegistration<?> configDescriptionProviderReg;

    private ServiceBinder thingTypeI18nProviderServiceBinder;

    private ServiceRegistration<?> thingTypeProviderReg;

    private XmlDocumentBundleTracker<List<?>> thingTypeTracker;

    @Override
    public void start(BundleContext context) throws Exception {

        XmlThingTypeProvider thingTypeProvider = new XmlThingTypeProvider();
        this.thingTypeI18nProviderServiceBinder = new ServiceBinder(context, thingTypeProvider);
        this.thingTypeI18nProviderServiceBinder.open();

        XmlConfigDescriptionProvider configDescriptionProvider = new XmlConfigDescriptionProvider();
        this.configDescriptionI18nProviderServiceBinder = new ServiceBinder(context, configDescriptionProvider);
        this.configDescriptionI18nProviderServiceBinder.open();

        XmlDocumentReader<List<?>> thingTypeReader = new ThingDescriptionReader();

        XmlChannelTypeProvider channelTypeProvider = new XmlChannelTypeProvider();
        this.channelTypeI18nProviderServiceBinder = new ServiceBinder(context, channelTypeProvider);
        this.channelTypeI18nProviderServiceBinder.open();

        XmlDocumentProviderFactory<List<?>> thingTypeProviderFactory = new ThingTypeXmlProviderFactory(
                configDescriptionProvider, thingTypeProvider, channelTypeProvider);

        this.thingTypeTracker = new XmlDocumentBundleTracker<>(context, XML_DIRECTORY, thingTypeReader,
                thingTypeProviderFactory);

        this.thingTypeTracker.open();

        this.configDescriptionProviderReg = context.registerService(ConfigDescriptionProvider.class.getName(),
                configDescriptionProvider, null);

        this.thingTypeProviderReg = context.registerService(ThingTypeProvider.class.getName(), thingTypeProvider, null);

        this.channelTypeProviderReg = context.registerService(ChannelTypeProvider.class.getName(), channelTypeProvider,
                null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {

        this.channelTypeProviderReg.unregister();
        this.channelTypeProviderReg = null;

        this.thingTypeProviderReg.unregister();
        this.thingTypeProviderReg = null;

        this.configDescriptionProviderReg.unregister();
        this.configDescriptionProviderReg = null;

        this.thingTypeTracker.close();

        this.channelTypeI18nProviderServiceBinder.close();
        this.configDescriptionI18nProviderServiceBinder.close();
        this.thingTypeI18nProviderServiceBinder.close();
    }

}
