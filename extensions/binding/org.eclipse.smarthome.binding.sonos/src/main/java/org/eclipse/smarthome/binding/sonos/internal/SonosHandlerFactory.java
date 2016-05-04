/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.sonos.internal;

import static org.eclipse.smarthome.binding.sonos.SonosBindingConstants.*;
import static org.eclipse.smarthome.binding.sonos.config.ZonePlayerConfiguration.UDN;

import java.util.Collection;
import java.util.Dictionary;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.smarthome.binding.sonos.handler.ZonePlayerHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;

import com.google.common.collect.Lists;

/**
 * The {@link SonosHandlerFactory} is responsible for creating things and thing
 * handlers.
 * 
 * @author Karel Goderis - Initial contribution
 */
public class SonosHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(SonosHandlerFactory.class);

    private UpnpIOService upnpIOService;
    private DiscoveryServiceRegistry discoveryServiceRegistry;

    // optional OPML URL that can be configured through configuration admin
    private String opmlUrl = null;

    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        Dictionary<String, Object> properties = componentContext.getProperties();
        opmlUrl = (String) properties.get("opmlUrl");
    };

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {

        if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            ThingUID sonosDeviceUID = getPlayerUID(thingTypeUID, thingUID, configuration);
            logger.debug("Creating a sonos thing with ID '{}'", sonosDeviceUID);
            return super.createThing(thingTypeUID, configuration, sonosDeviceUID, null);
        }
        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the sonos binding.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {

        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            logger.debug("Creating a ZonePlayerHandler for thing '{}' with UDN '{}'", thing.getUID(),
                    thing.getConfiguration().get(UDN));
            return new ZonePlayerHandler(thing, upnpIOService, discoveryServiceRegistry, opmlUrl);
        }

        return null;
    }

    private ThingUID getPlayerUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {

        String udn = (String) configuration.get(UDN);

        if (thingUID == null) {
            thingUID = new ThingUID(thingTypeUID, udn);
        }

        return thingUID;
    }

    protected void setUpnpIOService(UpnpIOService upnpIOService) {
        this.upnpIOService = upnpIOService;
    }

    protected void unsetUpnpIOService(UpnpIOService upnpIOService) {
        this.upnpIOService = null;
    }

    protected void setDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = discoveryServiceRegistry;
    }

    protected void unsetDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = null;
    }

}
