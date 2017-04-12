/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.xml;

import java.util.List;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.core.i18n.ConfigI18nLocalizationService;
import org.eclipse.smarthome.config.xml.internal.ConfigDescriptionReader;
import org.eclipse.smarthome.config.xml.internal.ConfigDescriptionXmlProviderFactory;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentBundleTracker;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentProviderFactory;
import org.eclipse.smarthome.config.xml.util.XmlDocumentReader;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides {@link ConfigDescription}s for configurations which are read from XML files.
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
@Component(service = ConfigDescriptionProvider.class, immediate = true, property = { "esh.scope=core.xml.config" })
public class ConfigXmlConfigDescriptionProvider extends AbstractXmlConfigDescriptionProvider {

    private static final String XML_DIRECTORY = "/ESH-INF/config/";

    private XmlDocumentBundleTracker<List<ConfigDescription>> configDescriptionTracker;

    private ConfigI18nLocalizationService configI18nLocalizerService;

    @Activate
    public void activate(ComponentContext componentContext) {
        XmlDocumentReader<List<ConfigDescription>> configDescriptionReader = new ConfigDescriptionReader();

        XmlDocumentProviderFactory<List<ConfigDescription>> configDescriptionProviderFactory = new ConfigDescriptionXmlProviderFactory(
                this);

        configDescriptionTracker = new XmlDocumentBundleTracker<>(componentContext.getBundleContext(), XML_DIRECTORY,
                configDescriptionReader, configDescriptionProviderFactory);
        configDescriptionTracker.open();
    }

    @Deactivate
    public void deactivate() {
        configDescriptionTracker.close();
    }

    @Reference
    public void setConfigI18nLocalizerService(ConfigI18nLocalizationService configI18nLocalizerService) {
        this.configI18nLocalizerService = configI18nLocalizerService;
    }

    public void unsetConfigI18nLocalizerService(ConfigI18nLocalizationService configI18nLocalizerService) {
        this.configI18nLocalizerService = null;
    }

    @Override
    protected ConfigI18nLocalizationService getConfigI18nLocalizerService() {
        return configI18nLocalizerService;
    }
}
