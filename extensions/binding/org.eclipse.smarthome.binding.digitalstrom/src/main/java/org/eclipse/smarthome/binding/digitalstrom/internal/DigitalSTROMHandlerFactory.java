/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal;

import static org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants.*;

import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.handler.BridgeHandler;
import org.eclipse.smarthome.binding.digitalstrom.handler.DeviceHandler;
import org.eclipse.smarthome.binding.digitalstrom.handler.SceneHandler;
import org.eclipse.smarthome.binding.digitalstrom.internal.discovery.DiscoveryServiceManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.impl.ConnectionManagerImpl;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link DigitalSTROMHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Ochel - Initial contribution
 * @author Mathias Siegele - Initial contribution
 */
public class DigitalSTROMHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(DigitalSTROMHandlerFactory.class);
    private HashMap<String, DiscoveryServiceManager> discoveryServiceManagers = new HashMap<String, DiscoveryServiceManager>();

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.union(SceneHandler.SUPPORTED_THING_TYPES,
            Sets.union(BridgeHandler.SUPPORTED_THING_TYPES, DeviceHandler.SUPPORTED_THING_TYPES));
    private HashMap<ThingUID, BridgeHandler> bridgeHandlers = null;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if (BridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID dSSUID = getBridgeThingUID(thingTypeUID, thingUID, configuration);
            if (dSSUID != null) {
                logger.info(dSSUID.toString());
                return super.createThing(thingTypeUID, configuration, dSSUID, null);
            } else {
                logger.error("Can't generate thing UID for thing type " + thingTypeUID
                        + ", because digitalSTROM-Server is not reachable. Please check these points:\n"
                        + "Are the server address and portnumber correct?\n" + "Is the server turned on?\n"
                        + "Is the network configured correctly?");
                return null;
            }
        }

        if (DeviceHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID dsDeviceUID = getDeviceUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, dsDeviceUID, bridgeUID);
        }

        if (SceneHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID dsSceneUID = getSceneUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, dsSceneUID, bridgeUID);
        }
        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the digitalSTROM binding.");
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID == null) {
            return null;
        }

        if (BridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            BridgeHandler handler = new BridgeHandler((Bridge) thing);
            if (bridgeHandlers == null) {
                bridgeHandlers = new HashMap<ThingUID, BridgeHandler>();
            }
            bridgeHandlers.put(thing.getUID(), handler);
            DiscoveryServiceManager discoveryServiceManager = new DiscoveryServiceManager(handler);
            discoveryServiceManager.registerDiscoveryServices(bundleContext);
            discoveryServiceManagers.put(handler.getThing().getUID().getAsString(), discoveryServiceManager);
            return handler;
        }

        if (DeviceHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new DeviceHandler(thing);
        }

        if (SceneHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new SceneHandler(thing);
        }
        return null;
    }

    private ThingUID getDeviceUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {
        if (StringUtils.isNotBlank((String) configuration.get(DEVICE_DSID))) {
            thingUID = new ThingUID(thingTypeUID, bridgeUID, configuration.get(DEVICE_DSID).toString());
        }
        return thingUID;
    }

    private ThingUID getSceneUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {
        if (thingUID.getId().split("-").length == 3) {
            return thingUID;
        }

        String sceneID = SceneHandler.getSceneID(configuration, bridgeHandlers.get(bridgeUID));
        switch (sceneID) {
            case SceneHandler.SCENE_WRONG:
                logger.error("Configured scene '" + configuration.get(DigitalSTROMBindingConstants.SCENE_ID)
                        + "' does not exist or can not be used, please check your configuration.");
                break;
            case SceneHandler.ZONE_WRONG:
                logger.error("Configured zone '" + configuration.get(DigitalSTROMBindingConstants.SCENE_ZONE_ID)
                        + "' does not exist, please check your configuration.");
                break;
            case SceneHandler.GROUP_WRONG:
                logger.error("Configured group '" + configuration.get(DigitalSTROMBindingConstants.SCENE_GROUP_ID)
                        + "' does not exist, please check your configuration.");
                break;
            case SceneHandler.NO_STRUC_MAN:
                logger.error("Waiting for building digitalSTROM model.");
                break;
            case SceneHandler.NO_SCENE:
                logger.error("No Scene-ID is set!");
                break;
            case SceneHandler.NO_BRIDGE:
                logger.error("No related bridge found!");
            default:
                return new ThingUID(thingTypeUID, bridgeUID, sceneID);
        }
        return null;
    }

    private ThingUID getBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {
        String dSID;
        if (StringUtils.isBlank((String) configuration.get(DS_ID))) {
            dSID = getDSSid(configuration);
            if (dSID != null) {
                configuration.put(DS_ID, dSID);
            }
        } else {
            dSID = configuration.get(DS_ID).toString();
        }
        if (dSID != null) {
            return new ThingUID(thingTypeUID, dSID);
        } else {
            return null;
        }
    }

    private String getDSSid(Configuration configuration) {
        String dsID = null;
        if (StringUtils.isNotBlank((String) configuration.get(HOST))) {
            String host = configuration.get(HOST).toString();
            String applicationToken = null;
            String user = null;
            String pw = null;

            if (StringUtils.isNotBlank((String) configuration.get(APPLICATION_TOKEN))) {
                applicationToken = configuration.get(APPLICATION_TOKEN).toString();
            }

            if (checkUserPassword(configuration)) {
                user = configuration.get(USER_NAME).toString();
                pw = configuration.get(PASSWORD).toString();
            }
            ConnectionManager connMan = new ConnectionManagerImpl(host, user, pw, applicationToken, false, true);
            // Only to get sessionToken for server versions which returns the dSID of the server only, if a user is
            // logged in
            connMan.checkConnection();
            dsID = connMan.getDigitalSTROMAPI().getDSID(connMan.getSessionToken());
        }
        return dsID;
    }

    private boolean checkUserPassword(Configuration configuration) {
        return StringUtils.isNotBlank((String) configuration.get(USER_NAME))
                && StringUtils.isNotBlank((String) configuration.get(PASSWORD));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof BridgeHandler) {
            String uid = thingHandler.getThing().getUID().getAsString();
            if (discoveryServiceManagers.get(uid) != null) {
                discoveryServiceManagers.get(uid).unregisterDiscoveryServices(bundleContext);
                discoveryServiceManagers.remove(uid);
            }
        }
    }
}
