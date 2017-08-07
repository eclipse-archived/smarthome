/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lirc.internal.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.binding.lirc.LIRCBindingConstants;
import org.eclipse.smarthome.binding.lirc.handler.LIRCBridgeHandler;
import org.eclipse.smarthome.binding.lirc.internal.LIRCMessageListener;
import org.eclipse.smarthome.binding.lirc.internal.messages.LIRCButtonEvent;
import org.eclipse.smarthome.binding.lirc.internal.messages.LIRCResponse;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andrew Nagle - Initial contribution
 */
public class LIRCRemoteDiscoveryService extends AbstractDiscoveryService
        implements ExtendedDiscoveryService, LIRCMessageListener {

    private final Logger logger = LoggerFactory.getLogger(LIRCRemoteDiscoveryService.class);

    private LIRCBridgeHandler bridgeHandler;
    private DiscoveryServiceCallback discoveryServiceCallback;

    public LIRCRemoteDiscoveryService(LIRCBridgeHandler lircBridgeHandler) {
        super(LIRCBindingConstants.SUPPORTED_DEVICE_TYPES, LIRCBindingConstants.DISCOVERY_TIMOUT, true);
        this.bridgeHandler = lircBridgeHandler;
        bridgeHandler.registerMessageListener(this);
    }

    @Override
    public void setDiscoveryServiceCallback(DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback;
    }

    @Override
    protected void startScan() {
        logger.debug("Discovery service scan started");
        bridgeHandler.startDeviceDiscovery();
    }

    @Override
    public void onButtonPressed(ThingUID bridge, LIRCButtonEvent buttonEvent) {
        addRemote(bridge, buttonEvent.getRemote());
    }

    @Override
    public void onMessageReceived(ThingUID bridge, LIRCResponse message) {
        LIRCResponse response = message;
        String command = response.getCommand();
        if ("LIST".equals(command) && response.isSuccess()) {
            for (String remoteID : response.getData()) {
                addRemote(bridge, remoteID);
            }
        }
    }

    private void addRemote(ThingUID bridge, String remote) {
        ThingTypeUID uid = LIRCBindingConstants.THING_TYPE_REMOTE;
        ThingUID thingUID = new ThingUID(uid, bridge, remote);
        if (thingUID != null) {
            if (discoveryServiceCallback != null
                    && discoveryServiceCallback.getExistingDiscoveryResult(thingUID) != null) {
                // Ignore this remote as we already know about it
                logger.debug("Remote {}: Already known.", remote);
                return;
            }
            logger.trace("Remote {}: Discovered new remote.", remote);
            Map<String, Object> properties = new HashMap<>(1);
            properties.put(LIRCBindingConstants.PROPERTY_REMOTE, remote);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(remote)
                    .withBridge(bridge).withProperties(properties).build();
            thingDiscovered(discoveryResult);
        }
    }

}
