/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.wemo.handler;

import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.*;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOParticipant;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WemoBridgeHandler} is the handler for a wemo bridge and connects it to
 * the framework.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
public class WemoBridgeHandler extends BaseBridgeHandler implements UpnpIOParticipant {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);

    private Logger logger = LoggerFactory.getLogger(WemoBridgeHandler.class);

    public WemoBridgeHandler(Bridge bridge, UpnpIOService upnpIOService) {
        super(bridge);

        logger.debug("Creating a WemoBridgeHandler for thing '{}'", getThing().getUID());

    }

    @Override
    public void initialize() {

        logger.debug("Initializing WemoBridgeHandler");

        Configuration configuration = getConfig();

        if (configuration.get("udn") != null) {
            logger.trace("Initializing WemoBridgeHandler for UDN '{}'", configuration.get("udn"));
            super.initialize();

            for (Thing thing : getThing().getThings()) {
                ThingHandler handler = thing.getHandler();
                if (handler != null) {
                    handler.initialize();
                }
            }

        } else {
            logger.debug("Cannot initalize WemoBridgeHandler. UDN not set.");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Not needed, all commands are handled in the {@link WemoLightHandler}
    }

    @Override
    public String getUDN() {
        return (String) this.getThing().getConfiguration().get(UDN);
    }

    @Override
    public void onValueReceived(String variable, String value, String service) {
        // Not needed, all commands are handled in the {@link WemoLightHandler}
    }

    @Override
    public void onStatusChanged(boolean status) {
        // Not needed, all commands are handled in the {@link WemoLightHandler}
    }
}
