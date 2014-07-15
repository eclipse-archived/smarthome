/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.binding.xml.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.xml.XmlConfigDescriptionProvider;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentProvider;
import org.eclipse.smarthome.core.binding.BindingInfo;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The {@link BindingInfoXmlProvider} is is responsible managing any created
 * objects by a {@link BindingInfoReader} for a certain bundle.
 * <p>
 * This implementation registers each {@link BindingInfo} object as service
 * at the <i>OSGi</i> service registry.
 * <p>
 * If there is a {@link ConfigDescription} object within the {@link BindingInfoXmlResult}
 * object, it is added to the {@link XmlConfigDescriptionProvider} which is itself
 * registered as <i>OSGi</i> service at the service registry.
 * 
 * @author Michael Grammling - Initial Contribution
 * 
 * @see BindingInfoXmlProviderFactory
 */
public class BindingInfoXmlProvider implements XmlDocumentProvider<BindingInfoXmlResult> {

    private Logger logger = LoggerFactory.getLogger(BindingInfoXmlProvider.class);

    private BundleContext bundleContext;
    private Bundle bundle;

    private XmlConfigDescriptionProvider configDescriptionProvider;

    private List<ServiceRegistration<?>> serviceRegistrationList;


    public BindingInfoXmlProvider(BundleContext bundleContext, Bundle bundle,
            XmlConfigDescriptionProvider configDescriptionProvider)
            throws IllegalArgumentException {

        if (bundleContext == null) {
            throw new IllegalArgumentException("The BundleContext must not be null!");
        }

        if (bundle == null) {
            throw new IllegalArgumentException("The Bundle must not be null!");
        }

        if (configDescriptionProvider == null) {
            throw new IllegalArgumentException("The XmlConfigDescriptionProvider must not be null!");
        }

        this.bundleContext = bundleContext;
        this.bundle = bundle;

        this.configDescriptionProvider = configDescriptionProvider;

        this.serviceRegistrationList = new ArrayList<>(10);
    }

    @Override
    public synchronized void addingObject(BindingInfoXmlResult bindingInfoXmlResult) {
        if (bindingInfoXmlResult != null) {
            ConfigDescription configDescription = bindingInfoXmlResult.getConfigDescription();

            if (configDescription != null) {
                try {
                    this.configDescriptionProvider.addConfigDescription(
                            this.bundle, configDescription);
                } catch (Exception ex) {
                    this.logger.error("Could not register ConfigDescription!", ex);
                }
            }

            try {
                BindingInfo bindingInfo = bindingInfoXmlResult.getBindingInfo();
    
                ServiceRegistration<?> bindingInfoReg = this.bundleContext.registerService(
                        BindingInfo.class.getName(), bindingInfo, null);
        
                this.serviceRegistrationList.add(bindingInfoReg);
            } catch (Exception ex) {
                this.logger.error("Could not register BindingInfo!", ex);
            }
        }
    }

    @Override
    public void addingFinished() {
        // nothing to do
    }

    @Override
    public synchronized void release() {
        for (int index = this.serviceRegistrationList.size() - 1; index >= 0; index--) {
            try {
                ServiceRegistration<?> bindingInfoReg = this.serviceRegistrationList.get(index);
                bindingInfoReg.unregister();
            } catch (Exception ex) {
                this.logger.error("Could not unregister BindingInfo!", ex);
            }
        }

        this.serviceRegistrationList.clear();
        this.configDescriptionProvider.removeAllConfigDescriptions(this.bundle);
    }

}
