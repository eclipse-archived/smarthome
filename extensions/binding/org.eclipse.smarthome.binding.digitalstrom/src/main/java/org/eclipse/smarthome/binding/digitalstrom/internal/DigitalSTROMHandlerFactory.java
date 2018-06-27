/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.digitalstrom.internal;

import static org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.handler.BridgeHandler;
import org.eclipse.smarthome.binding.digitalstrom.handler.CircuitHandler;
import org.eclipse.smarthome.binding.digitalstrom.handler.DeviceHandler;
import org.eclipse.smarthome.binding.digitalstrom.handler.SceneHandler;
import org.eclipse.smarthome.binding.digitalstrom.handler.ZoneTemperatureControlHandler;
import org.eclipse.smarthome.binding.digitalstrom.internal.discovery.DiscoveryServiceManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.impl.ConnectionManagerImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DigitalSTROMHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Ochel - Initial contribution
 * @author Mathias Siegele - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.digitalstrom")
public class DigitalSTROMHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(DigitalSTROMHandlerFactory.class);
    private final HashMap<String, DiscoveryServiceManager> discoveryServiceManagers = new HashMap<String, DiscoveryServiceManager>();

    private HashMap<ThingUID, BridgeHandler> bridgeHandlers;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return BridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                || SceneHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                || DeviceHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                || ZoneTemperatureControlHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                || CircuitHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if (BridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID dSSUID = getBridgeThingUID(thingTypeUID, thingUID, configuration);
            if (dSSUID != null) {
                return super.createThing(thingTypeUID, configuration, dSSUID, null);
            } else {
                logger.error("Can't generate thing UID for thing type {}"
                        + ", because digitalSTROM-Server is not reachable. Please check these points:\n"
                        + "Are the server address and portnumber correct?\n" + "Is the server turned on?\n"
                        + "Is the network configured correctly?", thingTypeUID);
                return null;
            }
        }

        if (DeviceHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID dsDeviceUID = getDeviceUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, dsDeviceUID, bridgeUID);
        }

        if (CircuitHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID dsDeviceUID = getDeviceUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, dsDeviceUID, bridgeUID);
        }

        if (ZoneTemperatureControlHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID zoneTempConUID = getZoneTemperatureControlUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, zoneTempConUID, bridgeUID);
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

        if (CircuitHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new CircuitHandler(thing);
        }

        if (ZoneTemperatureControlHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new ZoneTemperatureControlHandler(thing);
        }

        if (SceneHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new SceneHandler(thing);
        }
        return null;
    }

    private ThingUID getDeviceUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {
        if (thingUID == null && StringUtils.isNotBlank((String) configuration.get(DEVICE_DSID))) {
            return new ThingUID(thingTypeUID, bridgeUID, configuration.get(DEVICE_DSID).toString());
        }
        return thingUID;
    }

    private ThingUID getZoneTemperatureControlUID(ThingTypeUID thingTypeUID, ThingUID thingUID,
            Configuration configuration, ThingUID bridgeUID) {
        if (thingUID == null) {
            Integer zoneID = ZoneTemperatureControlHandler.getZoneID(configuration, bridgeHandlers.get(bridgeUID));
            if (zoneID > ZoneTemperatureControlHandler.ZONE_ID_NOT_EXISTS) {
                return new ThingUID(thingTypeUID, bridgeUID, zoneID.toString());
            } else {
                switch (zoneID) {
                    case ZoneTemperatureControlHandler.ZONE_ID_NOT_EXISTS:
                        logger.error("Configured zone '{}' does not exist, please check your configuration.",
                                configuration.get(DigitalSTROMBindingConstants.ZONE_ID));
                        break;
                    case ZoneTemperatureControlHandler.ZONE_ID_NOT_SET:
                        logger.error("ZoneID is missing at your configuration.");
                        break;
                    case ZoneTemperatureControlHandler.BRIDGE_IS_NULL:
                        logger.error("Bridge is missing, can not check the zoneID.");
                        break;
                }
            }
        }
        return thingUID;
    }

    private ThingUID getSceneUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {
        if (thingUID != null) {
            return thingUID;
        }
        String sceneID = SceneHandler.getSceneID(configuration, bridgeHandlers.get(bridgeUID));
        switch (sceneID) {
            case SceneHandler.SCENE_WRONG:
                logger.error(
                        "Configured scene '{}' does not exist or can not be used, please check your configuration.",
                        configuration.get(DigitalSTROMBindingConstants.SCENE_ID));
                break;
            case SceneHandler.ZONE_WRONG:
                logger.error("Configured zone '{}' does not exist, please check your configuration.",
                        configuration.get(DigitalSTROMBindingConstants.ZONE_ID));
                break;
            case SceneHandler.GROUP_WRONG:
                logger.error("Configured group '{}' does not exist, please check your configuration.",
                        configuration.get(DigitalSTROMBindingConstants.GROUP_ID));
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
        return thingUID;
    }

    private ThingUID getBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {
        if (thingUID != null) {
            return thingUID;
        }
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
        String dSID = null;
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
            Map<String, String> dsidMap = connMan.getDigitalSTROMAPI().getDSID(connMan.getSessionToken());
            if (dsidMap != null) {
                dSID = dsidMap.get(JSONApiResponseKeysEnum.DSID.getKey());
            }
        }
        return dSID;
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
