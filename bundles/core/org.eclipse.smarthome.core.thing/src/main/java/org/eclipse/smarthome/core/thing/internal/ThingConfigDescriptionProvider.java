/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
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
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeRegistry;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides a proxy for thing & channel configuration descriptions.
 *
 * If a thing config description is requested, the provider will look up the thing/channel type
 * to get the configURI and the config description for it. If there is a corresponding {@link ConfigOptionProvider}, it
 * will be used to get updated options.
 *
 * @author Chris Jackson - Initial Implementation
 * @author Chris Jackson - Updated to separate thing type from thing name
 * @author Simon Kaufmann - Added support for channel config descriptions
 *
 */
@Component
public class ThingConfigDescriptionProvider implements ConfigDescriptionProvider {
    private ThingRegistry thingRegistry;
    private ThingTypeRegistry thingTypeRegistry;
    private ConfigDescriptionRegistry configDescriptionRegistry;
    private ChannelTypeRegistry channelTypeRegistry;

    @Reference
    protected void setConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        this.configDescriptionRegistry = configDescriptionRegistry;
    }

    protected void unsetConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        this.configDescriptionRegistry = null;
    }

    @Reference
    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

    @Reference
    protected void setThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = thingTypeRegistry;
    }

    protected void unsetThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = null;
    }

    @Reference
    protected void setChannelTypeRegistry(ChannelTypeRegistry channelTypeRegistry) {
        this.channelTypeRegistry = channelTypeRegistry;
    }

    protected void unsetChannelTypeRegistry(ChannelTypeRegistry channelTypeRegistry) {
        this.channelTypeRegistry = null;
    }

    @Override
    public Collection<ConfigDescription> getConfigDescriptions(Locale locale) {
        return Collections.emptySet();
    }

    @Override
    public ConfigDescription getConfigDescription(URI uri, Locale locale) {
        // If this is not a concrete thing, then return
        if (uri == null || uri.getScheme() == null) {
            return null;
        }

        switch (uri.getScheme()) {
            case "thing":
                return getThingConfigDescription(uri, locale);
            case "channel":
                return getChannelConfigDescription(uri, locale);
            default:
                return null;
        }
    }

    private ConfigDescription getThingConfigDescription(URI uri, Locale locale) {
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

    private ConfigDescription getChannelConfigDescription(URI uri, Locale locale) {
        String stringUID = uri.getSchemeSpecificPart();
        if (uri.getFragment() != null) {
            stringUID = stringUID + "#" + uri.getFragment();
        }
        ChannelUID channelUID = new ChannelUID(stringUID);
        ThingUID thingUID = channelUID.getThingUID();

        // First, get the thing so we get access to the channel type via the channel
        Thing thing = thingRegistry.get(thingUID);
        if (thing == null) {
            return null;
        }

        Channel channel = thing.getChannel(channelUID.getId());
        if (channel == null) {
            return null;
        }

        ChannelType channelType = channelTypeRegistry.getChannelType(channel.getChannelTypeUID());
        if (channelType == null) {
            return null;
        }

        // Get the config description URI for this channel type
        URI configURI = channelType.getConfigDescriptionURI();
        if (configURI == null) {
            return null;
        }

        // Now get the channel type's config description
        ConfigDescription config = configDescriptionRegistry.getConfigDescription(configURI, locale);
        if (config == null) {
            return null;
        }

        // Return the new configuration description
        return config;
    }

}
