/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
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
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


/**
 * The {@link Activator} class is responsible to activate this module.
 * <p>
 * This module tracks any XML documents of other bundles in the {@link #XML_DIRECTORY}
 * folder and tries to extract thing description information from them. Any {@link ThingType}
 * objects are registered at the {@link XmlThingTypeProvider} which itself is registered as
 * service at the <i>OSGi</i> service registry and unregistered again, if the providing bundle
 * or this bundle is stopped.
 * <p>
 * If the thing description information contains a {@code config-description} section, the
 * according {@link ConfigDescription} object is added to the {@link XmlConfigDescriptionProvider}
 * and removed again, if the providing bundle or this bundle is stopped.<br>
 * The {@link XmlConfigDescriptionProvider} is registered itself as service at the <i>OSGi</i>
 * service registry.
 * 
 * @author Michael Grammling - Initial Contribution
 */
public class Activator implements BundleActivator {

    private static final String XML_DIRECTORY = "/ESH-INF/thing/";

    private ServiceRegistration<?> configDescriptionProviderReg;
    private ServiceRegistration<?> thingTypeProviderReg;

    private XmlDocumentBundleTracker<List<?>> thingTypeTracker;


    @Override
    public void start(BundleContext context) throws Exception {
        XmlConfigDescriptionProvider configDescriptionProvider = new XmlConfigDescriptionProvider();

        this.configDescriptionProviderReg = context.registerService(
                ConfigDescriptionProvider.class.getName(), configDescriptionProvider, null);

        XmlThingTypeProvider thingTypeProvider = new XmlThingTypeProvider();

        this.thingTypeProviderReg = context.registerService(
                ThingTypeProvider.class.getName(), thingTypeProvider, null);

        XmlDocumentReader<List<?>> thingTypeReader = new ThingDescriptionReader();

        XmlDocumentProviderFactory<List<?>> thingTypeProviderFactory =
                new ThingTypeXmlProviderFactory(configDescriptionProvider, thingTypeProvider);

        this.thingTypeTracker = new XmlDocumentBundleTracker<>(
                context, XML_DIRECTORY, thingTypeReader, thingTypeProviderFactory);

        this.thingTypeTracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        this.thingTypeTracker.close();

        this.thingTypeProviderReg.unregister();
        this.thingTypeProviderReg = null;

        this.configDescriptionProviderReg.unregister();
        this.configDescriptionProviderReg = null;
    }

}
