/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal;

import java.util.List;

import org.eclipse.smarthome.config.xml.XmlConfigDescriptionProvider;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentProvider;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentProviderFactory;
import org.osgi.framework.Bundle;

/**
 * The {@link ThingTypeXmlProviderFactory} is responsible to create {@link ThingTypeXmlProvider} instances for a certain
 * module. The factory is <i>not</i> responsible to clean-up any created
 * providers.
 *
 * @author Michael Grammling - Initial Contribution
 */
public class ThingTypeXmlProviderFactory implements XmlDocumentProviderFactory<List<?>> {

    private XmlConfigDescriptionProvider configDescriptionProvider;
    private XmlThingTypeProvider thingTypeProvider;
    private XmlChannelTypeProvider channelTypeProvider;

    public ThingTypeXmlProviderFactory(XmlConfigDescriptionProvider configDescriptionProvider,
            XmlThingTypeProvider thingTypeProvider, XmlChannelTypeProvider channelTypeProvider)
                    throws IllegalArgumentException {

        if (configDescriptionProvider == null) {
            throw new IllegalArgumentException("The XmlConfigDescriptionProvider must not be null!");
        }

        if (thingTypeProvider == null) {
            throw new IllegalArgumentException("The XmlThingTypeProvider must not be null!");
        }

        if (channelTypeProvider == null) {
            throw new IllegalArgumentException("The XmlChannelTypeProvider must not be null!");
        }

        this.configDescriptionProvider = configDescriptionProvider;
        this.thingTypeProvider = thingTypeProvider;
        this.channelTypeProvider = channelTypeProvider;
    }

    @Override
    public XmlDocumentProvider<List<?>> createDocumentProvider(Bundle bundle) {
        return new ThingTypeXmlProvider(bundle, this.configDescriptionProvider, this.thingTypeProvider,
                this.channelTypeProvider);
    }

}
