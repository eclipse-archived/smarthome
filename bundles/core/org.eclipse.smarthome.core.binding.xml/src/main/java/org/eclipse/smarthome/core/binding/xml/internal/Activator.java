/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.binding.xml.internal;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.xml.XmlConfigDescriptionProvider;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentBundleTracker;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentProviderFactory;
import org.eclipse.smarthome.config.xml.util.XmlDocumentReader;
import org.eclipse.smarthome.core.binding.BindingInfo;
import org.eclipse.smarthome.core.binding.BindingInfoProvider;
import org.eclipse.smarthome.core.common.osgi.ServiceBinder;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The {@link Activator} class is responsible to activate this module.
 * <p>
 * This module tracks any XML documents of other bundles in the {@link #XML_DIRECTORY} folder and tries to extract
 * binding information from them. Any {@link BindingInfo} objects are registered at the {@link XmlBindingInfoProvider}
 * which itself is registered as service at the <i>OSGi</i> service registry and unregistered again, if the providing
 * bundle or this bundle is stopped.
 * <p>
 * If the binding information contains a {@code config-description} section, the according {@link ConfigDescription}
 * object is added to the {@link XmlConfigDescriptionProvider} and removed again, if the providing bundle or this bundle
 * is stopped.<br>
 * The {@link XmlConfigDescriptionProvider} is registered itself as <i>OSGi</i> service at the service registry.
 *
 * @author Michael Grammling - Initial Contribution
 */
public class Activator implements BundleActivator {

    private static final String XML_DIRECTORY = "/ESH-INF/binding/";

    private ServiceRegistration<?> bindingInfoProviderReg;
    private ServiceRegistration<?> configDescriptionProviderReg;

    private ServiceBinder bindinginfoI18nProviderServiceBinder;
    private ServiceBinder configDescriptionI18nProviderServiceBinder;

    private XmlDocumentBundleTracker<BindingInfoXmlResult> bindingInfoTracker;

    @Override
    public void start(BundleContext context) throws Exception {
        XmlBindingInfoProvider bindingInfoProvider = new XmlBindingInfoProvider();
        this.bindinginfoI18nProviderServiceBinder = new ServiceBinder(context, bindingInfoProvider);
        this.bindinginfoI18nProviderServiceBinder.open();

        XmlConfigDescriptionProvider configDescriptionProvider = new XmlConfigDescriptionProvider();
        this.configDescriptionI18nProviderServiceBinder = new ServiceBinder(context, configDescriptionProvider);
        this.configDescriptionI18nProviderServiceBinder.open();

        XmlDocumentReader<BindingInfoXmlResult> bindingInfoReader = new BindingInfoReader();

        XmlDocumentProviderFactory<BindingInfoXmlResult> bindingInfoProviderFactory = new BindingInfoXmlProviderFactory(
                bindingInfoProvider, configDescriptionProvider);

        this.bindingInfoTracker = new XmlDocumentBundleTracker<>(context, XML_DIRECTORY, bindingInfoReader,
                bindingInfoProviderFactory);

        this.bindingInfoTracker.open();

        this.configDescriptionProviderReg = context.registerService(ConfigDescriptionProvider.class.getName(),
                configDescriptionProvider, null);

        this.bindingInfoProviderReg = context.registerService(BindingInfoProvider.class.getName(), bindingInfoProvider,
                null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        this.bindingInfoProviderReg.unregister();
        this.bindingInfoProviderReg = null;

        this.configDescriptionProviderReg.unregister();
        this.configDescriptionProviderReg = null;

        this.bindingInfoTracker.close();

        this.configDescriptionI18nProviderServiceBinder.close();
        this.bindinginfoI18nProviderServiceBinder.close();
    }

}
