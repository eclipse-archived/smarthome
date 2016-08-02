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
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.xml.XmlConfigDescriptionProvider;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentProvider;
import org.osgi.framework.Bundle;

/**
 * The {@link ConfigDescriptionXmlProvider} is responsible managing any created
 * objects by a {@link ConfigDescriptionReader} for a certain bundle.
 * <p>
 * This implementation registers each {@link ConfigDescription} object at the {@link XmlConfigDescriptionProvider} which
 * is itself registered as {@link ConfigDescriptionProvider} service at the <i>OSGi</i> service registry.
 *
 * @author Michael Grammling - Initial Contribution
 *
 * @see ConfigDescriptionXmlProviderFactory
 */
public class ConfigDescriptionXmlProvider implements XmlDocumentProvider<List<ConfigDescription>> {

    private Bundle bundle;
    private XmlConfigDescriptionProvider configDescriptionProvider;

    public ConfigDescriptionXmlProvider(Bundle bundle, XmlConfigDescriptionProvider configDescriptionProvider)
            throws IllegalArgumentException {

        if (bundle == null) {
            throw new IllegalArgumentException("The Bundle must not be null!");
        }

        if (configDescriptionProvider == null) {
            throw new IllegalArgumentException("The XmlConfigDescriptionProvider must not be null!");
        }

        this.bundle = bundle;
        this.configDescriptionProvider = configDescriptionProvider;
    }

    @Override
    public synchronized void addingObject(List<ConfigDescription> configDescriptions) {
        this.configDescriptionProvider.addConfigDescriptions(this.bundle, configDescriptions);
    }

    @Override
    public void addingFinished() {
        // nothing to do
    }

    @Override
    public synchronized void release() {
        this.configDescriptionProvider.removeAllConfigDescriptions(this.bundle);
    }

}
