/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.binding.xml.internal;

import org.eclipse.smarthome.config.xml.XmlConfigDescriptionProvider;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentProvider;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentProviderFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;


/**
 * The {@link BindingInfoXmlProviderFactory} is responsible to create
 * {@link BindingInfoXmlProvider} instances for a certain module.
 * The factory is <i>not</i> responsible to clean-up any created providers.
 * 
 * @author Michael Grammling - Initial Contribution
 */
public class BindingInfoXmlProviderFactory
        implements XmlDocumentProviderFactory<BindingInfoXmlResult> {

    private BundleContext bundleContext;
    private XmlConfigDescriptionProvider configDescriptionProvider;


    public BindingInfoXmlProviderFactory(BundleContext bundleContext,
            XmlConfigDescriptionProvider configDescriptionProvider)
            throws IllegalArgumentException {

        if (bundleContext == null) {
            throw new IllegalArgumentException("The BundleContext must not be null!");
        }

        if (configDescriptionProvider == null) {
            throw new IllegalArgumentException("The XmlConfigDescriptionProvider must not be null!");
        }

        this.bundleContext = bundleContext;
        this.configDescriptionProvider = configDescriptionProvider;
    }

    @Override
    public XmlDocumentProvider<BindingInfoXmlResult> createDocumentProvider(Bundle bundle) {
        return new BindingInfoXmlProvider(
                this.bundleContext, bundle, this.configDescriptionProvider);
    }

}
