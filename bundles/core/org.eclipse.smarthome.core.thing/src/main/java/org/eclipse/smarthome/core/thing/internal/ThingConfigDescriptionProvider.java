/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry;
import org.eclipse.smarthome.config.core.ConfigOptionProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;

/**
 * Provides a proxy for thing configuration descriptions.
 *
 * If a thing config description is requested, the provider will look up the thingType
 * to get the configURI, get the config description for the thingType. If the thingHandler
 * supports the {@link ConfigOptionProvider} interface, it will call the getParameterOptions
 * method to get updated options.
 *
 * @author Chris Jackson - Initial Implementation
 * @author Chris Jackson - Updated to separate thing type from thing name
 *
 */
public class ThingConfigDescriptionProvider implements ConfigDescriptionProvider {
    private ThingRegistry thingRegistry;
    private ThingTypeRegistry thingTypeRegistry;
    private ConfigDescriptionRegistry configDescriptionRegistry;

    protected void setConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        this.configDescriptionRegistry = configDescriptionRegistry;
    }

    protected void unsetConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        this.configDescriptionRegistry = null;
    }

    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

    protected void setThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = thingTypeRegistry;
    }

    protected void unsetThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = null;
    }

    @Override
    public Collection<ConfigDescription> getConfigDescriptions(Locale locale) {
        return Collections.emptySet();
    }

    @Override
    public ConfigDescription getConfigDescription(URI uri, Locale locale) {
        // If this is not a concrete thing, then return
        if (uri == null || "thing".equals(uri.getScheme()) == false) {
            return null;
        }

        // First, get the thing type so we get the generic config descriptions
        ThingUID thingUID = new ThingUID(uri.getSchemeSpecificPart());
        Thing thing = thingRegistry.get(thingUID);
        if (thing == null) {
            return null;
        }

        ThingType thingType = thingTypeRegistry.getThingType(thing.getThingTypeUID());
        if (thingType == null) {
            return null;
        }

        // Get the config description URI for this thing type
        URI configURI = thingType.getConfigDescriptionURI();
        if (configURI == null) {
            return null;
        }

        // Now call this again for the thing
        ConfigDescription config = configDescriptionRegistry.getConfigDescription(configURI, locale);
        if (config == null) {
            return null;
        }

        // Return the new configuration description
        return config;
    }

}
