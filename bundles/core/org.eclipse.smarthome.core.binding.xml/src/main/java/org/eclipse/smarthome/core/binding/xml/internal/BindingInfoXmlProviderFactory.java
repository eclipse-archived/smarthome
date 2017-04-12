/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.binding.xml.internal;

import org.eclipse.smarthome.config.xml.AbstractXmlConfigDescriptionProvider;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentProvider;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentProviderFactory;
import org.osgi.framework.Bundle;

/**
 * The {@link BindingInfoXmlProviderFactory} is responsible to create {@link BindingInfoXmlProvider} instances for a
 * certain module.
 * The factory is <i>not</i> responsible to clean-up any created providers.
 *
 * @author Michael Grammling - Initial Contribution
 */
public class BindingInfoXmlProviderFactory implements XmlDocumentProviderFactory<BindingInfoXmlResult> {

    private XmlBindingInfoProvider bindingInfoProvider;
    private AbstractXmlConfigDescriptionProvider configDescriptionProvider;

    public BindingInfoXmlProviderFactory(XmlBindingInfoProvider bindingInfoProvider,
            AbstractXmlConfigDescriptionProvider configDescriptionProvider) throws IllegalArgumentException {

        if (bindingInfoProvider == null) {
            throw new IllegalArgumentException("The XmlBindingInfoProvider must not be null!");
        }

        if (configDescriptionProvider == null) {
            throw new IllegalArgumentException("The XmlConfigDescriptionProvider must not be null!");
        }

        this.bindingInfoProvider = bindingInfoProvider;
        this.configDescriptionProvider = configDescriptionProvider;
    }

    @Override
    public XmlDocumentProvider<BindingInfoXmlResult> createDocumentProvider(Bundle bundle) {
        return new BindingInfoXmlProvider(bundle, this.bindingInfoProvider, this.configDescriptionProvider);
    }

}
