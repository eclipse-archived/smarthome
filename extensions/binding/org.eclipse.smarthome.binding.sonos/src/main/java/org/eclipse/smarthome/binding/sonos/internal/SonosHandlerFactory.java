/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.sonos.internal;

import static org.eclipse.smarthome.binding.sonos.config.ZonePlayerConfiguration.UDN;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.binding.sonos.SonosBindingConstants;
import org.eclipse.smarthome.binding.sonos.handler.ZonePlayerHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.audio.AudioHTTPServer;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.net.HttpServiceUtil;
import org.eclipse.smarthome.core.net.NetUtil;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private AudioHTTPServer audioHTTPServer;

    private Map<String, ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();

    // optional OPML URL that can be configured through configuration admin
    private String opmlUrl = null;

    // url (scheme+server+port) to use for playing notification sounds
    private String callbackUrl = null;

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        Dictionary<String, Object> properties = componentContext.getProperties();
        opmlUrl = (String) properties.get("opmlUrl");
        callbackUrl = (String) properties.get("callbackUrl");
    };

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {

        if (SonosBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            ThingUID sonosDeviceUID = getPlayerUID(thingTypeUID, thingUID, configuration);
            logger.debug("Creating a sonos thing with ID '{}'", sonosDeviceUID);
            return super.createThing(thingTypeUID, configuration, sonosDeviceUID, null);
        }
        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the sonos binding.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {

        return SonosBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SonosBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            logger.debug("Creating a ZonePlayerHandler for thing '{}' with UDN '{}'", thing.getUID(),
                    thing.getConfiguration().get(UDN));

            ZonePlayerHandler handler = new ZonePlayerHandler(thing, upnpIOService, discoveryServiceRegistry, opmlUrl);

            // register the speaker as an audio sink
            String callbackUrl = createCallbackUrl();
            SonosAudioSink audioSink = new SonosAudioSink(handler, audioHTTPServer, callbackUrl);
            @SuppressWarnings("unchecked")
            ServiceRegistration<AudioSink> reg = (ServiceRegistration<AudioSink>) bundleContext
                    .registerService(AudioSink.class.getName(), audioSink, new Hashtable<String, Object>());
            audioSinkRegistrations.put(thing.getUID().toString(), reg);

            return handler;
        }
        return null;
    }

    private String createCallbackUrl() {
        if (callbackUrl != null) {
            return callbackUrl;
        } else {
            final String ipAddress = NetUtil.getLocalIpv4HostAddress();
            if (ipAddress == null) {
                logger.warn("No network interface could be found.");
                return null;
            }

            // we do not use SSL as it can cause certificate validation issues.
            final int port = HttpServiceUtil.getHttpServicePort(bundleContext);
            if (port == -1) {
                logger.warn("Cannot find port of the http service.");
                return null;
            }

            return "http://" + ipAddress + ":" + port;
        }
    }

    @Override
    public void unregisterHandler(Thing thing) {
        super.unregisterHandler(thing);
        ServiceRegistration<AudioSink> reg = audioSinkRegistrations.get(thing.getUID().toString());
        if (reg != null) {
            reg.unregister();
        }
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

    protected void setAudioHTTPServer(AudioHTTPServer audioHTTPServer) {
        this.audioHTTPServer = audioHTTPServer;
    }

    protected void unsetAudioHTTPServer(AudioHTTPServer audioHTTPServer) {
        this.audioHTTPServer = null;
    }

}
