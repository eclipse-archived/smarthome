/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.xml.internal;

import java.util.List;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.xml.XmlConfigDescriptionProvider;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentProvider;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentProviderFactory;
import org.osgi.framework.Bundle;

/**
 * The {@link ConfigDescriptionXmlProviderFactory} is responsible to create {@link ConfigDescriptionXmlProvider}
 * instances for a certain module. The
 * factory is <i>not</i> responsible to clean-up any created providers.
 *
 * @author Michael Grammling - Initial Contribution
 */
public class ConfigDescriptionXmlProviderFactory implements XmlDocumentProviderFactory<List<ConfigDescription>> {

    private XmlConfigDescriptionProvider configDescriptionProvider;

    public ConfigDescriptionXmlProviderFactory(XmlConfigDescriptionProvider configDescriptionProvider) {

        if (configDescriptionProvider == null) {
            throw new IllegalArgumentException("The XmlConfigDescriptionProvider must not be null!");
        }

        this.configDescriptionProvider = configDescriptionProvider;
    }

    @Override
    public XmlDocumentProvider<List<ConfigDescription>> createDocumentProvider(Bundle bundle) {
        return new ConfigDescriptionXmlProvider(bundle, this.configDescriptionProvider);
    }

}
